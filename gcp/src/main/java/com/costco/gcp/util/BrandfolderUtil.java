package com.costco.gcp.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class BrandfolderUtil implements CommonConstants {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/* Simplify the response structure */
	public JsonNode prettifyBfResponse(JsonNode jsonNode) {
		if(jsonNode==null)
			return null;
		JsonObject jsonObject = CommonUtil.convertJsonNodetoJsonObject(jsonNode);
		if (jsonObject != null && jsonObject.has(BF_INCLUDED) && jsonObject.getAsJsonArray(BF_INCLUDED).size() > 0
				&& jsonObject.has(BF_DATA) && jsonObject.getAsJsonArray(BF_DATA).size() > 0) {
			/*initialize a map with all the IDs and its key-value pair */
			Map<String, JsonObject> includedData = new HashMap<String, JsonObject>();
			for (int i = 0; i < jsonObject.getAsJsonArray(BF_INCLUDED).size(); i++) {
				JsonObject data = jsonObject.getAsJsonArray(BF_INCLUDED).get(i).getAsJsonObject();
				if (data != null && data.get(BF_ID) != null && data.has(BF_ATTRIBUTES) && data.getAsJsonObject(BF_ATTRIBUTES).get(BF_KEY) != null
						&& data.getAsJsonObject(BF_ATTRIBUTES).get(BF_VALUE) != null) {
					includedData.put(data.get(BF_ID).getAsString(), data.getAsJsonObject(BF_ATTRIBUTES));
				}
			}

			/* Cross references the included section of the response and add the key and value JSON element within the asset data section  */
			for (int i = 0; i < jsonObject.getAsJsonArray(BF_DATA).size(); i++) {
				JsonObject data = (JsonObject) jsonObject.getAsJsonArray(BF_DATA).get(i);
				if (data.has(BF_RELATIONSHIPS) && data.getAsJsonObject(BF_RELATIONSHIPS).has(BF_CUSTOM_FIELD_VALUES)
						&& data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).has(BF_DATA)
						&& data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).getAsJsonArray(BF_DATA).size() > 0) {

					int iCustomFieldCount = data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).getAsJsonArray(BF_DATA).size();
					for (int cCount = 0; cCount < iCustomFieldCount; cCount++) {
						JsonObject customDataJson = data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).getAsJsonArray(BF_DATA).get(cCount).getAsJsonObject();
						JsonObject customFieldJsonValue = includedData.get(customDataJson.get(BF_ID).getAsString());
						if (customFieldJsonValue != null) {
							customDataJson.remove("type");
							for (Map.Entry<String, com.google.gson.JsonElement> entry : customFieldJsonValue.entrySet()) {
								customDataJson.add(entry.getKey(), entry.getValue());
							}
						}
					}
				}
			}
			/* Removing the custom field cross reference from the JSON response */
			jsonObject.remove("included");
			if(logger.isDebugEnabled())
				logger.debug(" {}", jsonObject.toString());
		}
		return CommonUtil.convertJsonObjecttoJsonNode(jsonObject);
	}

	public Map<String,Map<String,JsonObject>> getBfCustomFields(JsonNode jsonNode) {
		/* Map Schema:  {asset_key, Map<item/product/response, JsonObject>*/
		Map<String,Map<String,JsonObject>> bfAssetData = new HashMap<String,Map<String,JsonObject>>();
		if(jsonNode!=null && jsonNode.has(BF_DATA) && jsonNode.get(BF_DATA).size()>0) {					
			for (int i = 0; i < jsonNode.get(BF_DATA).size(); i++) {
				JsonNode data = jsonNode.get(BF_DATA).get(i);
				JsonArray itemNumbers= new JsonArray();
				JsonArray productNumbers= new JsonArray();
				JsonArray businessUnit= new JsonArray();
				Map<String,JsonObject> tempData= new HashMap<String,JsonObject>();

				if(data.has(BF_RELATIONSHIPS) && data.get(BF_RELATIONSHIPS).has(BF_CUSTOM_FIELD_VALUES)
						&& data.get(BF_RELATIONSHIPS).get(BF_CUSTOM_FIELD_VALUES).has(BF_DATA)
						&& data.get(BF_RELATIONSHIPS).get(BF_CUSTOM_FIELD_VALUES).get(BF_DATA).size()>0) {

					int iCustomFieldCount=data.get(BF_RELATIONSHIPS).get(BF_CUSTOM_FIELD_VALUES).get(BF_DATA).size();
					logger.info("custom field count for asset {} is {} ",data.get(BF_ID).asText(),iCustomFieldCount);
					for (int cCount = 0; cCount < iCustomFieldCount; cCount++) {
						JsonNode customDataJson = data.get(BF_RELATIONSHIPS).get(BF_CUSTOM_FIELD_VALUES).get(BF_DATA).get(cCount);
						if(customDataJson.get(BF_KEY).asText().equals(BF_ITEMNUMBERS)) {
							itemNumbers.add(customDataJson.get(BF_VALUE).asText());
						}else if(customDataJson.get(BF_KEY).asText().equals(BF_PRODUCTID)) { 
							productNumbers.add(customDataJson.get(BF_VALUE).asText());
						}else if(customDataJson.get(BF_KEY).asText().equals(BF_BUSINESSUNIT)) {
							businessUnit.add(customDataJson.get(BF_VALUE).asText());
						}
					}
				}
				JsonObject itemJson=new JsonObject();
				itemJson.add(BF_ITEMNUMBERS,itemNumbers);

				JsonObject productJson=new JsonObject();
				productJson.add(BF_PRODUCTID,productNumbers);
				
				JsonObject businessUnitJson=new JsonObject();
				businessUnitJson.add(BF_BUSINESSUNIT,businessUnit);

				tempData.put(BF_DATA,JsonParser.parseString(data.toString()).getAsJsonObject());
				tempData.put(BF_ITEMNUMBERS, itemJson);
				tempData.put(BF_PRODUCTID, productJson);
				tempData.put(BF_BUSINESSUNIT, businessUnitJson);
				bfAssetData.put(data.get(BF_ID).asText(), tempData);
			}
		}
		logger.debug("Map size {}", bfAssetData.size());
		return bfAssetData;
	}

	// public Map<String, Map<String, JsonObject>> getItemNumberList(JsonObject jsonObject) {
	//     Map<String, Map<String, JsonObject>> bfAssetData = new HashMap<String, Map<String, JsonObject>>();
	//     int jsonDataArrayCount = jsonObject.getAsJsonArray(BF_DATA).size();

	//     for (int i = 0; i < jsonDataArrayCount; i++) {
	//         JsonObject data = jsonObject.getAsJsonArray(BF_DATA).get(i).getAsJsonObject();
	//         JsonArray itemNumbers = new JsonArray();
	//         JsonArray productNumbers = new JsonArray();

	//         Map<String, JsonObject> tempData = new HashMap<String, JsonObject>();

	//         if (data.has(BF_RELATIONSHIPS) && data.getAsJsonObject(BF_RELATIONSHIPS).has(BF_CUSTOM_FIELD_VALUES)
	//                 && data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).has(BF_DATA)
	//                 && data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).getAsJsonArray(BF_DATA).size() > 0) {

	//             int iCustomFieldCount = data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).getAsJsonArray(BF_DATA).size();
	//             for (int cCount = 0; cCount < iCustomFieldCount; cCount++) {
	//                 JsonObject customDataJson = data.getAsJsonObject(BF_RELATIONSHIPS).getAsJsonObject(BF_CUSTOM_FIELD_VALUES).getAsJsonArray(BF_DATA).get(cCount).getAsJsonObject();
	//                 if (customDataJson.get(BF_KEY).getAsString().equals(BF_ITEMNUMBERS)) {
	//                     itemNumbers.add(customDataJson.get(BF_VALUE).getAsString()); 
	//                 }else if (customDataJson.get(BF_KEY).getAsString().equals(BF_PRODUCTID)) {
	//                     productNumbers.add(customDataJson.get(BF_VALUE).getAsString());
	//                 }
	//             }
	//             JsonObject itemJson = new JsonObject();
	//             itemJson.add(BF_ITEMNUMBERS, itemNumbers);

	//             JsonObject productJson = new JsonObject();
	//             productJson.add(BF_PRODUCTID, productNumbers);

	//             tempData.put(BF_DATA, JsonParser.parseString(data.toString()).getAsJsonObject());
	//             tempData.put(BF_ITEMNUMBERS, itemJson);
	//             tempData.put(BF_PRODUCTID, productJson);
	//             bfAssetData.put(data.get(BF_ID).getAsString(), tempData);
	//         }
	//     }

	//     return bfAssetData;

	// }

}
