package it.smartcommunitylab.resourcemanager.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.resourcemanager.util.ControllerUtil;
import it.smartcommunitylab.resourcemanager.util.SecurityUtil;

@RestController
public class DebugController {

	@GetMapping("/")
	public String root(HttpServletRequest request, HttpServletResponse response) {
		return "ResourceController";
	}

//	@GetMapping("/debug/userid")
//	public String username(HttpServletRequest request, HttpServletResponse response) {
//		return ControllerUtil.getUserId(request);
//	}
//
//	@GetMapping("/debug/roles")
//	public List<String> roles(HttpServletRequest request, HttpServletResponse response) {
//		return new ArrayList(SecurityUtil.getUserRoles());
//	}
}
