<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ page import="java.util.*,org.eclipse.help.*,org.eclipse.help.servlet.*,org.eclipse.help.servlet.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>
<%
	request.setCharacterEncoding("UTF-8");

	RequestData data = new RequestData(application,request);
	if(data.isIE() || data.isMozilla()){
		request.getRequestDispatcher("/advanced/index.jsp").forward(request, response);
	}else{
		request.getRequestDispatcher("/basic/index.jsp").forward(request, response);
	}
%>
