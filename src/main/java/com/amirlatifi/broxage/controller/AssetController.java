package com.amirlatifi.broxage.controller;

import com.amirlatifi.broxage.model.Asset;
import com.amirlatifi.broxage.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;

	public AssetController(AssetService assetService) {
		this.assetService = assetService;
	}

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