package com.bixterprise.gateway.config;

import com.bixterprise.gateway.security.*;

import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.security.*;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JHipsterProperties jHipsterProperties;

//    private final RememberMeServices rememberMeServices;

    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;

    @Value("${sp.gateway.user.username}")
	protected String GTUsername;
	
    @Value("${sp.gateway.user.password}")
    protected String GTPassword;


    public SecurityConfiguration(JHipsterProperties jHipsterProperties, 
//            RememberMeServices rememberMeServices, 
            CorsFilter corsFilter, SecurityProblemSupport problemSupport) {
        this.jHipsterProperties = jHipsterProperties;
//        this.rememberMeServices = rememberMeServices;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
    }

    @Bean
    public AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
        return new AjaxAuthenticationSuccessHandler();
    }

    @Bean
    public AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler() {
        return new AjaxAuthenticationFailureHandler();
    }

    @Bean
    public AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler() {
        return new AjaxLogoutSuccessHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    
	
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        auth.inMemoryAuthentication()
        .passwordEncoder(encoder)
        .withUser(GTUsername).password(encoder.encode(GTPassword)).authorities("GATEWAY");
        System.out.println("\nuser = "+GTUsername+" password = "+GTPassword);
    } 

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
         http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/api/commandes").authenticated()
        .antMatchers(HttpMethod.POST, "/api/commandes/").authenticated()
        .antMatchers(HttpMethod.GET,"/api/commandes/").denyAll()
        .antMatchers(HttpMethod.GET,"/api/commandes").denyAll()
        .anyRequest().permitAll()
        .and()
        .httpBasic();
    }
}
