package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Asset;
import com.amirlatifi.broxage.model.Customer;
import com.amirlatifi.broxage.service.AssetService;
import com.amirlatifi.broxage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	@Autowired
	private AssetService assetService;

	@Autowired
	private CustomerService customerService;

	@GetMapping
	public ResponseEntity<List<Asset>> listAssets(Authentication authentication,
												  @RequestParam(required = false) Long customerId) {
		Long effectiveCustomerId = getEffectiveCustomerId(authentication, customerId);
		List<Asset> assets = assetService.getAssetsByCustomerId(effectiveCustomerId);
		return ResponseEntity.ok(assets);
	}

	@PostMapping("/deposit")
	public ResponseEntity<Void> depositMoney(Authentication authentication,
											 @RequestParam(required = false) Long customerId,
											 @RequestParam BigDecimal amount) {
		Long effectiveCustomerId = getEffectiveCustomerId(authentication, customerId);
		assetService.updateAssetUsableSize(effectiveCustomerId, "TRY", amount);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/withdraw")
	public ResponseEntity<Void> withdrawMoney(Authentication authentication,
											  @RequestParam(required = false) Long customerId,
											  @RequestParam BigDecimal amount,
											  @RequestParam String iban) {
		Long effectiveCustomerId = getEffectiveCustomerId(authentication, customerId);
		assetService.updateAssetUsableSize(effectiveCustomerId, "TRY", amount.negate());
		return ResponseEntity.ok().build();
	}

	private Long getEffectiveCustomerId(Authentication authentication, Long providedCustomerId) {
		boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

		if (isAdmin) {
			if (providedCustomerId == null) {
				throw new IllegalArgumentException("Admin must provide a customer ID");
			}
			return providedCustomerId;
		} else {
			String username = authentication.getName();
			Customer customer = customerService.findByUsername(username);
			if (providedCustomerId != null && !providedCustomerId.equals(customer.getId())) {
				throw new IllegalArgumentException("Customers can only access their own assets");
			}
			return customer.getId();
		}
	}
}