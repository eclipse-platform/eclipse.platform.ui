<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>

<%@ page import="org.eclipse.help.internal.webapp.servlet.*,org.eclipse.help.internal.webapp.data.*"  contentType="text/html; charset=UTF-8"%>

<% 
	request.setCharacterEncoding("UTF-8");
%>

<% 
if (new RequestData(application,request).isMozilla()) {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<% 
} else {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
}
%>
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->

