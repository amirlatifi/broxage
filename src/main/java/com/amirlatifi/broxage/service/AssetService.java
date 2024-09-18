package com.amirlatifi.broxage.service;

import com.amirlatifi.broxage.model.Asset;
import com.amirlatifi.broxage.repository.AssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetService {

	private final AssetRepository assetRepository;

	public AssetService(AssetRepository assetRepository) {
		this.assetRepository = assetRepository;
	}

	public List<Asset> getAssetsByCustomerId(Long customerId) {
		return assetRepository.findByCustomerId(customerId);
	}

	@Transactional
	public void updateAssetUsableSize(Long customerId, String assetName, BigDecimal amount) {
		Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
		if (asset == null) {
			asset = new Asset();
			asset.setCustomerId(customerId);
			asset.setAssetName(assetName);
			asset.setSize(amount);
			asset.setUsableSize(amount);
		} else {
			asset.setSize(asset.getSize().add(amount));
			asset.setUsableSize(asset.getUsableSize().add(amount));
		}
		assetRepository.save(asset);
	}

	public boolean hasEnoughUsableAsset(Long customerId, String assetName, BigDecimal amount) {
		Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
		return asset != null && asset.getUsableSize().compareTo(amount) >= 0;
	}
}