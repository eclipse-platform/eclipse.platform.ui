<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	BookmarksData data = new BookmarksData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=ServletResources.getString("Bookmarks", request)%></title>

<style type="text/css">
<%@ include file="list.css"%>
</style>

<base target="ContentViewFrame">

<script language="JavaScript" src="list.js"></script>

<script language="JavaScript">		

/**
 * Removes bookmark 
 */
function removeBookmark() 
{
	if (!active) 
		return false;
		
	var bookmark = active;
	active = null;
	
	// Note: bookmark is an anchor "a"
	var url = bookmark.href;
	var i = url.indexOf("/topic/");
	if (i >=0 )
		url = url.substring(i+6);
	// remove any query string
	i = url.indexOf("?");
	if (i >= 0)
		url = url.substring(0, i);
		
	var title = bookmark.title;
	if (title == null || title == "")
		title = url;
			
	window.location.replace("bookmarksView.jsp?operation=remove&bookmark="+url+"&title="+escape(title));
	return true;
}

/**
 * If the Del key was pressed, remove the bookmark
 */
function bookmarkKeyDownHandler(e) {
	var key;
	
	if (isIE) {
		key = window.event.keyCode;
	} else if (isMozilla) {
		key = e.keyCode;
	}

	// Check if this is the Delete key (code 46)
	if (key != 46)
		return true;
		
	if (isMozilla)
		e.cancelBubble = true;
	else if (isIE)
		window.event.cancelBubble = true;
  	
  	return removeBookmark();
}
</script>

</head>


<body>
 
<%
if(data.getBookmarks().length == 0) {
	out.write(ServletResources.getString("addBookmark", request));
} else {
%>
<table id='list'  cellspacing='0' >

<%
	Topic[] bookmarks = data.getBookmarks();
	for (int i=0; i<bookmarks.length; i++) 
	{
%>

<tr class='list' id='r<%=i%>'>
	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' 
		   href='<%=bookmarks[i].getHref()%>' 
		   onclick='parent.parent.parent.setContentToolbarTitle(" ")' 
		   oncontextmenu="contextMenuHandler(event);return false;"
		   onkeydown="bookmarkKeyDownHandler(event);"
		   title="<%=UrlUtil.htmlEncode(bookmarks[i].getLabel())%>">
		   <img src="<%=prefs.getImagesDirectory()%>/topic.gif"><%=UrlUtil.htmlEncode(bookmarks[i].getLabel())%></a>
	</td>
</tr>

<%
	}
%>

</table>
<div id="menu">
	<div class="unselectedMenuItem" onmouseover="this.className='selectedMenuItem'" onmouseout="this.className='unselectedMenuItem'" onclick="removeBookmark()" ><%=ServletResources.getString("RemoveBookmark",request)%></div>
</div>

<%
}
%>

</body>
</html>
