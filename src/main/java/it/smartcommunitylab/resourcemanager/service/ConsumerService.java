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

	public Registration add(String scopeId, String userId, String type, String consumer,
			Map<String, Serializable> properties)
			throws NoSuchConsumerException {
		// TODO check auth
		//
		// call local service
		return consumerService.add(scopeId, userId, type, consumer, properties);
	}

	public void delete(String scopeId, String userId, long id) throws NoSuchConsumerException {
		// TODO check auth
		//
		// call local service
		consumerService.delete(id);
	}

	public Registration get(String scopeId, String userId, long id) throws NoSuchConsumerException {

		try {
			// TODO check auth
			//
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

	public Map<String, List<String>> listBuilders(String scopeId, String userId) {
		// TODO check auth
		//
		// call local service
		return consumerService.listBuilders();
	}

	public List<String> listBuilders(String scopeId, String userId, String type) {
		// TODO check auth
		//
		// call local service
		return consumerService.listBuilders(type);
	}

	public ConsumerBuilder getBuilder(String scopeId, String userId, String id) throws NoSuchConsumerException {
		// TODO check auth
		//
		// call local service
		return consumerService.getBuilder(id);
	}

	/*
	 * Count
	 */

	public long count(String scopeId, String userId) {
		// TODO check auth
		//
		// call local service with scope
		return registrationService.countByScopeId(scopeId);
	}

	public long countByType(String scopeId, String userId, String type) {
		// TODO check auth
		//
		// call local service
		return registrationService.countByTypeAndScopeId(type, scopeId);
	}

	public long countByConsumer(String scopeId, String userId, String provider) {
		// TODO check auth
		//
		// call local service
		return registrationService.countByConsumerAndScopeId(provider, scopeId);
	}

	public long countByUserId(String userId, String ownerId) {
		// TODO check auth
		//
		// call local service
		return registrationService.countByUserId(userId);
	}

	/*
	 * List
	 */

	public List<Registration> list(String scopeId, String userId) {
		// TODO check auth+filter
		//
		// call local service with scope
		return registrationService.listByScopeId(scopeId);
	}

	public List<Registration> list(String scopeId, String userId, int page, int pageSize) {
		// TODO check auth+filter
		//
		// call local service
		return list(scopeId, userId, page, pageSize, "id", SystemKeys.ORDER_ASC);
	}

	public List<Registration> list(String scopeId, String userId, int page, int pageSize, String orderBy,
			String order) {
		// TODO check auth+filter
		//
		Sort sort = (order.equals(SystemKeys.ORDER_ASC) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		// call local service
		return registrationService.listByScopeId(scopeId, pageable);
	}

	public List<Registration> listByType(String scopeId, String userId, String type) {
		// TODO check auth+filter
		//
		// call local service
		return registrationService.listByTypeAndScopeId(type, scopeId);
	}

	public List<Registration> listByConsumer(String scopeId, String userId, String provider) {
		// TODO check auth+filter
		//
		// call local service
		return registrationService.listByConsumerAndScopeId(provider, scopeId);
	}

	public List<Registration> listByUserId(String userId, String ownerId) {
		// TODO check auth+filter
		//
		// call local service
		return registrationService.listByUserId(userId);
	}

}
