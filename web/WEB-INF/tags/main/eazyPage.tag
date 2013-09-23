<%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"><%--
--%><%@tag pageEncoding="utf-8" import="com.serotonin.m2m2.module.UrlMappingDefinition"%><%--
--%><%@tag import="com.serotonin.m2m2.module.UriMappingDefinition"%><%--
--%><%@tag import="com.serotonin.m2m2.module.ModuleRegistry"%><%--
--%><%@tag import="com.serotonin.m2m2.Common"%><%--
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@taglib prefix="page" tagdir="/WEB-INF/tags/page" %><%--
--%><%@attribute name="styles" fragment="true" %><%--
--%><%@attribute name="dwr" rtexprvalue="true" %><%--
--%><%@attribute name="js" %><%--
--%><%@attribute name="onload" %>

<c:set var="theme">claro</c:set>
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
  <!--
  <c:forEach items="<%= Common.moduleStyles %>" var="modStyle">
    <link href="/${modStyle}" type="text/css" rel="stylesheet"/>
  </c:forEach>
  -->
  <jsp:invoke fragment="styles"/>
  
  <link rel="stylesheet" media="screen" type="text/css" href="${modulePath}/web/css/common.css">
  <!-- Scripts -->
  <script type="text/javascript" src="${dojoURI}dojo/dojo.js" data-dojo-config="async: false, parseOnLoad: true, isDebug:true, extraLocale: ['${lang}']"></script>
  <script type="text/javascript" src="/dwr/engine.js"></script>
  <script type="text/javascript" src="/dwr/util.js"></script>
  <script type="text/javascript" src="/dwr/interface/MiscDwr.js"></script>
  <script type="text/javascript" src="/resources/soundmanager2-nodebug-jsmin.js"></script>
  <script type="text/javascript" src="${modulePath}/web/js/common.js"></script>
  <c:forEach items="${dwr}" var="dwrname">
    <script type="text/javascript" src="/dwr/interface/${dwrname}.js"></script></c:forEach>
  <c:forEach items="${js}" var="jspath">
    <script type="text/javascript" src="${jspath}"></script></c:forEach>
  <script type="text/javascript">

    mango.i18n = <sst:convert obj="${clientSideMessages}"/>;
  </script>
  <c:if test="${!simple}">
    <script type="text/javascript" src="${modulePath}/web/js/header.js"></script>
    <script type="text/javascript">
      dwr.util.setEscapeHtml(false);
      dojo.require("dojo.parser");
      dojo.require("dijit.layout.ContentPane");
      dojo.require("dijit.layout.BorderContainer");

      dwr.util.setEscapeHtml(false);
      <c:if test="${!empty sessionUser}">
      require(["dojo/ready"],function(ready){
        ready(function(){
            mango.header.onLoad();
            setUserMuted(${sessionUser.muted});
        });
      });

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
    </script>
  </c:if>

  <c:forEach items="<%= Common.moduleScripts %>" var="modScript">
    <script type="text/javascript" src="/${modScript}"></script></c:forEach>

</head>

<body class="${theme}">

 <div  data-dojo-type="dijit.layout.BorderContainer" style="width: 100%; height: 100%;" >
    <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region:'top'" id="heading" >
	    <div class="top_left">
          <img src="${modulePath}/web/images/logo.png" style="padding-top:10px; padding-left: 20px; float: left;">
	    </div>

	    <div class="top_right">
		    <div class="top_right1">
			    <ul>
				    <li><a href="data_point_details.shtm"><img src="${modulePath}/web/images/home.png" /><span>配置主页</span></a></li>
				    <li><a href="logout.htm"><img src="${modulePath}/web/images/quit.png" /><span>退出</span></a></li>
			    </ul>
		    </div>
	    </div>

	    <div class="top_mid">
	        <c:if test="${!simple}">
                <a href="alarm.shtm" style="color:white">
                    <span id="__header__alarmLevelDiv" style="display:none;">
                        <img id="__header__alarmLevelImg" src="/images/spacer.gif" alt="" title=""/>
                        <span id="__header__alarmLevelText"></span>
                    </span>
                </a>
            </c:if>
	    </div>
    </div>

    <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region:'leading'" id="left_menu">
        <div class="sideBarNav">
        <div class="tit">
            <span class="innerTit">实时监测</span>
            <ul>
                <li><a href="temperHumidity.shtm">&nbsp;&nbsp;&nbsp;&nbsp;温湿度监测</a></li>
                <li><a href="leak.shtm">&nbsp;&nbsp;&nbsp;&nbsp;漏水监测</a></li>
                <li><a href="airCondition.shtm">&nbsp;&nbsp;&nbsp;&nbsp;空调监测</a></li>
                <li><a href="uspMonitor.shtm">&nbsp;&nbsp;&nbsp;&nbsp;UPS1监测</a></li>
                <li><a href="usp2Monitor.shtm">&nbsp;&nbsp;&nbsp;&nbsp;UPS2监测</a></li>
            </ul>
        </div>
        <div class="tit">
            <span class="innerTit">报表统计</span>
            <ul>
                <li><a href="history.shtm">&nbsp;&nbsp;&nbsp;&nbsp;历史数据</a></li>
		        <li><a href="report.shtm">&nbsp;&nbsp;&nbsp;&nbsp;报表</a></li>
<%--
		        <li><a href="realtime.shtm">&nbsp;&nbsp;&nbsp;&nbsp;实时曲线</a></li>
--%>
            </ul>
        </div>
        <div class="tit">
            <span class="innerTit">报警记录</span>
            <ul>
		        <li><a href="alarm.shtm">&nbsp;&nbsp;&nbsp;&nbsp;报警</a></li>
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


<div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region:'center'" id="right_content">
<jsp:doBody/>
</div>

</div><!--BorderContainer DIV-->

<c:if test="${!empty onload}">
  <script type="text/javascript">dojo.ready(${onload});</script>
</c:if>

</body>
</html>
