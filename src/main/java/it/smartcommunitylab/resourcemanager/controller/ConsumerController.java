package it.smartcommunitylab.resourcemanager.controller;

import java.io.Serializable;
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
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.dto.ConsumerDTO;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ConsumerService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
@Api(value = "/consumers")
public class ConsumerController {

	private final static Logger _log = LoggerFactory.getLogger(ConsumerController.class);

	@Autowired
	private ConsumerService consumerService;

	/*
	 * Consumer registration w/scope
	 */
	@GetMapping(value = "/c/{scope}/consumers/{id}", produces = "application/json")
	@ApiOperation(value = "Fetch a specific consumer by id")
	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
	@ResponseBody
	public ConsumerDTO get(
			@ApiParam(value = "Scope", defaultValue = "default") @PathVariable("scope") Optional<String> scope,
			@ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		String scopeId = scope.orElse("default");
		String userId = ControllerUtil.getUserId(request);

		_log.debug("get consumer " + String.valueOf(id) + " by " + userId + " for scope " + scopeId);

		Registration reg = consumerService.get(scopeId, userId, id);

		return ConsumerDTO.fromRegistration(reg);
	}

	@PostMapping(value = "/c/{scope}/consumers", produces = "application/json")
	@ApiOperation(value = "Add a new consumer")
	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
	@ResponseBody
	public ConsumerDTO add(
			@ApiParam(value = "Scope", defaultValue = "default") @PathVariable("scope") Optional<String> scope,
			@ApiParam(value = "Consumer json", required = true) @RequestBody ConsumerDTO res,
			HttpServletRequest request, HttpServletResponse response) throws NoSuchConsumerException {

		String scopeId = scope.orElse("default");
		String userId = ControllerUtil.getUserId(request);

		_log.debug("add consumer by " + userId + " for scope " + scopeId);

		// parse fields from post
		Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(res.getProperties());

		Registration reg = consumerService.add(scopeId, userId, res.getType(), res.getConsumer(), propertiesMap);

		return ConsumerDTO.fromRegistration(reg);

	}

	@DeleteMapping(value = "/c/{scope}/consumers/{id}", produces = "application/json")
	@ApiOperation(value = "Delete a specific consumer by id")
	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
	@ResponseBody
	public void delete(
			@ApiParam(value = "Scope", defaultValue = "default") @PathVariable("scope") Optional<String> scope,
			@ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		String scopeId = scope.orElse("default");
		String userId = ControllerUtil.getUserId(request);

		_log.debug("delete consumer " + String.valueOf(id) + " by " + userId + " for scope " + scopeId);

		consumerService.delete(scopeId, userId, id);

	}

	/*
	 * List w/scope
	 */

	@GetMapping(value = "/c/{scope}/consumers", produces = "application/json")
	@ApiOperation(value = "List consumers with filters")
	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
	@ResponseBody
	public List<ConsumerDTO> list(
			@ApiParam(value = "Scope", defaultValue = "default") @PathVariable("scope") Optional<String> scope,
			@ApiParam(value = "Consumer type") @RequestParam("type") Optional<String> type,
			@ApiParam(value = "Consumer id") @RequestParam("consumer") Optional<String> consumer,
			@ApiParam(value = "Consumer owner") @RequestParam("user") Optional<String> ownerId,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		String scopeId = scope.orElse("default");
		String userId = ControllerUtil.getUserId(request);

		_log.debug("list consumers by " + userId + " for scope " + scopeId);

		long total = 0;
		List<Registration> registrations = new ArrayList<>();

		// TODO refactor - ugly
		if (type.isPresent()) {
			total = consumerService.countByType(scopeId, userId, type.get());
			registrations = consumerService.listByType(scopeId, userId, type.get());
		} else if (consumer.isPresent()) {
			total = consumerService.countByConsumer(scopeId, userId, consumer.get());
			registrations = consumerService.listByConsumer(scopeId, userId, consumer.get());
		} else if (ownerId.isPresent()) {
			total = consumerService.countByUserId(userId, ownerId.get());
			registrations = consumerService.listByUserId(userId, ownerId.get());
		} else {
			total = consumerService.count(scopeId, userId);
			registrations = consumerService.list(scopeId, userId, pageable.getPageNumber(), pageable.getPageSize());
		}

		List<ConsumerDTO> results = registrations.stream().map(r -> ConsumerDTO.fromRegistration(r))
				.collect(Collectors.toList());
		// add total count as header
		response.setHeader("X-Total-Count", String.valueOf(total));

		return results;
	}

	/*
	 * Resource
	 */
	@GetMapping(value = "/consumers/{id}", produces = "application/json")
	@ApiOperation(value = "Fetch a specific consumer by id")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
			@ApiImplicitParam(name = "X-Scope", value = "Scope", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
	})
	@ResponseBody
	public ConsumerDTO get(
			@ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		Optional<String> scopeId = Optional.of(ControllerUtil.getScopeId(request));
		return get(scopeId, id, request, response);
	}

	@PostMapping(value = "/consumers", produces = "application/json")
	@ApiOperation(value = "Add a new consumer")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
			@ApiImplicitParam(name = "X-Scope", value = "Scope", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
	})
	@ResponseBody
	public ConsumerDTO add(
			@ApiParam(value = "Consumer json", required = true) @RequestBody ConsumerDTO consumer,
			HttpServletRequest request, HttpServletResponse response) throws NoSuchConsumerException {
		Optional<String> scopeId = Optional.of(ControllerUtil.getScopeId(request));
		return add(scopeId, consumer, request, response);

	}

	@DeleteMapping(value = "/consumers/{id}", produces = "application/json")
	@ApiOperation(value = "Delete a specific consumer by id")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
			@ApiImplicitParam(name = "X-Scope", value = "Scope", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
	})
	@ResponseBody
	public void delete(
			@ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		Optional<String> scopeId = Optional.of(ControllerUtil.getScopeId(request));
		delete(scopeId, id, request, response);

	}

	/*
	 * List
	 */

	@GetMapping(value = "/consumers", produces = "application/json")
	@ApiOperation(value = "List consumers with filters")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
			@ApiImplicitParam(name = "X-Scope", value = "Scope", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
	})
	@ResponseBody
	public List<ConsumerDTO> list(
			@ApiParam(value = "Consumer type") @RequestParam("type") Optional<String> type,
			@ApiParam(value = "Consumer id") @RequestParam("consumer") Optional<String> consumer,
			@ApiParam(value = "Consumer owner") @RequestParam("user") Optional<String> ownerId,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		Optional<String> scopeId = Optional.of(ControllerUtil.getScopeId(request));
		return list(scopeId, type, consumer, ownerId, request, response, pageable);
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
