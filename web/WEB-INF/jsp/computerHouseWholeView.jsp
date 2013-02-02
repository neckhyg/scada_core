<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.m2m2.vo.UserComment"%>
<%@ taglib prefix="main" tagdir="/WEB-INF/tags/main" %>

<tag:eazyCorePage  dwr="FlexWatchListDwr" js="/resources/view.js">
  <jsp:attribute name="styles">
    <style>
    
    </style>
    <style>

</style>
   <link href="eazytec/css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/bootstrap.css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/common.css">
	<link rel="stylesheet" media="screen" type="text/css" href="eazytec/css/admin.css">    
    
  </jsp:attribute>
  
  <jsp:body>
        <style type="text/css" media="screen"> 
			html, body	{ height:100%; }
			body { margin:0; padding:0; overflow:auto; text-align:center; 
			       background-color: #ffffff; }   
			object:focus { outline:none; }
			#flashContent { display:none;width:980;height:550; }
        </style>
		
		<!-- Enable Browser History by replacing useBrowserHistory tokens with two hyphens -->
        <!-- BEGIN Browser History required section -->
        <link rel="stylesheet" type="text/css" href="builder/history/history.css" />
        <script type="text/javascript" src="builder/history/history.js"></script>
        <!-- END Browser History required section -->  
		    
        <script type="text/javascript" src="builder/swfobject.js"></script>
           <!-- For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. -->
                 
        <script type="text/javascript">
  
            var swfVersionStr = "10.0.0";
 
            var xiSwfUrlStr = "builder/playerProductInstall.swf";
            var flashvars = {};
            var params = {};
            params.quality = "high";
            params.bgcolor = "#ffffff";
            params.allowscriptaccess = "sameDomain";
            params.allowfullscreen = "true";
            var attributes = {};
            attributes.id = "eazyscada";
            attributes.name = "eazyscada";
            attributes.align = "middle";
            swfobject.embedSWF(
                "builder/eazyscada.swf", "flashContent", 
                "100%", "100%", 
                swfVersionStr, xiSwfUrlStr, 
                flashvars, params, attributes);

			swfobject.createCSS("#flashContent", "display:block;text-align:left;");
			
        </script>
  <script type="text/javascript">
    
      dojo.require("dojo.widget.SplitContainer");
      dojo.require("dojo.widget.ContentPane");
      mango.view.initWatchlist();
      mango.share.dwr = FlexWatchListDwr;
      var owner;
      var pointNames = {};
      var lastValue = {};
      var watchlistChangeId = 0;

  function getHistoryChartValue(dpid) {
 
   /*          
            var periodType = 4;
            var  periodCount = 1;
         FlexWatchListDwr.getDateRangeDefaults(periodType, periodCount, function(data) {
              setDateRange(data);
             // getImageChart();
          });
         */ 
 /*         FlexWatchListDwr.handleRequestInternal("1",function(data){
               //  alert(data.point);
          });
  */        
       var dataList;
        
        var limit = 10;//parseInt($get("historyLimit"));
        if (isNaN(limit))
            alert("<fmt:message key="pointDetails.recordCountError"/>");
        else {
           // startImageFader($("historyLimitImg"));
         // alert(dpid);
            FlexWatchListDwr.getHistoryChartValue(limit,dpid,function(response) {
  
            	   dataList =response.data.Flexchart;
            	// alert(dataList);
            	
            });
                   
   
           return dataList;
           
           
        }
 
    }  
    
   
function changeDocumentTitle() {   
   
             
             
             var retValue = 200;//Integer.parseInt(dataMangolist[0]);
             
        
		 
		          FlexWatchListDwr.readRealTimeDataFromDB(1, function(data) {
        	
             
               //   var strs= new Array();
                 dataMangolist = data.dataMangos;
                 // strs =  dataMangolist.split(",");
         
                 // return strs[1];
              //    return dataMangolist;
                 // alert(strs[1]);
             
          });  
          // alert(dataMangolist.length);
           for (var i=0; i<dataMangolist.length; i++) {
              // alert(dataMangolist[i]);
               
               retValue = parseInt(dataMangolist[i]);
              }
           return  retValue;  
	} 
function readPowerValue() {   
                             
            
                    		 
		     FlexWatchListDwr.readRealTimeDataFromDB(1, function(data) {

                 dataMangolist = data.realTimeData;
             
          });  
          // alert(dataMangolist.length);
           var retValue = new Array(dataMangolist.length);
           for (var i=0; i<dataMangolist.length; i++) {
              // alert(dataMangolist[i]);
               
               retValue[i] = parseFloat(dataMangolist[i]);
              }
           return  retValue[2];  
	}	
function readCurrentStateValue() {   
                             
            
                    		 
		     FlexWatchListDwr.readRealTimeDataFromDB(1, function(data) {

                 dataMangolist = data.realTimeData;
             
          });  
          // alert(dataMangolist.length);
           var retValue = new Array(dataMangolist.length);
           for (var i=0; i<dataMangolist.length; i++) {
            //   alert(dataMangolist[i]);
               
               retValue[i] = parseFloat(dataMangolist[i]);
              }
            // alert(retValue);
           return  retValue;  
	}	
var dataPowerlist ; 
function readPowerSecondLevelStateValue() {   
                             
            
                    		 
		     FlexWatchListDwr.readRealTimePowerDataFromDB(1, function(data) {

                 dataPowerlist = data.powerTopLevelValues;
             
          });  
          // alert(dataMangolist.length);
           var retValue = new Array(dataPowerlist.length);
           for (var i=0; i<dataPowerlist.length; i++) {
              // alert(dataMangolist[i]);
               
               retValue[i] = parseFloat(dataPowerlist[i]);
              }
         //   alert(retValue);
           return  retValue;  
	}		
function readPowerValue2() {   
                             
            
                    		 
		     FlexWatchListDwr.readRealTimeDataFromDB(1, function(data) {

                 dataMangolist = data.realTimeData;
             
          });  
          // alert(dataMangolist.length);
           var retValue = new Array(dataMangolist.length);
           for (var i=0; i<dataMangolist.length; i++) {
              // alert(dataMangolist[i]);
               
               retValue[i] = parseFloat(dataMangolist[i]);
              }
           return  retValue[3];  
	}		
	 var dataMangolist ;      
function readTemperatureArray() {   
                                               		 
		     FlexWatchListDwr.readRealTimeDataFromDB(1, function(data) {

                 dataMangolist = data.realTimeData;
             
          });  
       
           var retValue = new Array(dataMangolist.length);
           for (var i=0; i<dataMangolist.length; i++) {
            // alert(dataMangolist[i]);
               
               retValue[i] = parseFloat(dataMangolist[i]);
              }
           return  retValue[0];  
          
          /*
            var selectedWatchList ;
             var pointIds ;
             var pointXids ;
             var watchListArray = new Array();
                       
            ComputerHouseWholeViewDwr.init(function(data) {
          
              selectedWatchList = data.selectedWatchList;
              pointIds = selectedWatchList.pointIds;
              pointXids = selectedWatchList.pointXids;
             var watchListStateValues = selectedWatchList.watchListStateValues;
             
             for (var i = 0 ; i < watchListStateValues.length; i++){
                watchListArray[i] = watchListStateValues[i];
             }
              
              return watchListArray[0];
             
           });    
           */
	}     
function readHumidityArray() {   
                  
         FlexWatchListDwr.readRealTimeDataFromDB(1, function(data) {
        	                         
                 dataMangolist = data.realTimeData;
             
          });  
         
             var retValue = new Array(dataMangolist.length);
           for (var i=0; i<dataMangolist.length; i++) {
 
               retValue[i] = parseFloat(dataMangolist[i]);
              }
            
          //   alert("humid ="+2);
           return  retValue[1];  
        //    return Math.floor(500*Math.random() );
          
	} 
   function deliverData(){   
 

    	    //   alert(document.getElementById("name").value);
    var flashPlay = document.getElementById("testFlash");   
    flashPlay.getData(document.getElementById("name").value);  
    
    return false;
   
   }  
   

function readHistoryDataFromDB() {   
   		  // window.document.title=a;         
            return Math.floor(500*Math.random() ); 
           //  var dataMangolist = data.dataMangos;
             
           //  var retValue = 200;//Integer.parseInt(dataMangolist[0]);
             
           //  return  retValue;
		//	return document.getElementById("name").value;     
	}  
function   goToDetails(pointId) {
            window.location.href="http://localhost:8080/eazyscada/data_point_details.shtm?dpid="+pointId;
            }  	   	
</script>         
  
 
 <div class="clear"></div>

<table width="100%">
    <tr><td>
   
      <div dojoType="SplitContainer" orientation="horizontal" sizerWidth="3" activeSizing="true" class="borderDiv"
              widgetId="splitContainer" style="width:100%; height: 600px;">
      
           <div id="flashContent"> </div>
        
        </div>

    </td></tr>

    </table>    
 

  </jsp:body>
</tag:eazyCorePage>
