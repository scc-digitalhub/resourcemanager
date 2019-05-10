package it.smartcommunitylab.resourcemanager.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ScopePermissionEvaluator implements PermissionEvaluator {
	private final static Logger _log = LoggerFactory.getLogger(ScopePermissionEvaluator.class);

	@Value("${scopes.enabled}")
	private boolean enabled;

	@Value("${scopes.list}")
	private List<String> scopes;

	@Value("${scopes.default}")
	private String defaultScope;

	@PostConstruct
	public void init() {
		_log.debug("scopePermission enabled? " + enabled);

		if (scopes == null) {
			scopes = new ArrayList<>();
		}

		// always add default scope if defined
		if (!defaultScope.isEmpty() && !scopes.contains(defaultScope)) {
			scopes.add(defaultScope);
		}

	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		// no scope object to check
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {

		String scopeId = targetId.toString();
		String action = permission.toString();

		boolean isPermitted = true;

		if (enabled) {
			// check for scope in permitted
			isPermitted = scopes.contains(scopeId);
		}
		_log.debug("hasPermission scope " + scopeId + " permitted " + isPermitted);

		// TODO check in Auth!
		boolean hasPermission = true;
		_log.debug("hasPermission for scope " + scopeId + ":" + action + " " + hasPermission);

		return (isPermitted && hasPermission);
	}

}
