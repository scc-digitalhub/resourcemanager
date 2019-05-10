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
	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '"
			+ SystemKeys.PERMISSION_CONSUMER_CREATE + "')")
	public Registration add(String scopeId, String userId, String type, String consumer,
			Map<String, Serializable> properties)
			throws NoSuchConsumerException, ConsumerException {

		// call local service
		return consumerService.add(scopeId, userId, type, consumer, properties);
	}

	@PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_REGISTRATION +
			"', '" + SystemKeys.PERMISSION_CONSUMER_DELETE + "')")
	public void delete(String scopeId, String userId, long id) throws NoSuchConsumerException {

		// call local service
		consumerService.delete(id);
	}

	@PreAuthorize("hasPermission(#id, '" + SystemKeys.ENTITY_REGISTRATION +
			"', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public Registration get(String scopeId, String userId, long id) throws NoSuchConsumerException {

		try {
			// call local service
			return registrationService.get(id);
		} catch (NoSuchRegistrationException e) {
			throw new NoSuchConsumerException();
		}
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

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public Map<String, List<String>> listBuilders(String scopeId, String userId) {

		// call local service
		return consumerService.listBuilders();
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public List<String> listBuilders(String scopeId, String userId, String type) {

		// call local service
		return consumerService.listBuilders(type);
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public ConsumerBuilder getBuilder(String scopeId, String userId, String id) throws NoSuchConsumerException {

		// call local service
		return consumerService.getBuilder(id);
	}

	/*
	 * Count
	 */

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public long count(String scopeId, String userId) {

		// call local service with scope
		return registrationService.countByScopeId(scopeId);
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public long countByType(String scopeId, String userId, String type) {

		// call local service
		return registrationService.countByTypeAndScopeId(type, scopeId);
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public long countByConsumer(String scopeId, String userId, String provider) {

		// call local service
		return registrationService.countByConsumerAndScopeId(provider, scopeId);
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public long countByUserId(String scopeId, String userId, String ownerId) {

		// call local service
		return registrationService.countByUserIdAndScopeId(userId, scopeId);
	}

	/*
	 * List
	 */
	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	@PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public List<Registration> list(String scopeId, String userId) {

		// call local service with scope
		// need to create new MUTABLE list for postFilter usage of collection.clear()
		// see DefaultMethodSecurityExpressionHandler.java
		return new ArrayList<>(registrationService.listByScopeId(scopeId));
	}

	public List<Registration> list(String scopeId, String userId, int page, int pageSize) {

		// call local service
		return list(scopeId, userId, page, pageSize, "id", SystemKeys.ORDER_ASC);
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	@PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public List<Registration> list(String scopeId, String userId, int page, int pageSize, String orderBy,
			String order) {

		Sort sort = (order.equals(SystemKeys.ORDER_ASC) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		// call local service
		// need to create new MUTABLE list for postFilter usage of collection.clear()
		// see DefaultMethodSecurityExpressionHandler.java
		return new ArrayList<>(registrationService.listByScopeId(scopeId, pageable));
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	@PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public List<Registration> listByType(String scopeId, String userId, String type) {

		// call local service
		// need to create new MUTABLE list for postFilter usage of collection.clear()
		// see DefaultMethodSecurityExpressionHandler.java
		return new ArrayList<>(registrationService.listByTypeAndScopeId(type, scopeId));
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	@PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public List<Registration> listByConsumer(String scopeId, String userId, String provider) {

		// call local service
		// need to create new MUTABLE list for postFilter usage of collection.clear()
		// see DefaultMethodSecurityExpressionHandler.java
		return new ArrayList<>(registrationService.listByConsumerAndScopeId(provider, scopeId));
	}

	@PreAuthorize("hasPermission(#scopeId, '" + SystemKeys.SCOPE + "', '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	@PostFilter("hasPermission(filterObject, '" + SystemKeys.PERMISSION_CONSUMER_VIEW + "')")
	public List<Registration> listByUserId(String scopeId, String userId, String ownerId) {

		// call local service
		// need to create new MUTABLE list for postFilter usage of collection.clear()
		// see DefaultMethodSecurityExpressionHandler.java
		return new ArrayList<>(registrationService.listByUserIdAndScopeId(userId, scopeId));
	}

}
