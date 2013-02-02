<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.m2m2.vo.UserComment"%>
<%@ taglib prefix="main" tagdir="/WEB-INF/tags/main" %>

<tag:eazyCorePage dwr="DayPowerPointDetailsDwr" js="/resources/view.js" onload="init">
  <jsp:attribute name="styles">
    <style>
	</style>    
  </jsp:attribute>
  
  <jsp:body>
        <style type="text/css" media="screen"> 

        </style>
    <link href="eazytec/css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/bootstrap.css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/common.css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/admin.css">
    <!--[if lt IE 9]> <script src="../js/html5shiv.js"></script> <![endif]-->
	<script type="text/javascript" src="eazytec/js/jquery-1.7.2.min.js"></script>
    <script  type="text/javascript" src="eazytec/js/jquery-ui-1.8.21.custom.min.js"></script>
    <script  type="text/javascript"  src= "eazytec/js/highcharts.js" ></script>
    <!--  
	<script  type="text/javascript" src="eazytec/js/ui.datetimepicker.js" ></script>
	-->
	<script  type= "text/javascript" src="eazytec/js/jquery-ui-timepicker-addon.js" ></script> 
	<!-- 
	<script  type="text/javascript" src="eazytec/js/query.ui.datepicker-zh-CN.js"   ></script>
	<script  type="text/javascript" src="eazytec/js/query.ui.timepicker-zh-CN.js"  ></script>  
	 -->
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
                               
               
                  height:400,        
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
               // layout: 'vertical',
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
       series:[{name:'',data:[]}
                     ,{
                type: 'column',
                name: 'Average',
                data: [],
                marker: {
                	lineWidth: 2,
                	lineColor: Highcharts.getOptions().colors[3],
                	fillColor: 'white'
                } 
        }]    
    }; 
 function init() {
            //    alert("inti") ;    
             //   var result = ${"dayEnergyList"} ;
           
        } 
function DrawDayPowerChart(){

     DayPowerPointDetailsDwr.init(function(data){
  
                 var pointValueList = data.pointValueList;
                 var pointAddValueList = data.pointAddValueList;
                 var pointDateList = data.pointDateList;
                 var num = pointDateList.length;
                 var x =new Array(num); 
	             var y =new Array(num); 
	             var z =new Array(num);
	             var  EnergyItemName;
					
	              for ( var i = 0; i < num; i++){
	                    x[i] = pointDateList[i].substring(0,10);
                        y[i] = pointValueList[i];
                        z[i] = pointAddValueList[i];
	                  
	                   }
                   EnergyItemName = data.companyName;//data.energyItemDayResultList[i].energyItem.name;
	                xAxis.categories = x; 
	               options.xAxis = xAxis ;
	               series.name =  EnergyItemName;
	               series.data = y 
	              // options.series[0].name = series.name; 
	               options.series[0].name = "日累计用电总量";
	               options.series[0].data = series.data; 
	               
	               options.series[1].name = "日实际用电量";
	               options.series[1].data = z; 
	               
	               options.title.text = "<b>能耗分类分项类型："  +  series.name +"</b>"; 
	          //     options.subtitle.text = "日期："+data.energyItemDayResultList[0].substring(0,10) +"    单位： KWH";
	               
	            	var chart = new Highcharts.Chart(options); 
	            	 
	           var meterItemIdList = data.meterItemIdList;
	           var meterItemNameList = data.meterItemNameList;
				var str="";  
				for(var i=0;i<meterItemIdList.length;i++){  
					str+="<option value='"+meterItemIdList[i]+"'>"+meterItemNameList[i]+"</option>"  ;
				}
               
               if (meterItemIdList == null)		{
 	            	$("#info").show(); 
	             	$("#admin_container").hide(); 
	            	$("#admin_list").hide();               
               }else{
 	            	$("#info").hide(); 
	             	$("#admin_container").show(); 
	            	$("#admin_list").show();               
               }			                        

	           
	           $("#meterItemName").empty();
	           

				$("#meterItemName").html(str);  
				
    });
}

function UpdateDayPowerChart(startDate,endDate,companyId){

     DayPowerPointDetailsDwr.updateChart(startDate,endDate,companyId, function(data){
  
                 var pointValueList = data.pointValueList;
                 var pointAddValueList = data.pointAddValueList;
                 var pointDateList = data.pointDateList;
                 var num = pointDateList.length;
                 var x =new Array(num); 
	             var y =new Array(num); 
	             var z =new Array(num);	
	             var  EnergyItemName;
					
	              for ( var i = 0; i < num; i++){
	                    x[i] = pointDateList[i].substring(0,10);;
                        y[i] = pointValueList[i];
                       z[i] = pointAddValueList[i];
	                  
	                   }
            
             EnergyItemName = data.companyName;
	               
	               xAxis.categories = x; 
	               options.xAxis = xAxis ;
	               series.name =  EnergyItemName;
	               series.data = y 
	               //options.series[0].name = series.name; 
	                options.series[0].name = "日累计用电总量";
	               options.series[0].data = series.data; 
	               
	                options.series[1].name = "日实际用电量";
	               options.series[1].data = z;
	               options.title.text = "<b>能耗分类分项类型："  +  series.name +"</b>"; 
	          //     options.subtitle.text = "日期："+data.energyItemDayResultList[0].substring(0,10) +"    单位： KWH";
	               
	            	var chart = new Highcharts.Chart(options); 
	            	 
	           var meterItemIdList = data.meterItemIdList;
	           var meterItemNameList = data.meterItemNameList;
				var str="";  
				for(var i=0;i<meterItemIdList.length;i++){  
					str+="<option value='"+meterItemIdList[i]+"'>"+meterItemNameList[i]+"</option>"  ;
				}
					                        
               if (meterItemIdList == null)		{
 	            	$("#info").show(); 
	             	$("#admin_container").hide(); 
	            	$("#admin_list").hide();               
               }else{
 	            	$("#info").hide(); 
	             	$("#admin_container").show(); 
	            	$("#admin_list").show();               
               }	
	           
    });
}
 $(function() {


		$(document).ready(function () {             
		$("#startDay").datepicker({
		 	 showMonthAfterYear: true, // 月在年之后显示   
  			 changeMonth: true,   // 允许选择月份   
  			 changeYear: true,   // 允许选择年份   
  			// timeFormat: 'hh:mm:ss' 
 		  	 dateFormat:'yy-mm-dd'  // 设置日期格式   
   		
		}); 
		$("#endDay").datepicker(
		{
		 	 showMonthAfterYear: true, // 月在年之后显示   
  			 changeMonth: true,   // 允许选择月份   
  			 changeYear: true,   // 允许选择年份   
  			// timeFormat: 'hh:mm:ss' 
 		  	 dateFormat:'yy-mm-dd',  // 设置日期格式     
		});
	
		}); 
	});
	
$(function(){ 
    $("#find_btn").click(function(){   
      var startDay = $("#startDay").val();       
      var endDay = $("#endDay").val();        
      var buildingName = escape($("#buildingName").val());       
      var meterItemName = $("#meterItemName").val();   
   //   alert("energyItemName =" + meterItemName);    
 //     $("#meterItemName").val(id);
 			UpdateDayPowerChart(startDay,endDay,meterItemName);
         }); 
         
      });

 $(document).ready(function(){
		$.post("../energy/getBuildingInfo.action",
			{selectId:1},
			function(data){
				$("#buildingName").empty();
				var str="";  
				for(var i=0;i<data.length;i++){  
					str+="<option value='"+data[i].id+"'>"+data[i].name+"</option>"  
				}
			//	alert(str);
				$("#buildingName").html(str);
				$("#buildingName").val(1);  
		},
		"json");		
});  
function onchangeSelectBuilding(id){
      $("#buildingName").val(id);
	}          	

$(document).ready(function(){
		$.post("../energy/selectAll.action",
			{selectId:3},
			function(data){
				$("#meterItemName").empty();
				var str="";  
				for(var i=0;i<data.length;i++){  
					str+="<option value='"+data[i].id+"'>"+data[i].name+"</option>"  
				}
				$("#meterItemName").html(str);  
				$("#meterItemName").val(3);
		},
		"json");
});  
function onchangeSelectEnergyItem(id){
      $("#meterItemName").val(id);
	}  
	
$(function () {
    var chart;
    $(document).ready(function() {
    
    
    DrawDayPowerChart();
 
 
    });
    
});	  


           	
</script>	

<title>能耗查询分析</title>
</head>
<body>
<div id="opt"> 
<div class="partition"><h3>公司日耗电量趋势图</h3></div>
<div  class = "buildinglabel2">
      <label>开始日期：</label><input type="text"  id="startDay" /> 
       <label>结束日期：</label><input type="text"  id="endDay" /> 
       <label>公司能耗：</label><select id="meterItemName"  style="width:160px;top:25px ;height:25px"  name="meterItemName" onchange="onchangeSelectEnergyItem(this.value)"> </select>
       <input type="submit" class="ui-button-primary " id="find_btn" value="查 询" /> 

</div>        
    
</div> 




 <div id="admin_container" ></div> 
 
 
  </jsp:body>
</tag:eazyCorePage>
