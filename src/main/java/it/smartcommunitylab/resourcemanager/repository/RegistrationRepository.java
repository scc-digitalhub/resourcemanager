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

	Long countByUserIdAndScopeId(String userId, String scopeId);

	Long countByScopeId(String scopeId);

	Long countByTypeAndScopeId(String type, String scopeId);

	Long countByConsumerAndScopeId(String consumer, String scopeId);

	List<Registration> findByType(String type);

	List<Registration> findByType(String type, Sort sort);

	Page<Registration> findByType(String type, Pageable pageable);

	List<Registration> findByUserIdAndScopeId(String userId, String scopeId);

	List<Registration> findByScopeId(String scopeId);

	List<Registration> findByScopeId(String scopeId, Sort sort);

	Page<Registration> findByScopeId(String scopeId, Pageable pageable);

	List<Registration> findByConsumer(String consumer);

	List<Registration> findByConsumer(String consumer, Sort sort);

	Page<Registration> findByConsumer(String consumer, Pageable pageable);

	List<Registration> findByTypeAndScopeId(String type, String scopeId);

	List<Registration> findByTypeAndScopeId(String type, String scopeId, Sort sort);

	Page<Registration> findByTypeAndScopeId(String type, String scopeId, Pageable pageable);

	List<Registration> findByConsumerAndScopeId(String consumer, String scopeId);

	List<Registration> findByConsumerAndScopeId(String consumer, String scopeId, Sort sort);

	Page<Registration> findByConsumerAndScopeId(String consumer, String scopeId, Pageable pageable);
}
