package it.smartcommunitylab.resourcemanager.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import it.smartcommunitylab.resourcemanager.security.PermissionEvaluatorManager;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

	@Autowired
	private Map<String, PermissionEvaluator> _evaluators;

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
		methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator());

		return methodSecurityExpressionHandler;
	}

	@Bean
	public PermissionEvaluator permissionEvaluator() {
		// map as [ENTITY]-[EVALUATOR]
		Map<String, PermissionEvaluator> map = new HashMap<>();
		for (String className : _evaluators.keySet()) {
			String key = className.replace("PermissionEvaluator", "").toUpperCase();
			map.put(key, _evaluators.get(className));
		}
		return new PermissionEvaluatorManager(map);
	}
}
