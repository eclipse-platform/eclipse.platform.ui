/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
function liveAction(pluginId, className, argument)
{
		var url=""+location;
		var i=url.indexOf("content/help:");
		if(i<0)
			return;
		var url=url.substring(0, i);
		url=url+"livehelp/?pluginID="+pluginId+"&class="+className+"&arg="+escape(argument)+"&nocaching="+Math.random();
		if(parent && parent.ToolbarFrame && parent.ToolbarFrame.liveHelpFrame)
			parent.ToolbarFrame.liveHelpFrame.location=url;
}

