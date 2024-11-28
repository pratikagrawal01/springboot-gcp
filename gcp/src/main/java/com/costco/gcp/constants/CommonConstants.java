package com.costco.gcp.constants;

/**
 * All common constants IMPORTANT: "Common" means that these constants are
 * common ACROSS objects. If there's an object where a constant should logically
 * be located, don't put it here - put it on the object to which it belongs. The
 * Constant Interface pattern has some significant design issues and should be
 * avoided when it can be - this interface is for those more "unavoidable"
 * situations.
 */
public interface CommonConstants {

	
	public static final String CONTENT_JSON="application/json";
	public static final String CONTENT_TYPE="Content-Type";
	public static final String AUTHORIZATION="Authorization";
	
	public static final String PATH_VALUE_SEPERATOR = "-";
	public static final String[] BRANDFOLDER_ITEM_SEARCH_PARAM = {"{businessunit}","{itemnumber}"};
	public static final String[] BRANDFOLDER_PRODUCT_SEARCH_PARAM = {"{businessunit}","{productnumber}"};
	
	public static final String BRANDFOLDER_ASSET_REPLACE="{assetkey}";
	
	public static final String BF_ATTRIBUTES = "attributes";
	public static final String BF_FILENAME = "filename";
	public static final String BF_NAME = "name";
	public static final String BF_CDN_URL = "cdn_url";
	public static final String BF_DATA = "data";
	public static final String BF_INCLUDED = "included";
	public static final String BF_KEY = "key";
	public static final String BF_VALUE = "value";
	public static final String BF_TAGS = "tags";
	public static final String BF_CUSTOM_FIELD_VALUES = "custom_field_values";
	public static final String BF_RELATIONSHIPS = "relationships";
	public static final String BF_ATTACHMENTS = "attachments";
	public static final String BF_ID = "id";
	public static final String BF_PRIORITY = "Priority";
	public static final String BF_ITEMNUMBERS = "ItemNumbers";
	public static final String BF_PRODUCTID = "ProductID";
	public static final String BF_BUSINESSUNIT = "BusinessUnit";
	
	public static final String GCP_FILETYPE=".json";
	
	public static final String EVENT_WEBHOOK_UPDATE = "asset.update";
	public static final String EVENT_WEBHOOK_CREATE = "asset.create";
	public static final String EVENT_WEBHOOK_DELETE = "asset.delete";
	
}

