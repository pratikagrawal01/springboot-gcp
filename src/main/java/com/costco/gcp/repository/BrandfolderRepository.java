package com.costco.gcp.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.costco.gcp.model.Brandfolder;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

@RepositoryRestResource
public interface BrandfolderRepository extends SpannerRepository<Brandfolder, String> {

}
