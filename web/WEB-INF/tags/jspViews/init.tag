<%--
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@taglib prefix="jviews" uri="/modules/jspViews/web/jviews.tld" %><%--
--%><%@tag body-content="empty" pageEncoding="UTF-8"%><%--
--%><%@attribute name="username" required="true"%>
<c:set var="modulePath" value="/modules/jspViews"/>
<script type="text/javascript" src="/dwr/interface/JspViewDwr.js"></script>
<script type="text/javascript" src="${modulePath}/web/jviews.js"></script>
<jviews:viewInit username="${username}"/>
<script type="text/javascript">
  dwr.util.setEscapeHtml(false);
  mango.view.initJspView();
  dojo.ready(mango.longPoll.start);
  
  function setPoint(xid, value, callback) {
      mango.view.setPoint(xid, value, callback);
  }
</script>
