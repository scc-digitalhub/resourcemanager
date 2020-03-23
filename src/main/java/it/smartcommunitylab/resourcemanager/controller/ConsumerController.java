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
import it.smartcommunitylab.resourcemanager.common.ConsumerException;
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.dto.ConsumerDTO;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.Registration;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ConsumerService;
import it.smartcommunitylab.resourcemanager.util.ControllerUtil;

@RestController
@Api(value = "/consumers")
public class ConsumerController {

    private final static Logger _log = LoggerFactory.getLogger(ConsumerController.class);

    @Value("${spaces.default}")
    private String defaultSpace;

    @Autowired
    private ConsumerService consumerService;

    /*
     * Consumer registration w/space
     */
    @GetMapping(value = {
            "/api/consumers/{id}",
            "/api/-/{space}/consumers/{id}"
    }, produces = "application/json")
    @ApiOperation(value = "Fetch a specific consumer by id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ConsumerDTO get(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchConsumerException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("get consumer " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        // call exists to trigger 404, otherwise get() will
        // check permissions *before* checking existence
        consumerService.exists(spaceId, userId, id);

        Consumer consumer = consumerService.lookup(spaceId, userId, id);

        // include private fields on detail view
        return ConsumerDTO.fromConsumer(consumer, true);
    }

    @PostMapping(value = {
            "/api/consumers",
            "/api/-/{space}/consumers"
    }, produces = "application/json")
    @ApiOperation(value = "Add a new consumer")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ConsumerDTO add(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Consumer json", required = true) @RequestBody ConsumerDTO res,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchConsumerException, ConsumerException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("add consumer by " + userId + " for space " + spaceId);

        // parse fields from post
        Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(res.getProperties());
        List<String> tags = new ArrayList<>(Arrays.asList(res.getTags()));

        Registration reg = consumerService.add(spaceId, userId, res.getType(), res.getConsumer(), propertiesMap, tags);

        // include private fields on create view
        return ConsumerDTO.fromRegistration(reg, true);

    }

    @PutMapping(value = {
            "/api/consumers/{id}",
            "/api/-/{space}/consumers/{id}"
    }, produces = "application/json")
    @ApiOperation(value = "Update a consumer")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ConsumerDTO update(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
            @ApiParam(value = "Consumer json", required = true) @RequestBody ConsumerDTO res,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchConsumerException, ConsumerException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("update consumer by " + userId + " for space " + spaceId);

        // parse fields from post
        Map<String, Serializable> propertiesMap = Resource.propertiesFromValue(res.getProperties());
        List<String> tags = new ArrayList<>(Arrays.asList(res.getTags()));

        res.id = id;

        // call exists to trigger 404, otherwise update() will
        // check permissions *before* checking existence
        consumerService.exists(spaceId, userId, id);

        Registration reg = consumerService.update(spaceId, userId, id, propertiesMap, tags);

        // include private fields on create view
        return ConsumerDTO.fromRegistration(reg, true);

    }

    @DeleteMapping(value = {
            "/api/consumers/{id}",
            "/api/-/{space}/consumers/{id}"
    }, produces = "application/json")
    @ApiOperation(value = "Delete a specific consumer by id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public ConsumerDTO delete(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
            HttpServletRequest request, HttpServletResponse response)
            throws NoSuchConsumerException {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("delete consumer " + String.valueOf(id) + " by " + userId + " for space " + spaceId);

        // call exists to trigger 404, otherwise delete() will
        // check permissions *before* checking existence
        consumerService.exists(spaceId, userId, id);

        // fetch resource to provide as result on success
        Consumer consumer = consumerService.lookup(spaceId, userId, id);

        consumerService.delete(spaceId, userId, id);

        return ConsumerDTO.fromConsumer(consumer, false);
    }

    /*
     * List w/space
     */

    @GetMapping(value = {
            "/api/consumers",
            "/api/-/{space}/consumers"
    }, produces = "application/json")
    @ApiOperation(value = "List consumers with filters")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
    })
    @ResponseBody
    public List<ConsumerDTO> list(
            @ApiParam(value = "Space", defaultValue = "default") @PathVariable("space") Optional<String> space,
            @ApiParam(value = "Consumer type") @RequestParam("type") Optional<String> type,
            @ApiParam(value = "Consumer id") @RequestParam("consumer") Optional<String> consumer,
            @ApiParam(value = "Consumer owner") @RequestParam("user") Optional<String> ownerId,
            HttpServletRequest request, HttpServletResponse response,
            Pageable pageable) {

        Optional<String> xSpace = Optional.ofNullable(ControllerUtil.getSpaceId(request));
        String spaceId = xSpace.orElse(defaultSpace);
        String userId = ControllerUtil.getUserId(request);

        _log.debug("list consumers by " + userId + " for space " + spaceId);

        long total = 0;
        List<Registration> registrations = new ArrayList<>();

        // TODO refactor - ugly
        if (type.isPresent()) {
            total = consumerService.countByType(spaceId, userId, type.get());
            registrations = consumerService.listByType(spaceId, userId, type.get());
        } else if (consumer.isPresent()) {
            total = consumerService.countByConsumer(spaceId, userId, consumer.get());
            registrations = consumerService.listByConsumer(spaceId, userId, consumer.get());
        } else if (ownerId.isPresent()) {
            total = consumerService.countByUserId(spaceId, userId, ownerId.get());
            registrations = consumerService.listByUserId(spaceId, userId, ownerId.get());
        } else {
            total = consumerService.count(spaceId, userId);
            registrations = consumerService.list(spaceId, userId, pageable.getPageNumber(), pageable.getPageSize());
        }

        List<ConsumerDTO> results = registrations.stream().map(r -> ConsumerDTO.fromRegistration(r))
                .collect(Collectors.toList());
        // add total count as header
        response.setHeader("X-Total-Count", String.valueOf(total));

        return results;
    }

//    /*
//     * Resource
//     */
//    @GetMapping(value = "/api/consumers/{id}", produces = "application/json")
//    @ApiOperation(value = "Fetch a specific consumer by id")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public ConsumerDTO get(
//            @ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
//            HttpServletRequest request, HttpServletResponse response)
//            throws NoSuchConsumerException {
//
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return get(spaceId, id, request, response);
//    }
//
//    @PostMapping(value = "/api/consumers", produces = "application/json")
//    @ApiOperation(value = "Add a new consumer")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public ConsumerDTO add(
//            @ApiParam(value = "Consumer json", required = true) @RequestBody ConsumerDTO consumer,
//            HttpServletRequest request, HttpServletResponse response)
//            throws NoSuchConsumerException, ConsumerException {
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return add(spaceId, consumer, request, response);
//
//    }
//
//    @PutMapping(value = "/api/consumers/{id}", produces = "application/json")
//    @ApiOperation(value = "Update a consumer")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public ConsumerDTO update(
//            @ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
//            @ApiParam(value = "Consumer json", required = true) @RequestBody ConsumerDTO consumer,
//            HttpServletRequest request, HttpServletResponse response)
//            throws NoSuchConsumerException, ConsumerException {
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return update(spaceId, id, consumer, request, response);
//
//    }
//
//    @DeleteMapping(value = "/api/consumers/{id}", produces = "application/json")
//    @ApiOperation(value = "Delete a specific consumer by id")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public ConsumerDTO delete(
//            @ApiParam(value = "Consumer id", required = true) @PathVariable("id") long id,
//            HttpServletRequest request, HttpServletResponse response)
//            throws NoSuchConsumerException {
//
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return delete(spaceId, id, request, response);
//
//    }
//
//    /*
//     * List
//     */
//
//    @GetMapping(value = "/api/consumers", produces = "application/json")
//    @ApiOperation(value = "List consumers with filters")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
//            @ApiImplicitParam(name = "X-Space", value = "Space", required = false, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "default", defaultValue = "default")
//    })
//    @ResponseBody
//    public List<ConsumerDTO> list(
//            @ApiParam(value = "Consumer type") @RequestParam("type") Optional<String> type,
//            @ApiParam(value = "Consumer id") @RequestParam("consumer") Optional<String> consumer,
//            @ApiParam(value = "Consumer owner") @RequestParam("user") Optional<String> ownerId,
//            HttpServletRequest request, HttpServletResponse response,
//            Pageable pageable) {
//
//        Optional<String> spaceId = Optional.ofNullable(ControllerUtil.getSpaceId(request));
//        return list(spaceId, type, consumer, ownerId, request, response, pageable);
//    }

    /*
     * Exceptions
     */

    @ExceptionHandler(NoSuchConsumerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String notFound(NoSuchConsumerException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ConsumerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String consumerError(ConsumerException ex) {
        return ex.getMessage();
    }

    /*
     * Helper
     */

}
