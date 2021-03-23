package com.github.thomasdarimont.keycloak.avatar;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class AvatarResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public AvatarResourceProvider(KeycloakSession session) {
        this.session = session;
    }


    @Override
    public Object getResource() {
        return new AvatarResource(session);
    }

    @Override
    public void close() {
        // NOOP
    }
}
