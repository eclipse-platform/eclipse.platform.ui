<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	RequestData data = new RequestData(application,request);
	WebappPreferences prefs = data.getPrefs();

	// It looks like we cannot put the if statement just around the extra button
	if (prefs.isBookmarksView()) 
	{
%>
<jsp:include page="toolbar.jsp">
	<jsp:param name="script" value="contentActions.js"/>
	<jsp:param name="view" value="contents"/>
	
	<jsp:param name="name"     value="back"/>
	<jsp:param name="tooltip"  value='back_tip'/>
	<jsp:param name="image"    value='back.gif'/>
	<jsp:param name="action"   value="goBack"/>
	
	<jsp:param name="name"     value="forward"/>
	<jsp:param name="tooltip"  value='forward_tip'/>
	<jsp:param name="image"    value='forward.gif'/>
	<jsp:param name="action"   value="goForward"/>
	
	<jsp:param name="name"     value=""/>
	<jsp:param name="tooltip"  value=""/>
	<jsp:param name="image"    value=""/>
	<jsp:param name="action"   value=""/>
	
	<jsp:param name="name"     value="synch"/>
	<jsp:param name="tooltip"  value='Synch'/>
	<jsp:param name="image"    value="synch_toc_nav.gif"/>
	<jsp:param name="action"   value="resynch"/>
	
	<jsp:param name="name"     value="add_bkmrk"/>
	<jsp:param name="tooltip"  value='BookmarkPage'/>
	<jsp:param name="image"    value="add_bkmrk.gif"/>
	<jsp:param name="action"   value="bookmarkPage"/>

	<jsp:param name="name"     value="print"/>
	<jsp:param name="tooltip"  value='Print'/>
	<jsp:param name="image"    value="print_edit.gif"/>
	<jsp:param name="action"   value="printContent"/>

</jsp:include>
<%
	} else {
%>
<jsp:include page="toolbar.jsp">
	<jsp:param name="view" value="contents"/>

	<jsp:param name="name"     value="back"/>
	<jsp:param name="tooltip"  value='back_tip'/>
	<jsp:param name="image"    value='back.gif'/>
	<jsp:param name="action"   value="goBack"/>
	
	<jsp:param name="name"     value="forward"/>
	<jsp:param name="tooltip"  value='forward_tip'/>
	<jsp:param name="image"    value='forward.gif'/>
	<jsp:param name="action"   value="goForward"/>
	
	<jsp:param name="name"     value=""/>
	<jsp:param name="tooltip"  value=""/>
	<jsp:param name="image"    value=""/>
	<jsp:param name="action"   value=""/>
	
	<jsp:param name="name"     value="synch"/>
	<jsp:param name="tooltip"  value='Synch'/>
	<jsp:param name="image"    value="synch_toc_nav.gif"/>
	<jsp:param name="action"   value="resynch"/>

	<jsp:param name="name"     value="print"/>
	<jsp:param name="tooltip"  value='Print'/>
	<jsp:param name="image"    value="print_edit.gif"/>
	<jsp:param name="action"   value="printContent"/>

</jsp:include>

<%
	}
%>