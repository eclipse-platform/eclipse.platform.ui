<%@ page contentType="text/html; charset=UTF-8"%>
<%
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
	String agent=request.getHeader("User-Agent").toLowerCase();
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean mozilla  = (!ie && (agent.indexOf("mozilla/5")!=-1));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body onload='window.location.replace("<%=ie||mozilla?"":"ns4/"%>help.jsp<%=request.getQueryString()!=null?"?"+request.getQueryString():""%>")'>
</body>


</html>