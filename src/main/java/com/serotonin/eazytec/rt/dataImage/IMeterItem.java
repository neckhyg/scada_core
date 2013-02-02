package com.serotonin.eazytec.rt.dataImage;

import java.util.List;

import com.serotonin.m2m2.db.dao.PointValueDao;





public interface IMeterItem {
	public List<MeterItem> getMeterItemList(PointValueDao pointValueDao);
	public List<MeterItem> getMeterItemRootList(PointValueDao pointValueDao, int parentId);	
	public List<MeterItem> getMeterItemList();	
}
