package com.serotonin.eazytec.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HibernateDateUtil {
	  public static Calendar cal = Calendar.getInstance();        

	        public static DateFormat dateFormat = null;

	         public static Date date = null;

        



	         public static Date[] hibernateDateHelper(String dateStr){

	                  Date[] dateArray = new Date[2];

	                  dateArray[0] = parseDate(dateStr);

	                  dateArray[0] = getDateStart(dateArray[0]);

	                  dateArray[1] = getDateEnd(dateArray[0]);

	                 return dateArray;

	          }

	         public static Date[] hibernateDateHelper(String dateStr,int hour){

                 Date[] dateArray = new Date[2];

                 dateArray[0] = parseDate(dateStr);

                 dateArray[0] = getDateStart(dateArray[0],hour);

                 dateArray[1] = getDateEnd(dateArray[0],hour);

                return dateArray;

         }


	         public static Date parseDate(String dateStr, String format) {

	                  try {

	                         dateFormat = new SimpleDateFormat(format);

	                          String dt = dateStr.replaceAll("-", "/");

	                          if ((!dt.equals("")) && (dt.length() < format.length())) {

	                                  dt += format.substring(dt.length()).replaceAll("[YyMmDdHhSs]", "0");

	                          }

	                          date = (Date) dateFormat.parse(dt);

	                  } catch (Exception e) {

	                  }

	                 return date;

	          } 



	          public static Date getDateStart(Date d){

	                 cal.setTime(d);

	                  cal.set(Calendar.HOUR, 0);

	                 cal.set(Calendar.MINUTE, 0);

	                 cal.set(Calendar.SECOND, 0);

	                 return cal.getTime();

	          }
	          public static Date getDateStart(Date d,int hour){

	                 cal.setTime(d);

	                  cal.set(Calendar.HOUR, hour);

	                 cal.set(Calendar.MINUTE, 0);

	                 cal.set(Calendar.SECOND, 0);

	                 return cal.getTime();

	          }
	         
	          public static Date getDateEnd(Date d,int hour){

	        	      int temphour = 0;
	        	      
	        	      if ( hour < 12 ){
	        	    	  temphour = hour;
	        	      }else if (hour > 12){
	        	    	  temphour = hour -12;
	        	      }else{
	        	    	  temphour = 0;
	        	      }
	        	 
	                 cal.setTime(d);
                    
	                  cal.set(Calendar.HOUR, temphour);

	                 cal.set(Calendar.MINUTE, 59);

	                 cal.set(Calendar.SECOND, 59);

	                 return cal.getTime();

	          }
	         /**

	           * 闁兼儳鍢茶ぐ鍥籍閵夛附鍩傞柡鍫嫹濞呭嫰寮崼鏇燂紵闁挎稑鑻—鍡樺閻樻彃寮�010-12-26闁挎稑鐭佺换鎴﹀炊閿燂拷10-12-26 23:59:59

	           * @param date

	           * @return

	           */

	          public static Date getDateEnd(Date d){

	                  cal.setTime(d);

	                 cal.set(Calendar.HOUR, 23);

	                 cal.set(Calendar.MINUTE, 59);

	                 cal.set(Calendar.SECOND, 59);

	                  return cal.getTime();

	          }
//	          public static Date getDateEnd(Date d,int hour){
//
//                  cal.setTime(d);
//
//                 cal.set(Calendar.HOUR, hour);
//
//                 cal.set(Calendar.MINUTE, 59);
//
//                 cal.set(Calendar.SECOND, 59);
//
//                  return cal.getTime();
//
//          }
	          public static Date getDateHMS(Date d){

                  cal.setTime(d);

                 cal.set(Calendar.HOUR, 23);

                 cal.set(Calendar.MINUTE, 59);

                 cal.set(Calendar.SECOND, 59);

                  return cal.getTime();

          }	 
	          public static Date getDateHMS(Date d,int hour){

                  cal.setTime(d);

                 cal.set(Calendar.HOUR, hour);

                 cal.set(Calendar.MINUTE, 59);

                 cal.set(Calendar.SECOND, 59);

                  return cal.getTime();

          }	 


	         public static Date parseDate(String dateStr) {

	                  return parseDate(dateStr, "yyyy/MM/dd");
	                //  return parseDate(dateStr, "yyyy-MM-ddHH:mm:ss"); 
	          }

	          



	         public static String printDate(Date date){

	        	 
	                  return date.toLocaleString();

	          }

	          
	         public static String printDate2(Date date){

	               SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	               String datetime = tempDate.format(date);
	        	 return datetime;
	               //   return date.toLocaleString();

	          }

	         public static String[] hibernateWeekHelper(){

               String[] dateArray = new String[2];



   			Calendar cal = Calendar.getInstance();
  			int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 2;
  			cal.add(Calendar.DATE, -day_of_week);
  			Date  weekStartDay = cal.getTime();
  			cal.add(Calendar.DATE, 6);
  			Date  weekEndDay = cal.getTime();
  			SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd");
  			dateArray[0] = tempDate.format(weekStartDay);
  			dateArray[1]  = tempDate.format(weekEndDay);
              return dateArray;

       }	          
	 
	public static String[] hibernateMonthHelper(){

               String[] dateArray = new String[2];

  			 Calendar ca = Calendar.getInstance();
 		      int year = ca.get(Calendar.YEAR);//
 		      int month=ca.get(Calendar.MONTH);//
 		      int nowMonth = month+1;
 		      switch(nowMonth){
 		      case 1:
 		      case 3:
 		      case 5:
 		      case 7:
 		      case 8:
 		      {
 		    	   dateArray[0] = year+"-0"+nowMonth+"-01";
 				   dateArray[1]  = year+"-0"+nowMonth+"-31";
 		      }
 		    	  break;
 		      case 4:
 		      case 6:
 		      case 9:
 		      {
 		    	   dateArray[0] = year+"-0"+nowMonth+"-01";
 				   dateArray[1]  = year+"-0"+nowMonth+"-30";  
 		      }
 		    	  break;
 		      case 2:
 		      {
 		    	   dateArray[0] = year+"-0"+nowMonth+"-01";
 		    	   
 		    	   if ( (year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
 		    		   dateArray[1]  = year+"-0"+nowMonth+"-29";  
 		    	   }else{
 		    		   dateArray[1]  = year+"-0"+nowMonth+"-28";
 		    	   }
 				    
 		      }		    	  
 		    	  break;
 		      case 10:
 		      case 12:
 		      {
 		    	   dateArray[0] = year+"-"+nowMonth+"-01";
 				   dateArray[1]  = year+"-"+nowMonth+"-31";  
 		      }
 		    	  break;
 		      case 11:
 		      {
 		    	   dateArray[0] = year+"-"+nowMonth+"-01";
 				   dateArray[1]  = year+"-"+nowMonth+"-30";  
 		      }
 		    	  break;
 		      default:
 		    	//dateArray[0] = year+"-"+month+"-01";
   			//dateArray[1]  = year+"-"+month+"-30";
 		    		  break;
 		      }

 		
          return dateArray;

       }
	public static String[] hibernateMonthHelper(int year,int month){

        String[] dateArray = new String[2];

		 Calendar ca = Calendar.getInstance();
	   //   int year = ca.get(Calendar.YEAR);//
	    //  int month=ca.get(Calendar.MONTH);//
	      int nowMonth = month;
	      switch(nowMonth){
	      case 1:
	      case 3:
	      case 5:
	      case 7:
	      case 8:
	      {
	    	   dateArray[0] = year+"-0"+nowMonth+"-01";
			   dateArray[1]  = year+"-0"+nowMonth+"-31";
	      }
	    	  break;
	      case 4:
	      case 6:
	      case 9:
	      {
	    	   dateArray[0] = year+"-0"+nowMonth+"-01";
			   dateArray[1]  = year+"-0"+nowMonth+"-30";  
	      }
	    	  break;
	      case 2:
	      {
	    	   dateArray[0] = year+"-0"+nowMonth+"-01";
	    	   
	    	   if ( (year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
	    		   dateArray[1]  = year+"-0"+nowMonth+"-29";  
	    	   }else{
	    		   dateArray[1]  = year+"-0"+nowMonth+"-28";
	    	   }
			    
	      }		    	  
	    	  break;
	      case 10:
	      case 12:
	      {
	    	   dateArray[0] = year+"-"+nowMonth+"-01";
			   dateArray[1]  = year+"-"+nowMonth+"-31";  
	      }
	    	  break;
	      case 11:
	      {
	    	   dateArray[0] = year+"-"+nowMonth+"-01";
			   dateArray[1]  = year+"-"+nowMonth+"-30";  
	      }
	    	  break;
	      default:
	    	//dateArray[0] = year+"-"+month+"-01";
		//dateArray[1]  = year+"-"+month+"-30";
	    		  break;
	      }

	
   return dateArray;

}	
	         public static void main(String[] args) {

	                 String dateStr = "2010-12-27 9:30:30";

	                 Date start = hibernateDateHelper(dateStr)[0];

	                  Date end = hibernateDateHelper(dateStr)[1];

	                  System.out.println("start:" + printDate(start) + ", end:" + printDate(end));
	                  
	                  Date[] date = (Date[])hibernateDateHelper(dateStr,13);
	                  
	              //   Date start1 = date[0];
	               //  Date end1 = date[1];
	                  
		               Date start1 = hibernateDateHelper(dateStr,0)[0];

		               Date end1 = hibernateDateHelper(dateStr,0)[1];

		               System.out.println("start2:" +printDate(start1) + ", end2:" + printDate(end1));
		               
		               
		               SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		               String datetime = tempDate.format(new java.util.Date());
		               
		               System.out.println("datetime: = " +datetime);
		               
		               
		               dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		               
		               try{
		                    Date  newdate = (Date) dateFormat.parse("2010-12-27 9:30:30");
		                    
		                    System.out.println("newdate: = " + newdate);
		               } catch(Exception e){
		            	   
		            	   e.printStackTrace();
		               }


	          }

}
