<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<c:if test='${m2m2:envBoolean("ssl.on", false)}'>
  <c:if test='${pageContext.request.scheme == "http"}'>
    <c:redirect url='https://${pageContext.request.serverName}:${m2m2:envString("ssl.port", "8443")}${requestScope["javax.servlet.forward.request_uri"]}'/>
  </c:if>
</c:if>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"></meta>
    <link rel="stylesheet" type="text/css" href="/resources/login.css"></link>
  </head>
  <body>
    <form name="form1" method="post" action="login.htm">
      <table id="login">

        <tr></tr>
        <tr></tr>
        <tr> <td> <div id="login-title"><fmt:message key="login.title"/></div> <td>
        </tr>
        <tr>
          <td>
            <div class="login-main">
              <table class="login-main-ie">
             <spring:bind path="login.username">
                <tr> <td><fmt:message key="login.username"/></td><td><input type="text" class="user_text"  name="" size="12" maxlength="30" value=""/></td>
                </tr>
                </spring:bind>
                <spring:bind path="login.password">
                <tr> <td><fmt:message key="login.password"/></td><td><input type="password" class="user_text"  name="" size="12" maxlength="30" value=""/></td>
                </tr>
                </spring:bind>
                <spring:bind path="login">
                <tr> <td colspan="2"> <input id="login-button" type="submit" value="<fmt:message key="login.login"/>" style="cursor: pointer"/></td>
                </tr>
                </spring:bind>
              </table>
            </div>
        </tr>
        <tr></tr>
        <tr>2008-2013 Eazytec Inf. Company</tr>
        <tr></tr>
        <tr></tr>
      </table>
    </form>
  </body>
</html>
