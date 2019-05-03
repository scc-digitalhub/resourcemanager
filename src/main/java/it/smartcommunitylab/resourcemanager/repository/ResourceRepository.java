package it.smartcommunitylab.resourcemanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.smartcommunitylab.resourcemanager.model.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

	Long countByType(String type);

	Long countByProvider(String provider);

	Long countByScopeId(String scopeId);

	Long countByUserId(String userId);

	Long countByTypeAndScopeId(String type, String scopeId);

	Long countByProviderAndScopeId(String provider, String scopeId);

	List<Resource> findByType(String type);

	List<Resource> findByType(String type, Sort sort);

	Page<Resource> findByType(String type, Pageable pageable);

	List<Resource> findByUserId(String userId);

	List<Resource> findByUserId(String userId, Sort sort);

	Page<Resource> findByUserId(String userId, Pageable pageable);

	List<Resource> findByScopeId(String scopeId);

	List<Resource> findByScopeId(String scopeId, Sort sort);

	Page<Resource> findByScopeId(String scopeId, Pageable pageable);

	List<Resource> findByProvider(String provider);

	List<Resource> findByProvider(String provider, Sort sort);

	Page<Resource> findByProvider(String provider, Pageable pageable);

	List<Resource> findByTypeAndScopeId(String type, String scopeId);

	List<Resource> findByTypeAndScopeId(String type, String scopeId, Sort sort);

	Page<Resource> findByTypeAndScopeId(String type, String scopeId, Pageable pageable);

	List<Resource> findByProviderAndScopeId(String provider, String scopeId);

	List<Resource> findByProviderAndScopeId(String provider, String scopeId, Sort sort);

	Page<Resource> findByProviderAndScopeId(String provider, String scopeId, Pageable pageable);
}
