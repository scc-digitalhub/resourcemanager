package it.smartcommunitylab.resourcemanager.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.smartcommunitylab.resourcemanager.security.InternalSpacePermissionEvaluator;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

//@RestController
//@Api(value = "/permissions")
public class PermissionController {

    private final static Logger _log = LoggerFactory.getLogger(PermissionController.class);

    @Value("${spaces.default}")
    private String defaultSpace;

    @Value("${spaces.list}")
    private List<String> spaces;

    @Autowired
    private InternalSpacePermissionEvaluator permissionEvaluator;

//    /*
//     * Permission list w/space
//     */
//    @GetMapping(value = "/api/c/{space}/permissions", produces = "application/json")
//    @ApiOperation(value = "Fetch all permissions for the space")
//    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
//    @ResponseBody
//    public List<String> permissions(
//            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
//            HttpServletRequest request, HttpServletResponse response) {
//
//        String spaceId = space.orElse(defaultSpace);
//        String userId = ControllerUtil.getUserId(request);
//
//        _log.debug("get permissions for user " + userId + " for space " + spaceId);
//
//        List<String> permissions = new ArrayList<>();
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            if (permissionEvaluator.isSpacePermitted(spaceId)) {
//                List<String> roles = permissionEvaluator.getSpaceRoles(spaceId, auth);
//                for (String role : roles) {
//                    permissions.addAll(permissionEvaluator.roleToPermissions(role));
//                }
//            }
//        }
//
//        return permissions;
//    }
//
//    /*
//     * Roles list w/space
//     */
//    @GetMapping(value = "/api/c/{space}/roles", produces = "application/json")
//    @ApiOperation(value = "Fetch all roles for the space")
//    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
//    @ResponseBody
//    public List<String> roles(
//            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
//            HttpServletRequest request, HttpServletResponse response) {
//
//        String spaceId = space.orElse(defaultSpace);
//        String userId = ControllerUtil.getUserId(request);
//
//        _log.debug("get roles for user " + userId + " for space " + spaceId);
//
//        List<String> roles = new ArrayList<>();
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            if (permissionEvaluator.isSpacePermitted(spaceId)) {
//                roles.addAll(permissionEvaluator.getSpaceRoles(spaceId, auth));
//            }
//        }
//
//        return roles;
//    }
//
//    /*
//     * Spaces list
//     */
//    @GetMapping(value = "/api/spaces", produces = "application/json")
//    @ApiOperation(value = "Fetch all spaces")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
//    })
//    @ResponseBody
//    public List<String> spaces(
//            HttpServletRequest request, HttpServletResponse response) {
//
//        if (spaces.isEmpty()) {
//            spaces.add(defaultSpace);
//        }
//        return spaces;
//    }
//
//    /*
//     * Permissions list
//     */
//    @GetMapping(value = "/api/permissions", produces = "application/json")
//    @ApiOperation(value = "Fetch all permissions for the space")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public List<String> permissions(
//            HttpServletRequest request, HttpServletResponse response) {
//
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return permissions(spaceId, request, response);
//    }
//
//    /*
//     * Roles list
//     */
//    @GetMapping(value = "/api/roles", produces = "application/json")
//    @ApiOperation(value = "Fetch all roles for the space")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public List<String> roles(
//            HttpServletRequest request, HttpServletResponse response) {
//
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return roles(spaceId, request, response);
//    }
}
