package it.smartcommunitylab.resourcemanager.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import it.smartcommunitylab.aac.security.permission.Space;
import it.smartcommunitylab.aac.security.permission.DelegatePermissionEvaluator;
import it.smartcommunitylab.aac.security.permission.PermitAllPermissionEvaluator;
import it.smartcommunitylab.aac.security.permission.SpacePermissionEvaluator;
import it.smartcommunitylab.aac.security.roles.RoleActionConverter;
import it.smartcommunitylab.resourcemanager.security.PermissionEvaluatorManager;
import it.smartcommunitylab.resourcemanager.security.RegistrationPermissionEvaluator;
import it.smartcommunitylab.resourcemanager.security.ResourcePermissionEvaluator;
import it.smartcommunitylab.resourcemanager.security.ResourceRoleActionConverter;
import it.smartcommunitylab.resourcemanager.service.RegistrationLocalService;
import it.smartcommunitylab.resourcemanager.service.ResourceLocalService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    @Value("${auth.component}")
    private String component;

    @Value("${spaces.enabled}")
    private boolean enabled;

    @Value("${spaces.list}")
    private List<String> spaces;

//	@Autowired
//	private Map<String, PermissionEvaluator> _evaluators;

    @Autowired
    RegistrationLocalService registrationService;

    @Autowired
    ResourceLocalService resourceService;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
        methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator());

        return methodSecurityExpressionHandler;
    }

//	@Bean
//	public PermissionEvaluator permissionEvaluator() {
//		// map as [ENTITY]-[EVALUATOR]
//		Map<String, PermissionEvaluator> map = new HashMap<>();
//		for (String className : _evaluators.keySet()) {
//			String key = className.replace("PermissionEvaluator", "").toUpperCase();
//			map.put(key, _evaluators.get(className));
//		}
//		return new PermissionEvaluatorManager(map);
//	}

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        if (enabled) {
            RoleActionConverter<Space> converter = new ResourceRoleActionConverter<>(component);
            SpacePermissionEvaluator spaceEvaluator = new SpacePermissionEvaluator(spaces);
            spaceEvaluator.setRoleActionConverter(converter);

            ResourcePermissionEvaluator resourceEvaluator = new ResourcePermissionEvaluator(resourceService,
                    spaceEvaluator);
            RegistrationPermissionEvaluator registrationEvaluator = new RegistrationPermissionEvaluator(
                    registrationService, spaceEvaluator);

            // return composite manager
            return new DelegatePermissionEvaluator(spaceEvaluator, resourceEvaluator, registrationEvaluator);
        } else {
            return new PermitAllPermissionEvaluator();
        }
    }
}
