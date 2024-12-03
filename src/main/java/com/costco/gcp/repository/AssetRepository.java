package com.costco.gcp.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.costco.gcp.model.AssetData;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

@RepositoryRestResource
public interface AssetRepository extends SpannerRepository<AssetData, String> {

}
