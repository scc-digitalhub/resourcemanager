package it.smartcommunitylab.resourcemanager.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

	public static String getSpaceId(final HttpServletRequest request) {
		String spaceId = null;

		Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		if (pathVariables.containsKey("space")) {
		    spaceId = (String) pathVariables.get("space");
		}

		if (request.getHeader("X-Space") != null) {
		    spaceId = request.getHeader("X-Space");
		}

		return spaceId;
	}
}
