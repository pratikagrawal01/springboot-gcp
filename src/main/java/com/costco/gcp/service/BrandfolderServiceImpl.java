package com.costco.gcp.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.costco.gcp.brandfolder.api.BrandFolderClient;
import com.costco.gcp.constants.CommonConstants;
import com.costco.gcp.util.BrandfolderUtil;
import com.costco.gcp.util.CommonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

@Service
public class BrandfolderServiceImpl implements BrandfolderService,CommonConstants {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BrandfolderUtil brandfolderUtil;

	@Autowired
	GcpSpannerService gcpSpannerService;

	@Autowired
	BrandFolderClient brandFolderClient;

	@Value("${bf.item.asset.url}")
	private String bfItemSearchUrl;

	@Value("${bf.product.asset.url}")
	private String bfProductSearchUrl;

	@Value("${bf.attachment.url}")
	private String bfAttachmentSearchUrl;

	@Value("${bf.asset.url}")
	private String bfAssetSearchUrl;

	@Override
	public JsonNode getItemNumberResponse(String site_itemNumber, boolean updateDB) {
		try {
			if(logger.isDebugEnabled())
				logger.debug("Making BF api call for Item : {}",site_itemNumber);
			String[] params = site_itemNumber.split(PATH_VALUE_SEPERATOR, 2);
			String url = StringUtils.replaceEach(bfItemSearchUrl, BRANDFOLDER_ITEM_SEARCH_PARAM, params);
			JsonNode bfResponse = brandfolderUtil.prettifyBfResponse(brandFolderClient.makeBFApiCal(url));
			if(bfResponse==null)
				return null;
			bfResponse=getAttachmentData(bfResponse);
			if(updateDB)
				gcpSpannerService.insertItemAssetData(brandfolderUtil.getBfCustomFields(bfResponse));
			return bfResponse;
		} catch (Exception e) {
			logger.error("Error making api call for {} {}",site_itemNumber,e);
		}
		return null;
	}
	
	@Override
	public JsonNode getItemNumberResponse(String site_itemNumber) {
		return getItemNumberResponse(site_itemNumber,true);
	}

	@Override
	public JsonNode getProductNumberResponse(String site_productNumber, boolean updateDB) {
		try {
			if(logger.isDebugEnabled())
				logger.debug("Making BF api call for Product : {}",site_productNumber);
			String[] params = site_productNumber.split(CommonConstants.PATH_VALUE_SEPERATOR, 2);
			String url = StringUtils.replaceEach(bfProductSearchUrl, CommonConstants.BRANDFOLDER_PRODUCT_SEARCH_PARAM, params);
			JsonNode bfResponse = brandfolderUtil.prettifyBfResponse(brandFolderClient.makeBFApiCal(url));
			bfResponse=getAttachmentData(bfResponse);
			if(updateDB)
				gcpSpannerService.insertItemAssetData(brandfolderUtil.getBfCustomFields(bfResponse));
			return bfResponse;
		} catch (Exception e) {
			logger.error("Error making api call for {} {}",site_productNumber,e);
		}
		return null;
	}

	
	@Override
	public JsonNode getProductNumberResponse(String site_productNumber) {
		return getProductNumberResponse(site_productNumber,true);
	}
	
	public JsonNode getAttachmentData(JsonNode jsonNode) {
		try {
			if(jsonNode==null)
				return null;
			JsonObject jsonObject = CommonUtil.convertJsonNodetoJsonObject(jsonNode);
			int iJsonSize=jsonObject.getAsJsonArray(BF_DATA).size();
			for (int i = 0; i < iJsonSize; i++) {
				JsonObject data = (JsonObject) jsonObject.getAsJsonArray(BF_DATA).get(i);
				String url = StringUtils.replace(bfAttachmentSearchUrl, BRANDFOLDER_ASSET_REPLACE, data.get(BF_ID).getAsString());
				if(logger.isDebugEnabled())
					logger.debug("Making bf attachment call : {}",url );
				JsonNode bfResponse = brandFolderClient.makeBFApiCal(url);
				if(logger.isDebugEnabled())
					logger.debug("Attachment response: {} ",bfResponse);
				if(bfResponse!=null)
					data.add(BF_ATTACHMENTS,  CommonUtil.convertJsonNodetoJsonObject(bfResponse));
			}
			return CommonUtil.convertJsonObjecttoJsonNode(jsonObject);
		} catch (Exception e) {
			logger.error("Error making attachement call for jsonNode {}",jsonNode);
		}
		return null;
	}

	@Override
	public JsonNode getAssetData(String assetKey) {
		try {
			String url = StringUtils.replace(bfAssetSearchUrl, BRANDFOLDER_ASSET_REPLACE, assetKey);
			if(logger.isDebugEnabled())
				logger.debug("Making bf asset call : {}",url );
			JsonNode bfResponse = brandFolderClient.makeBFApiCal(url);
			if(logger.isDebugEnabled())
				logger.debug("Asset response: {} ",bfResponse);
			return getAttachmentData(bfResponse);
		} catch (Exception e) {
			logger.error("Error making API call for assetKey {}",assetKey);
		}
		return null;
	}
}
