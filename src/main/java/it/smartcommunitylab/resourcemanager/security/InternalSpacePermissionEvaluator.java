package it.smartcommunitylab.resourcemanager.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;

//@Component
public class InternalSpacePermissionEvaluator implements PermissionEvaluator {
    private final static Logger _log = LoggerFactory.getLogger(InternalSpacePermissionEvaluator.class);

    @Value("${spaces.enabled}")
    private boolean enabled;

    @Value("${spaces.list}")
    private List<String> spaces;

    @Value("${spaces.default}")
    private String defaultSpace;

    @Value("${spaces.roles.mapping.admin}")
    private String roleAdminMapping;

    @Value("${spaces.roles.mapping.resourceAdmin}")
    private String roleResourceAdminMapping;

    @Value("${spaces.roles.mapping.consumerAdmin}")
    private String roleConsumerAdminMapping;

    @Value("${spaces.roles.mapping.user}")
    private String roleUserMapping;

    @PostConstruct
    public void init() {
        _log.debug("spacePermission enabled? " + enabled);

        if (spaces == null) {
            spaces = new ArrayList<>();
        }

        // add placeholder to spaces if empty
        if (spaces.isEmpty()) {
            spaces.add("*");
        }

        // always add default space if defined
        if (!defaultSpace.isEmpty() && !spaces.contains(defaultSpace)) {
            spaces.add(defaultSpace);
        }

        _log.debug("spaces: " + spaces.toString());

        // set default mappings
        if (roleAdminMapping.isEmpty()) {
            roleAdminMapping = SystemKeys.ROLE_ADMIN;
        }
        if (roleResourceAdminMapping.isEmpty()) {
            roleResourceAdminMapping = SystemKeys.ROLE_RESOURCE_ADMIN;
        }
        if (roleConsumerAdminMapping.isEmpty()) {
            roleConsumerAdminMapping = SystemKeys.ROLE_CONSUMER_ADMIN;
        }
        if (roleUserMapping.isEmpty()) {
            roleUserMapping = SystemKeys.ROLE_USER;
        }

        _log.debug("role mapping " + SystemKeys.ROLE_ADMIN + " to " + roleAdminMapping);
        _log.debug("role mapping " + SystemKeys.ROLE_RESOURCE_ADMIN + " to " + roleResourceAdminMapping);
        _log.debug("role mapping " + SystemKeys.ROLE_CONSUMER_ADMIN + " to " + roleConsumerAdminMapping);
        _log.debug("role mapping " + SystemKeys.ROLE_USER + " to " + roleUserMapping);

    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // no space object to check
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {

        String spaceId = targetId.toString();
        String userId = authentication.getName();
        String action = permission.toString();

        for (GrantedAuthority ga : authentication.getAuthorities()) {
            _log.trace("user " + userId + " authority " + ga.toString());
        }

        boolean isPermitted = isSpacePermitted(spaceId);
        _log.trace("user " + userId + " hasPermission space " + spaceId + " permitted " + isPermitted);

        // check in Auth
        boolean hasPermission = false;

        // fetch ONLY space roles
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        _log.trace("user " + userId + " authorities " + authorities.toString());

        
        Set<String> roles = new HashSet<>();
        roles.addAll(getSpaceRoles(spaceId, authorities));

        // fetch all permissions related to any assigned role
        Set<String> permissions = new HashSet<>();
        for (String role : roles) {
            permissions.addAll(roleToPermissions(role));
        }

        _log.trace("user " + userId + " permissions " + permissions.toString());

        hasPermission = permissions.contains(action);

        _log.debug("user " + userId + " hasPermission for space " + spaceId + ":" + action + " " + hasPermission);

        return (isPermitted && hasPermission);
    }

    /*
     * Helpers
     */
    public List<String> getSpaceRoles(String spaceId, Authentication authentication) {
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        return getSpaceRoles(spaceId, authorities);
    }

    public boolean isSpacePermitted(String spaceId) {

        if (!defaultSpace.isEmpty() && spaceId.equals(defaultSpace)) {
            // default space always enabled if defined
            return true;
        }

        if (enabled) {
            if (spaces.contains("*")) {
                return true;
            }
            return spaces.contains(spaceId);
        }

        return false;
    }

    private List<String> getSpaceRoles(String spaceId, List<GrantedAuthority> authorities) {
        List<String> roles = new ArrayList<>();

        for (GrantedAuthority ga : authorities) {
            // support variable substitution with placeholder <space>
            String auth = ga.getAuthority();
            if (auth != null) {
                // check against mappings
                if (auth.equals(roleAdminMapping.replace("<space>", spaceId))) {
                    roles.add(SystemKeys.ROLE_ADMIN);
                }
                if (auth.equals(roleResourceAdminMapping.replace("<space>", spaceId))) {
                    roles.add(SystemKeys.ROLE_RESOURCE_ADMIN);
                }
                if (auth.equals(roleConsumerAdminMapping.replace("<space>", spaceId))) {
                    roles.add(SystemKeys.ROLE_CONSUMER_ADMIN);
                }
                if (auth.equals(roleUserMapping.replace("<space>", spaceId))) {
                    roles.add(SystemKeys.ROLE_USER);
                }

            }
        }

        return roles;
    }

    public Set<String> roleToPermissions(String role) {
        // statically resolve roles => permission mapping
        // TODO refactor
        Set<String> permissions = new HashSet<String>();

        if (role.equals(SystemKeys.ROLE_ADMIN)) {
            permissions.addAll(Arrays.asList(
                    SystemKeys.PERMISSION_RESOURCE_CREATE,
                    SystemKeys.PERMISSION_RESOURCE_UPDATE,
                    SystemKeys.PERMISSION_RESOURCE_DELETE,
                    SystemKeys.PERMISSION_RESOURCE_CHECK,
                    SystemKeys.PERMISSION_RESOURCE_VIEW,

                    SystemKeys.PERMISSION_CONSUMER_CREATE,
                    SystemKeys.PERMISSION_CONSUMER_UPDATE,
                    SystemKeys.PERMISSION_CONSUMER_DELETE,
                    SystemKeys.PERMISSION_CONSUMER_VIEW));
        } else if (role.equals(SystemKeys.ROLE_RESOURCE_ADMIN)) {
            permissions.addAll(Arrays.asList(
                    SystemKeys.PERMISSION_RESOURCE_CREATE,
                    SystemKeys.PERMISSION_RESOURCE_UPDATE,
                    SystemKeys.PERMISSION_RESOURCE_DELETE,
                    SystemKeys.PERMISSION_RESOURCE_CHECK,
                    SystemKeys.PERMISSION_RESOURCE_VIEW,

                    SystemKeys.PERMISSION_CONSUMER_VIEW));
        } else if (role.equals(SystemKeys.ROLE_CONSUMER_ADMIN)) {
            permissions.addAll(Arrays.asList(
                    SystemKeys.PERMISSION_RESOURCE_CHECK,
                    SystemKeys.PERMISSION_RESOURCE_VIEW,

                    SystemKeys.PERMISSION_CONSUMER_CREATE,
                    SystemKeys.PERMISSION_CONSUMER_UPDATE,
                    SystemKeys.PERMISSION_CONSUMER_DELETE,
                    SystemKeys.PERMISSION_CONSUMER_VIEW));
        } else if (role.equals(SystemKeys.ROLE_USER)) {
            permissions.addAll(Arrays.asList(
                    SystemKeys.PERMISSION_RESOURCE_CHECK,
                    SystemKeys.PERMISSION_RESOURCE_VIEW,
                    SystemKeys.PERMISSION_CONSUMER_VIEW));
        }

        return permissions;
    }

}
