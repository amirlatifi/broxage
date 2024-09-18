package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Order;
import com.amirlatifi.broxage.model.OrderSide;
import com.amirlatifi.broxage.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<Order> createOrder(Authentication authentication,
											 @RequestParam(required = false) Long customerId,
											 @RequestParam String assetName,
											 @RequestParam OrderSide side,
											 @RequestParam BigDecimal size,
											 @RequestParam BigDecimal price) {
		Long effectiveCustomerId = getEffectiveCustomerId(authentication, customerId);
		Order order = orderService.createOrder(effectiveCustomerId, assetName, side, size, price);
		return ResponseEntity.ok(order);
	}

	@GetMapping
	public ResponseEntity<List<Order>> listOrders(Authentication authentication,
												  @RequestParam(required = false) Long customerId,
												  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
												  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
		Long effectiveCustomerId = getEffectiveCustomerId(authentication, customerId);
		List<Order> orders = orderService.listOrders(effectiveCustomerId, startDate, endDate);
		return ResponseEntity.ok(orders);
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<Void> cancelOrder(Authentication authentication,
											@PathVariable Long orderId,
											@RequestParam(required = false) Long customerId) {
		Long effectiveCustomerId = getEffectiveCustomerId(authentication, customerId);
		orderService.cancelOrder(effectiveCustomerId, orderId);
		return ResponseEntity.ok().build();
	}

	private Long getEffectiveCustomerId(Authentication authentication, Long requestedCustomerId) {
		boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
		Long authenticatedCustomerId = Long.parseLong(authentication.getName());

		if (isAdmin && requestedCustomerId != null) {
			return requestedCustomerId;
		} else if (isAdmin) {
			throw new IllegalArgumentException("Admin must specify a customer ID");
		} else if (requestedCustomerId != null && !requestedCustomerId.equals(authenticatedCustomerId)) {
			throw new IllegalArgumentException("Customers can only access their own data");
		} else {
			return authenticatedCustomerId;
		}
	}
}