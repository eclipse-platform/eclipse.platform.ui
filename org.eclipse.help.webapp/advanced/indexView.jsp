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
 IndexData data = new IndexData(application, request, response);
 WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">
<title><%=ServletResources.getString("Index", request)%></title>
</head>

<style type="text/css">

/* need this one for Mozilla */
html { 
  width:100%;
  height:100%;
  margin:0px;
  padding:0px;
  border:0px;
}

body {
  width:100%;
  height:100%;
  margin:0px;
  padding:0px;
  background-color: window;
  color:WindowText;
}

table {
  width: 100%;
  height: 100%;
  cell-spacing: 10px;
}

td {
  width: 100%;
  padding: 4px;
}

td.editor {
  height:16px;
}

input.editor {
  color:black;
  width:100%;
  font-size:12px;
  border: 1px solid ThreeDShadow;;
  background-color: window;
}

td.index {
  height:100%;
}

select.index {
  width:100%;
  height:100%;
  color:black;
  font-family: "sans serif";
  font-size:10px
  border: 1px solid;
  background-color: window;
}

td.display {
  height:16px;
  text-align: right;
}

input.display {
  background:ThreeDShadow;
  color:Window;
  font-weight:bold;
  border: 1px solid ThreeDShadow;
}

</style>
<script language="JavaScript">
var advancedDialog;

var oldEditor;
var index;
var editor;

var w = 500;
var h = 250;

function keyListener(e){
   if(!e){
      //for IE
      e = window.event;
   }
   switch (e.keyCode) {
  case 38:
    if (index.selectedIndex > 0) {
      index.selectedIndex = index.selectedIndex - 1;
      onIndexChange();
  	  setCaretToEnd(editor);
      return false;
    }
    return true;
  case 40:
    if (index.selectedIndex < (index.length - 1) ) {
      index.selectedIndex = index.selectedIndex + 1;
      onIndexChange(index);
  	  setCaretToEnd(editor);
      return false;
    }
    return true;
   default:
    return true;
   };
}

function selectTopic(i) {

<%
if (data.isIE()){
%>
  var l = top.screenLeft + (top.document.body.clientWidth - w) / 2;
  var t = top.screenTop + (top.document.body.clientHeight - h) / 2;
<%
} else {
%>
  var l = top.screenX + (top.innerWidth - w) / 2;
  var t = top.screenY + (top.innerHeight - h) / 2;
<%
}
%>

  // move the dialog just a bit higher than the middle
  if (t-50 > 0) t = t-50;

  window.location="javascript://needModal";
  advancedDialog = window.open("selectTopic.jsp?entry="+encodeURIComponent(index.options[i].value), "advancedDialog", "resizeable=no,height="+h+",width="+w+",left="+l+",top="+t );
  advancedDialog.focus(); 
}

function openTopic(href) {
  window.parent.parent.parent.parent.ContentFrame.ContentViewFrame.window.location.replace(href);
}

function alertEmpty() {
  alert("To locate information about this keyword \nplease select one of the subentries in the list");
}
function onEnterKeyPress(onEvent) {
  //alert("onEnterKeyPress");
  if(onEvent.keyCode==13) {
    displayTopic();
    return false;
  }
  else {
    return true;
  }
}

function setCaretToEnd (control) {
  if (control.createTextRange) {
    var range = control.createTextRange();
    range.collapse(false);
    range.select();
  }
  else if (control.setSelectionRange) {
    control.focus();
    var length = control.value.length;
    control.setSelectionRange(length, length);
  }
}

function onIndexChange() {
  //alert("onIndexChange");
  editor.value = index.options[index.selectedIndex].value;
  oldEditor = editor.value;
}

function compare(keyword, pattern) {
  var kI = 0, pI = 0;
  var kCh, pCh;
  while ( kI < keyword.length && pI < pattern.length) {
    kCh = keyword.charAt(kI).toLowerCase();
    pCh = pattern.charAt(pI).toLowerCase();
	if(kCh == ',') {
		if(pCh != ',') {
			return -1;
		}
	}
	else if (pCh == ',') {
		return 1;
	}
	else if ( kCh > pCh ) {
      return 1;
    }
    else if ( kCh < pCh) {
      return -1;
    }
    kI++;
    pI++;
  }
  if( keyword.length >= pattern.length ) {
    return 0;
  }
  else {
  	return -1;
  }
}

function searchPattern(pattern) {
  var from = 0;
  var to = index.length;
  var i;
  var res;
  do {
    i = Math.floor((to + from) / 2);
    res = compare(index.options[i].value, pattern);
    if( res == 0) {
      while (i > 0) {
        res = compare(index.options[--i].value, pattern);
        if (res != 0) {
          i++;
          break;
        }
      };
      index.selectedIndex = i;
      return;
    }
    else if (res < 0) {
      from = i + 1;
    }
    else {
      to = i;
    }
  } while (to > from) ; 
}

function updateIndex() {
  var newEditor = editor.value;
  if(oldEditor != newEditor) {
    oldEditor = newEditor;
    searchPattern(oldEditor);
  }
}

function onloadHandler() {
  editor = document.getElementById("editor");
  index = document.getElementById("index");
  index.selectedIndex = 0;
  editor.value = index.options[0].value;
  oldEditor=editor.value;
  //editor.focus();
  document.onkeydown = keyListener;
  window.self.setInterval("updateIndex()", 200);
}

</script>
<body onload="onloadHandler()">

<table>
  <tr> 
    <td><p><%=ServletResources.getString("Index_header", request)%></p>
    </td>
  </tr>
  <tr> 
    <td class="editor"> <input type="text" class="editor" id="editor" onKeyPress="return onEnterKeyPress(event);" >
    </td>
  </tr>
  <tr> 
    <td class="index"> 
  <select name="select" size="2" class="index" id="index" 
          onChange="onIndexChange();"
      onKeyPress="return onEnterKeyPress(event);"
      onDblClick="javascript:displayTopic();">

  <%
    data.generateIndex(out, "&nbsp;&nbsp;");
  %>
      </select> </td>
  </tr>
  <tr>
    <td class="display"><input class="display" type="submit" id="display" value="<%=ServletResources.getString("Index_display", request)%>" onClick="javascript:displayTopic();"></td>
  </tr>
</table>
</body>
<script language="JavaScript">

function displayTopic() {
  switch (index.selectedIndex) {
  <%
    data.generateHrefs(out);
  %>
    default:
      selectTopic(index.selectedIndex);
  };
}


</script>
</html>
