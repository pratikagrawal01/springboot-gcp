package com.costco.gcp.service;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.costco.gcp.brandfolder.api.BrandFolderClient;
import com.costco.gcp.constants.CommonConstants;
import com.costco.gcp.util.BrandfolderUtil;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class GcpApiServiceImpl implements GcpApiService,CommonConstants{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BrandfolderService brandfolderService;

	@Autowired
	BrandfolderUtil brandfolderUtil;
	
	@Autowired
	BrandFolderClient brandFolderClient;

	@Autowired
	GcpFileStorageService gcpFileStorageService;

	@Value("${spring.cloud.gcp.project.id}")
	private String gcpProjectId;

	@Value("${spring.cloud.gcp.bucket}")
	private String gcpBucket;

	@Override
	public JsonNode getGCPItemProductData(String site_itemproductNumber,String folderDirectory) {
		String fileName = folderDirectory + site_itemproductNumber + GCP_FILETYPE;
		JsonNode itemData = gcpFileStorageService.getJsonDataFromGcs(fileName);
		if (itemData == null) {
			logger.error("File {} not found in GCS.",site_itemproductNumber);
			JsonNode bfResponse=(GCP_PRODUCT_DIRECTORY.equals(folderDirectory)?
					brandfolderService.getProductNumberResponse(site_itemproductNumber):
						brandfolderService.getItemNumberResponse(site_itemproductNumber));
			if(bfResponse==null) {
				logger.error("No BF data found for item: {}",site_itemproductNumber);
				return null;
			}
			try {
				gcpFileStorageService.uploadFile(bfResponse,site_itemproductNumber,folderDirectory);
				logger.info("Uploded file {} to GCS.",fileName);
			} catch (IOException e) {
				logger.error("Could not upload the file {} to GCP. {} : ",fileName,e);
			}
			return bfResponse;
		}
		return itemData;
	}

	@Override
	public ResponseEntity<byte[]> getHeroImage(String site_itemproductNumber,String folderDirectory) throws IOException {
		JsonNode itemData = getGCPItemProductData(site_itemproductNumber,folderDirectory);
		String heroImageUrl= brandfolderUtil.getHeroImage(itemData);
		if(StringUtils.isNotBlank(heroImageUrl)) 
			return brandFolderClient.getImageFromUrl(heroImageUrl);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}