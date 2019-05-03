package it.smartcommunitylab.resourcemanager.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.model.Provider;

@Component
public class ProviderService {
	private final static Logger _log = LoggerFactory.getLogger(ProviderService.class);

	@Autowired
	private ProviderLocalService providerService;

	public Map<String, List<Provider>> list(String scopeId, String userId) {
		// TODO check auth
		//
		// call local service
		return providerService.listProviders();
	}

	public List<Provider> list(String scopeId, String userId, String type) {
		// TODO check auth
		//
		// call local service
		return providerService.listProviders(type);
	}

	public Provider get(String scopeId, String userId, String id) throws NoSuchProviderException {
		// TODO check auth
		//
		// call local service
		return providerService.getProvider(id);
	}
}
