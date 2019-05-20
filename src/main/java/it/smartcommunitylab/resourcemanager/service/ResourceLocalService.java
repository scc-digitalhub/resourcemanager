package it.smartcommunitylab.resourcemanager.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.crypt.CryptoService;
import it.smartcommunitylab.resourcemanager.dto.ResourceDTO;
import it.smartcommunitylab.resourcemanager.event.ResourceEventHandler;
import it.smartcommunitylab.resourcemanager.model.ResourceProvider;
import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.model.ResourceEvent;
import it.smartcommunitylab.resourcemanager.repository.ResourceRepository;

@Component
public class ResourceLocalService {
    private final static Logger _log = LoggerFactory.getLogger(ResourceLocalService.class);

    @Value("${encrypt.enabled}")
    private boolean toEncrypt;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProviderLocalService providerLocalService;

    @Autowired
    CryptoService crypto;

    @Autowired
    private ResourceEventHandler eventHandler;

    @Autowired
    private ConsumerLocalService consumerLocalService;

    /*
     * Data
     */
    public Resource create(String scopeId, String userId, String type, String providerId,
            Map<String, Serializable> properties)
            throws NoSuchProviderException, ResourceProviderException {
        _log.info("create " + type + " resource with " + String.valueOf(providerId) + " by user " + userId);

        // call provider to require creation
        ResourceProvider provider = providerLocalService.getProvider(providerId);
        // check type match
        if (!provider.getType().equals(type)) {
            throw new NoSuchProviderException();
        }
        // sync call - should validate properties
        Resource res = provider.createResource(scopeId, userId, properties);

        // update fields
        res.setScopeId(scopeId);
        res.setUserId(userId);

        // encrypt URI
        if (toEncrypt) {
            try {
                String encrypted = crypto.encrypt(res.getUri());
                res.setUri(encrypted);
            } catch (Exception ex) {
                // wipe private field
                res.setUri("");
                _log.debug("crypto error " + ex.getMessage());
            }
        }

        // persist resource
        return resourceRepository.saveAndFlush(res);

    }

    @Transactional
    public Resource add(String scopeId, String userId, String type, String providerId,
            String uri,
            Map<String, Serializable> properties)
            throws NoSuchProviderException, ResourceProviderException {
        _log.info("add " + type + " resource with " + String.valueOf(providerId) + " by user " + userId);

        // call provider to check existence
        ResourceProvider provider = providerLocalService.getProvider(providerId);
        // check type match
        if (!provider.getType().equals(type)) {
            throw new NoSuchProviderException();
        }

        // nothing asked to provider, resource should already exists
        Resource res = new Resource();
        res.setType(type);
        res.setProvider(provider.getId());
        res.setPropertiesMap(properties);
        // update fields
        res.setScopeId(scopeId);
        res.setUserId(userId);

        // set uri as provided
        res.setUri(uri);

        // disabled managed for externally created resources
        res.setManaged(false);

        // encrypt URI
        if (toEncrypt) {
            try {
                String encrypted = crypto.encrypt(res.getUri());
                res.setUri(encrypted);
            } catch (Exception ex) {
                // wipe private field
                res.setUri("");
                _log.debug("crypto error " + ex.getMessage());
            }
        }

        // persist resource
        return resourceRepository.saveAndFlush(res);

    }
    
    @Transactional
    public Resource update(long id, Map<String, Serializable> properties)
            throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        _log.info("update resource " + String.valueOf(id));

        Resource res = get(id);
        // update fields
        res.setPropertiesMap(properties);

        if (res.isManaged()) {
            // call provider to require update
            ResourceProvider provider = providerLocalService.getProvider(res.getProvider());
            // sync call - should validate properties
            provider.updateResource(res);
        }

        // encrypt URI
        if (toEncrypt) {
            try {
                String encrypted = crypto.encrypt(res.getUri());
                res.setUri(encrypted);
            } catch (Exception ex) {
                // wipe private field
                res.setUri("");
                _log.debug("crypto error " + ex.getMessage());
            }
        }

        return resourceRepository.save(res);

    }

    @Transactional
    public void delete(long id) throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        _log.info("delete resource " + String.valueOf(id));

        Resource res = get(id);

        if (res.isManaged()) {
            // call provider to require removal
            ResourceProvider provider = providerLocalService.getProvider(res.getProvider());
            // sync call
            provider.deleteResource(res);
        }

        // notify consumers *before* removal from db
        // since bus is async directly dispatch event to consumer
        ResourceEvent event = new ResourceEvent(res,
                res.getScopeId(), res.getUserId(), res.getType(),
                res.getId(), SystemKeys.ACTION_DELETE);
        consumerLocalService.receiveResourceEvent(event);

        // remove from DB
        resourceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Resource get(long id) throws NoSuchResourceException {
        _log.debug("get resource " + String.valueOf(id));

        Optional<Resource> p = resourceRepository.findById(id);

        if (!p.isPresent()) {
            throw new NoSuchResourceException();
        }

        Resource res = p.get();
        
        // decrypt URI
        if (toEncrypt) {
            //clone entity to detach from JPA
            //workaround for setting field *WITHOUT* persisting
            res = Resource.clone(p.get());
            
            try {
                _log.trace("get resource uri " + String.valueOf(res.getUri()));

                String decrypted = crypto.decrypt(res.getUri());
                res.setUri(decrypted);
            } catch (Exception ex) {
                // wipe private field
                res.setUri("");
                _log.debug("crypto error " + ex.getMessage());
            }
        }

        return res;
    }

    @Transactional(readOnly = true)
    public Resource fetch(long id) {
        Optional<Resource> p = resourceRepository.findById(id);

        if (!p.isPresent()) {
            return null;
        }

        return p.get();
    }

    /*
     * Count
     */
    public long count() {
        return resourceRepository.count();
    }

    public long countByType(String type) {
        return resourceRepository.countByType(type);
    }

    public long countByProvider(String provider) {
        return resourceRepository.countByProvider(provider);
    }

    public long countByScopeId(String scopeId) {
        return resourceRepository.countByScopeId(scopeId);
    }

    public long countByUserIdAndScopeId(String userId, String scopeId) {
        return resourceRepository.countByUserIdAndScopeId(userId, scopeId);
    }

    public long countByTypeAndScopeId(String type, String scopeId) {
        return resourceRepository.countByTypeAndScopeId(type, scopeId);
    }

    public long countByProviderAndScopeId(String provider, String scopeId) {
        return resourceRepository.countByProviderAndScopeId(provider, scopeId);
    }
    /*
     * List
     */
    
    public List<Resource> list() {
        return resourceRepository.findAll();
    }

    public List<Resource> list(Pageable pageable) {
        Page<Resource> result = resourceRepository.findAll(pageable);
        return result.getContent();
    }

    public List<Resource> listByType(String type) {
        return resourceRepository.findByType(type);
    }

    public List<Resource> listByProvider(String provider) {
        return resourceRepository.findByProvider(provider);
    }

    public List<Resource> listByUserIdAndScopeId(String userId, String scopeId) {
        return resourceRepository.findByUserIdAndScopeId(userId, scopeId);
    }

    public List<Resource> listByScopeId(String scopeId) {
        return resourceRepository.findByScopeId(scopeId);
    }

    public List<Resource> listByScopeId(String scopeId, Pageable pageable) {
        Page<Resource> result = resourceRepository.findByScopeId(scopeId, pageable);
        return result.getContent();
    }

    public List<Resource> listByTypeAndScopeId(String type, String scopeId) {
        return resourceRepository.findByTypeAndScopeId(type, scopeId);
    }

    public List<Resource> listByTypeAndScopeId(String type, String scopeId, Pageable pageable) {
        Page<Resource> result = resourceRepository.findByTypeAndScopeId(type, scopeId, pageable);
        return result.getContent();
    }

    public List<Resource> listByProviderAndScopeId(String provider, String scopeId) {
        return resourceRepository.findByProviderAndScopeId(provider, scopeId);
    }

    /*
     * Check
     */
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void check(long id) throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        Optional<Resource> p = resourceRepository.findById(id);

        if (!p.isPresent()) {
            throw new NoSuchResourceException();
        }

        Resource res = p.get();

        // call provider to require check
        ResourceProvider provider = providerLocalService.getProvider(res.getProvider());
        // sync call
        provider.checkResource(res);

        // notify all consumers via events
        ResourceEvent event = new ResourceEvent(this, res.getScopeId(), res.getUserId(), res.getType(), id,
                SystemKeys.ACTION_CHECK);
        applicationEventPublisher.publishEvent(event);
    }

}
