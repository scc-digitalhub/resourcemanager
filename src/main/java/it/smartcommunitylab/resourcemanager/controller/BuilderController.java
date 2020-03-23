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
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.dto.BuilderDTO;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;
import it.smartcommunitylab.resourcemanager.service.ConsumerService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
@Api(value = "/builders")
public class BuilderController {

    private final static Logger _log = LoggerFactory.getLogger(BuilderController.class);

    @Value("${spaces.default}")
    private String defaultSpace;

    @Autowired
    private ConsumerService consumerService;

    /*
     * List w/space
     */

    @GetMapping(value = "/api/c/{space}/builders", produces = "application/json")
    @ApiOperation(value = "List available consumer builders")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public List<BuilderDTO> list(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @RequestParam("type") Optional<String> type,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        String spaceId = space.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("list builders by " + userId + " for space " + spaceId);

        List<BuilderDTO> results = new ArrayList<>();
        if (type.isPresent()) {
            List<ConsumerBuilder> builders = consumerService.listBuilders(spaceId, userId, type.get());
            for (ConsumerBuilder cb : builders) {
                results.add(BuilderDTO.fromBuilder(cb));
            }

        } else {
            Map<String, List<ConsumerBuilder>> map = consumerService.listBuilders(spaceId, userId);
            for (String t : map.keySet()) {
                for (ConsumerBuilder cb : map.get(t)) {
                    results.add(BuilderDTO.fromBuilder(cb));
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

    @GetMapping(value = "/api/c/{space}/builders/{id}", produces = "application/json")
    @ApiOperation(value = "Fetch a specific consumer builder by id")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public BuilderDTO get(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @PathVariable("id") String id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchConsumerException {

        String spaceId = space.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("get builder " + id + " by " + userId + " for space " + spaceId);

        ConsumerBuilder cb = consumerService.getBuilder(spaceId, userId, id);

        return BuilderDTO.fromBuilder(cb);
    }

    /*
     * Types w/space
     */

    @GetMapping(value = "/api/c/{space}/builders/types", produces = "application/json")
    @ApiOperation(value = "List available consumer types")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public List<String> types(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        String spaceId = space.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("list types by " + userId + " for space " + spaceId);

        List<String> results = consumerService.listTypes(spaceId, userId);

        // add total count as header
        response.setHeader("X-Total-Count", String.valueOf(results.size()));

        return results;
    }

    /*
     * List
     */

    @GetMapping(value = "/api/builders", produces = "application/json")
    @ApiOperation(value = "List available consumer builders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    public List<BuilderDTO> list(
            @RequestParam("type") Optional<String> type,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        return list(spaceId, type, request, response, pageable);
    }

    /*
     * Get
     */

    @GetMapping(value = "/api/builders/{id}", produces = "application/json")
    @ApiOperation(value = "Fetch a specific consumer builder by id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public BuilderDTO get(
            @PathVariable("id") String id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchConsumerException {

        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        return get(spaceId, id, request, response);
    }

    /*
     * Type
     */

    @GetMapping(value = "/api/builders/types", produces = "application/json")
    @ApiOperation(value = "List available consumer types")
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