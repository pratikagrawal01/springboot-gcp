package com.costco.gcp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.costco.gcp.model.AssetData;
import com.costco.gcp.model.webhook.WebHook;
import com.costco.gcp.util.BrandfolderUtil;
import com.costco.gcp.util.CommonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import com.google.gson.JsonObject;

@Service
public class BrandfolderAssetSyncServiceImpl implements BrandfolderAssetSyncService, CommonConstants{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BrandfolderService brandfolderService;

	@Autowired
	GcpApiService gcpApiService;

	@Autowired
	GcpSpannerService gcpSpannerService;

	@Autowired
	GcpFileStorageService gcpFileStorageService;

	@Autowired
	BrandfolderUtil brandfolderUtil;

	Map<String,Map<String,String>> eventMap = new HashMap<String,Map<String,String>>();

	public void processMessages(Collection<AcknowledgeablePubsubMessage> messages) {
		for (AcknowledgeablePubsubMessage message : messages) {
			try {
				String pubsubMsg= message.getPubsubMessage().getData().toStringUtf8();
				ObjectMapper objectMapper = new ObjectMapper();
				WebHook webHookData = objectMapper.readValue(pubsubMsg, WebHook.class);
				logger.info("asset {}, event {}, webHookId {}",
						webHookData.data().attributes().key(),webHookData.data().attributes().eventType(),message.getAckId());
				if(!eventMap.containsKey(webHookData.data().attributes().eventType()))
					eventMap.put(webHookData.data().attributes().eventType(), new HashMap<String,String>());
				String eventType=webHookData.data().attributes().eventType();
				if(eventMap.get(eventType).containsKey(webHookData.data().attributes().key()))
					message.ack();
				else
					eventMap.get(eventType).put(webHookData.data().attributes().key(), message.getAckId());
			}catch (JsonMappingException e) {
				logger.error("Error parsing pub/sub message: {}",e);
			} catch (JsonProcessingException e) {
				logger.error("Error parsing pub/sub message: {}",e);
			}
		}
		discardDeletedAssets(messages);
		syncDBAndBlob(messages);
	}


	public void syncDBAndBlob(Collection<AcknowledgeablePubsubMessage> messages) {
		for (Map.Entry<String,Map<String,String>> entry : eventMap.entrySet()) {
			switch(entry.getKey()) {
			case EVENT_WEBHOOK_CREATE:
				syncUpdateEvents(entry.getValue(),EVENT_WEBHOOK_CREATE);
				break;
			case EVENT_WEBHOOK_UPDATE:
				syncUpdateEvents(entry.getValue(),EVENT_WEBHOOK_UPDATE);
				break;
			case EVENT_WEBHOOK_DELETE:
				break;
			default:
				logger.error("Unrecognized webhook event {}", entry.getKey());
			}
		}
	}

	public void syncUpdateEvents(Map<String,String> eventDataMap,String eventtype) {
		for (Map.Entry<String,String> entry : eventDataMap.entrySet()) {
			try {
				String assetKey=entry.getKey();
				AssetData assetData = gcpSpannerService.fetchAsset(assetKey).orElse(new AssetData());
				logger.info("Data from spanner: {}",assetData.getAssetId());

				JsonNode bfAssetResponse = brandfolderUtil.prettifyBfResponse(brandfolderService.getAssetData(assetKey));
				Map<String,Map<String,JsonObject>> bfDataMap = brandfolderUtil.getBfCustomFields(bfAssetResponse);

				List<String> siteItemList = getAssetMetaData(bfDataMap,assetKey,BF_ITEMNUMBERS);
				List<String> siteProductList = getAssetMetaData(bfDataMap,assetKey,BF_PRODUCTID);
				consolidateOldAndNewItemData(siteItemList,siteProductList,assetData);
				updateGCPFileStorage(siteItemList,bfDataMap,assetKey);
			}catch(Exception ex) {
				eventMap.get(eventtype).entrySet().removeIf(k -> k.getValue().equals(entry.getValue()));
			}
		}
	}

	public void updateGCPFileStorage(List<String> siteItemList,Map<String,Map<String,JsonObject>> bfDataMap,String assetKey) throws IOException {
		JsonObject assetMetaData = bfDataMap.get(assetKey).get(BF_DATA);
		for(String filename: siteItemList) {
			JsonNode gcpFileData = gcpApiService.getItemData(filename);
			gcpFileData=replaceAssetJsonNode(gcpFileData,assetMetaData,assetKey);
			if(gcpFileData!=null)
				gcpFileStorageService.uploadFile(gcpFileData, filename, "Item/");
		}
	}


	public JsonNode replaceAssetJsonNode(JsonNode gcpFileData,JsonObject assetData,String assetKey) {
		JsonObject tempJson = CommonUtil.convertJsonNodetoJsonObject(gcpFileData);
		if (tempJson != null && tempJson.has(BF_DATA) && tempJson.getAsJsonArray(BF_DATA).size() > 0) {
			for (int i = 0; i < tempJson.getAsJsonArray(BF_DATA).size(); i++) {
				JsonObject data = tempJson.getAsJsonArray(BF_DATA).get(i).getAsJsonObject();
				if(data.get(BF_ID).getAsString().equals(assetKey)) {
					tempJson.getAsJsonArray(BF_DATA).set(i, assetData);
					return CommonUtil.convertJsonObjecttoJsonNode(tempJson);
				}
			}
		}
		return null;
	}



	public void consolidateOldAndNewItemData(List<String> siteItemList,List<String> siteProductList,AssetData assetData) {
		if(assetData.getSite()!=null && assetData.getSite().has(BF_BUSINESSUNIT)) {
			if(assetData.getItemNumber()!=null && assetData.getItemNumber().has(BF_ITEMNUMBERS))
				siteItemList.addAll(CommonUtil.generateStringPermutation(
						assetData.getSite().get(BF_BUSINESSUNIT).getAsJsonArray(),
						assetData.getItemNumber().get(BF_ITEMNUMBERS).getAsJsonArray()));

			if(assetData.getProductnumber()!=null && assetData.getProductnumber().has(BF_PRODUCTID))
				siteProductList.addAll(CommonUtil.generateStringPermutation(
						assetData.getSite().get(BF_BUSINESSUNIT).getAsJsonArray(),
						assetData.getProductnumber().get(BF_PRODUCTID).getAsJsonArray()));
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Combined Asset Size of ItemList {} , Item={} ",siteItemList.size(),siteItemList);
			logger.debug("Combined Asset Size of ProductList {} , Product={} ",siteProductList.size(),siteProductList);
		}

		siteItemList = siteItemList.stream().distinct().collect(Collectors.toList());
		siteProductList = siteProductList.stream().distinct().collect(Collectors.toList());

		if(logger.isDebugEnabled()) {
			logger.debug("Distinct Asset Size of ItemList {} , Item={} ",siteItemList.size(),siteItemList);
			logger.debug("Distinct Asset Size of ProductList {} , Product={} ",siteProductList.size(),siteProductList);
		}
	}


	public List<String> getAssetMetaData(Map<String,Map<String,JsonObject>> bfDataMap,String assetKey,String itemType) {

		List<String> siteItemList=new ArrayList<String>();
		if(bfDataMap!=null && bfDataMap.containsKey(assetKey) && bfDataMap.get(assetKey).containsKey(BF_BUSINESSUNIT)) {
			if(bfDataMap.get(assetKey).containsKey(itemType))
				siteItemList = CommonUtil.generateStringPermutation(bfDataMap.get(assetKey).get(BF_BUSINESSUNIT).getAsJsonObject().get(BF_BUSINESSUNIT).getAsJsonArray(), 
						bfDataMap.get(assetKey).get(itemType).getAsJsonObject().get(itemType).getAsJsonArray());
		}
		if(logger.isDebugEnabled()) 
			logger.debug("BF Asset Size of {} is {}, ItemList={} ",itemType,siteItemList.size(),siteItemList);

		return siteItemList;
	}

	public void discardDeletedAssets(Collection<AcknowledgeablePubsubMessage> messages){
		Set<String> discardableEvents=new HashSet<String>();
		if(eventMap.get(EVENT_WEBHOOK_DELETE)!=null) {
			if(eventMap.get(EVENT_WEBHOOK_CREATE)!=null)
				discardableEvents.addAll(CommonUtil.fetchDuplicateKeys(eventMap.get(EVENT_WEBHOOK_CREATE), eventMap.get(EVENT_WEBHOOK_DELETE)));
			if(eventMap.get(EVENT_WEBHOOK_UPDATE)!=null)
				discardableEvents.addAll(CommonUtil.fetchDuplicateKeys(eventMap.get(EVENT_WEBHOOK_UPDATE), eventMap.get(EVENT_WEBHOOK_DELETE)));
		}
		if(eventMap.get(EVENT_WEBHOOK_UPDATE)!=null && eventMap.get(EVENT_WEBHOOK_CREATE)!=null)
			discardableEvents.addAll(CommonUtil.fetchDuplicateKeys(eventMap.get(EVENT_WEBHOOK_UPDATE), eventMap.get(EVENT_WEBHOOK_CREATE)));


		if(logger.isDebugEnabled()) {
			logger.debug("Total size for create {}, update {} , duplicates {}",
					(eventMap.get(EVENT_WEBHOOK_CREATE)==null?null:eventMap.get(EVENT_WEBHOOK_CREATE).size()),
					(eventMap.get(EVENT_WEBHOOK_UPDATE)==null?null:eventMap.get(EVENT_WEBHOOK_UPDATE).size()),
					discardableEvents.size()
					);
		}
		if(discardableEvents==null || discardableEvents.size()==0)
			return;
		for (AcknowledgeablePubsubMessage message : messages) {
			if(discardableEvents.contains(message.getAckId())) {
				logger.debug("Discared : {} ",message.getAckId());
				message.ack();
				eventMap.get(EVENT_WEBHOOK_UPDATE).entrySet().removeIf(entry -> entry.getValue().equals(message.getAckId()));
				eventMap.get(EVENT_WEBHOOK_CREATE).entrySet().removeIf(entry -> entry.getValue().equals(message.getAckId()));
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Total size after dedublication, create {}, update {} , duplicates {}",
					(eventMap.get(EVENT_WEBHOOK_CREATE)==null?null:eventMap.get(EVENT_WEBHOOK_CREATE).size()),
					(eventMap.get(EVENT_WEBHOOK_UPDATE)==null?null:eventMap.get(EVENT_WEBHOOK_UPDATE).size()),
					discardableEvents.size()
					);
		}
	}
}



