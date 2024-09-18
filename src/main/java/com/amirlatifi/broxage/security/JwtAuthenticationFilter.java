package com.amirlatifi.broxage.security;

import com.amirlatifi.broxage.model.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
		setFilterProcessesUrl("/api/customers/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req,
												HttpServletResponse res) throws AuthenticationException {
		try {
			Customer customer = new ObjectMapper()
					.readValue(req.getInputStream(), Customer.class);

			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							customer.getUsername(),
							customer.getPassword(),
							new ArrayList<>())
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req,
											HttpServletResponse res,
											FilterChain chain,
											Authentication auth) throws IOException {
		String token = Jwts.builder()
				.setSubject(((Customer) auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + 864_000_000))
				.signWith(SignatureAlgorithm.HS512, "SecretKeyToGenJWTs".getBytes())
				.compact();

		res.addHeader("Authorization", "Bearer " + token);
	}
}
