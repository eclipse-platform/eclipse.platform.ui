<%
	String agent=request.getHeader("User-Agent").toLowerCase();
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean mozilla  = (!ie && (agent.indexOf("mozilla/5")!=-1));
%>
<html>
<head>
<meta http-equiv="Refresh" content="0; URL=<%=ie||mozilla?"":"ns4/"%>help.jsp<%=request.getQueryString()!=null?"?"+request.getQueryString():""%>">
</head>

<body>
</body>


</html>