package com.amirlatifi.broxage.repository;

import com.amirlatifi.broxage.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
	List<Asset> findByCustomerId(Long customerId);

	Asset findByCustomerIdAndAssetName(Long customerId, String assetName);
}
