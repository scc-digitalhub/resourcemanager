package it.smartcommunitylab.resourcemanager.security;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import it.smartcommunitylab.aac.security.permission.NamedPermissionEvaluator;
import it.smartcommunitylab.aac.security.permission.SpacePermissionEvaluator;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.service.RegistrationLocalService;

//@Component
public class RegistrationPermissionEvaluator implements NamedPermissionEvaluator {
    private final static Logger _log = LoggerFactory.getLogger(RegistrationPermissionEvaluator.class);

    public final static String TARGET = Registration.class.getSimpleName().toUpperCase();

    SpacePermissionEvaluator spacePermission;

    RegistrationLocalService service;

    public RegistrationPermissionEvaluator(RegistrationLocalService registrationService,
            SpacePermissionEvaluator spacePermissionEvaluator) {
        this.service = registrationService;
        this.spacePermission = spacePermissionEvaluator;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject != null && Registration.class.isAssignableFrom(targetDomainObject.getClass())) {
            Registration reg = (Registration) targetDomainObject;

            _log.debug("hasPermission for registration " + reg.getId() + ":" + permission);

            // delegate to SpacePermission
            return spacePermission.hasPermission(authentication, reg.getSpaceId(), spacePermission.TARGET, permission);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        try {
            long id = Long.parseLong(targetId.toString());
            Registration res = service.fetch(id);

            return hasPermission(authentication, res, permission);

        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return false;
        }
    }

    @Override
    public String getName() {
        return TARGET;
    }
}
