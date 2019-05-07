package it.smartcommunitylab.resourcemanager.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.dto.BuilderDTO;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;
import it.smartcommunitylab.resourcemanager.service.ConsumerService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
@Api(value = "/builders")
public class BuilderController {

	private final static Logger _log = LoggerFactory.getLogger(BuilderController.class);

	@Autowired
	private ConsumerService consumerService;

	/*
	 * List w/scope
	 */

	@GetMapping(value = "/c/{scope}/builders", produces = "application/json")
	@ApiOperation(value = "List available consumer builders")
	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
	public List<BuilderDTO> list(
			@ApiParam(value = "Scope", defaultValue = "default") @PathVariable("scope") Optional<String> scope,
			@RequestParam("type") Optional<String> type,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		String scopeId = scope.orElse("default");
		String userId = ControllerUtil.getUserId(request);

		_log.debug("list builders by " + userId + " for scope " + scopeId);

		List<BuilderDTO> results = new ArrayList<>();
		if (type.isPresent()) {
			List<String> builders = consumerService.listBuilders(scopeId, userId, type.get());
			for (String b : builders) {
				results.add(new BuilderDTO(type.get(), b));
			}

		} else {
			Map<String, List<String>> map = consumerService.listBuilders(scopeId, userId);
			for (String t : map.keySet()) {
				for (String b : map.get(t)) {
					results.add(new BuilderDTO(t, b));
				}
			}

		}

		// add total count as header
		response.setHeader("X-Total-Count", String.valueOf(results.size()));

		return results;
	}

	/*
	 * Get w/scope
	 */

	@GetMapping(value = "/c/{scope}/builders/{id}", produces = "application/json")
	@ApiOperation(value = "Fetch a specific consumer builder by id")
	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
	@ResponseBody
	public BuilderDTO get(
			@ApiParam(value = "Scope", defaultValue = "default") @PathVariable("scope") Optional<String> scope,
			@PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		String scopeId = scope.orElse("default");
		String userId = ControllerUtil.getUserId(request);

		_log.debug("get builder " + id + " by " + userId + " for scope " + scopeId);

		ConsumerBuilder cb = consumerService.getBuilder(scopeId, userId, id);

		return BuilderDTO.fromBuilder(cb);
	}
	/*
	 * List
	 */

	@GetMapping(value = "/builders", produces = "application/json")
	@ApiOperation(value = "List available consumer builders")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
			@ApiImplicitParam(name = "X-Scope", value = "Scope", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
	})
	public List<BuilderDTO> list(
			@RequestParam("type") Optional<String> type,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		Optional<String> scopeId = Optional.of(ControllerUtil.getScopeId(request));
		return list(scopeId, type, request, response, pageable);
	}

	/*
	 * Get
	 */

	@GetMapping(value = "/builders/{id}", produces = "application/json")
	@ApiOperation(value = "Fetch a specific consumer builder by id")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
			@ApiImplicitParam(name = "X-Scope", value = "Scope", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
	})
	@ResponseBody
	public BuilderDTO get(
			@PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		Optional<String> scopeId = Optional.of(ControllerUtil.getScopeId(request));
		return get(scopeId, id, request, response);
	}

	/*
	 * Exceptions
	 */

	@ExceptionHandler(NoSuchConsumerException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public String notFound(NoSuchConsumerException ex) {
		return ex.getMessage();
	}

	/*
	 * Helper
	 */
}