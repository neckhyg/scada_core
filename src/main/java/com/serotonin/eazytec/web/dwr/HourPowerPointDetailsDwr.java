package com.serotonin.eazytec.web.dwr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.serotonin.eazytec.db.dao.HourPowerPointValueDao;
import com.serotonin.eazytec.db.dao.MeterItemDao;
import com.serotonin.eazytec.rt.dataImage.MeterItem;
import com.serotonin.eazytec.util.HibernateDateUtil;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.web.dwr.BaseDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;



public class HourPowerPointDetailsDwr  extends BaseDwr{
	 @DwrPermission(user = true)
	public Map<String, Object> init() {
		
        Map<String, Object> data = new HashMap<String, Object>();        
        HourPowerPointValueDao hourPowerPointValueDao = new HourPowerPointValueDao();
		MeterItemDao  meterItemDao = new MeterItemDao();
				
        Date today = new Date();
        
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        String dateStr = sdf.format(today);
        dateStr = dateStr.substring(0,10);

        Date start = HibernateDateUtil.hibernateDateHelper(dateStr)[0];

        Date end = HibernateDateUtil.hibernateDateHelper(dateStr)[1];


       //  java.util.Date dt = new Date(time ); 
         String sDateTimeStart = sdf.format(start);  
         String sDateTimeEnd = sdf.format(end);  
        	
//         SimpleDateFormat df2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        	Date dateTimeStart = null;
//        	Date dateTimeEnd = null;
//        	try{
//        		dateTimeStart = (Date)df2.parse(sDateTimeStart);
//        		dateTimeEnd = (Date)df2.parse(sDateTimeEnd);
//        	}catch(Exception e){
//        		
//        		e.printStackTrace();
//        	}
//        	
//          System.out.println("datetimeStart: = " +dateTimeStart);
         
      	int CompanyId = 1;
      	MeterItem  meterItem= meterItemDao.getMeterItemCodebyId(CompanyId);   
      	
		 data.put("companyName", meterItem.getName());
		 
		 System.out.println("companyName   = "+ meterItem.getName());
		List<PointValueTime> pointValueTimeList = hourPowerPointValueDao.gethourPowerPointValuesBetween(meterItem.getCode(), sDateTimeStart, sDateTimeEnd);  

		List<Double> pointValueList = new ArrayList<Double>(pointValueTimeList.size());
		List<Double> pointAddValueList = new ArrayList<Double>(pointValueTimeList.size());
		List<String> pointDateList = new ArrayList<String>(pointValueTimeList.size());	
		
		if ( !pointValueTimeList.equals(null)){
			
			System.out.println("pointValueTimeList size  = "+ pointValueTimeList.size());
			
			
			
		//	int i = 0;
			double addValue = 0;
			 double initValue = 0;
			 
			String  strAddValue ;//= String.format("%.1f",addValue);
		//	pointAddValueList.add(Double.parseDouble(strAddValue));	
			if ( pointValueTimeList.size()> 0 ){
				initValue = pointValueTimeList.get(0).getDoubleValue();
			}
			double dbValue = initValue;
			
			Iterator iter = pointValueTimeList.iterator();
			
			while(iter.hasNext()){
				PointValueTime tempPointValueTime = (PointValueTime)iter.next();
		      //  SimpleDateFormat sdf1= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		     //   java.util.Date dt = new Date(time ); 
		        String sDateTime = sdf.format(tempPointValueTime.getTime());  //得到精确到秒的表示：08/31/2006 21:08:00
		        
				System.out.println("value   = "+ tempPointValueTime.getDoubleValue()+"Date = " + sDateTime);

				//addValue = dbValue - addValue;

				
				 dbValue = tempPointValueTime.getDoubleValue();
				 
				String  strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));

			    addValue = dbValue - initValue;
				 strAddValue = String.format("%.1f",addValue);
				pointAddValueList.add(Double.parseDouble(strAddValue));	
				initValue = dbValue ;
			//	pointValueList.add(tempPointValueTime.getDoubleValue());
				pointDateList.add(sDateTime);
				
				
				
			}
		}

		 List<MeterItem> meterItemList = meterItemDao.getMeterItemList();// getLatestPointValue(3);

			List<Integer> meterItemIdList = new ArrayList<Integer>(meterItemList.size());
			List<String> meterItemNameList = new ArrayList<String>(meterItemList.size());	
			if ( !meterItemList.equals(null)){
				
				System.out.println("meterItemList size  = "+ meterItemList.size());
				Iterator iter = meterItemList.iterator();
				
				int i = 0;
				while(iter.hasNext() && (i < 20)){
					MeterItem tempMeterItem = (MeterItem)iter.next();
					
					meterItemIdList.add(tempMeterItem.getId());
					meterItemNameList.add(tempMeterItem.getName());
					i++;
					
					
				}				
				
			}else{
				System.out.println("meterItemList size  = null");
			}
			
			 data.put("pointValueList", pointValueList);
			 data.put("pointDateList", pointDateList);
			 data.put("meterItemIdList", meterItemIdList);
			 data.put("meterItemNameList", meterItemNameList);
			 data.put("pointAddValueList", pointAddValueList);

			 
//		 DataPointDao dataPointDao = new DataPointDao();
//			
//		 DataPointVO dataPointVo = dataPointDao.getDataPoint(1);
//			
//		 System.out.println("Xid ="+dataPointVo.getDataSourceXid());		
			
		  return data;

	}
	 @DwrPermission(user = true)
	public Map<String, Object> updateChart(String startDate,String endDate, String CompanyId) {
        Map<String, Object> data = new HashMap<String, Object>();
        
        HourPowerPointValueDao hourPowerPointValueDao = new HourPowerPointValueDao();
		MeterItemDao  meterItemDao = new MeterItemDao();


        Date today = new Date();
        
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        String dateStr = sdf.format(today);
        dateStr = dateStr.substring(0,10);

        Date start = HibernateDateUtil.hibernateDateHelper(dateStr)[0];

        Date end = HibernateDateUtil.hibernateDateHelper(dateStr)[1];
 
        String sDateTimeStart = sdf.format(start);  
        String sDateTimeEnd = sdf.format(end);  
        
        String startDateTime ;
        String endDateTime;
        
           if ( startDate == null){
        	   startDateTime = sDateTimeStart ;
           }else{
           	 startDateTime = startDate+":00";
           }
 
           if ( endDate == null){
        	   endDateTime = sDateTimeEnd ;
           }else{
           	  endDateTime = endDate+":00";
           }
   
        	
//			SimpleDateFormat df2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        	Date dateTimeStart = null;
//        	Date dateTimeEnd = null;
//        	try{
//        		dateTimeStart = (Date)df2.parse(startDateTime);
//        		dateTimeEnd = (Date)df2.parse(endDateTime);
//        	}catch(Exception e){
//        		
//        		e.printStackTrace();
//        	}
        	int Id = Integer.parseInt(CompanyId);
        	MeterItem  meterItem= meterItemDao.getMeterItemCodebyId(Id);
   		    data.put("companyName", meterItem.getName());
         
        	System.out.println("meterItem code: = " +meterItem.getCode());
         
                 
         
		List<PointValueTime> pointValueTimeList = hourPowerPointValueDao.gethourPowerPointValuesBetween(meterItem.getCode(),startDateTime,endDateTime);
		data.put("dayEnergyList", pointValueTimeList);

		List<Double> pointValueList = new ArrayList<Double>(pointValueTimeList.size());
		List<Double> pointAddValueList = new ArrayList<Double>(pointValueTimeList.size());		
		List<String> pointDateList = new ArrayList<String>(pointValueTimeList.size());	
		
		if ( !pointValueTimeList.equals(null)){

			double addValue = 0;
			double initValue = 0;
			
			
			String  strAddValue ;//= String.format("%.1f",addValue);
			
			if ( pointValueTimeList.size()> 0 ){
				initValue = pointValueTimeList.get(0).getDoubleValue();
			}	
			
			double dbValue = initValue; 
			System.out.println("pointValueTimeList size  = "+ pointValueTimeList.size());
			
			Iterator iter = pointValueTimeList.iterator();
			
			int i = 0;
			while(iter.hasNext() ){
				PointValueTime tempPointValueTime = (PointValueTime)iter.next();
		        SimpleDateFormat sdf1= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		     //   java.util.Date dt = new Date(time ); 
		        String sDateTime = sdf1.format(tempPointValueTime.getTime());  //得到精确到秒的表示：08/31/2006 21:08:00
		       
		        dbValue = tempPointValueTime.getDoubleValue();		        
				System.out.println("value   = "+ tempPointValueTime.getDoubleValue()+"Date = " + sDateTime);
				
				dbValue = tempPointValueTime.getDoubleValue();
				String  strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));

			    addValue = dbValue - initValue;
				 strAddValue = String.format("%.1f",addValue);
				pointAddValueList.add(Double.parseDouble(strAddValue));	
				initValue = dbValue ;
						
			//	pointValueList.add(tempPointValueTime.getDoubleValue());
				pointDateList.add(sDateTime);
				i++;
				
				
			}
		}

		 List<MeterItem> meterItemList = meterItemDao.getMeterItemList();// getLatestPointValue(3);

			List<Integer> meterItemIdList = new ArrayList<Integer>(meterItemList.size());
			List<String> meterItemNameList = new ArrayList<String>(meterItemList.size());	
			if ( !meterItemList.equals(null)){
				
				System.out.println("meterItemList size  = "+ meterItemList.size());
				Iterator iter = meterItemList.iterator();
				
				int i = 0;
				while(iter.hasNext() ){
					MeterItem tempMeterItem = (MeterItem)iter.next();
					
					meterItemIdList.add(tempMeterItem.getId());
					meterItemNameList.add(tempMeterItem.getName());
					i++;
					
					
				}				
				
			}else{
				System.out.println("meterItemList size  = null");
			}
			
			 data.put("pointValueList", pointValueList);
			 data.put("pointDateList", pointDateList);
			 data.put("meterItemIdList", meterItemIdList);
			 data.put("meterItemNameList", meterItemNameList);
			 data.put("pointAddValueList", pointAddValueList);			 
			
		 
		  return data;


	}	
}
