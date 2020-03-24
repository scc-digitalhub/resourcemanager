package it.smartcommunitylab.resourcemanager.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.ConsumerException;
import it.smartcommunitylab.resourcemanager.common.NoSuchConsumerException;
import it.smartcommunitylab.resourcemanager.common.NoSuchRegistrationException;
import it.smartcommunitylab.resourcemanager.model.Consumer;
import it.smartcommunitylab.resourcemanager.model.ConsumerBuilder;
import it.smartcommunitylab.resourcemanager.model.Registration;

@Component
public class ConsumerService {
    private final static Logger _log = LoggerFactory.getLogger(ConsumerService.class);

    @Autowired
    private ConsumerLocalService consumerService;

    @Autowired
    private RegistrationLocalService registrationService;

    /*
     * Data
     */
    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '"
            + SystemKeys.PERMISSION_CONSUMER_CREATE + "')")
    public Registration add(String spaceId, String userId, String type, String consumer,
            Map<String, Serializable> properties,
            List<String> tags)
            throws NoSuchConsumerException, ConsumerException {

        // call local service
        return consumerService.add(spaceId, userId, type, consumer, properties, tags);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '"
            + SystemKeys.PERMISSION_CONSUMER_UPDATE + "')")
    public Registration update(String spaceId, String userId, long id,
            Map<String, Serializable> properties, List<String> tags)
            throws NoSuchConsumerException, ConsumerException {

        // call local service
        return consumerService.update(id, properties, tags);
    }

    @PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_REGISTRATION +
            "', '" + SystemKeys.PERMISSION_CONSUMER_DELETE + "')")
    public void delete(String spaceId, String userId, long id) throws NoSuchConsumerException {

        // call local service
        consumerService.delete(id);
    }

    @PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_REGISTRATION +
            "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public Registration get(String spaceId, String userId, long id) throws NoSuchConsumerException {

        try {
            // call local service
            return registrationService.get(id);
        } catch (NoSuchRegistrationException e) {
            throw new NoSuchConsumerException();
        }
    }

    @PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_REGISTRATION +
            "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public Consumer lookup(String spaceId, String userId, long id) throws NoSuchConsumerException {
        // call local service
        return consumerService.lookup(id);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE +
            "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public boolean exists(String spaceId, String userId, long id) throws NoSuchConsumerException {
        _log.info("exists registration " + String.valueOf(id) + " by user " + userId);

        // call local service
        boolean ret = registrationService.exists(id);
        if (!ret) {
            throw new NoSuchConsumerException();
        }

        return ret;
    }

    /*
     * Builders
     */

//	public boolean hasBuilder(String id) {
//		// TODO check auth
//		//
//		// call local service
//		return consumerService.hasBuilder(id);
//	}

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public Map<String, List<ConsumerBuilder>> listBuilders(String spaceId, String userId) {

        // call local service
        return consumerService.listBuilders();
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<ConsumerBuilder> listBuilders(String spaceId, String userId, String type) {

        // call local service
        return consumerService.listBuilders(type);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public ConsumerBuilder getBuilder(String spaceId, String userId, String id) throws NoSuchConsumerException {

        // call local service
        return consumerService.getBuilder(id);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<String> listTypes(String spaceId, String userId) {

        // call local service
        return consumerService.listTypes();
    }

    /*
     * Count
     */

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public long count(String spaceId, String userId) {

        // call local service with space
        return registrationService.countBySpaceId(spaceId);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public long countByType(String spaceId, String userId, String type) {

        // call local service
        return registrationService.countByTypeAndSpaceId(type, spaceId);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public long countByConsumer(String spaceId, String userId, String provider) {

        // call local service
        return registrationService.countByConsumerAndSpaceId(provider, spaceId);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public long countByUserId(String spaceId, String userId, String ownerId) {

        // call local service
        return registrationService.countByUserIdAndSpaceId(userId, spaceId);
    }

    /*
     * List
     */
    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<Registration> list(String spaceId, String userId) {

        // call local service with space
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(registrationService.listBySpaceId(spaceId));
    }

    public List<Registration> list(String spaceId, String userId, int page, int pageSize) {

        // call local service
        return list(spaceId, userId, page, pageSize, "id", SystemKeys.ORDER_ASC);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<Registration> list(String spaceId, String userId, int page, int pageSize, String orderBy,
            String order) {

        Sort sort = (order.equals(SystemKeys.ORDER_ASC) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(registrationService.listBySpaceId(spaceId, pageable));
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<Registration> listByType(String spaceId, String userId, String type) {

        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(registrationService.listByTypeAndSpaceId(type, spaceId));
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<Registration> listByConsumer(String spaceId, String userId, String provider) {

        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(registrationService.listByConsumerAndSpaceId(provider, spaceId));
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SPACE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
    public List<Registration> listByUserId(String spaceId, String userId, String ownerId) {

        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(registrationService.listByUserIdAndSpaceId(userId, spaceId));
    }

}
