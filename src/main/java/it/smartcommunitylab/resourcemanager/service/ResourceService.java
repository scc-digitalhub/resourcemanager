package it.smartcommunitylab.resourcemanager.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.model.Resource;

@Component
public class ResourceService {

	private final static Logger _log = LoggerFactory.getLogger(ResourceService.class);

	@Autowired
	private ResourceLocalService resourceLocalService;

	/*
	 * Data
	 */
	public Resource create(String scopeId, String userId, String type, String providerId,
			Map<String, Serializable> properties)
			throws NoSuchProviderException {
		_log.info("create resource with " + String.valueOf(providerId) + " by user " + userId);

		// TODO check auth
		//
		// call local service
		return resourceLocalService.create(scopeId, userId, type, providerId, properties);

	}

	public Resource update(String scopeId, String userId, long id, Map<String, Serializable> properties)
			throws NoSuchResourceException, NoSuchProviderException {
		_log.info("update resource " + String.valueOf(id) + " by user " + userId);

		// TODO check auth
		//
		// call local service
		return resourceLocalService.update(id, properties);

	}

	public void delete(String scopeId, String userId, long id) throws NoSuchResourceException, NoSuchProviderException {
		_log.info("delete resource " + String.valueOf(id) + " by user " + userId);

		// TODO check auth
		//
		// call local service
		resourceLocalService.delete(id);
	}

	public Resource get(String scopeId, String userId, long id) throws NoSuchResourceException {
		_log.info("get resource " + String.valueOf(id) + " by user " + userId);

		// TODO check auth
		//
		// call local service
		return resourceLocalService.get(id);
	}

	/*
	 * Count
	 */

	public long count(String scopeId, String userId) {
		// TODO check auth
		//
		// call local service with scope
		return resourceLocalService.countByScopeId(scopeId);
	}

	public long countByType(String scopeId, String userId, String type) {
		// TODO check auth
		//
		// call local service
		return resourceLocalService.countByTypeAndScopeId(type, scopeId);
	}

	public long countByProvider(String scopeId, String userId, String provider) {
		// TODO check auth
		//
		// call local service
		return resourceLocalService.countByProviderAndScopeId(provider, scopeId);
	}

	public long countByUserId(String userId, String ownerId) {
		// TODO check auth
		//
		// call local service
		return resourceLocalService.countByUserId(userId);
	}

	/*
	 * List
	 */

	public List<Resource> list(String scopeId, String userId) {
		// TODO check auth+filter
		//
		// call local service with scope
		return resourceLocalService.listByScopeId(scopeId);
	}

	public List<Resource> list(String scopeId, String userId, int page, int pageSize) {
		// TODO check auth+filter
		//
		// call local service
		return list(scopeId, userId, page, pageSize, "id", SystemKeys.ORDER_ASC);
	}

	public List<Resource> list(String scopeId, String userId, int page, int pageSize, String orderBy, String order) {
		// TODO check auth+filter
		//
		Sort sort = (order.equals(SystemKeys.ORDER_ASC) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		// call local service
		return resourceLocalService.listByScopeId(scopeId, pageable);
	}

	public List<Resource> listByType(String scopeId, String userId, String type) {
		// TODO check auth+filter
		//
		// call local service
		return resourceLocalService.listByTypeAndScopeId(type, scopeId);
	}

	public List<Resource> listByProvider(String scopeId, String userId, String provider) {
		// TODO check auth+filter
		//
		// call local service
		return resourceLocalService.listByProviderAndScopeId(provider, scopeId);
	}

	public List<Resource> listByUserId(String userId, String ownerId) {
		// TODO check auth+filter
		//
		// call local service
		return resourceLocalService.listByUserId(ownerId);
	}

	/*
	 * Check
	 */

	public void check(String scopeId, String userId, long id) throws NoSuchResourceException, NoSuchProviderException {
		_log.info("check resource " + String.valueOf(id) + " by user " + userId);

		// TODO check auth
		//
		// call local service
		resourceLocalService.check(id);

	}

}
