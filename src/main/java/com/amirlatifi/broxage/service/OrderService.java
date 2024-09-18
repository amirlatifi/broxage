package com.amirlatifi.broxage.service;

import com.amirlatifi.broxage.model.Order;
import com.amirlatifi.broxage.model.OrderSide;
import com.amirlatifi.broxage.model.OrderStatus;
import com.amirlatifi.broxage.repository.OrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	private final AssetService assetService;

	public OrderService(OrderRepository orderRepository, AssetService assetService) {
		this.orderRepository = orderRepository;
		this.assetService = assetService;
	}

	@Transactional
	public Order createOrder(Long customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
		if (side == OrderSide.BUY) {
			BigDecimal totalCost = size.multiply(price);
			if (!assetService.hasEnoughUsableAsset(customerId, "TRY", totalCost)) {
				throw new IllegalStateException("Insufficient TRY balance");
			}
			assetService.updateAssetUsableSize(customerId, "TRY", totalCost.negate());
		} else {
			if (!assetService.hasEnoughUsableAsset(customerId, assetName, size)) {
				throw new IllegalStateException("Insufficient " + assetName + " balance");
			}
			assetService.updateAssetUsableSize(customerId, assetName, size.negate());
		}

		Order order = new Order();
		order.setCustomerId(customerId);
		order.setAssetName(assetName);
		order.setOrderSide(side);
		order.setSize(size);
		order.setPrice(price);
		order.setStatus(OrderStatus.PENDING);
		order.setCreateDate(LocalDateTime.now());

		return orderRepository.save(order);
	}

	public List<Order> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
		return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
	}

	@Transactional
	public void cancelOrder(Long customerId, Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found"));

		if (!order.getCustomerId().equals(customerId)) {
			throw new AccessDeniedException("You don't have permission to cancel this order");
		}

		if (order.getStatus() != OrderStatus.PENDING) {
			throw new IllegalStateException("Only pending orders can be canceled");
		}

		order.setStatus(OrderStatus.CANCELED);
		orderRepository.save(order);

		if (order.getOrderSide() == OrderSide.BUY) {
			BigDecimal totalCost = order.getSize().multiply(order.getPrice());
			assetService.updateAssetUsableSize(order.getCustomerId(), "TRY", totalCost);
		} else {
			assetService.updateAssetUsableSize(order.getCustomerId(), order.getAssetName(), order.getSize());
		}
	}

	public Order getOrderById(Long customerId, Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found"));

		if (!order.getCustomerId().equals(customerId)) {
			throw new AccessDeniedException("You don't have permission to view this order");
		}

		return order;
	}
}