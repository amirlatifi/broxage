package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Asset;
import com.amirlatifi.broxage.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssetControllerTest {

	private MockMvc mockMvc;

	@Mock
	private AssetService assetService;

	@InjectMocks
	private AssetController assetController;

	private Authentication customerAuth;
	private Authentication adminAuth;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(assetController).build();

		customerAuth = createAuthentication("1", Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
		adminAuth = createAuthentication("2", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}

	private Authentication createAuthentication(String name, Collection<SimpleGrantedAuthority> authorities) {
		return new Authentication() {
			@Override
			public Collection<SimpleGrantedAuthority> getAuthorities() {
				return authorities;
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return null;
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			}

			@Override
			public String getName() {
				return name;
			}
		};
	}

	@Test
	void listAssets_customerSuccess() throws Exception {
		List<Asset> assets = Arrays.asList(
				createSampleAsset(1L, "BTC", BigDecimal.ONE),
				createSampleAsset(1L, "ETH", BigDecimal.TEN)
		);
		when(assetService.getAssetsByCustomerId(1L)).thenReturn(assets);

		mockMvc.perform(get("/api/assets")
						.with(request -> {
							request.setUserPrincipal(customerAuth);
							return request;
						})
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerId").value(1))
				.andExpect(jsonPath("$[0].assetName").value("BTC"))
				.andExpect(jsonPath("$[1].customerId").value(1))
				.andExpect(jsonPath("$[1].assetName").value("ETH"));
	}

	@Test
	void listAssets_adminSuccess() throws Exception {
		List<Asset> assets = Arrays.asList(
				createSampleAsset(3L, "BTC", BigDecimal.ONE),
				createSampleAsset(3L, "ETH", BigDecimal.TEN)
		);
		when(assetService.getAssetsByCustomerId(3L)).thenReturn(assets);

		mockMvc.perform(get("/api/assets")
						.with(request -> {
							request.setUserPrincipal(adminAuth);
							return request;
						})
						.param("customerId", "3")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerId").value(3))
				.andExpect(jsonPath("$[0].assetName").value("BTC"))
				.andExpect(jsonPath("$[1].customerId").value(3))
				.andExpect(jsonPath("$[1].assetName").value("ETH"));
	}

	@Test
	void depositMoney_customerSuccess() throws Exception {
		mockMvc.perform(post("/api/assets/deposit")
						.with(request -> {
							request.setUserPrincipal(customerAuth);
							return request;
						})
						.param("amount", "1000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	void depositMoney_adminSuccess() throws Exception {
		mockMvc.perform(post("/api/assets/deposit")
						.with(request -> {
							request.setUserPrincipal(adminAuth);
							return request;
						})
						.param("customerId", "3")
						.param("amount", "1000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	void withdrawMoney_customerSuccess() throws Exception {
		mockMvc.perform(post("/api/assets/withdraw")
						.with(request -> {
							request.setUserPrincipal(customerAuth);
							return request;
						})
						.param("amount", "500")
						.param("iban", "DE89370400440532013000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	void withdrawMoney_adminSuccess() throws Exception {
		mockMvc.perform(post("/api/assets/withdraw")
						.with(request -> {
							request.setUserPrincipal(adminAuth);
							return request;
						})
						.param("customerId", "3")
						.param("amount", "500")
						.param("iban", "DE89370400440532013000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	private Asset createSampleAsset(Long customerId, String assetName, BigDecimal amount) {
		Asset asset = new Asset();
		asset.setCustomerId(customerId);
		asset.setAssetName(assetName);
		asset.setSize(amount);
		asset.setUsableSize(amount);
		return asset;
	}
}