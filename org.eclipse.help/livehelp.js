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
	// construct the proper url for communicating with the server
	var url= window.location.href;
	var i = url.indexOf("content/help:");
	if(i < 0)
		i = url.lastIndexOf("/")+1;

	url=url.substring(0, i);
	url=url+"livehelp/?pluginID="+pluginId+"&class="+className+"&arg="+escape(argument)+"&nocaching="+Math.random();

	// this script can be called by content page or by our jsp pages. 
	// we need to find the toolbar frame.
	// to do: cleanup this, including the location of the hidden livehelp frame.

	// navigate to top help frameset
	for (var x=self; x; x = x.parent)
		if (x.EclipseHelpSystem)
			break;
	
	if (x == null) 
		return;
		
	var toolbarFrame = x.ToolbarFrame;
	if (!toolbarFrame)
		return;

	if(toolbarFrame.liveHelpFrame){
		toolbarFrame.liveHelpFrame.location=url;
	} else if(toolbarFrame.document && toolbarFrame.document.liveHelpFrame){
		toolbarFrame.document.liveHelpFrame.src=url;
	} 
}


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
	var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
	var isIE = navigator.userAgent.indexOf('MSIE') != -1;

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
