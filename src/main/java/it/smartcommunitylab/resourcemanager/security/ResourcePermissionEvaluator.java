package it.smartcommunitylab.resourcemanager.security;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import it.smartcommunitylab.aac.security.permission.NamedPermissionEvaluator;
import it.smartcommunitylab.aac.security.permission.SpacePermissionEvaluator;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ResourceLocalService;

//@Component
public class ResourcePermissionEvaluator implements NamedPermissionEvaluator {
    private final static Logger _log = LoggerFactory.getLogger(ResourcePermissionEvaluator.class);

    public final static String TARGET = Resource.class.getSimpleName().toUpperCase();

    SpacePermissionEvaluator spacePermission;

    ResourceLocalService service;

    public ResourcePermissionEvaluator(ResourceLocalService resourceService,
            SpacePermissionEvaluator spacePermissionEvaluator) {
        this.service = resourceService;
        this.spacePermission = spacePermissionEvaluator;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject != null && Resource.class.isAssignableFrom(targetDomainObject.getClass())) {
            Resource res = (Resource) targetDomainObject;

            _log.debug("hasPermission for resource " + res.getId() + ":" + permission);

            // delegate to SpacePermission
            return spacePermission.hasPermission(authentication, res.getSpaceId(), spacePermission.TARGET, permission);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        try {
            long id = Long.parseLong(targetId.toString());
            _log.debug("hasPermission for resource " + String.valueOf(id) + ":" + permission);

            Resource res = service.fetch(id);
            _log.debug("hasPermission resource " + String.valueOf(res));

            return hasPermission(authentication, res, permission);

        } catch (Exception ex) {
            ex.printStackTrace();
            _log.error(ex.getMessage());
            return false;
        }

    }

    @Override
    public String getName() {
        return TARGET;
    }

}
