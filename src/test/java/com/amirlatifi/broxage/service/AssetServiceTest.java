package com.amirlatifi.broxage.service;

import com.amirlatifi.broxage.model.Asset;
import com.amirlatifi.broxage.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetServiceTest {

	@Mock
	private AssetRepository assetRepository;

	@InjectMocks
	private AssetService assetService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void getAssetsByCustomerId_success() {
		Long customerId = 1L;
		List<Asset> expectedAssets = Arrays.asList(
				createAsset(customerId, "BTC", BigDecimal.ONE),
				createAsset(customerId, "ETH", BigDecimal.TEN)
		);

		when(assetRepository.findByCustomerId(customerId)).thenReturn(expectedAssets);

		List<Asset> actualAssets = assetService.getAssetsByCustomerId(customerId);

		assertEquals(expectedAssets, actualAssets);
		verify(assetRepository).findByCustomerId(customerId);
	}

	@Test
	void updateAssetUsableSize_existingAsset_success() {
		Long customerId = 1L;
		String assetName = "BTC";
		BigDecimal initialAmount = BigDecimal.ONE;
		BigDecimal updateAmount = BigDecimal.valueOf(0.5);

		Asset existingAsset = createAsset(customerId, assetName, initialAmount);
		when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(existingAsset);
		when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assetService.updateAssetUsableSize(customerId, assetName, updateAmount);

		verify(assetRepository).save(argThat(asset ->
				asset.getCustomerId().equals(customerId) &&
						asset.getAssetName().equals(assetName) &&
						asset.getSize().equals(initialAmount.add(updateAmount)) &&
						asset.getUsableSize().equals(initialAmount.add(updateAmount))
		));
	}

	@Test
	void updateAssetUsableSize_newAsset_success() {
		Long customerId = 1L;
		String assetName = "BTC";
		BigDecimal amount = BigDecimal.ONE;

		when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(null);
		when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assetService.updateAssetUsableSize(customerId, assetName, amount);

		verify(assetRepository).save(argThat(asset ->
				asset.getCustomerId().equals(customerId) &&
						asset.getAssetName().equals(assetName) &&
						asset.getSize().equals(amount) &&
						asset.getUsableSize().equals(amount)
		));
	}

	@Test
	void hasEnoughUsableAsset_sufficientBalance_returnsTrue() {
		Long customerId = 1L;
		String assetName = "BTC";
		BigDecimal balance = BigDecimal.TEN;
		BigDecimal requiredAmount = BigDecimal.ONE;

		Asset asset = createAsset(customerId, assetName, balance);
		when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(asset);

		boolean result = assetService.hasEnoughUsableAsset(customerId, assetName, requiredAmount);

		assertTrue(result);
	}

	@Test
	void hasEnoughUsableAsset_insufficientBalance_returnsFalse() {
		Long customerId = 1L;
		String assetName = "BTC";
		BigDecimal balance = BigDecimal.ONE;
		BigDecimal requiredAmount = BigDecimal.TEN;

		Asset asset = createAsset(customerId, assetName, balance);
		when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(asset);

		boolean result = assetService.hasEnoughUsableAsset(customerId, assetName, requiredAmount);

		assertFalse(result);
	}

	@Test
	void hasEnoughUsableAsset_assetNotFound_returnsFalse() {
		Long customerId = 1L;
		String assetName = "BTC";
		BigDecimal requiredAmount = BigDecimal.ONE;

		when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(null);

		boolean result = assetService.hasEnoughUsableAsset(customerId, assetName, requiredAmount);

		assertFalse(result);
	}

	private Asset createAsset(Long customerId, String assetName, BigDecimal amount) {
		Asset asset = new Asset();
		asset.setCustomerId(customerId);
		asset.setAssetName(assetName);
		asset.setSize(amount);
		asset.setUsableSize(amount);
		return asset;
	}
}
