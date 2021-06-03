package it.smartcommunitylab.resourcemanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import it.smartcommunitylab.aac.security.jwt.JwtAudienceValidator;
import it.smartcommunitylab.aac.security.jwt.JwtAuthenticationConverter;
import it.smartcommunitylab.aac.security.jwt.authority.JwtComponentAwareAuthoritiesRoleConverter;
import it.smartcommunitylab.aac.security.jwt.authority.JwtScopeAuthoritiesConverter;

@Configuration
@EnableWebSecurity
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${auth.enabled}")
    private boolean authenticate;

    @Value("${auth.component}")
    private String component;

    @Value("${auth.rolesclaim}")
    private String rolesClaimName;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.client-secret}")
    private String clientSecret;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        if (authenticate) {
            http
                    .authorizeRequests()
                    .antMatchers("/api/auth/**").permitAll()
                    .antMatchers("/api/**").authenticated()
                    .antMatchers("/h2-console/**").permitAll()
                    .and()
                    .oauth2ResourceServer()
                    .jwt()
                    .jwtAuthenticationConverter(jwtTokenConverter());

        } else {
            http.authorizeRequests().anyRequest().permitAll();
        }

        // enable X-Frame options for console
        http.headers().frameOptions().sameOrigin();

    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new JwtAudienceValidator(clientId);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    Converter<Jwt, AbstractAuthenticationToken> jwtTokenConverter() {
        JwtComponentAwareAuthoritiesRoleConverter authoritiesConverter = new JwtComponentAwareAuthoritiesRoleConverter(
                component);
        authoritiesConverter.setAuthoritiesClaimName(rolesClaimName);

        return new JwtAuthenticationConverter(
                authoritiesConverter,
                new JwtScopeAuthoritiesConverter());
        // example: assign any user a default role
//        return new JwtAuthenticationConverter(
//                new ComponentAwareAuthoritiesRoleConverter(component),
//                new ScopeAuthoritiesConverter(),
//                new SimpleUserAuthoritiesConverter());
    }

}
