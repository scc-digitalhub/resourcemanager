package it.smartcommunitylab.resourcemanager.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import it.smartcommunitylab.aac.security.permission.SpacePermissionEvaluator;

public class ExtSpacePermissionEvaluator extends SpacePermissionEvaluator {

    /*
     * expose roles TODO remove when UI is independent
     */
    public List<String> extractSpaceRoles(Authentication auth, String spaceId) {
        // keep ONLY space roles
        List<String> roles = getSpaceRoles(new ArrayList<>(auth.getAuthorities()), spaceId);

        // append converter roles if defined
        roles.addAll(getConverterRoles(auth, spaceId));

        return roles;

    }
}
