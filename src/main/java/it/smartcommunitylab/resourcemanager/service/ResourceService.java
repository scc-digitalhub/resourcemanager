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
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.model.Resource;

@Component
public class ResourceService {

    private final static Logger _log = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private ResourceLocalService resourceLocalService;

    /*
     * Data
     */
    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '"
            + SystemKeys.PERMISSION_RESOURCE_CREATE + "')")
    public Resource create(String spaceId, String userId,
            String type, String providerId, String name,
            Map<String, Serializable> properties, List<String> tags)
            throws NoSuchProviderException, ResourceProviderException, InvalidNameException, DuplicateNameException {
        _log.info("create resource with " + String.valueOf(providerId) + " by user " + userId);

        // call local service
        return resourceLocalService.create(spaceId, userId, type, providerId, name, properties, tags);

    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '"
            + SystemKeys.PERMISSION_RESOURCE_CREATE + "')")
    public Resource add(String spaceId, String userId, String type, String providerId,
            String name, String uri,
            Map<String, Serializable> properties, List<String> tags)
            throws NoSuchProviderException, ResourceProviderException {
        _log.info("add resource with " + String.valueOf(providerId) + " by user " + userId);

        // call local service
        return resourceLocalService.add(spaceId, userId, type, name, providerId, uri, properties, tags);

    }

    @PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_RESOURCE +
            "', '" + SystemKeys.PERMISSION_RESOURCE_UPDATE + "')")
    public Resource update(String spaceId, String userId, long id,
            Map<String, Serializable> properties, List<String> tags)
            throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        _log.info("update resource " + String.valueOf(id) + " by user " + userId);

        // call local service
        return resourceLocalService.update(id, properties, tags);

    }

    @PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_RESOURCE +
            "', '" + SystemKeys.PERMISSION_RESOURCE_DELETE + "')")
    public void delete(String spaceId, String userId, long id)
            throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        _log.info("delete resource " + String.valueOf(id) + " by user " + userId);

        // call local service
        resourceLocalService.delete(id);
    }

    @PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_RESOURCE +
            "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public Resource get(String spaceId, String userId, long id) throws NoSuchResourceException {
        _log.info("get resource " + String.valueOf(id) + " by user " + userId);

        // call local service
        return resourceLocalService.get(id);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE +
            "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public boolean exists(String spaceId, String userId, long id) throws NoSuchResourceException {
        _log.info("exists resource " + String.valueOf(id) + " by user " + userId);

        // call local service
        boolean ret = resourceLocalService.exists(id);
        if (!ret) {
            throw new NoSuchResourceException();
        }

        return ret;
    }
    /*
     * Count
     */

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE +
            "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public long count(String spaceId, String userId) {

        // call local service with space
        return resourceLocalService.countBySpaceId(spaceId);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE
            + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public long countByType(String spaceId, String userId, String type) {

        // call local service
        return resourceLocalService.countByTypeAndSpaceId(type, spaceId);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE
            + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public long countByProvider(String spaceId, String userId, String provider) {

        // call local service
        return resourceLocalService.countByProviderAndSpaceId(provider, spaceId);
    }

    @PreAuthorize("hasPermission(#spaceId, '"
            + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public long countByUserId(String spaceId, String userId, String ownerId) {

        // call local service
        return resourceLocalService.countByUserIdAndSpaceId(spaceId, ownerId);
    }

    /*
     * List
     */

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public List<Resource> list(String spaceId, String userId) {

        // call local service with space
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(resourceLocalService.listBySpaceId(spaceId));
    }

    public List<Resource> list(String spaceId, String userId, int page, int pageSize) {

        // call local service
        return list(spaceId, userId, page, pageSize, "id", SystemKeys.ORDER_ASC);
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public List<Resource> list(String spaceId, String userId, int page, int pageSize, String orderBy, String order) {

        Sort sort = (order.equals(SystemKeys.ORDER_ASC) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(resourceLocalService.listBySpaceId(spaceId, pageable));
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public List<Resource> listByType(String spaceId, String userId, String type) {

        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(resourceLocalService.listByTypeAndSpaceId(type, spaceId));
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public List<Resource> listByProvider(String spaceId, String userId, String provider) {

        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(resourceLocalService.listByProviderAndSpaceId(provider, spaceId));
    }

    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    @PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public List<Resource> listByUserId(String spaceId, String userId, String ownerId) {

        // call local service
        // need to create new MUTABLE list for postFilter usage of collection.clear()
        // see DefaultMethodSecurityExpressionHandler.java
        return new ArrayList<>(resourceLocalService.listByUserIdAndSpaceId(ownerId, spaceId));
    }

    /*
     * Check
     */
    @PreAuthorize("hasPermission(#spaceId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_RESOURCE_VIEW + "')")
    public void check(String spaceId, String userId, long id)
            throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        _log.info("check resource " + String.valueOf(id) + " by user " + userId);

        // call local service
        resourceLocalService.check(id);

    }

}
