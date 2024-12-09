package com.costco.gcp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.costco.gcp.constants.CommonConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CommonUtil {

	public static JsonArray convertListToString(List<String> list) {
		JsonArray jsonArray = new JsonArray();
		list.forEach(jsonArray::add);
		return jsonArray;
	}

	public static JsonObject convertJsonNodetoJsonObject(JsonNode jsonNode) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonData = objectMapper.writeValueAsString(jsonNode);
			return JsonParser.parseString(jsonData).getAsJsonObject();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JsonNode convertJsonObjecttoJsonNode(JsonObject jsonObj) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();			
			return objectMapper.readTree(jsonObj.toString());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Set<String> fetchDuplicateKeys(Map<String,String> map1,Map<String,String> map2) {
		return 	 map1.entrySet().stream()
	            .filter(entry -> map2.containsKey(entry.getKey())) // Check if the key is in map2
	            .map(Map.Entry::getValue) // Get the value
	            .collect(Collectors.toSet()); // Collect the values into a set
	}
	
	public static List<String> generateStringPermutation(JsonArray array1, JsonArray array2){
		if(isNullOrEmptyJsonArray(array1) || isNullOrEmptyJsonArray(array2))
			return null;
		List<String> permutations = new ArrayList<>();
       //  Iterate over elements in the first array
        for (int i = 0; i < array1.size(); i++) {
            String element1 = array1.get(i).getAsString();
           //  Iterate over elements in the second array
            for (int j = 0; j < array2.size(); j++) {
                String element2 = array2.get(j).getAsString();
               //  Combine the two elements into a string
                permutations.add(element1 + CommonConstants.PATH_VALUE_SEPERATOR + element2);
            }
        }
        return permutations;
	}
	
	public static boolean isNullOrEmptyJsonArray(JsonArray jsonArray) {
		return jsonArray == null || jsonArray.size() == 0;
    }
	
	public static boolean isNullOrEmptyList(List<String> list) {
		return Optional.ofNullable(list)
                .filter(l -> !l.isEmpty())
                .isPresent();
    }
}