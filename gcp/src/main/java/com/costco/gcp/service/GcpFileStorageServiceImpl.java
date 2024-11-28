package com.costco.gcp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.costco.gcp.constants.CommonConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class GcpFileStorageServiceImpl implements GcpFileStorageService,CommonConstants{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${spring.cloud.gcp.project.id}")
	private String gcpProjectId;
	
	@Value("${spring.cloud.gcp.bucket}")
	private String gcpBucket;
	
	@Override
	public void uploadFile(JsonNode jsonData,String filename, String folderpath) throws IOException{
		
	    Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
	    Path filePath = Paths.get(filename + GCP_FILETYPE);
	    Files.write(filePath, jsonData.toString().getBytes());
	    
	    BlobId blobId = BlobId.of(gcpBucket, folderpath + filename + GCP_FILETYPE);
	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

	    Blob blob = storage.create(blobInfo, Files.readAllBytes(filePath));
	    logger.info("File Uploaded to {}",blob.getSelfLink());
	    Files.delete(filePath);
		
	}
	
	@Override
	public JsonNode getJsonDataFromGcs(String fileName) {
		try {
			Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
			BlobId blobId = BlobId.of(gcpBucket, fileName);
			Blob blob = storage.get(blobId);
			if (blob == null) {
				return null;
			}
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			blob.downloadTo(outputStream);
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				return objectMapper.readTree(outputStream.toByteArray());
			} catch (IOException e) {
				logger.error("Could not convert the file content of file {} to JsonNode : ",fileName);
			}
		} catch (Exception e) {
			logger.error("Could not Connect to gcp : ");
		}
		return null;
	}
}
