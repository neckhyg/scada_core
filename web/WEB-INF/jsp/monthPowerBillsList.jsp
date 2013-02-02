<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.m2m2.vo.UserComment"%>
<%@ taglib prefix="main" tagdir="/WEB-INF/tags/main" %>

<tag:eazyCorePage dwr="MonthPowerBillsListDwr" js="/resources/view.js">
  <jsp:body>
  
    <link href="eazytec/css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/bootstrap.css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/common.css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/admin.css">
    <!--[if lt IE 9]> <script src="../js/html5shiv.js"></script> <![endif]-->
	<script type="text/javascript" src="eazytec/js/jquery-1.7.2.min.js"></script>
    <script  type="text/javascript" src="eazytec/js/jquery-ui-1.8.21.custom.min.js"></script>
    <script  type="text/javascript"  src= "eazytec/js/highcharts.js" ></script>
	<script  type="text/javascript" src="eazytec/js/ui.datetimepicker.js" ></script>
	<script  type= "text/javascript" src="eazytec/js/jquery-ui-timepicker-addon.js" ></script> 
	<script  type="text/javascript" src="eazytec/js/query.ui.datepicker-zh-CN.js"   ></script>
	<script  type="text/javascript" src="eazytec/js/query.ui.timepicker-zh-CN.js"  ></script>  
<script  type="text/javascript">
      var xAxis = {                 
                    categories: []
	            };	
       var series = {                   
                    name: '',                   
	                data: []
	            };	            	                
    var options = {
           chart: {
               renderTo: 'admin_container',
               plotBorderColor: 'gray',
                plotBorderWidth: 2,              
                               
               
                  height:300,        
                 type: 'spline',
               
            },
            title: {
                 text: '公司能耗分析',
                 style:{fontFamily:'"Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif',fontSize:"12px",color:"#F64108"}, 
                 align:'left'
            },
            subtitle: {           
                  align:'left'
            },
            xAxis: {
                categories: []
            },
           yAxis: {
                title: {
                    text: ''
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function() {
                        return '<b>'+ this.series.name +'</b><br/>'+
                        this.x +': '+ this.y +' KWH';
                }
            },
            legend: {
                align: 'top',
                verticalAlign: 'top',
                x: 300,
                y: 0,
                 enabled:true,
                borderWidth:1
            },            
             plotOptions: {
                spline: {
                    dataLabels: {
                        enabled: true
                    },
                    enableMouseTracking: false
                  },
					column: {
                    dataLabels: {
                        enabled: true
                    },
                    enableMouseTracking: false
                  }	                          
                },   
       series:[{type: 'column',name:'',data:[]}
                     ,{
                type: 'column',
                name: '',
                data: [],
                marker: {
                	lineWidth: 2,
                	lineColor: Highcharts.getOptions().colors[3],
                	fillColor: 'white'
                } 
        }]     
    }; 
function DrawMonthPowerBillsList(){

     MonthPowerBillsListDwr.init(function(data){
  
                 var pointValueList = data.pointValueList;
                 var pointDateList = data.pointDateList;
                 var pointAddValueList = data.pointAddValueList;
                 var num = pointValueList.length;
                 var DateNum = pointDateList.length-2;
                 var x =new Array(DateNum); 
	             var y =new Array(DateNum); 
	              var z =new Array(DateNum);
	            var  EnergyItemName = "公司名称：";

var tableStr = "<table class=\"datalist\" border=\"2\" align=\"center\" width=\"100%\" height=\"50\">"+
               "<tr align=\"center\"><td>本月期初字数(KWH)</td><td>本月期末字数(KWH)</td><td>实际用电(KWH)</td><td>单价(元/KWH)</td><td>应交电费(元)</td></tr>";
               
               tableStr += "<tr align=\"center\"><td>";	             
	            for ( var i = 0; i < num; i++){
	            
	               if ( i < DateNum ){
	                  x[i] = pointDateList[i].substring(8,10);
	               
                       y[i] = pointAddValueList[i];//pointValueList[i];
                       z[i] = pointAddValueList[i]*0.5;
                       
                        
                     } else {
                      tableStr += pointValueList[i];
                      tableStr += "</td><td>" ;  
	                  
	                   }
	                

                 }
                  tableStr += "</tr>" ;   
                  $("#createtable").html(tableStr); 
           		

                   EnergyItemName += data.companyName;//data.energyItemDayResultList[i].energyItem.name;
                   
	                xAxis.categories = x; 
	               options.xAxis = xAxis ;
	               series.name =  EnergyItemName;
	               series.data = y 
	               options.series[0].name = "日实际用电量"; 
	               options.series[0].data = y; 
	               
	                options.series[1].name = "日应交电费(元)";
	               options.series[1].data = z;
	            //  alert(z);
	               options.title.text = "<b>"  +  series.name +"</b>"; 
	          //     options.subtitle.text = "日期："+data.energyItemDayResultList[0].substring(0,10) +"    单位： KWH";
	               
	            	var chart = new Highcharts.Chart(options); 
	            	                   
                   EnergyItemName += ";&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;计费时段:&nbsp;&nbsp;";
                   EnergyItemName += pointDateList[0];
                   EnergyItemName += "---";
                   EnergyItemName += pointDateList[DateNum-1];

	            $("#companyName").html(EnergyItemName); 
	            	 
	           var meterItemIdList = data.meterItemIdList;
	           var meterItemNameList = data.meterItemNameList;
				var str="";  
				for(var i=0;i<meterItemIdList.length;i++){  
					str+="<option value='"+meterItemIdList[i]+"'>"+meterItemNameList[i]+"</option>"  ;
				}
               
 		                        
	           
	           $("#meterItemName").empty();
	           

				$("#meterItemName").html(str);  
				
    });
}
function UpdateMonthPowerBillsList(year,month,companyId){

     MonthPowerBillsListDwr.updateTable(year,month,companyId, function(data){
  
                 var pointValueList = data.pointValueList;
                 var pointAddValueList = data.pointAddValueList;
                 var pointDateList = data.pointDateList;
                 var num = pointValueList.length;
                 var DateNum = pointDateList.length-2;
                 var x =new Array(DateNum); 
	             var y =new Array(DateNum); 
	             var z =new Array(DateNum);	
	             var  EnergyItemName = "公司名称："; 
					
var tableStr = "<table  class=\"datalist\" border=\"2\" align=\"center\" width=\"100%\" height=\"50\">"+
               "<tr align=\"center\"><td>本月期初字数(KWH)</td><td>本月期末字数(KWH)</td><td>实际用电(KWH)</td><td>单价(元/KWH)</td><td>应交电费(元)</td></tr>";
               
               tableStr += "<tr align=\"center\"><td>";	             
	            for ( var i = 0; i < num; i++){
	            
	                if ( i < DateNum ){
	                
	                  	x[i] = pointDateList[i].substring(8,10);	                 
                        y[i] = pointValueList[i];
                        z[i] = pointAddValueList[i];
                        }else{
                           tableStr += pointValueList[i];
             			   tableStr += "</td><td>" ; 
                        }
                   
	              }
	          tableStr += "</tr>" ; 
            $("#createtable").html(tableStr); 
            
             EnergyItemName += data.companyName;
             
	               xAxis.categories = x; 
	               options.xAxis = xAxis ;
	               series.name =  EnergyItemName;
	               series.data = y 
	             //  options.series[0].name =  "每日仪表电量字数"; 
	             //  options.series[0].data = series.data; 
	               options.series[0].name = "日实际用电量"; 
	               options.series[0].data = y; 
	               
	                options.series[1].name = "日应交电费(元)";
	               options.series[1].data = z;
	               
	               options.title.text = "<b>"  +  series.name +"</b>"; 
	          //     options.subtitle.text = "日期："+data.energyItemDayResultList[0].substring(0,10) +"    单位： KWH";
	               
	            	var chart = new Highcharts.Chart(options); 
	            	             
            	  EnergyItemName += ";&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;计费时段:&nbsp;&nbsp;";
                   EnergyItemName += pointDateList[0];
                   EnergyItemName += "---";
                   EnergyItemName += pointDateList[1];
                   
	           $("#companyName").html(EnergyItemName);       

	            	 
	           var meterItemIdList = data.meterItemIdList;
	           var meterItemNameList = data.meterItemNameList;
				var str="";  
				for(var i=0;i<meterItemIdList.length;i++){  
					str+="<option value='"+meterItemIdList[i]+"'>"+meterItemNameList[i]+"</option>"  ;
				}
					                        
 
	           
    });
}
$(function(){ 
    $("#find_btn").click(function(){   
      var yearItemName = $("#yearItemName").val();       
      var monthItemName = $("#monthItemName").val();        
     
      var meterItemName = $("#meterItemName").val();   
   
 			UpdateMonthPowerBillsList(yearItemName,monthItemName,meterItemName);
         }); 
         
      });
      
$(document).ready(function(){

				$("#yearItemName").empty();
				var str="";  
				var year = 2013 ;
				for(var i=0;i< 10;i++){  
				  				  
					str+="<option value='"+ i +"'>"+ ( year++)+"年"+"</option>" ;
										
				}
			//	alert(str);
				$("#yearItemName").html(str);
				$("#yearItemName").val(0);  		
});  
function onchangeSelectYearItem(id){
      $("#yearItemName").val(id);
	}   
	
$(document).ready(function(){

				$("#monthItemName").empty();
				var str="";  
				var month = 1 ;
				for(var i=0;i< 12;i++){  
				  				  
					str+="<option value='"+ i +"'>"+ ( month++)+"月"+"</option>" ;
										
				}
			//	alert(str);
				$("#monthItemName").html(str);
				$("#monthItemName").val(0);  		
});  
function onchangeSelectMonthItem(id){
      $("#monthItemName").val(id);
	}  
	
	  	       	

$(document).ready(function(){	
		$("#meterItemName").empty();
});  
function onchangeSelectEnergyItem(id){
      $("#meterItemName").val(id);
	} 	
$(function () {
   // var chart;
    $(document).ready(function() {
    
    
    DrawMonthPowerBillsList();
 
 
    });
    
});	  
</script>  	

<div id="opt"> 

<div class="partition"><h3>公司用电收费服务单</h3></div>

<div  class = "buildinglabel2">
       <label>选择年度：</label><select id="yearItemName"  style="width:160px;top:25px ;height:25px"  name="yearItemName" onchange="onchangeSelectYearItem(this.value)"> </select>       
       <label>选择月份：</label><select id="monthItemName"  style="width:160px;top:25px ;height:25px"  name="monthItemName" onchange="onchangeSelectMonthItem(this.value)"> </select>       
       <label>公司名称：</label><select id="meterItemName"  style="width:160px;top:25px ;height:25px"  name="meterItemName" onchange="onchangeSelectEnergyItem(this.value)"> </select>
       <input type="submit" class="ui-button-primary " id="find_btn" value="查 询" /> 

</div>        
      
</div> 
<div id="admin_container" ></div> 
<br>
<div id = "companyName"></div>
<div id="createtable"></div> 

</jsp:body>
</tag:eazyCorePage>