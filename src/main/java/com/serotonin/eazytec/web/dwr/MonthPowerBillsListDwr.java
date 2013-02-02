package com.serotonin.eazytec.web.dwr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.serotonin.eazytec.db.dao.DayPowerPointValueDao;
import com.serotonin.eazytec.db.dao.MeterItemDao;
import com.serotonin.eazytec.rt.dataImage.MeterItem;
import com.serotonin.eazytec.util.HibernateDateUtil;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.web.dwr.BaseDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;



public class MonthPowerBillsListDwr extends BaseDwr{
	 @DwrPermission(user = true)
	public Map<String, Object> init() {
		
        Map<String, Object> data = new HashMap<String, Object>();        
        DayPowerPointValueDao dayPowerPointValueDao = new DayPowerPointValueDao();
		MeterItemDao  meterItemDao = new MeterItemDao();
				
        Date today = new Date();
        
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        String dateStr = sdf.format(today);
        dateStr = dateStr.substring(0,10);


        String sDateTimeEnd = dateStr;//sdf.format(end);  
        	
		String sDateTimeStart = HibernateDateUtil.hibernateMonthHelper()[0];
	//	String sDateTimeEnd = HibernateDateUtil.hibernateMonthHelper()[1];
         
      	int CompanyId = 1;
      	MeterItem  meterItem= meterItemDao.getMeterItemCodebyId(CompanyId);   
      	
		 data.put("companyName", meterItem.getName());
		 
		 System.out.println("companyName   = "+ meterItem.getName());
		List<PointValueTime> pointValueTimeList = dayPowerPointValueDao.getdayPowerPointValuesBetween(meterItem.getCode(), sDateTimeStart, sDateTimeEnd);  

		List<Double> pointValueList = new ArrayList<Double>(pointValueTimeList.size());
		List<Double> pointAddValueList = new ArrayList<Double>(pointValueTimeList.size());		
		List<String> pointDateList = new ArrayList<String>(pointValueTimeList.size());	
		
		if ( !pointValueTimeList.equals(null)&& pointValueTimeList.size()> 0){
			
			System.out.println("pointValueTimeList size  = "+ pointValueTimeList.size());
			double startValue,endValue;
			String  strValue;

			double addValue = 0;
			 double initValue = 0;
			
			String  strAddValue ;
			
			if ( pointValueTimeList.size()> 0 ){
				initValue = pointValueTimeList.get(0).getDoubleValue();
			}
			 double dbValue = initValue;
			 
			Iterator iter = pointValueTimeList.iterator();
			
		//	int i = 0;
			while(iter.hasNext()){
				PointValueTime tempPointValueTime = (PointValueTime)iter.next();

		        String sDateTime = sdf.format(tempPointValueTime.getTime());  //得到精确到秒的表示：08/31/2006 21:08:00
		        
				System.out.println("value   = "+ tempPointValueTime.getDoubleValue()+"Date = " + sDateTime);
				
				dbValue = tempPointValueTime.getDoubleValue();
			    strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));
				
			    addValue = dbValue - initValue;
				strAddValue = String.format("%.1f",addValue);
				pointAddValueList.add(Double.parseDouble(strAddValue));	
				initValue = dbValue ;
				
				pointDateList.add(sDateTime);
				
				
				
			}			
			 ///期初
				PointValueTime startPointValueTime = (PointValueTime)pointValueTimeList.get(0);				
				startValue = startPointValueTime.getDoubleValue();
			    strValue = String.format("%.1f",startValue);
				pointValueList.add(Double.parseDouble(strValue));
				pointDateList.add(sDateTimeStart);
				
			///期末
				PointValueTime endPointValueTime = (PointValueTime)pointValueTimeList.get(pointValueTimeList.size()-1);
				endValue = endPointValueTime.getDoubleValue();
			    strValue = String.format("%.1f",endValue);
				pointValueList.add(Double.parseDouble(strValue));
				pointDateList.add(sDateTimeEnd);
				
			//result
				dbValue = endValue - startValue;
				double rate = 0.5;
			    strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));
				
				dbValue *= rate ;
			    strValue = String.format("%.1f",rate);
				pointValueList.add(Double.parseDouble(strValue));
			    strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));				
				
		}else{
			 ///期初

				pointValueList.add(Double.parseDouble("0"));
				pointDateList.add(sDateTimeStart);
				
			///期末

				pointValueList.add(Double.parseDouble("0"));
				pointDateList.add(sDateTimeEnd);
				
			//result
			//	dbValue = endValue - startValue;
				double rate = 0.5;
			 //   strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble("0"));
				

				pointValueList.add(Double.parseDouble("0"));				
		}

		 List<MeterItem> meterItemList = meterItemDao.getMeterTopLevelItemList();// getLatestPointValue(3);

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
		  return data;

	}
	 @DwrPermission(user = true)
	public Map<String, Object> updateTable(String startYear,String startMonth, String CompanyId) {
        Map<String, Object> data = new HashMap<String, Object>();

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        DayPowerPointValueDao dayPowerPointValueDao = new DayPowerPointValueDao();
		MeterItemDao  meterItemDao = new MeterItemDao();


        int year = Integer.parseInt(startYear); 
        year += 2013;
        int month = Integer.parseInt(startMonth);  
        month += 1;
        
        String startDateTime ;
        String endDateTime;
        
        startDateTime = HibernateDateUtil.hibernateMonthHelper(year, month)[0];
        endDateTime = HibernateDateUtil.hibernateMonthHelper(year, month)[1];        
        

        int Id = Integer.parseInt(CompanyId);
        MeterItem  meterItem= meterItemDao.getMeterItemCodebyId(Id);
   		data.put("companyName", meterItem.getName());
         
        System.out.println("meterItem code: = " +meterItem.getCode());
         
                          
		List<PointValueTime> pointValueTimeList = dayPowerPointValueDao.getdayPowerPointValuesBetween(meterItem.getCode(),startDateTime,endDateTime);
		data.put("dayEnergyList", pointValueTimeList);

		List<Double> pointValueList = new ArrayList<Double>(pointValueTimeList.size());
		List<Double> pointAddValueList = new ArrayList<Double>(pointValueTimeList.size());
		List<String> pointDateList = new ArrayList<String>(pointValueTimeList.size());	
		
		if ( !pointValueTimeList.equals(null)&& pointValueTimeList.size()> 0){
			
			System.out.println("pointValueTimeList size  = "+ pointValueTimeList.size());
			double startValue,endValue;
			String  strValue;

			double addValue = 0;
			double initValue = 0;
						
			String  strAddValue ;
			
			if ( pointValueTimeList.size()> 0 ){
				initValue = pointValueTimeList.get(0).getDoubleValue();
			}
			
			double dbValue = initValue;
			Iterator iter = pointValueTimeList.iterator();
			
		//	int i = 0;
			while(iter.hasNext()){
				PointValueTime tempPointValueTime = (PointValueTime)iter.next();
		      //  SimpleDateFormat sdf1= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		     //   java.util.Date dt = new Date(time ); 
		        String sDateTime = sdf.format(tempPointValueTime.getTime());  //得到精确到秒的表示：08/31/2006 21:08:00
		        
				System.out.println("value   = "+ tempPointValueTime.getDoubleValue()+"Date = " + sDateTime);
				
				dbValue = tempPointValueTime.getDoubleValue();
			    strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));
				
				 addValue = dbValue - initValue;
				strAddValue = String.format("%.1f",addValue);
				pointAddValueList.add(Double.parseDouble(strAddValue));	
				initValue = dbValue ;
				pointDateList.add(sDateTime);
				
				
				
			}			
			 ///月初
				PointValueTime startPointValueTime = (PointValueTime)pointValueTimeList.get(0);				
				startValue = startPointValueTime.getDoubleValue();
			    strValue = String.format("%.1f",startValue);
				pointValueList.add(Double.parseDouble(strValue));
				pointDateList.add(startDateTime);
				
			///月末
				PointValueTime endPointValueTime = (PointValueTime)pointValueTimeList.get(pointValueTimeList.size()-1);
				endValue = endPointValueTime.getDoubleValue();
			    strValue = String.format("%.1f",endValue);
				pointValueList.add(Double.parseDouble(strValue));
				pointDateList.add(endDateTime);
				
				//result
				dbValue = endValue - startValue;
				double rate = 0.5;
			    strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));
				
				dbValue *= rate ;
			    strValue = String.format("%.1f",rate);
				pointValueList.add(Double.parseDouble(strValue));
			    strValue = String.format("%.1f",dbValue);
				pointValueList.add(Double.parseDouble(strValue));					
		}else{
			 ///期初
//			PointValueTime startPointValueTime = PointValueTime(0,null);				
//			startValue = startPointValueTime.getDoubleValue();
//		    strValue = String.format("%.1f",startValue);
			pointValueList.add(Double.parseDouble("0"));
			pointDateList.add(startDateTime);
			
		///期末
		//	PointValueTime endPointValueTime = (PointValueTime)pointValueTimeList.get(pointValueTimeList.size()-1);
//			endValue = endPointValueTime.getDoubleValue();
//		    strValue = String.format("%.1f",endValue);
			pointValueList.add(Double.parseDouble("0"));
			pointDateList.add(endDateTime);
			
		//result
		//	dbValue = endValue - startValue;
			double rate = 0.5;
		 //   strValue = String.format("%.1f",dbValue);
			pointValueList.add(Double.parseDouble("0"));
			
//			dbValue *= rate ;
//		    strValue = String.format("%.1f",rate);
//			pointValueList.add(Double.parseDouble(strValue));
//		    strValue = String.format("%.1f",dbValue);
			pointValueList.add(Double.parseDouble("0"));	
		}

		 List<MeterItem> meterItemList = meterItemDao.getMeterTopLevelItemList();// getLatestPointValue(3);

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
