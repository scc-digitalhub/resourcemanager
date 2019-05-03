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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.dto.BuilderDTO;
import it.smartcommunitylab.resourcemanager.dto.ConsumerDTO;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ConsumerService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
public class ConsumerController {

	private final static Logger _log = LoggerFactory.getLogger(ConsumerController.class);

	@Autowired
	private ConsumerService consumerService;

	/*
	 * Resource
	 */
	@GetMapping({ "/consumers/{id}", "/c/{scope}/consumers/{id}" })
	@ResponseBody
	public ConsumerDTO get(
			@PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		_log.debug("get " + String.valueOf(id) + " by " + userId);

		Registration reg = consumerService.get(scopeId, userId, id);

		return ConsumerDTO.fromRegistration(reg);
	}

	@PostMapping({ "/consumers", "/c/{scope}/consumers" })
	@ResponseBody
	public ConsumerDTO add(
			@RequestBody ConsumerDTO res,
			HttpServletRequest request, HttpServletResponse response) throws NoSuchConsumerException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		// parse fields from post
		Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(res.getProperties());

		Registration reg = consumerService.add(scopeId, userId, res.getType(), res.getConsumer(), propertiesMap);

		return ConsumerDTO.fromRegistration(reg);

	}

	@DeleteMapping({ "/consumers/{id}", "/c/{scope}/consumers/{id}" })
	@ResponseBody
	public void delete(
			@PathVariable("id") long id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchConsumerException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		consumerService.delete(scopeId, userId, id);

	}

	/*
	 * List
	 */

	@GetMapping({ "/consumers", "/c/{scope}/consumers" })
	@ResponseBody
	public List<ConsumerDTO> list(
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		long total = consumerService.count(scopeId, userId);
		List<Registration> registrations = consumerService.list(scopeId, userId, pageable.getPageNumber(),
				pageable.getPageSize());
		List<ConsumerDTO> results = registrations.stream().map(r -> ConsumerDTO.fromRegistration(r))
				.collect(Collectors.toList());
		// add total count as header
		response.setHeader("X-Total-Count", String.valueOf(total));

		return results;
	}

	/*
	 * Builders
	 */

	@GetMapping({ "/builders", "/c/{scope}/builders" })
	public List<BuilderDTO> listBuilders(
			@RequestParam("type") Optional<String> type,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

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
