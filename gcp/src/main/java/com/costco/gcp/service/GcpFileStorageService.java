package com.costco.gcp.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

public interface GcpFileStorageService {
	
	public void uploadFile(JsonNode jsonData,String filename,String folderpath) throws IOException;

	public JsonNode getJsonDataFromGcs(String fileName);
	
}
