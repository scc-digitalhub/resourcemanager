package it.smartcommunitylab.resourcemanager.security;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ResourceLocalService;

@Component
public class ResourcePermissionEvaluator implements PermissionEvaluator {
	private final static Logger _log = LoggerFactory.getLogger(ResourcePermissionEvaluator.class);

	@Autowired
	SpacePermissionEvaluator spacePermission;

	@Autowired
	ResourceLocalService resourceService;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		if (targetDomainObject != null && Resource.class.isAssignableFrom(targetDomainObject.getClass())) {
			Resource res = (Resource) targetDomainObject;

			_log.debug("hasPermission for resource " + res.getId() + ":" + permission);

			// delegate to ScopePermission
			return spacePermission.hasPermission(authentication, res.getSpaceId(), "space", permission);
		}

		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		try {
			long id = Long.parseLong(targetId.toString());
			Resource res = resourceService.fetch(id);

			return hasPermission(authentication, res, permission);

		} catch (Exception ex) {
			_log.error(ex.getMessage());
			return false;
		}

	}

}
