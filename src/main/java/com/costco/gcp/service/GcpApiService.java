package com.costco.gcp.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

public interface GcpApiService {
	
	public JsonNode getGCPItemProductData(String site_productNumber,String folderDirectory);

	public ResponseEntity<byte[]>  getHeroImage(String site_itemproductNumber, String folderDirectory) throws IOException ;
	
}
