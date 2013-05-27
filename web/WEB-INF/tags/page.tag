<%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"><%--
--%><%@tag import="com.serotonin.m2m2.module.ModuleRegistry"%><%--
--%><%@tag import="com.serotonin.m2m2.Common"%><%--
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@ taglib prefix="page" tagdir="/WEB-INF/tags/page" %><%--
--%><%@attribute name="styles" fragment="true" %><%--
--%><%@attribute name="dwr" rtexprvalue="true" %><%--
--%><%@attribute name="js" %><%--
--%><%@attribute name="onload" %>

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
  <meta name="Copyright" content="&copy;2006-2011 Serotonin Software Technologies Inc."/>
  <meta name="DESCRIPTION" content="EazyTec Software"/>
  <meta name="KEYWORDS" content="EazyTec Software"/>
  
  <c:if test="${empty dojoURI}">
    <c:set var="dojoURI">http://ajax.googleapis.com/ajax/libs/dojo/1.7.3/</c:set>
    <%-- <c:set var="dojoURI">http://ajax.googleapis.com/ajax/libs/dojo/1.8.0/</c:set> --%>
  </c:if>
  
  <!-- Style -->
  <link rel="icon" href="<%= Common.applicationFavicon %>"/>
  <link rel="shortcut icon" href="<%= Common.applicationFavicon %>"/>
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
    <script type="text/javascript" src="/resources/header.js"></script>
    <script type="text/javascript">
      dwr.util.setEscapeHtml(false);
      <c:if test="${!empty sessionUser}">
        dojo.ready(mango.header.onLoad);
        dojo.ready(function() { setUserMuted(${sessionUser.muted}); });
      </c:if>
      
      function setLocale(locale) {
          MiscDwr.setLocale(locale, function() { window.location = window.location });
      }
      
      function goHomeUrl() {
          MiscDwr.getHomeUrl(function(loc) { window.location = loc; });
      }
      
      function setHomeUrl() {
          MiscDwr.setHomeUrl(window.location.href, function() { alert("<fmt:message key="header.homeUrlSaved"/>"); });
      }
      
      function deleteHomeUrl() {
          MiscDwr.deleteHomeUrl(function() { alert("<fmt:message key="header.homeUrlDeleted"/>"); });
      }
    </script>
  </c:if>
  <c:forEach items="<%= Common.moduleScripts %>" var="modScript">
    <script type="text/javascript" src="/${modScript}"></script></c:forEach>
</head>

<body class="${theme}">

<table id="mainContainer" width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr id="headerArea">
    <td>
      <page:header/>
      <page:toolbar/>
    </td>
  </tr>

  <tr id="contentArea">
    <td>
      <div id="mainContent" style="padding:5px;">
        <jsp:doBody/>
      </div>
    </td>
  </tr>

  <tr id="footerArea">
    <td>
      <table width="100%" cellspacing="0" cellpadding="0" border="0">
        <tr><td colspan="2">&nbsp;</td></tr>
        <tr>
          <td colspan="2" class="footer" align="center">&copy;2008-2013 <fmt:message key="footer.companyName"/>, <fmt:message key="footer.rightsReserved"/></td>
        </tr>
        <tr>
          <td colspan="2" align="center"><a href="http://www.eazytec.com/" ><b></b>Distributed by Eazyscada System Inc.</a></td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<c:if test="${!empty onload}">
  <script type="text/javascript">dojo.ready(${onload});</script>
</c:if>

<c:forEach items="<%= Common.moduleJspfs %>" var="modJspf">
  <jsp:include page="${modJspf}" /></c:forEach>

</body>
</html>
