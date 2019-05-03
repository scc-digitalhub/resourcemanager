package it.smartcommunitylab.resourcemanager.controller;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.dto.ResourceDTO;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ResourceService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
public class ResourceController {

	private final static Logger _log = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	private ResourceService resourceService;

	/*
	 * Resource
	 */
	@GetMapping({ "/resources/{id}", "/c/{scope}/resources/{id}" })
	@ResponseBody
	public ResourceDTO get(
			@PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchResourceException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		_log.debug("get " + String.valueOf("id") + " by " + userId);

		Resource resource = resourceService.get(scopeId, userId, id);

		return ResourceDTO.fromResource(resource);
	}

	@PostMapping({ "/resources", "/c/{scope}/resources" })
	@ResponseBody
	public ResourceDTO add(
			@RequestBody ResourceDTO res,
			HttpServletRequest request, HttpServletResponse response) throws NoSuchProviderException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		// parse fields from post
		Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(res.getProperties());

		Resource resource = resourceService.create(scopeId, userId, res.getType(), res.getProvider(), propertiesMap);

		return ResourceDTO.fromResource(resource);

	}

	@PutMapping({ "/resources/{id}", "/c/{scope}/resources/{id}" })
	@ResponseBody
	public ResourceDTO update(
			@PathVariable("id") long id,
			@RequestBody ResourceDTO res,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchProviderException, NoSuchResourceException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		// parse fields from post
		Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(res.getProperties());
		res.id = id;

		Resource resource = resourceService.update(scopeId, userId, id, propertiesMap);

		return ResourceDTO.fromResource(resource);

	}

	@DeleteMapping({ "/resources/{id}", "/c/{scope}/resources/{id}" })
	@ResponseBody
	public void delete(
			@PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchProviderException, NoSuchResourceException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		resourceService.delete(scopeId, userId, id);

	}

	/*
	 * List
	 */

	@GetMapping({ "/resources", "/c/{scope}/resources" })
	@ResponseBody
	public List<ResourceDTO> list(
			@RequestParam("type") Optional<String> type,
			@RequestParam("provider") Optional<String> provider,
			@RequestParam("user") Optional<String> ownerId,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		long total = 0;
		List<Resource> resources = new ArrayList<>();

		// TODO refactor - ugly
		if (type.isPresent()) {
			total = resourceService.countByType(scopeId, userId, type.get());
			resources = resourceService.listByType(scopeId, userId, type.get());
		} else if (provider.isPresent()) {
			total = resourceService.countByProvider(scopeId, userId, provider.get());
			resources = resourceService.listByProvider(scopeId, userId, provider.get());
		} else if (ownerId.isPresent()) {
			total = resourceService.countByUserId(userId, ownerId.get());
			resources = resourceService.listByUserId(userId, ownerId.get());
		} else {
			total = resourceService.count(scopeId, userId);
			resources = resourceService.list(scopeId, userId, pageable.getPageNumber(), pageable.getPageSize());
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
