<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	WorkingSetManagerData data = new WorkingSetManagerData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>	


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<title><%=WebappResources.getString("SelectWorkingSet", request)%></title>

<style type="text/css">

/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }

BODY {
	margin:0px;
	padding:0px;
	border-right:1px solid WindowText;
	/* Mozilla does not like width:100%, so we set height only */
	height:100%;
}

IFRAME {
	width:300px;
	height:300px;
}

.hidden {
	visibility:hidden;
	width:0;
	height:0;
}

.visible {
	visibility:visible;
	width:100%;
	height:100%;
}

</style>

<script language="Javascript">

</script>

</head>
   
<body>

 	<iframe width=300 height=300 src='<%="workingSetManager.jsp?"+request.getQueryString()%>' frameborder="0" class="xvisible" name="left" id="left"></iframe>
	<iframe width=300 height=300 src='<%="workingSet.jsp?"+request.getQueryString()%>' frameborder="0" class="xvisible" name="right" id="right"></iframe>
 
</body>
</html>