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
		String userId = authentication.getName();
		String action = permission.toString();

		for (GrantedAuthority ga : authentication.getAuthorities()) {
			_log.debug("user " + userId + " authority " + ga.toString());
		}

		boolean isPermitted = true;

		if (enabled) {
			// check for scope in permitted
			isPermitted = scopes.contains(scopeId);
		}
		_log.debug("user " + userId + " hasPermission scope " + scopeId + " permitted " + isPermitted);

		// check in Auth
		boolean hasPermission = false;

		// fetch realm AND scope roles
		List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
		Set<String> roles = new HashSet<>();
		roles.addAll(getRealmRoles(authorities));
		roles.addAll(getScopeRoles(scopeId, authorities));

		// fetch all permissions related to any assigned role
		Set<String> permissions = new HashSet<>();
		for (String role : roles) {
			permissions.addAll(roleToPermissions(role));
		}

		_log.debug("user " + userId + " permissions " + permissions.toString());

		hasPermission = permissions.contains(action);

		_log.debug("user " + userId + " hasPermission for scope " + scopeId + ":" + action + " " + hasPermission);

		return (isPermitted && hasPermission);
	}

	/*
	 * Helpers
	 */
	private List<String> getRealmRoles(List<GrantedAuthority> authorities) {
		List<String> roles = new ArrayList<>();

		for (GrantedAuthority ga : authorities) {
			// expected format [ROLE_*]
			String auth = ga.getAuthority();
			if (auth != null) {
				if (auth.startsWith("ROLE_")) {
					roles.add(auth);
				}
			}
		}

		return roles;
	}

	private List<String> getScopeRoles(String scopeId, List<GrantedAuthority> authorities) {
		List<String> roles = new ArrayList<>();
		String prefix = "/" + scopeId + "/";

		for (GrantedAuthority ga : authorities) {
			// expected format [scopeId]/[ROLE_*]
			String auth = ga.getAuthority();
			if (auth != null) {
				if (auth.startsWith(prefix + "ROLE_")) {
					roles.add(auth.replaceFirst(prefix, ""));
				}
			}
		}

		return roles;
	}

	private Set<String> roleToPermissions(String role) {
		// statically resolve roles => permission mapping
		// TODO refactor
		Set<String> permissions = new HashSet<String>();

		if (role.equals("ROLE_ADMIN")) {
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
		} else if (role.equals("ROLE_RESOURCE_ADMIN")) {
			permissions.addAll(Arrays.asList(
					SystemKeys.PERMISSION_RESOURCE_CREATE,
					SystemKeys.PERMISSION_RESOURCE_UPDATE,
					SystemKeys.PERMISSION_RESOURCE_DELETE,
					SystemKeys.PERMISSION_RESOURCE_CHECK,
					SystemKeys.PERMISSION_RESOURCE_VIEW,

					SystemKeys.PERMISSION_CONSUMER_VIEW));
		} else if (role.equals("ROLE_CONSUMER_ADMIN")) {
			permissions.addAll(Arrays.asList(
					SystemKeys.PERMISSION_RESOURCE_CHECK,
					SystemKeys.PERMISSION_RESOURCE_VIEW,

					SystemKeys.PERMISSION_CONSUMER_CREATE,
					SystemKeys.PERMISSION_CONSUMER_UPDATE,
					SystemKeys.PERMISSION_CONSUMER_DELETE,
					SystemKeys.PERMISSION_CONSUMER_VIEW));
		} else if (role.equals("ROLE_USER")) {
			permissions.addAll(Arrays.asList(
					SystemKeys.PERMISSION_RESOURCE_CHECK,
					SystemKeys.PERMISSION_RESOURCE_VIEW,
					SystemKeys.PERMISSION_CONSUMER_VIEW));
		}

		return permissions;
	}

}
