package it.smartcommunitylab.resourcemanager.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.model.Provider;

@Component
public class ProviderLocalService {
	private final static Logger _log = LoggerFactory.getLogger(ProviderLocalService.class);

	@Autowired
	private Map<String, Provider> _providers;

	public Map<String, Provider> availableProviders() {
		// return only active providers
		return _providers.entrySet().stream()
				.filter(entry -> (entry.getValue().getStatus() > -1))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public Map<String, List<Provider>> listProviders() {
		Map<String, List<Provider>> map = new HashMap<>();

		// static init for all types
		map.put(SystemKeys.TYPE_SQL, new ArrayList<>());
		map.put(SystemKeys.TYPE_NOSQL, new ArrayList<>());
		map.put(SystemKeys.TYPE_FILE, new ArrayList<>());
		map.put(SystemKeys.TYPE_OBJECT, new ArrayList<>());

		for (Provider p : _providers.values()) {
			if (p.getStatus() > -1) {
				map.get(p.getType()).add(p);
			}
		}
		return map;
	}

	public List<Provider> listProviders(String type) {
		// return only active providers
		return _providers.entrySet().stream()
				.map(entry -> entry.getValue())
				.filter(entry -> (entry.getStatus() > -1 && entry.getType().equals(type)))
				.collect(Collectors.toList());
	}

	public Provider getProvider(String id) throws NoSuchProviderException {
		if (!_providers.containsKey(id)) {
			_log.error("no provider for " + id);

			throw new NoSuchProviderException();
		}

		Provider provider = _providers.get(id);

		// check if enabled
		if (provider.getStatus() < 0) {
			_log.error("provider for " + id + " is not available");

			throw new NoSuchProviderException();
		}

		return provider;
	}

}
