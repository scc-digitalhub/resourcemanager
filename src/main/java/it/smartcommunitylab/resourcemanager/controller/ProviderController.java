package it.smartcommunitylab.resourcemanager.controller;

import java.security.Principal;
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

import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.dto.ProviderDTO;
import it.smartcommunitylab.resourcemanager.model.Provider;
import it.smartcommunitylab.resourcemanager.service.ProviderService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
public class ProviderController {

	private final static Logger _log = LoggerFactory.getLogger(ProviderController.class);

	@Autowired
	private ProviderService providerService;

	/*
	 * List
	 */

	@GetMapping({ "/providers", "/c/{scope}/providers" })
	public List<ProviderDTO> list(
			@RequestParam("type") Optional<String> type,
			HttpServletRequest request, HttpServletResponse response,
			Pageable pageable) {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		List<ProviderDTO> results = new ArrayList<>();
		if (type.isPresent()) {
			List<Provider> providers = providerService.list(scopeId, userId, type.get());
			for (Provider p : providers) {
				results.add(ProviderDTO.fromProvider(p));
			}
		} else {
			Map<String, List<Provider>> providers = providerService.list(scopeId, userId);
			for (String t : providers.keySet()) {
				for (Provider p : providers.get(t)) {
					results.add(ProviderDTO.fromProvider(p));
				}
			}

		}

		// add total count as header
		response.setHeader("X-Total-Count", String.valueOf(results.size()));

		return results;
	}

	/*
	 * Get
	 */

	@GetMapping({ "/providers/{id}", "/c/{scope}/providers/{id}" })
	@ResponseBody
	public ProviderDTO get(
			@PathVariable("id") String id,
			HttpServletRequest request, HttpServletResponse response)
			throws NoSuchProviderException {

		String scopeId = ControllerUtil.getScopeId(request);
		String userId = ControllerUtil.getUserId(request);

		_log.debug("get " + id + " by " + userId);

		Provider p = providerService.get(scopeId, userId, id);

		return ProviderDTO.fromProvider(p);
	}
	/*
	 * Exceptions
	 */

	@ExceptionHandler(NoSuchProviderException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public String notFound(NoSuchProviderException ex) {
		return ex.getMessage();
	}

	/*
	 * Helper
	 */

}
