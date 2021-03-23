package com.github.thomasdarimont.keycloak.avatar;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

import static org.keycloak.Config.Scope;

public class AvatarResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "avatar-provider";

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        return new AvatarResourceProvider(keycloakSession);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
