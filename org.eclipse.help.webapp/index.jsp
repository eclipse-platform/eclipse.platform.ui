<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ page import="java.util.*,org.eclipse.help.*,org.eclipse.help.internal.webapp.servlet.*,org.eclipse.help.internal.webapp.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>
<%
	request.setCharacterEncoding("UTF-8");

	if (request.getParameter("noscript") != null) {
		request.getRequestDispatcher("/basic/index.jsp").forward(request, response);
		return;
	}

	RequestData data = new RequestData(application,request);
	if(data.isIE() || (data.isMozilla() && data.isGecko()) ){
		request.getRequestDispatcher("/advanced/index.jsp").forward(request, response);
	}else{
		request.getRequestDispatcher("/basic/index.jsp").forward(request, response);
	}
%>
