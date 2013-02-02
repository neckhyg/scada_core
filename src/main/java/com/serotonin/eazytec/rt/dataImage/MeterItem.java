package com.serotonin.eazytec.rt.dataImage;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.PointValueDao;


public class MeterItem implements Serializable, IMeterItem,
JsonSerializable {
	
	private static final long serialVersionUID = -3;
	
	private int id;
	private String code;
	private String name;
	private  int parentId;
	
//	private final PointValueDao pointValueDao ;
   
   
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getParentId() {
		return parentId;
	}


	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public MeterItem() {
	
	}
	public MeterItem(int id, String code, String name, int parentId) {
	//	super();
		this.id = id;
		this.code = code;
		this.name = name;
		this.parentId = parentId;
	//	this.pointValueDao = pointValueDao;
	}


	@Override
	public List<MeterItem> getMeterItemList(PointValueDao pointValueDao) {
		// TODO Auto-generated method stub
		
		return pointValueDao.getMeterItemList();
	// return null;
	}


	@Override
	public List<MeterItem> getMeterItemList() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<MeterItem> getMeterItemRootList(PointValueDao pointValueDao, int parentId) {
		// TODO Auto-generated method stub
		return pointValueDao.getMeterItemRootList(parentId);
		//return null;
	}


	@Override
	public void jsonRead(JsonReader arg0, JsonObject arg1) throws JsonException {
		// TODO Auto-generated method stub
		System.out.println("METER  ITEM  READ");
	}


	@Override
	public void jsonWrite(ObjectWriter arg0) throws IOException, JsonException {
		// TODO Auto-generated method stub
		System.out.println("METER  ITEM  WRITE");
	}


//	@Override
//	public void jsonDeserialize(JsonReader arg0, JsonObject arg1)
//			throws JsonException {
//		// TODO Auto-generated method stub
//		
//		System.out.println("METER  ITEM  DESERIALIZE");
//		
//	}
//
//
//	@Override
//	public void jsonSerialize(Map<String, Object> arg0) {
//		// TODO Auto-generated method stub
//		System.out.println("METER  ITEM  SERIALIZE");
//	}

}
