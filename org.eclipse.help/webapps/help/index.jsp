<%
	String agent=request.getHeader("User-Agent").toLowerCase();
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean ns6  = (!ie && (agent.indexOf("mozilla/5")!=-1));
%>

<html>
<head>
<meta http-equiv="Refresh" content="0; URL=<%=ie?"ie":ns6?"ns6":"ns4"%>/index.jsp">
</head>

<body>
</body>


</html>