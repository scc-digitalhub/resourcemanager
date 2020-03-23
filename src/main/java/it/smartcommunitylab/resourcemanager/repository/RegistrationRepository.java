package it.smartcommunitylab.resourcemanager.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import it.smartcommunitylab.resourcemanager.model.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

	Long countByType(String type);

	Long countByConsumer(String consumer);

	Long countByUserIdAndSpaceId(String userId, String spaceId);

	Long countBySpaceId(String spaceId);

	Long countByTypeAndSpaceId(String type, String spaceId);

	Long countByConsumerAndSpaceId(String consumer, String spaceId);

	List<Registration> findByType(String type);

	List<Registration> findByType(String type, Sort sort);

	Page<Registration> findByType(String type, Pageable pageable);

	List<Registration> findByUserIdAndSpaceId(String userId, String spaceId);

	List<Registration> findBySpaceId(String spaceId);

	List<Registration> findBySpaceId(String spaceId, Sort sort);

	Page<Registration> findBySpaceId(String spaceId, Pageable pageable);

	List<Registration> findByConsumer(String consumer);

	List<Registration> findByConsumer(String consumer, Sort sort);

	Page<Registration> findByConsumer(String consumer, Pageable pageable);

	List<Registration> findByTypeAndSpaceId(String type, String spaceId);

	List<Registration> findByTypeAndSpaceId(String type, String spaceId, Sort sort);

	Page<Registration> findByTypeAndSpaceId(String type, String spaceId, Pageable pageable);

	List<Registration> findByConsumerAndSpaceId(String consumer, String spaceId);

	List<Registration> findByConsumerAndSpaceId(String consumer, String spaceId, Sort sort);

	Page<Registration> findByConsumerAndSpaceId(String consumer, String spaceId, Pageable pageable);
}
