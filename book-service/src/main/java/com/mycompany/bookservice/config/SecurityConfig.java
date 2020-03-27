package com.mycompany.bookservice.config;

import org.keycloak.adapters.springboot.KeycloakBaseSpringBootConfiguration;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    private static final String MANAGE_BOOKS = "manage_books";

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/books").hasRole(MANAGE_BOOKS)
                .antMatchers(HttpMethod.PATCH, "/api/books/*").hasRole(MANAGE_BOOKS)
                .antMatchers(HttpMethod.DELETE, "/api/books/*").hasRole(MANAGE_BOOKS)
                .anyRequest().permitAll();
    }

    @Bean
    @Override
    @ConditionalOnMissingBean(HttpSessionManager.class)
    protected HttpSessionManager httpSessionManager() {
        return new HttpSessionManager();
    }

    /**
     * In order to make {@code BookControllerTest} works, the static class {@link CustomKeycloakBaseSpringBootConfiguration}
     * and the annotation {@link EnableConfigurationProperties} were added here.
     * <p>
     * Without them, a NullPointerException will be throw in
     * {@link org.keycloak.adapters.KeycloakDeploymentBuilder#internalBuild(AdapterConfig)}, because the
     * {@code adapterConfig} parameter is null
     */
    @Configuration
    static class CustomKeycloakBaseSpringBootConfiguration extends KeycloakBaseSpringBootConfiguration {
    }

}
