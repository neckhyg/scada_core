<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ page pageEncoding="UTF-8"%>

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
        <tr>
          <td>
          <div style="text-align:center">
            <div class="login-main">
              <table class="login-main-ie">
                <tr>
                  <td>
                    <spring:bind path="login.username">
                    <div class="userText">
                      <input type="text" class="user_text"  name="username" size="12" maxlength="30" value=""/>
                    </div>
                    </spring:bind>
                    <spring:bind path="login.password">
                    <div class="pswText">
                      <input type="password" class="psw_text"  name="password" size="12" maxlength="30" value=""/>
                    </div>
                    </spring:bind>
                  </td>
                  <td>
                    <spring:bind path="login">
                      <input id="login-button" type="submit" value="" style="cursor: pointer"/>
                    </spring:bind>
                  </td>
                </tr>
              </table>
            </div>
            </div>
        </tr>
        <tr></tr>
        <tr><td><div id="login-footer">2008-2013 江苏卓易信息科技有限公司</div></td></tr>
        <tr></tr>
        <tr></tr>
      </table>
    </form>
  </body>
</html>
