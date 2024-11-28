package com.costco.gcp.model;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import com.google.gson.JsonObject;
import com.google.spanner.v1.TypeCode;

import lombok.AllArgsConstructor;
import lombok.Data;

@Table(name = "brandfolder")
@Data
@AllArgsConstructor
public class Brandfolder {

	@PrimaryKey
	private String id;	
	
	@Column(spannerType = TypeCode.JSON)
	private JsonObject  response;
}

