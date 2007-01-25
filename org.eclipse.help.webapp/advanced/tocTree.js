/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

// Tree code specific to the help toc

/*
 * Returns the currently selected topic's href, or null if no
 * topic is selected.
 */
function getSelectedTopic() {
	var node = getActiveAnchor();
	if (node != null) {
		var href = node.href;
		var index = href.indexOf("/topic/");
		if (index != -1) {
			return href.substring(index + 6);
		}
		index = href.indexOf("/nav/");
		if (index != -1) {
			return "/.." + href.substring(index);
		}
	}
	// no selection
	return null;
}

function selectTopic(topic)
{
    var indexAnchor=topic.indexOf('#');
	var parameters;			
	if (indexAnchor!=-1) {
		var anchor=topic.substr(indexAnchor+1);
		topic=topic.substr(0,indexAnchor);
		parameters = "?topic="+topic+"&anchor="+anchor;	
	} else {
		parameters = "?topic="+topic;
	}
	makeNodeRequest(parameters);	
    return true;
}

function selectTopicById(topic) {
    // TODO is this ever called?
    // alert("Select topic by ID: " + topic);
    return true;
}

function collapseAll() {
    window.location.replace("tocView.jsp");
    return true;
}

function setShowAll(isShowAll, href) {
    var showAllParam = isShowAll ? "on" : "off";
    window.location.replace("tocView.jsp?showAll=" + showAllParam);
}

function setImage(imageNode, image) {
    var imageFile = imagesDirectory + "/" + image + ".gif";
    imageNode.src = imageFile;
    if (image == "plus") {
        imageNode.alt = altPlus;       
    } else if (image == "minus") {
        imageNode.alt = altMinus;     
    } else if (image == "toc_open") {
        imageNode.alt = altBookOpen;    
    } else if (image == "toc_closed") {
        imageNode.alt = altBookClosed;  
    } else if (image == "container_obj") {
        imageNode.alt = altContainer;  
    } else if (image == "container_topic") {
        imageNode.alt = altContainerTopic;  
    } else if (image == "topic") {
        imageNode.alt = altTopic;
    } else {
        imageNode.alt = "";
    }
}

function updateImage(imageNode, isExpanded) {
    var src = imageNode.src;
    if (isExpanded) {   
        if (src.match( /toc_closed.gif$/)) {
            setImage(imageNode, "toc_open");
        }
    } else {
        if (src.match( /toc_open.gif$/)) {           
            setImage(imageNode, "toc_closed");
        }
    }
}

function loadChildren(treeItem) { 
    var parameters = "";
    if (treeItem !== null  && treeItem.nodeid !== null) {
        setLoadingMessage(treeItem, loadingMessage);
        var topAncestor = getTopAncestor(treeItem);
        parameters += "?toc=";
        parameters += topAncestor.nodeid;
        if (topAncestor !== treeItem) {
            parameters += "&path=";
            parameters += treeItem.nodeid;
        }
    }
    makeNodeRequest(parameters);
}

function makeNodeRequest(parameters) {
    var href = "../tocfragment" + parameters;
    var callback = function(xml) { updateTree(xml);}; 
    var errorCallback = function() { 
        // alert("ajax error"); 
    };
    ajaxRequest(href, callback, errorCallback);
}

if (isInternetExplorer){
   document.onclick = mouseClickHandler;
   document.onmousemove = mouseMoveHandler;
   document.onkeydown = keyDownHandler;
} else {
   document.addEventListener('click', mouseClickHandler, true);
   document.addEventListener('mousemove', mouseMoveHandler, true);
   document.addEventListener('keydown', keyDownHandler, true);
}
