/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

// Code to handle the expansion, contraction and navigation
// of nodes in a tree.

var isIE = navigator.userAgent.indexOf('MSIE') != -1;

/**
 * Returns the target node of an event
 */
function getTarget(e) {
	var target;
  	if (isIE) {
   		target = window.event.srcElement; 
   	} else {  	
  		target = e.target;
  	}

	return target;
}

/**
 * handler for expanding / collapsing topic tree
 */
function mouseClickHandler(e) {

  	var clickedNode = getTarget(e);
	if (!clickedNode) { return; }
	
	// Is it an expander node?
	
	if (clickedNode.className == "expander") {
	    toggleExpandState(clickedNode);
	}
  	
  	if (isIE) {
  		window.event.cancelBubble = true;
  	} else {	
  		e.cancelBubble = true;
  	}
}

/**
 * Handler for key down (arrows)
 */
function keyDownHandler(e)
{
	var key;

	if (isIE) {
		key = window.event.keyCode;
	} else if (isMozilla) {
		key = e.keyCode;
	}
		
	if ( key <37 || key > 40) {
		return true;
	}
		
	var clickedNode = getTarget(e);
	var treeItem = getTreeItem(clickedNode);
    if (!treeItem) { return true; }
	
  	if (isIE) {
  		window.event.cancelBubble = true;
  	} else {	
  		e.cancelBubble = true;
  	}
  	
  	if (key == 37) { // Left arrow, collapse
  	    goLeft(treeItem);
  	} else if (key == 38 ) { // Down arrow, go down
  	    goUp(treeItem);
  	} if (key == 39) { // Right arrow, expand
  	    goRight(treeItem);
  	} else if (key == 40 ) { // Down arrow, go down
  		goDown(treeItem);
  	}
  				
  	return true;
}

// Handle a left arrow key event
function goLeft(treeItem) {  
    var childClass = getChildClass(treeItem); 
    if (childClass == "visible") {
        toggleExpandState(treeItem);
     } else {
         focusOnItem(getTreeItem(treeItem.parentNode));
     }
}

function goRight(treeItem) {     
    var childClass = getChildClass(treeItem);
    if (childClass == "hidden" || childClass == "unopened") {
        toggleExpandState(treeItem);
        return;
     }       
     focusOnItem(findChild(treeItem, "DIV"));
}

function goUp(treeItem) {
   // If there is a previous sibling, visit it's last child
   // otherwise focus on the parent
  
   for (var prev = treeItem.previousSibling; prev !== null; prev = prev.previousSibling) {
        if (prev.tagName == "DIV") {
            focusOnDeepestVisibleChild(prev);
            return;
        }
    } 
    focusOnItem(getTreeItem(treeItem.parentNode));
}

function goDown(treeItem) {
    // If the node is expanded visit the first child       
    var childClass = getChildClass(treeItem);
    if (childClass == "visible") {
        focusOnItem(findChild(treeItem, "DIV"));
        return;
    }
    // visit the next sibling at this level, if not found try highter levels
    for (var level = treeItem; level !== null; level = getTreeItem(level.parentNode)) {
        for (var next = level.nextSibling; next !== null; next = next.nextSibling) {
            if (next.tagName == "DIV") {
                focusOnItem(next);
                return;
            }
        }
    }   
}

function focusOnDeepestVisibleChild(treeItem) { 
        var childDiv = findLastChild(treeItem, "DIV");
        if (childDiv) {  
            if (childDiv.className == "visible") {        
                focusOnDeepestVisibleChild(childDiv);
                return;
            }
        }
    focusOnItem(treeItem);
}

// Focus on the anchor within a tree item
function focusOnItem(treeItem) {
    if (treeItem === null) { return; }
    // Items with children will use a span to contain the anchor
    var anchorContainer = findChild(treeItem, "SPAN");
    if (!anchorContainer) { anchorContainer = treeItem; }
    var anchor = findChild(anchorContainer, "A");
    if (anchor) {
        anchor.focus();
    }
}

// The tree consists of tree items which can be nested or in sequences
// Each tree item is a DIV node which contains an expander for non-leaf nodes as
// well as an anchor containing an image and text. Non-leaf nodes use a <SPAN> to contain
// the expander and anchor to prevent line breaking, they also contain
// a DIV to hold their children. 

function getTreeItem(node) {
    if (node === null) {
        return null;
    }
    for (var candidate = node; candidate !== null; candidate = candidate.parentNode) {
        if (candidate.tagName == "DIV") {
            var className = candidate.className;
            if (className == "visible" || className == "hidden" || className == "unopened" || className == "root") {
                return candidate;
            }
        }
    }
    return null;
}

// Get the top level item in this tree
// Do this by visiting parent nodes until we hit the root of the tree
function getTopAncestor(node) {
    var candidate = node;
    var next = candidate; 
    while (next !== null && next.className != "root") {
        candidate = next;
        next = getTreeItem(candidate.parentNode);
        if (next.className == "root") {   
            return next;
        }
    }
    return candidate;
}

// The type of a treeItem can be determined from the class of it's DIV children.
// The possible return values are leaf, visible, hidden, unopened
// If there are no children this is a leaf, otherwise return the class of the first child 

function getChildClass(node) {
    var child = findChild(node, "DIV");
    if (child) { return child.className; }
    return "leaf";
}

// Get the image node from a DIV representing a tree item
function getIcon(treeItem) {
    var anchorContainer = findChild(treeItem, "SPAN");
    if (!anchorContainer) { anchorContainer = treeItem; }
    var anchor = findChild(anchorContainer, "A");
    if (anchor) {
        return findChild(anchor, "IMG");
    }
    return null;
}

// Return the first child with this tag
function findChild(node, tag) {
    var children = node.childNodes;
    for (var i = 0; i < children.length; i++) {
        if (tag == children[i].tagName ) {
            return children[i];
        }
    }
    return null;
}

// Return the last child with this tag
function findLastChild(node, tag) {
    var children = node.childNodes;
    for (var i = children.length - 1; i >= 0; i--) {
        if (tag == children[i].tagName ) {
            return children[i];
        } 
    }
    return null;
}

function getExpander(treeItem) {    
    var imgContainer = findChild(treeItem, "SPAN");
    if (!imgContainer) { imgContainer = treeItem; }
    var children = imgContainer.childNodes;
    var done = false;
    for (var i = 0; i < children.length && !done; i++) {
        if (children[i].className == "expander") {
            return children[i];
        }
    }
    return null;
}

function toggleExpandState(node) {
    var treeItem = getTreeItem(node);
    if (!treeItem) { return; } 
    var oldChildClass = getChildClass(treeItem);
    var newChildClass;
    
    if (oldChildClass == "unopened") {
        loadChildren(treeItem);
        return;  // loadChildren is responsible for updating the expander image
    } else if (oldChildClass == "hidden") {
        newChildClass = "visible";
    } else if (oldChildClass == "visible") {
        newChildClass = "hidden";
    } else {
        return;
    }

    // set the childrens class to the new class
    var children = treeItem.childNodes;
    for (var i = 0; i < children.length; i++) {
        if ("DIV" == children[i].tagName ) {
            children[i].className = newChildClass;
        }
    }
    
    changeExpanderImage(treeItem, newChildClass == "visible");    
}

function changeExpanderImage(treeItem, isExpanded) {
    var icon = getIcon(treeItem);
    var expander = getExpander(treeItem);   
    if (expander) {
        if (isExpanded) {
            expander.src = "images/minus.gif";
        } else {
            expander.src = "images/plus.gif";
        }
    }
    if (icon) {
        setImage(icon, isExpanded);
    }
}

function mouseMoveHandler(e) {
}
