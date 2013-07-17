<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="java.io.*,java.text.*,java.util.*,java.sql.*,javax.servlet.http.*" %>   
<%@page import="com.goldgrid.iDBManager2000"%>
<%@page import="com.goldgrid.iSMSClient2000"%>
<%@page import="com.serotonin.m2m2.db.dao.SmsTbMsgDao"%>
<%@page import="com.serotonin.m2m2.vo.SmsTbMsg"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Insert title here</title>
</head>
<body>
<%
com.goldgrid.iDBManager2000 DbaObj=new com.goldgrid.iDBManager2000();
String strOption =  request.getParameter("OPTION")==null?"":request.getParameter("OPTION").toString();
String strRecord =request.getParameter("RECORD")==null?"":request.getParameter("RECORD").toString();
String strMobile = new String(request.getParameter("MOBILE").getBytes("8859_1"));
String strContent =DbaObj.MarkText(new String(request.getParameter("CONTENT").getBytes("8859_1")));
String strDateTime = new String(request.getParameter("DATETIME").getBytes("8859_1"));
boolean mResult=false;
SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd"); 
java.util.Date now=new java.util.Date();
String dateString = formatter.format(now); 
String datestr=formatter1.format(now);
SmsTbMsgDao smsdao=new SmsTbMsgDao();
iSMSClient2000 ObjiSMSClient2000=new iSMSClient2000();
if (DbaObj.OpenConnection())
{
  try
  {
    if (strOption.equalsIgnoreCase("PUT")){
  	  smsdao.UpdateSms(strDateTime, strRecord);
    }
    else if(!strOption.equalsIgnoreCase("PUT") && !strOption.equalsIgnoreCase("GET")){
  	  List<SmsTbMsg> smslist= smsdao.getSmsList(dateString, datestr);
  	  for(int i=0;i<smslist.size();i++)
  	  {
  		  String content=smslist.get(i).getContent();
  		  String phone=smslist.get(i).getTo_mobile();
  		  String smsid=smslist.get(i).getSms_msg_no();
  		  System.out.println("content is:" + content + " gbk:" + content.getBytes("ISO-8859-1"));
		  if (ObjiSMSClient2000 .OpenSMS("127.0.0.1",8090)){
			  	mResult=ObjiSMSClient2000.SendSMS(smsid ,"86"+phone,content); 
	  		}
		  ObjiSMSClient2000.CloseSMS();
  	  }
    }
  }
  catch(Exception e)
  {
    System.out.println(e.toString());
  }
  DbaObj.CloseConnection() ;
}
if (mResult)
{
	System.out.println("OK");
}
%>
</body>
</html>