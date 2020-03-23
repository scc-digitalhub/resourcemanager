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

	Long countBySpaceId(String spaceId);

	Long countByUserIdAndSpaceId(String userId, String spaceId);

	Long countByTypeAndSpaceId(String type, String spaceId);

	Long countByProviderAndSpaceId(String provider, String spaceId);

	List<Resource> findByType(String type);

	List<Resource> findByType(String type, Sort sort);

	Page<Resource> findByType(String type, Pageable pageable);

	List<Resource> findByUserId(String userId);

	List<Resource> findByUserId(String userId, Sort sort);

	Page<Resource> findByUserId(String userId, Pageable pageable);

	List<Resource> findBySpaceId(String spaceId);

	List<Resource> findBySpaceId(String spaceId, Sort sort);

	Page<Resource> findBySpaceId(String spaceId, Pageable pageable);

	List<Resource> findByProvider(String provider);

	List<Resource> findByProvider(String provider, Sort sort);

	Page<Resource> findByProvider(String provider, Pageable pageable);

	List<Resource> findByTypeAndSpaceId(String type, String spaceId);

	List<Resource> findByTypeAndSpaceId(String type, String spaceId, Sort sort);

	Page<Resource> findByTypeAndSpaceId(String type, String spaceId, Pageable pageable);

	List<Resource> findByProviderAndSpaceId(String provider, String spaceId);

	List<Resource> findByProviderAndSpaceId(String provider, String spaceId, Sort sort);

	Page<Resource> findByProviderAndSpaceId(String provider, String spaceId, Pageable pageable);

	List<Resource> findByUserIdAndSpaceId(String userId, String spaceId);

}
