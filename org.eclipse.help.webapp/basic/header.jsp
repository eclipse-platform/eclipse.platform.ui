<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>

<%@ page import="java.util.*,org.eclipse.help.*,org.eclipse.help.servlet.*,org.eclipse.help.servlet.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	request.setCharacterEncoding("UTF-8");
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->

