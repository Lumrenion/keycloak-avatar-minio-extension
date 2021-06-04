package com.github.thomasdarimont.keycloak.avatar;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resources.RealmsResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import org.jboss.logging.Logger;

public class AvatarResource extends AbstractAvatarResource {
    private static final Logger logger = Logger.getLogger(AvatarAdminResource.class);
    
    public static final String STATE_CHECKER_ATTRIBUTE = "state_checker";
    public static final String STATE_CHECKER_PARAMETER = "stateChecker";

    private final AuthenticationManager.AuthResult auth;

    public AvatarResource(KeycloakSession session) {
        super(session);
        this.auth = resolveAuthentication(session);
    }

    private AuthenticationManager.AuthResult resolveAuthentication(KeycloakSession keycloakSession) {
        AppAuthManager appAuthManager = new AppAuthManager();
        RealmModel realm = keycloakSession.getContext().getRealm();

        return appAuthManager.authenticateIdentityCookie(keycloakSession, realm);
    }

    @Path("/admin")
    @GET
    @Produces(MediaType.WILDCARD)
    public AvatarAdminResource admin() {
        AvatarAdminResource service = new AvatarAdminResource(session);
        ResteasyProviderFactory.getInstance().injectProperties(service);
        service.init();
        return service;
    }

    @Path("/")
    @GET
    @Produces({"image/png", "image/jpeg", "image/gif"})
    public Response downloadCurrentUserAvatarImage(@Context UriInfo uriInfo) {
        // make avatar images publicly available to allow server side processing
//        if (auth == null) {
//            return badRequest();
//        }

        String realmName = session.getContext().getRealm().getName();
        String userId = null;
        if (uriInfo.getQueryParameters().containsKey("user")) {
            userId = uriInfo.getQueryParameters().get("user").get(0);
        } else {
            userId = auth.getUser().getId();
        }

        return Response.ok(fetchUserImage(realmName, userId)).build();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.WILDCARD)
    @NoCache
    public Response uploadCurrentUserAvatarImage(MultipartFormDataInput input, @Context UriInfo uriInfo) {
        if (auth == null) {
            return badRequest();
        }

        if (!isValidStateChecker(input)) {
            return badRequest();
        }

        try {

            InputStream imageInputStream = input.getFormDataPart(AVATAR_IMAGE_PARAMETER, InputStream.class, null);

            String realmName = auth.getSession().getRealm().getName();
            String userId = auth.getUser().getId();
            String baseUri = session.getContext().getUri().getBaseUri().toString();
            auth.getUser().setSingleAttribute("avatar", baseUri + "realms/" + realmName + "/avatar-provider/?user=" + userId);
            
            saveUserImage(realmName, userId, imageInputStream);
            if (uriInfo.getQueryParameters().containsKey("account")) {
                return Response.seeOther(RealmsResource.accountUrl(session.getContext().getUri().getBaseUriBuilder()).build(realmName)).build();
            }

            return Response.ok().build();

        } catch (Exception ex) {
            return Response.serverError().build();
        }
    }

    private boolean isValidStateChecker(MultipartFormDataInput input) {

        try {
            String actualStateChecker = input.getFormDataPart(STATE_CHECKER_PARAMETER, String.class, null);
            String requiredStateChecker = (String) session.getAttribute(STATE_CHECKER_ATTRIBUTE);

            return Objects.equals(requiredStateChecker, actualStateChecker);
        } catch (Exception ex) {
            return false;
        }
    }
}
