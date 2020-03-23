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
import org.springframework.beans.factory.annotation.Value;
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
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.dto.ProviderDTO;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;
import it.smartcommunitylab.resourcemanager.service.ProviderService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
@Api(value = "/providers")
public class ProviderController {

    private final static Logger _log = LoggerFactory.getLogger(ProviderController.class);

    @Value("${spaces.default}")
    private String defaultSpace;

    @Autowired
    private ProviderService providerService;

    /*
     * List w/space
     */

    @GetMapping(value = "/api/c/{space}/providers", produces = "application/json")
    @ApiOperation(value = "List available resource providers")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public List<ProviderDTO> list(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @RequestParam("type") Optional<String> type,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        String spaceId = space.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("list providers by " + userId + " for space " + spaceId);

        List<ProviderDTO> results = new ArrayList<>();
        if (type.isPresent()) {
            List<ResourceProvider> providers = providerService.list(spaceId, userId, type.get());
            for (ResourceProvider p : providers) {
                results.add(ProviderDTO.fromProvider(p));
            }
        } else {
            Map<String, List<ResourceProvider>> providers = providerService.list(spaceId, userId);
            for (String t : providers.keySet()) {
                for (ResourceProvider p : providers.get(t)) {
                    results.add(ProviderDTO.fromProvider(p));
                }
            }

        }

        // add total count as header
        response.setHeader("X-Total-Count", String.valueOf(results.size()));

        return results;
    }

    /*
     * Get w/space
     */

    @GetMapping(value = "/api/c/{space}/providers/{id}", produces = "application/json")
    @ApiOperation(value = "Fetch a specific resource provider by id")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ProviderDTO get(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @PathVariable("id") String id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchProviderException {

        String spaceId = space.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("get provider " + id + " by " + userId + " for space " + spaceId);

        ResourceProvider p = providerService.get(spaceId, userId, id);

        return ProviderDTO.fromProvider(p);
    }

    /*
     * Types w/space
     */

    @GetMapping(value = "/api/c/{space}/providers/types", produces = "application/json")
    @ApiOperation(value = "List available provider types")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public List<String> types(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        String spaceId = space.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("list types by " + userId + " for space " + spaceId);

        List<String> results = providerService.listTypes(spaceId, userId);

        // add total count as header
        response.setHeader("X-Total-Count", String.valueOf(results.size()));

        return results;
    }

    /*
     * List
     */

    @GetMapping(value = "/api/providers", produces = "application/json")
    @ApiOperation(value = "List available resource providers")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    public List<ProviderDTO> list(
            @RequestParam("type") Optional<String> type,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        return list(spaceId, type, request, response, pageable);
    }

    /*
     * Get
     */

    @GetMapping(value = "/api/providers/{id}", produces = "application/json")
    @ApiOperation(value = "Fetch a specific resource provider by id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ProviderDTO get(
            @PathVariable("id") String id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchProviderException {

        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        return get(spaceId, id, request, response);
    }

    /*
     * Type
     */

    @GetMapping(value = "/api/providers/types", produces = "application/json")
    @ApiOperation(value = "List available provider types")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    public List<String> types(
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        return types(spaceId, request, response, pageable);
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
