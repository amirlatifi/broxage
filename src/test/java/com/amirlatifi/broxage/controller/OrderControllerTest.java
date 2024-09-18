package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Order;
import com.amirlatifi.broxage.model.OrderSide;
import com.amirlatifi.broxage.model.OrderStatus;
import com.amirlatifi.broxage.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

	private MockMvc mockMvc;

	@Mock
	private OrderService orderService;

	@InjectMocks
	private OrderController orderController;

	private Authentication customerAuth;
	private Authentication adminAuth;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

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
	void createOrder_customerSuccess() throws Exception {
		Order order = createSampleOrder(1L, 1L);
		when(orderService.createOrder(anyLong(), anyString(), any(OrderSide.class), any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(order);

		mockMvc.perform(post("/api/orders")
						.with(request -> {
							request.setUserPrincipal(customerAuth);
							return request;
						})
						.param("assetName", "BTC")
						.param("side", "BUY")
						.param("size", "1")
						.param("price", "50000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.customerId").value(1));
	}

	@Test
	void createOrder_adminSuccess() throws Exception {
		Order order = createSampleOrder(2L, 3L);
		when(orderService.createOrder(anyLong(), anyString(), any(OrderSide.class), any(BigDecimal.class), any(BigDecimal.class)))
				.thenReturn(order);

		mockMvc.perform(post("/api/orders")
						.with(request -> {
							request.setUserPrincipal(adminAuth);
							return request;
						})
						.param("customerId", "3")
						.param("assetName", "ETH")
						.param("side", "SELL")
						.param("size", "2")
						.param("price", "3000")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.customerId").value(3));
	}

	@Test
	void listOrders_customerSuccess() throws Exception {
		List<Order> orders = Arrays.asList(createSampleOrder(1L, 1L), createSampleOrder(2L, 1L));
		when(orderService.listOrders(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenReturn(orders);

		mockMvc.perform(get("/api/orders")
						.with(request -> {
							request.setUserPrincipal(customerAuth);
							return request;
						})
						.param("startDate", "2023-01-01T00:00:00")
						.param("endDate", "2023-12-31T23:59:59")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].customerId").value(1))
				.andExpect(jsonPath("$[1].id").value(2))
				.andExpect(jsonPath("$[1].customerId").value(1));
	}

	@Test
	void listOrders_adminSuccess() throws Exception {
		List<Order> orders = Arrays.asList(createSampleOrder(1L, 3L), createSampleOrder(2L, 3L));
		when(orderService.listOrders(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenReturn(orders);

		mockMvc.perform(get("/api/orders")
						.with(request -> {
							request.setUserPrincipal(adminAuth);
							return request;
						})
						.param("customerId", "3")
						.param("startDate", "2023-01-01T00:00:00")
						.param("endDate", "2023-12-31T23:59:59")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].customerId").value(3))
				.andExpect(jsonPath("$[1].id").value(2))
				.andExpect(jsonPath("$[1].customerId").value(3));
	}

	@Test
	void cancelOrder_customerSuccess() throws Exception {
		mockMvc.perform(delete("/api/orders/1")
						.with(request -> {
							request.setUserPrincipal(customerAuth);
							return request;
						})
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	void cancelOrder_adminSuccess() throws Exception {
		mockMvc.perform(delete("/api/orders/1")
						.with(request -> {
							request.setUserPrincipal(adminAuth);
							return request;
						})
						.param("customerId", "3")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	private Order createSampleOrder(Long id, Long customerId) {
		Order order = new Order();
		order.setId(id);
		order.setCustomerId(customerId);
		order.setAssetName("BTC");
		order.setOrderSide(OrderSide.BUY);
		order.setSize(BigDecimal.ONE);
		order.setPrice(BigDecimal.valueOf(50000));
		order.setStatus(OrderStatus.PENDING);
		order.setCreateDate(LocalDateTime.now());
		return order;
	}
}