/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
/**
 * Call this Javascript method to trigger a specified live help action
 * in the workbench. 
 * The parameters for liveAction  are:
 * - the id of the plug-in that contains the action
 * - the name of the class that implements the action
 * - the String that will be passed to the live help action using setInitializationString
 */

function liveAction(pluginId, className, argument)
{
		var url=""+location;
		var i=url.indexOf("ns4/content/help:");
		if(i<0){
			i=url.indexOf("content/help:");
		}
		if(i<0){
			return;
		}
		var url=url.substring(0, i);
		url=url+"livehelp/?pluginID="+pluginId+"&class="+className+"&arg="+escape(argument)+"&nocaching="+Math.random();
		if(parent && parent.ToolbarFrame && parent.ToolbarFrame.liveHelpFrame){
			parent.ToolbarFrame.liveHelpFrame.location=url;
		} else if(parent && parent.ToolbarFrame && parent.ToolbarFrame.document && parent.ToolbarFrame.document.liveHelpFrame){
			parent.ToolbarFrame.document.liveHelpFrame.src=url;
		}
}

var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;


/**
 * Show specified topic in the Contents tree.
 * The topic must be passed as a URL string.
 * Example:
 *  // include the script first
 *  <script src="../org.eclipse.help/livehelp.js"></script>
 *  ......
 *  // show specified topic in the tree
 *  showTopicInContents(window.location.href); 
 */
function showTopicInContents(topic) {
	if (!isIE && !isMozilla) 
		return;
		
	try
	{
		parent.displayTocFor(topic);
	}
	catch(e)
	{
	}
}
