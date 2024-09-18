package com.amirlatifi.broxage.service;

import com.amirlatifi.broxage.model.Order;
import com.amirlatifi.broxage.model.OrderSide;
import com.amirlatifi.broxage.model.OrderStatus;
import com.amirlatifi.broxage.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private AssetService assetService;

	@InjectMocks
	private OrderService orderService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createOrder_buyOrder_success() {
		when(assetService.hasEnoughUsableAsset(eq(1L), eq("TRY"), any(BigDecimal.class))).thenReturn(true);
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Order order = orderService.createOrder(1L, "BTC", OrderSide.BUY, BigDecimal.ONE, BigDecimal.valueOf(50000));

		assertNotNull(order);
		assertEquals(1L, order.getCustomerId());
		assertEquals("BTC", order.getAssetName());
		assertEquals(OrderSide.BUY, order.getOrderSide());
		assertEquals(BigDecimal.ONE, order.getSize());
		assertEquals(BigDecimal.valueOf(50000), order.getPrice());
		assertEquals(OrderStatus.PENDING, order.getStatus());

		verify(assetService).updateAssetUsableSize(eq(1L), eq("TRY"), any(BigDecimal.class));
	}

	@Test
	void createOrder_sellOrder_success() {
		when(assetService.hasEnoughUsableAsset(eq(1L), eq("BTC"), any(BigDecimal.class))).thenReturn(true);
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Order order = orderService.createOrder(1L, "BTC", OrderSide.SELL, BigDecimal.ONE, BigDecimal.valueOf(50000));

		assertNotNull(order);
		assertEquals(1L, order.getCustomerId());
		assertEquals("BTC", order.getAssetName());
		assertEquals(OrderSide.SELL, order.getOrderSide());
		assertEquals(BigDecimal.ONE, order.getSize());
		assertEquals(BigDecimal.valueOf(50000), order.getPrice());
		assertEquals(OrderStatus.PENDING, order.getStatus());

		verify(assetService).updateAssetUsableSize(eq(1L), eq("BTC"), any(BigDecimal.class));
	}

	@Test
	void createOrder_insufficientFunds_throwsException() {
		when(assetService.hasEnoughUsableAsset(eq(1L), eq("TRY"), any(BigDecimal.class))).thenReturn(false);

		assertThrows(IllegalStateException.class, () ->
				orderService.createOrder(1L, "BTC", OrderSide.BUY, BigDecimal.ONE, BigDecimal.valueOf(50000))
		);

		verify(assetService, never()).updateAssetUsableSize(any(), any(), any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void listOrders_success() {
		LocalDateTime startDate = LocalDateTime.now().minusDays(1);
		LocalDateTime endDate = LocalDateTime.now();
		List<Order> expectedOrders = Arrays.asList(new Order(), new Order());

		when(orderRepository.findByCustomerIdAndCreateDateBetween(1L, startDate, endDate)).thenReturn(expectedOrders);

		List<Order> actualOrders = orderService.listOrders(1L, startDate, endDate);

		assertEquals(expectedOrders, actualOrders);
	}

	@Test
	void cancelOrder_success() {
		Order order = new Order();
		order.setId(1L);
		order.setCustomerId(1L);
		order.setStatus(OrderStatus.PENDING);
		order.setOrderSide(OrderSide.BUY);
		order.setSize(BigDecimal.ONE);
		order.setPrice(BigDecimal.valueOf(50000));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		orderService.cancelOrder(1L, 1L);

		assertEquals(OrderStatus.CANCELED, order.getStatus());
		verify(orderRepository).save(order);
		verify(assetService).updateAssetUsableSize(eq(1L), eq("TRY"), any(BigDecimal.class));
	}

	@Test
	void cancelOrder_orderNotFound_throwsException() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(1L, 1L));
	}

	@Test
	void cancelOrder_notOwnOrder_throwsException() {
		Order order = new Order();
		order.setId(1L);
		order.setCustomerId(2L);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder(1L, 1L));
	}

	@Test
	void cancelOrder_notPendingOrder_throwsException() {
		Order order = new Order();
		order.setId(1L);
		order.setCustomerId(1L);
		order.setStatus(OrderStatus.MATCHED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L, 1L));
	}

	@Test
	void getOrderById_success() {
		Order order = new Order();
		order.setId(1L);
		order.setCustomerId(1L);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		Order result = orderService.getOrderById(1L, 1L);

		assertEquals(order, result);
	}

	@Test
	void getOrderById_orderNotFound_throwsException() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(1L, 1L));
	}

	@Test
	void getOrderById_notOwnOrder_throwsException() {
		Order order = new Order();
		order.setId(1L);
		order.setCustomerId(2L);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(AccessDeniedException.class, () -> orderService.getOrderById(1L, 1L));
	}
}