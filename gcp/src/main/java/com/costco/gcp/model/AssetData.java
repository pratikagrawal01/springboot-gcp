package com.costco.gcp.model;

import java.time.LocalDateTime;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import com.google.gson.JsonObject;
import com.google.spanner.v1.TypeCode;

import lombok.Data;

@Table(name = "assetdata")
@Data
public class AssetData {

	@PrimaryKey
	@Column(name = "assetid")
	private String assetId ;	
	
	@Column(name = "site", spannerType = TypeCode.JSON)
	private JsonObject site;
	
	@Column(name = "itemnumber", spannerType = TypeCode.JSON)
	private JsonObject itemNumber;
	
	@Column(name = "productnumber", spannerType = TypeCode.JSON)
	private JsonObject productnumber ;
	
	@Column(name = "metadata", spannerType = TypeCode.JSON)
	private JsonObject metaData;
	
	@Column(name = "updatedate" , spannerType = TypeCode.TIMESTAMP)
	private LocalDateTime updateDate;
	
	/**
	 * @return the assetId
	 */
	public String getAssetId() {
		return assetId;
	}

	/**
	 * @return the site
	 */
	public JsonObject getSite() {
		return site;
	}

	/**
	 * @return the itemNumber
	 */
	public JsonObject getItemNumber() {
		return itemNumber;
	}

	/**
	 * @return the productnumber
	 */
	public JsonObject getProductnumber() {
		return productnumber;
	}

	/**
	 * @return the metaData
	 */
	public JsonObject getMetaData() {
		return metaData;
	}

	/**
	 * @return the updateDate
	 */
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param assetId the assetId to set
	 */
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	/**
	 * @param site the site to set
	 */
	public void setSite(JsonObject site) {
		this.site = site;
	}

	/**
	 * @param itemNumber the itemNumber to set
	 */
	public void setItemNumber(JsonObject itemNumber) {
		this.itemNumber = itemNumber;
	}

	/**
	 * @param productnumber the productnumber to set
	 */
	public void setProductnumber(JsonObject productnumber) {
		this.productnumber = productnumber;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(JsonObject metaData) {
		this.metaData = metaData;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}
	
}

