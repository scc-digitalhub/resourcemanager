package it.smartcommunitylab.resourcemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import it.smartcommunitylab.resourcemanager.audit.AuditorAwareImpl;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
	@Bean
	public AuditorAware<String> auditorAware() {
		return new AuditorAwareImpl();
	}
}
