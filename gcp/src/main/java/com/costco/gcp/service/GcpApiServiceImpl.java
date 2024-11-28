package com.costco.gcp.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class GcpApiServiceImpl implements GcpApiService,CommonConstants{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BrandfolderService brandfolderService;

	@Autowired
	GcpFileStorageService gcpFileStorageService;
	
	@Value("${spring.cloud.gcp.project.id}")
	private String gcpProjectId;
	
	@Value("${spring.cloud.gcp.bucket}")
	private String gcpBucket;

	@Override
	public JsonNode getItemData(String site_itemNumber) {
		String fileName="Item/"+site_itemNumber + GCP_FILETYPE;
		JsonNode itemData=gcpFileStorageService.getJsonDataFromGcs(fileName);
		if (itemData == null) {
			logger.error("File {} not found in GCS.",site_itemNumber);
			JsonNode bfResponse=brandfolderService.getItemNumberResponse(site_itemNumber);
			try {
				gcpFileStorageService.uploadFile(bfResponse,site_itemNumber,"Item/");
				logger.info("Uploded file {} to GCS.",site_itemNumber);
			} catch (IOException e) {
				logger.error("Could not upload the file {} to GCP. {} : ",fileName,e);
			}
			return bfResponse;
		}
		return itemData;
	}
	
	@Override
	public JsonNode getProductData(String site_productNumber) {
		String fileName="Product/"+site_productNumber + GCP_FILETYPE;
		JsonNode itemData=gcpFileStorageService.getJsonDataFromGcs(fileName);
		if (itemData == null) {
			logger.error("File {} not found in GCS.",site_productNumber);
			JsonNode bfResponse=brandfolderService.getProductNumberResponse(site_productNumber);
			try {
				gcpFileStorageService.uploadFile(bfResponse,site_productNumber,"Product/");
				logger.info("Uploded file {} to GCS.",site_productNumber);
			} catch (IOException e) {
				logger.error("Could not upload the file {} to GCP. {} : ",fileName,e);
			}
			return bfResponse;
		}
		return itemData;
	}
	
}
