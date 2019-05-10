package it.smartcommunitylab.resourcemanager.security;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.service.RegistrationLocalService;

@Component
public class RegistrationPermissionEvaluator implements PermissionEvaluator {
	private final static Logger _log = LoggerFactory.getLogger(RegistrationPermissionEvaluator.class);

	@Autowired
	ScopePermissionEvaluator scopePermission;

	@Autowired
	RegistrationLocalService registrationService;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		if (targetDomainObject != null && Registration.class.isAssignableFrom(targetDomainObject.getClass())) {
			Registration reg = (Registration) targetDomainObject;

			_log.debug("hasPermission for registration " + reg.getId() + ":" + permission);

			// delegate to ScopePermission
			return scopePermission.hasPermission(authentication, reg.getScopeId(), "scope", permission);
		}

		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		try {
			long id = Long.parseLong(targetId.toString());
			Registration res = registrationService.fetch(id);

			return hasPermission(authentication, res, permission);

		} catch (Exception ex) {
			_log.error(ex.getMessage());
			return false;
		}
	}

}
