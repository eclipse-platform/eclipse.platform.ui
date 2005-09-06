<%--
 Copyright (c) 2005 Intel Corporation.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     Intel Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	SelectTopicData data = new SelectTopicData(application, request, response);
%>

<html>
<head>
<title><%=ServletResources.getString("select_topic", request)%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<style type="text/css">
body {
	background-color: ButtonFace;
	color:WindowText;
	font: icon;
	border:0;
	cursor:default;
	padding: 10px;
	margin:0px;
	text-align: center;
}


td {
	padding: 0px;
	margin: 0px;
	border-width: 0px;
}

table {
	padding: 0px;
	margin: 0px;
	text-align: left;
}

table table {
	width: 447px;
	border-width: 1px;
	border-style: solid;
}

table table td {
	border-width: 0px;
	border-style: solid;
	padding: 1px 3px 1px 3px;
}

table table td.c0 {
	width: 25px;
	border-bottom-width: 1px;
}

table table td.c1 {
	border-left-width: 1px;
	border-right-width: 1px;
	border-bottom-width: 1px;
	width: 200px;
}

table table  td.c2 {
	border-bottom-width: 1px;
	width: 200px;
}

table.header {
	height: 16px;
	border-color: black;
	border-bottom-width: 0px;
}

table.header td {
	background: #AAA;
	border-color: black;
}

div.content {
	padding: 0px;
	margin: 0px;
	height: 120px;
	overflow: auto;
	width: 463px; /*Original width + 2 pixels for border + 16 pixels for scrollbar*/
	border-width: 0px;
}

table.content {
	border-top-width: 0px;
	border-bottom-width: 0px;
	border-color: black;
	height: 100%;
}

table.content td {
	background: white;
	border-color: #AAA;
}

table.footer {
	border-width: 0px;
	border-color: black;
	border-top-width: 1px;
}

table.footer td{
	text-align: right;
	padding: 5px;
}

td.title {
	padding: 5px;
}

</style>
<script language="JavaScript">

function findChecked(group) {
	for(var i=0; i<group.length; i++) {
		if(group[i].checked) {
			return i;
		}
	}
	return -1;
}

function keyListener(e){
   if(!e){
      //for IE
      e = window.event;
   }
   var hrefs = document.topicForm.hrefs;
   var checked =  findChecked(hrefs);
   if(checked < 0) return;
   switch (e.keyCode) {
	case 38:
		if (checked > 0) {
			checked = checked  -1;
			break;
		}
		return true;
	case 40:
		if (checked < (hrefs.length - 1) ) {
			checked = checked  + 1;
			break;
		}
		return true;
   	default:
		return true;
   };
   hrefs[checked].checked = true;
   hrefs[checked].focus();
   return false;
}

function openTopic() {
	var hrefs = document.topicForm.hrefs;
	for(var i=0; i<hrefs.length; i++) {
		if(hrefs[i].checked) {
	        window.opener.openTopic(hrefs[i].value);
			window.close();
		}
	}
}

function onloadHandler() {
	document.topicForm.hrefs[0].checked = true;
	document.topicForm.hrefs[0].focus();
	document.onkeydown = keyListener;
}


</script>
<body onload="onloadHandler()">
<form name="topicForm" onSubmit="javascript:openTopic();" onReset="window.close()">
<table border="0" cellpadding="0" cellspacing="0">
  <tr> 
      <td class="title"><%=ServletResources.getString("select_topic_header", request)%>
	</td>
  </tr>
  <tr> 
      <td> 
        <table border="0" cellpadding="0" cellspacing="0" class="header">
            <tr> 
              <td class="c0">&nbsp;</td>
              <td class="c1"><%=ServletResources.getString("select_topic_title", request)%></td>
              <td class="c2"><%=ServletResources.getString("select_topic_location", request)%></td>
            </tr>
		</table>
	</td>
  </tr>
  <tr> 
    <td> 
	<div class="content"> 
        <table border="0" cellpadding="0" cellspacing="0" class="content">
<%
	data.generateTopics(out);
%>
        </table>
      </div></td>
  </tr>
  <tr> 
      <td> <table border="0" cellpadding="0" cellspacing="0" class="footer">
          <tr> 
            <td> <input type="reset" value="Cancel"> &nbsp; <input  type="submit" id="display" value="<%=ServletResources.getString("select_topic_display", request)%>"> 
            </td>
          </tr>
        </table></td>
  </tr>
</table>
</form>
</body>
</html>
