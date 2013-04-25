<%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@tag pageEncoding="utf-8" import="com.serotonin.m2m2.module.UrlMappingDefinition"%>
<%@tag import="com.serotonin.m2m2.module.UriMappingDefinition"%>
<%@tag import="com.serotonin.m2m2.module.ModuleRegistry"%>
<%@tag import="com.serotonin.m2m2.Common"%>
<%@include file="/WEB-INF/tags/decl.tagf"%>
<%@ taglib prefix="page" tagdir="/WEB-INF/tags/page" %>
<%@attribute name="styles" fragment="true" %>
<%@attribute name="dwr" rtexprvalue="true" %>
<%@attribute name="js" %>
<%@attribute name="onload" %>

<c:set var="theme">claro</c:set>
<%-- <c:set var="theme">nihilo</c:set> --%>
<%-- <c:set var="theme">soria</c:set> --%>
<%-- <c:set var="theme">tundra</c:set> --%>
<html>
<head>
  <title><c:choose>
    <c:when test="${!empty instanceDescription}">${instanceDescription}</c:when>
    <c:otherwise><fmt:message key="header.title"/></c:otherwise>
  </c:choose></title>
  
  <!-- Meta -->
  <meta http-equiv="content-type" content="application/xhtml+xml;charset=utf-8"/>
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta name="Copyright" content="&copy;2008-2011 EazyTec Software Technologies Inc."/>
  <meta name="DESCRIPTION" content="EazyTec SCADA"/>
  <meta name="KEYWORDS" content="EazyTec SCADA"/>
  <c:if test="${empty dojoURI}">
    <c:set var="dojoURI">http://ajax.googleapis.com/ajax/libs/dojo/1.7.3/</c:set>
  </c:if>

  <!-- Style -->
  <link rel="icon" href="/images/favicon.ico"/>
  <link rel="shortcut icon" href="/images/favicon.ico"/>
  <style type="text/css">
    @import "${dojoURI}dojox/editor/plugins/resources/css/StatusBar.css";
    @import "${dojoURI}dojox/layout/resources/FloatingPane.css";
    @import "${dojoURI}dijit/themes/${theme}/${theme}.css";
    @import "${dojoURI}dojo/resources/dojo.css";
  </style>
  <link href="/resources/common.css" type="text/css" rel="stylesheet"/>
  <c:forEach items="<%= Common.moduleStyles %>" var="modStyle">
    <link href="/${modStyle}" type="text/css" rel="stylesheet"/></c:forEach>
  <jsp:invoke fragment="styles"/>
  
  <link rel="stylesheet" media="screen" type="text/css" href="${modulePath}/web/css/common.css">
  <!-- Scripts -->
  <script type="text/javascript" src="${dojoURI}dojo/dojo.js" data-dojo-config="async: false, parseOnLoad: true, isDebug:true, extraLocale: ['${lang}']"></script>
  <script type="text/javascript" src="/dwr/engine.js"></script>
  <script type="text/javascript" src="/dwr/util.js"></script>
  <script type="text/javascript" src="/dwr/interface/MiscDwr.js"></script>
  <script type="text/javascript" src="/resources/soundmanager2-nodebug-jsmin.js"></script>
  <script type="text/javascript" src="/resources/common.js"></script>
  <c:forEach items="${dwr}" var="dwrname">
    <script type="text/javascript" src="/dwr/interface/${dwrname}.js"></script></c:forEach>
  <c:forEach items="${js}" var="jspath">
    <script type="text/javascript" src="${jspath}"></script></c:forEach>
  <script type="text/javascript">
    mango.i18n = <sst:convert obj="${clientSideMessages}"/>;
  </script>
  <c:if test="${!simple}">
  <%--
    <script type="text/javascript" src="/resources/header.js"></script>
    --%>
    <script type="text/javascript">
      dwr.util.setEscapeHtml(false);
      dojo.require("dijit.layout.ContentPane");
      dojo.ready(setRightContentSize);
      <c:if test="${!empty sessionUser}">
     //   dojo.ready(mango.header.onLoad);
     //   dojo.ready(function() { setUserMuted(${sessionUser.muted}); });
      </c:if>
      
      function setLocale(locale) {
          MiscDwr.setLocale(locale, function() { window.location = window.location });
      }
      
      function setHomeUrl() {
          MiscDwr.setHomeUrl(window.location.href, function() { alert("Home URL saved"); });
      }
      
      function goHomeUrl() {
          MiscDwr.getHomeUrl(function(loc) { window.location = loc; });
      }
      
      function storeCheck(){
          if (window.location.href.indexOf('?You%20Are%20Currently') != -1) {
              //alert(unescape(window.location.search));
              var ss = unescape(window.location.search).substring(1);
              ss = ss.replace(/---/g,'\n');
              alert(ss);
          }
      }
	function setRightContentSize(){
	   var sideBarNavWidth = dojo.query(".sideBarNav").style("width");
	   var rightWidth = window.screen.width - sideBarNavWidth - 74;
	  var menuHeight = dojo.query("#menu").style("height");
	  var rightHeight = window.screen.height - menuHeight  - 100;
	  dojo.query("#right_content").style("width",rightWidth + "px");
	  dojo.query("#right_content").style("height",rightHeight + "px");
	}

require(["dojo/ready","dojo/query!css3","dojo/NodeList-traverse"],function(ready,query,dom){
    ready(function(){
        query(".tit > ul").style('display','none');
        query(".tit > ul").at(0).style('display','block');
        query(".tit").onclick(function(e){
        query(".tit > ul").style('display','none');
        query(e.target).next().style('display','block');
        });
    });
});

    </script>
  </c:if>

  <c:forEach items="<%= Common.moduleScripts %>" var="modScript">
    <script type="text/javascript" src="/${modScript}"></script></c:forEach>

</head>

<body class="${theme}">
<%--
<table id="mainContainer" width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr id="headerArea">
    <td>
      <page:header/>
      <page:toolbar/>
    </td>
  </tr>
--%>


 <div id="menu">
	<div class="t2"></div>
	<div class="top_left">
          <img src="images/logo.png"
			style="padding-left: 20px; float: left;">
	</div>

	<div class="top_right">

		<div class="top_right1">
			<ul>
				<li><a href="http://localhost:8080/data_point_details.shtm"><img src="${modulePath}/web/images/home.png" /><span>配置主页</span></a></li>
				<li><a href="logout.htm"><img src="${modulePath}/web/images/quit.png" /><span>退出</span></a></li>
			</ul>
		</div>
	</div>

	<div class="top_mid"></div>

</div>
 
    <div id="left_menu">
      
      <div class="sideBarNav">  
    <div class="tit">  
        <span class="innerTit">监测数据</span>
        <ul>  
        <li><a href="monitoring_point.shtm">&nbsp;&nbsp;&nbsp;&nbsp;监测点信息</a></li>
        <li><a href="monitoring_data.shtm">&nbsp;&nbsp;&nbsp;&nbsp;监测数据</a></li>
        </ul>  
    </div>  
    <div class="tit">
        <span class="innerTit">报表统计</span>
        <ul>
		<li><a href="data_analysis.shtm">&nbsp;&nbsp;&nbsp;&nbsp;数据分析</a></li>
		<li><a href="statistic.shtm">&nbsp;&nbsp;&nbsp;&nbsp;统计</a></li>
		<li><a href="report.shtm">&nbsp;&nbsp;&nbsp;&nbsp;报表</a></li>
        </ul>
    </div>
    <div class="tit">
        <span class="innerTit">超标报警</span>
        <ul>
		<li><a href="alarm.shtm">&nbsp;&nbsp;&nbsp;&nbsp;报警</a></li>
        </ul>
    </div>
    <div class="tit">
        <span class="innerTit">排污企业记录</span>
        <ul>
		<li><a href="sewage_comp_record.shtm">&nbsp;&nbsp;&nbsp;&nbsp;排污企业记录</a></li>
        </ul>
    </div>
    <div class="tit">
        <span class="innerTit">用户管理</span>
        <ul>
		<li><a href="user_management.shtm">&nbsp;&nbsp;&nbsp;&nbsp;用户管理</a></li>
        </ul>
    </div>
</div>
</div>
<!-- 
      <div dojoType="SplitContainer" orientation="horizontal" sizerWidth="3" activeSizing="true" class="borderDiv"
              widgetId="splitContainer" style="width:100%; height: 300px;">
       -->

        <jsp:doBody/>
        <!--
         <div dojoType="ContentPane" widgetId="right_content" name="right_content" id="right_content"> </div>
         -->
        
       
<%--
  <tr id="footerArea">
    <td>
      <table width="100%" cellspacing="0" cellpadding="0" border="0">
        <tr><td colspan="2">&nbsp;</td></tr>
        <tr>
          <td colspan="2" class="footer" align="center">&copy;2006-2012 EazyTec Software Technologies Inc., <fmt:message key="footer.rightsReserved"/></td>
        </tr>
        <tr>
          <td colspan="2" align="center"><a href="http://www.eazytec.com/" ><b></b>Distributed by EazyTec Information Inc.</a></td>
        </tr>
      </table>
    </td>
  </tr>
--%>

<%--
<c:if test="${!empty onload}">
  <script type="text/javascript">dojo.ready(${onload});</script>
</c:if>
--%>

<script type="text/javascript">


</script>
</body>
</html>
