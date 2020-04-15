package com.studyolle.config;

import com.studyolle.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	private final AccountService accountService;

	private final DataSource dataSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
						"/email-login", "/login-by-email", "/search/study").permitAll()
				.mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
				.anyRequest().authenticated();

		http.formLogin()
				.loginPage("/login").permitAll();
		http.logout()
				.logoutSuccessUrl("/");
		http.rememberMe()
				.userDetailsService(accountService)
				.tokenRepository(tokenRepository());
	}

	/**
	쿠키 안전하게 설정
	 토큰, 키값
	 테이블에 저장해서 쓰기때문에 이에 맞는 디비 테이블과 필드를 만들어줘야 한다. (PersistentLogins.java)
	 JdbcTokenRepositoryImpl 안에 "create~~ " 쿼리가 있는 것을 확인 가능
	 */
	@Bean
	public PersistentTokenRepository tokenRepository(){
		JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
		jdbcTokenRepository.setDataSource(dataSource);
		return jdbcTokenRepository;
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
				.mvcMatchers("/node_modules/**")
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}
}
