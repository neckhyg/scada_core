<%--
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ page import="com.serotonin.m2m2.Common"%>
<c:set var="storeUrl" value="<%= Common.envProps.getString("store.url") %>"/>

<tag:page>
  <br/>
  <span class="bigTitle"><fmt:message key="ipLimit.title"/></span><br/>
  <br/>
  <fmt:message key="ipLimit.message">
    <fmt:param value="${storeUrl}"/>
  </fmt:message>
  <br/>
</tag:page>