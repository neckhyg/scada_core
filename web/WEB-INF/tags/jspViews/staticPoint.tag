<%--
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@taglib prefix="jviews" uri="/modules/jspViews/web/jviews.tld" %><%--
--%><%@tag body-content="empty" pageEncoding="UTF-8"%><%--
--%><%@attribute name="xid" required="true"%><%--
--%><%@attribute name="raw" type="java.lang.Boolean"%><%--
--%><%@attribute name="disabledValue" required="false"%><%--
--%><jviews:staticPoint xid="${xid}" raw="${raw}" disabledValue="${disabledValue}"/>
