package it.smartcommunitylab.resourcemanager.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.dto.ResourceDTO;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ResourceService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
@Api(value = "/resources")
public class ResourceController {

    private final static Logger _log = LoggerFactory.getLogger(ResourceController.class);

    @Value("${spaces.default}")
    private String defaultSpace;

    @Autowired
    private ResourceService resourceService;

    /*
     * Resource w/space
     */
    @GetMapping(value = {
            "/api/resources/{id}",
            "/api/-/{space}/resources/{id}"
    }, produces = "application/json")
    @ApiOperation(value = "Fetch a specific resource by id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ResourceDTO get(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Resource id", required = true) @PathVariable("id") long id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchResourceException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("get resource " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        // call exists to trigger 404, otherwise get() will
        // check permissions *before* checking existence
        resourceService.exists(spaceId, userId, id);
        _log.debug("exists resource " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        Resource resource = resourceService.get(spaceId, userId, id);
        _log.debug("fetched resource " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        // include private fields on detail view
        return ResourceDTO.fromResource(resource, true);
    }

    @PostMapping(value = {
            "/api/resources",
            "/api/-/{space}/resources"
    }, produces = "application/json")
    @ApiOperation(value = "Create a new resource")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ResourceDTO create(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Resource json", required = true) @RequestBody ResourceDTO resource,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchProviderException, ResourceProviderException, InvalidNameException, DuplicateNameException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        // parse fields from post
        Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(resource.getProperties());
        List<String> tags = new ArrayList<>(Arrays.asList(resource.getTags()));

        _log.debug("create resource by " + userId + " for space " + spaceId);

        Resource result = resourceService.create(spaceId, userId,
                resource.getType(), resource.getProvider(), resource.getName(),
                propertiesMap, tags);

        // include private fields on create view
        return ResourceDTO.fromResource(result, true);

    }

    @PutMapping(value = {
            "/api/resources",
            "/api/-/{space}/resources"
    }, produces = "application/json")
    @ApiOperation(value = "Add an existing resource")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ResourceDTO add(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Resource json", required = true) @RequestBody ResourceDTO resource,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchProviderException, ResourceProviderException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        // parse fields from post
        Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(resource.getProperties());
        List<String> tags = new ArrayList<>(Arrays.asList(resource.getTags()));

        _log.debug("add resource by " + userId + " for space " + spaceId);

        Resource result = resourceService.add(spaceId, userId, resource.getType(), resource.getName(),
                resource.getProvider(),
                resource.getUri(),
                propertiesMap, tags);

        // include private fields on create view
        return ResourceDTO.fromResource(result, true);

    }

    @PutMapping(value = {
            "/api/resources/{id}",
            "/api/-/{space}/resources/{id}"
    }, produces = "application/json")
    @ApiOperation(value = "Update a specific resource")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ResourceDTO update(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Resource id", required = true) @PathVariable("id") long id,
            @ApiParam(value = "Resource json", required = true) @RequestBody ResourceDTO resource,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchProviderException, NoSuchResourceException, ResourceProviderException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        // parse fields from post
        Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(resource.getProperties());
        List<String> tags = new ArrayList<>(Arrays.asList(resource.getTags()));

        resource.id = id;

        _log.debug("update resource " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        // call exists to trigger 404, otherwise update() will
        // check permissions *before* checking existence
        resourceService.exists(spaceId, userId, id);

        Resource result = resourceService.update(spaceId, userId, id, propertiesMap, tags);

        // include private fields on update view
        return ResourceDTO.fromResource(result, true);

    }

    @DeleteMapping(value = {
            "/api/resources/{id}",
            "/api/-/{space}/resources/{id}"
    }, produces = "application/json")
    @ApiOperation(value = "Delete a specific resource")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ResourceDTO delete(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Resource id", required = true) @PathVariable("id") long id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchProviderException, NoSuchResourceException, ResourceProviderException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("delete resource " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        // call exists to trigger 404, otherwise delete() will
        // check permissions *before* checking existence
        resourceService.exists(spaceId, userId, id);

        // fetch resource to provide as result on success
        Resource resource = resourceService.get(spaceId, userId, id);

        resourceService.delete(spaceId, userId, id);

        return ResourceDTO.fromResource(resource, false);

    }

    /*
     * List w/space
     */

    @GetMapping(value = {
            "/api/resources",
            "/api/-/{space}/resources"
    }, produces = "application/json")
    @ApiOperation(value = "List resources with filters")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public List<ResourceDTO> list(
            @ApiParam(value = "Resource space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Resource type") @RequestParam("type") Optional<String> type,
            @ApiParam(value = "Resource provider") @RequestParam("provider") Optional<String> provider,
            @ApiParam(value = "Resource owner") @RequestParam("user") Optional<String> ownerId,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("list resources by " + userId + " for space " + spaceId);

        long total = 0;
        List<Resource> resources = new ArrayList<>();

        // TODO refactor - ugly
        if (type.isPresent()) {
            total = resourceService.countByType(spaceId, userId, type.get());
            resources = resourceService.listByType(spaceId, userId, type.get());
        } else if (provider.isPresent()) {
            total = resourceService.countByProvider(spaceId, userId, provider.get());
            resources = resourceService.listByProvider(spaceId, userId, provider.get());
        } else if (ownerId.isPresent()) {
            total = resourceService.countByUserId(spaceId, userId, ownerId.get());
            resources = resourceService.listByUserId(spaceId, userId, ownerId.get());
        } else {
            total = resourceService.count(spaceId, userId);
            resources = resourceService.list(spaceId, userId, pageable.getPageNumber(), pageable.getPageSize());
        }
        List<ResourceDTO> results = resources.stream().map(r -> ResourceDTO.fromResource(r))
                .collect(Collectors.toList());
        // add total count as header
        response.setHeader("X-Total-Count", String.valueOf(total));

        return results;
    }

    /*
     * Exceptions
     */

    @ExceptionHandler(NoSuchResourceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String notFound(NoSuchResourceException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NoSuchProviderException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String notFound(NoSuchProviderException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ResourceProviderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String providerError(ResourceProviderException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String nameError(InvalidNameException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DuplicateNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String duplicateError(DuplicateNameException ex) {
        return ex.getMessage();
    }

    /*
     * Helper
     */

//	private String getUserId() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if (!(authentication instanceof AnonymousAuthenticationToken)) {
//			String currentUserName = authentication.getName();
//			return currentUserName;
//		}
//	}

}
