package it.smartcommunitylab.resourcemanager.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import it.smartcommunitylab.aac.security.authority.SpaceGrantedAuthority;
import it.smartcommunitylab.aac.security.permission.Space;
import it.smartcommunitylab.aac.security.roles.RoleActionConverter;
import it.smartcommunitylab.resourcemanager.SystemKeys;

public class ResourceRoleActionConverter<T> implements RoleActionConverter<T> {
    private final static Logger logger = LoggerFactory.getLogger(ResourceRoleActionConverter.class);

    private final String component;

    public ResourceRoleActionConverter(String component) {
        this.component = component;
    }

    protected static final String[] PERMISSIONS_ADMIN = {
            SystemKeys.PERMISSION_RESOURCE_CREATE,
            SystemKeys.PERMISSION_RESOURCE_UPDATE,
            SystemKeys.PERMISSION_RESOURCE_DELETE,
            SystemKeys.PERMISSION_RESOURCE_CHECK,
            SystemKeys.PERMISSION_RESOURCE_VIEW,

            SystemKeys.PERMISSION_CONSUMER_CREATE,
            SystemKeys.PERMISSION_CONSUMER_UPDATE,
            SystemKeys.PERMISSION_CONSUMER_DELETE,
            SystemKeys.PERMISSION_CONSUMER_VIEW
    };

    protected static final String[] PERMISSIONS_RESOURCE_ADMIN = {
            SystemKeys.PERMISSION_RESOURCE_CREATE,
            SystemKeys.PERMISSION_RESOURCE_UPDATE,
            SystemKeys.PERMISSION_RESOURCE_DELETE,
            SystemKeys.PERMISSION_RESOURCE_CHECK,
            SystemKeys.PERMISSION_RESOURCE_VIEW,

    };

    protected static final String[] PERMISSIONS_CONSUMER_ADMIN = {
            SystemKeys.PERMISSION_RESOURCE_CHECK,
            SystemKeys.PERMISSION_RESOURCE_VIEW,

            SystemKeys.PERMISSION_CONSUMER_CREATE,
            SystemKeys.PERMISSION_CONSUMER_UPDATE,
            SystemKeys.PERMISSION_CONSUMER_DELETE,
            SystemKeys.PERMISSION_CONSUMER_VIEW
    };

    protected static final String[] PERMISSIONS_USER = {
            SystemKeys.PERMISSION_RESOURCE_CHECK,
            SystemKeys.PERMISSION_RESOURCE_VIEW,
            SystemKeys.PERMISSION_CONSUMER_VIEW
    };

    @Override
    public List<String> extractRoles(Authentication authentication, Object entity) {
        String space = entity.toString();
        List<String> roles = new ArrayList<>();

        logger.trace(authentication.getAuthorities().toString());

        for (GrantedAuthority a : authentication.getAuthorities()) {
            if (a instanceof SpaceGrantedAuthority) {
                SpaceGrantedAuthority sa = (SpaceGrantedAuthority) a;
                if (sa.getSpace().equals(space)) {
                    // we consider space provider as admin
                    if (sa.getRole().equals("ROLE_PROVIDER")) {
                        roles.add(SystemKeys.ROLE_ADMIN);
                    }
                }
            }
        }

        logger.trace("extracted roles for space " + String.valueOf(space) + ": " + roles.toString());
        return roles;
    }

    @Override
    public List<String> allowedRoles(String action) {
        List<String> roles = new ArrayList<>();
        if (ArrayUtils.contains(PERMISSIONS_ADMIN, action)) {
            roles.add(SystemKeys.ROLE_ADMIN);
        }
        if (ArrayUtils.contains(PERMISSIONS_RESOURCE_ADMIN, action)) {
            roles.add(SystemKeys.ROLE_RESOURCE_ADMIN);
        }
        if (ArrayUtils.contains(PERMISSIONS_CONSUMER_ADMIN, action)) {
            roles.add(SystemKeys.ROLE_CONSUMER_ADMIN);
        }
        if (ArrayUtils.contains(PERMISSIONS_USER, action)) {
            roles.add(SystemKeys.ROLE_USER);
        }

        return roles;
    }

    @Override
    public List<String> grantsActions(String role) {
        List<String> actions = Collections.emptyList();
        switch (role) {
        case SystemKeys.ROLE_ADMIN:
            actions = Arrays.asList(PERMISSIONS_ADMIN);
            break;
        case SystemKeys.ROLE_RESOURCE_ADMIN:
            actions = Arrays.asList(PERMISSIONS_RESOURCE_ADMIN);
            break;
        case SystemKeys.ROLE_CONSUMER_ADMIN:
            actions = Arrays.asList(PERMISSIONS_CONSUMER_ADMIN);
            break;
        case SystemKeys.ROLE_USER:
            actions = Arrays.asList(PERMISSIONS_USER);
            break;
        }

        return actions;

    }

}
