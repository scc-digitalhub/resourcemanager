package it.smartcommunitylab.resourcemanager.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.resourcemanager.SystemKeys;
import it.smartcommunitylab.resourcemanager.common.DuplicateNameException;
import it.smartcommunitylab.resourcemanager.common.InvalidNameException;
import it.smartcommunitylab.resourcemanager.common.NoSuchProviderException;
import it.smartcommunitylab.resourcemanager.common.NoSuchResourceException;
import it.smartcommunitylab.resourcemanager.common.ResourceProviderException;
import it.smartcommunitylab.resourcemanager.crypt.CryptoService;
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
    public Resource create(String spaceId, String userId,
            String type, String providerId, String name,
            Map<String, Serializable> properties, List<String> tags)
            throws NoSuchProviderException, ResourceProviderException, InvalidNameException, DuplicateNameException {
        _log.info("create " + type + " resource with " + String.valueOf(providerId) + " by user " + userId);

        // call provider to require creation
        ResourceProvider provider = providerLocalService.getProvider(providerId);
        // check type match
        if (!provider.getType().equals(type)) {
            throw new NoSuchProviderException();
        }
        // sync call - should validate properties
        Resource res = provider.createResource(spaceId, userId, name, properties);

        // update fields
        res.setSpaceId(spaceId);
        res.setUserId(userId);

        // persist tags
        res.setTags(tags);

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
    public Resource add(String spaceId, String userId, String type, String providerId,
            String name, String uri,
            Map<String, Serializable> properties, List<String> tags)
            throws NoSuchProviderException, ResourceProviderException {
        _log.info("add " + type + " resource with " + String.valueOf(providerId) + " by user " + userId);

        // call provider to check existence
        // does NOT need to be active
        ResourceProvider provider = providerLocalService.fetchProvider(providerId);
        // check type match
        if (!provider.getType().equals(type)) {
            throw new NoSuchProviderException();
        }

        // nothing asked to provider, resource should already exists
        Resource res = new Resource();
        res.setType(type);
        res.setProvider(provider.getId());
        res.setName(name);
        
        res.setPropertiesMap(properties);
        // update fields
        res.setSpaceId(spaceId);
        res.setUserId(userId);

        // persist tags
        res.setTags(tags);

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
    public Resource update(long id, Map<String, Serializable> properties, List<String> tags)
            throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {
        _log.info("update resource " + String.valueOf(id));

        Resource res = get(id);
        // update fields
        res.setPropertiesMap(properties);
        res.setTags(tags);

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
                res.getSpaceId(), res.getUserId(), res.getType(),
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
            // clone entity to detach from JPA
            // workaround for setting field *WITHOUT* persisting
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

    public boolean exists(long id) {
        return resourceRepository.existsById(id);
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

    public long countBySpaceId(String spaceId) {
        return resourceRepository.countBySpaceId(spaceId);
    }

    public long countByUserIdAndSpaceId(String userId, String spaceId) {
        return resourceRepository.countByUserIdAndSpaceId(userId, spaceId);
    }

    public long countByTypeAndSpaceId(String type, String spaceId) {
        return resourceRepository.countByTypeAndSpaceId(type, spaceId);
    }

    public long countByProviderAndSpaceId(String provider, String spaceId) {
        return resourceRepository.countByProviderAndSpaceId(provider, spaceId);
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

    public List<Resource> listByUserIdAndSpaceId(String userId, String spaceId) {
        return resourceRepository.findByUserIdAndSpaceId(userId, spaceId);
    }

    public List<Resource> listBySpaceId(String spaceId) {
        return resourceRepository.findBySpaceId(spaceId);
    }

    public List<Resource> listBySpaceId(String spaceId, Pageable pageable) {
        Page<Resource> result = resourceRepository.findBySpaceId(spaceId, pageable);
        return result.getContent();
    }

    public List<Resource> listByTypeAndSpaceId(String type, String spaceId) {
        return resourceRepository.findByTypeAndSpaceId(type, spaceId);
    }

    public List<Resource> listByTypeAndSpaceId(String type, String spaceId, Pageable pageable) {
        Page<Resource> result = resourceRepository.findByTypeAndSpaceId(type, spaceId, pageable);
        return result.getContent();
    }

    public List<Resource> listByProviderAndSpaceId(String provider, String spaceId) {
        return resourceRepository.findByProviderAndSpaceId(provider, spaceId);
    }

    /*
     * Check
     */
//    @Autowired
//    private ApplicationEventPublisher applicationEventPublisher;

    public void check(long id) throws NoSuchResourceException, NoSuchProviderException, ResourceProviderException {

        Resource res = get(id);

        // call provider to require check
        ResourceProvider provider = providerLocalService.getProvider(res.getProvider());
        // sync call
        provider.checkResource(res);

        // notify all consumers via events
        eventHandler.notifyAction(res.getSpaceId(), res.getUserId(), res.getType(), id,
                SystemKeys.ACTION_CHECK);
    }

}
