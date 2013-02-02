package com.serotonin.eazytec.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.serotonin.eazytec.rt.dataImage.MeterItem;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;



public class MeterItemDao extends BaseDao{
	private static final String METER_ITEM_SELECT = "select  * from meter_item  ";

	public MeterItemDao() {
		super();
	}

//	public MeterItemDao(DataSource dataSource) {
//		//super(dataSource);
//	}
	
	class MeterItemValueRowMapper implements RowMapper<MeterItem> {
	public MeterItem mapRow(ResultSet rs, int rowNum)
			throws SQLException {
			//MangoValue value = createMangoValue(rs);
		    int id = rs.getInt(1);
		    String code = rs.getString(2);
		    String  name = rs.getString(3);
		    int   parentId = rs.getInt(4);
			long time = rs.getLong(2);
			
			//int sourceType = rs.getInt(6);
			//if (rs.wasNull())
			// No annotations, just return a point value.
			return new MeterItem(id,code,name,parentId);
			
			// There was a source for the point value, so return an annotated
			// version.
			//return new AnnotatedPointValueTime(value, time, sourceType,
			//rs.getInt(7));
	}
}
	
	public List<MeterItem> getMeterItemList() {
		List<MeterItem> result = query(METER_ITEM_SELECT, new Object[] {},
		new MeterItemValueRowMapper(), 0);
		//updateAnnotations(result);
		return result;		
	//	return null;
	}
	public List<MeterItem> getMeterTopLevelItemList() {
		int parentId = 0;
		String sql = METER_ITEM_SELECT + "where parentId = ?";
		List<MeterItem> result = query(sql, new Object[] {parentId},
		new MeterItemValueRowMapper(), 0);
		//updateAnnotations(result);
		return result;		
	//	return null;
	}	
	public List<MeterItem> getMeterItemListByLevel(int level) {
		
		String sql = METER_ITEM_SELECT ;
		List<MeterItem> result = new ArrayList<MeterItem>();
		List<MeterItem> tempresult = query(sql, new Object[] {},
		new MeterItemValueRowMapper(), 0);
		
		if (tempresult.size() == 0)
			return null;
		else{
			
			Iterator iter = tempresult.iterator();
			while(iter.hasNext()){
				
				MeterItem meterItem = (MeterItem)iter.next();
				if ( meterItem.getCode().length() == level)
					result.add(meterItem);
			}
		}
		return result;		
	//	return null;
	}		
	
	public MeterItem getMeterItemCodebyId(int id) {
		String sql = METER_ITEM_SELECT + "where id = ?";
		List<MeterItem> result = query(sql, new Object[] {id},
		new MeterItemValueRowMapper(), 1);
		
		if (result.size() == 0)
			return null;
		
		return result.get(0);
		

	}	
	DataValue createMangoValue(ResultSet rs)
	throws SQLException {
	//int dataType = rs.getInt(firstParameter);
		DataValue value;
	//switch (dataType) {
	//case (DataTypes.NUMERIC):
	value = new NumericValue(rs.getDouble(1));

	return value;
	}	
}
