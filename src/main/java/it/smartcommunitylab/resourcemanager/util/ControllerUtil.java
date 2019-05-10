package it.smartcommunitylab.resourcemanager.util;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;

public class ControllerUtil {

//	public static String getUserId(final HttpServletRequest request) {
//		Principal principal = request.getUserPrincipal();
//		if (principal != null) {
//			return principal.getName();
//		} else {
//			return "anonymous";
//		}
//	}

	public static String getUserId(final HttpServletRequest request) {
		return SecurityUtil.getUserName();
	}

	public static String getScopeId(final HttpServletRequest request) {
		String scopeId = null;

		Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		if (pathVariables.containsKey("scope")) {
			scopeId = (String) pathVariables.get("scope");
		}

		if (request.getHeader("X-Scope") != null) {
			scopeId = request.getHeader("X-Scope");
		}

		return scopeId;
	}
}
