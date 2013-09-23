<%--
--%><%@tag import="com.serotonin.m2m2.DataTypes"%><%--
--%><%@include file="/WEB-INF/tags/decl.tagf" %><%--
--%><%@tag body-content="empty" %><%--
--%><%@attribute name="id" %><%--
--%><%@attribute name="name" %><%--
--%><%@attribute name="value" %><%--
--%><%@attribute name="onchange" %><%--
--%><select<%--
--%><c:if test="${!empty id}"> id="${id}"</c:if><%--
--%><c:if test="${!empty name}"> name="${name}"</c:if><%--
--%><c:if test="${!empty value}"> value="${value}"</c:if><%--
--%><c:if test="${!empty onchange}"> onchange="${onchange}"</c:if>>
<option value="DI0">DI0</option>
<option value="DI1">DI1</option>
<option value="DI2">DI2</option>
<option value="DI3">DI3</option>
<option value="DI4">DI4</option>
<option value="DI5">DI5</option>
<option value="DI6">DI6</option>
<option value="DI7">DI7</option>
<option value="DO0">DO0</option>
<option value="DO1">DO1</option>
<option value="DO2">DO2</option>
<option value="DO3">DO3</option>
<option value="DO4">DO4</option>
<option value="DO5">DO5</option>
<option value="DO6">DO6</option>
<option value="DO7">DO7</option>
<option value="AD0">AD0</option>
<option value="AD1">AD1</option>
<option value="AD2">AD2</option>
<option value="AD3">AD3</option>
</select>
