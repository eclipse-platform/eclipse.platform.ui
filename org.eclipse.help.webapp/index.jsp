<%
	String agent=request.getHeader("User-Agent").toLowerCase();
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean mozilla  = (!ie && (agent.indexOf("mozilla/5")!=-1));
%>
<html>
<head>
</head>
<body onload='window.location.replace("<%=ie||mozilla?"":"ns4/"%>help.jsp<%=request.getQueryString()!=null?"?"+request.getQueryString():""%>")'>
</body>


</html>