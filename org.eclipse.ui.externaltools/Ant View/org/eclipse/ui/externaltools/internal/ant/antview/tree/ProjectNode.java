/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.tree;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.preferences.Preferences;

public class ProjectNode extends TreeNode {
    public ProjectNode(String filename, String name) { 
    	super(name);             
        setProperty("BuildFile", filename);
        
        IPath path = new Path(filename);	
        path = path.setDevice("");
        int trimCount = path.matchingFirstSegments(Platform.getLocation());            
		if (trimCount > 0) 
		   path = path.removeFirstSegments(trimCount);
		path.removeLastSegments(1);
		setProperty("DisplayPath", path.toString());    
    }
        
	public Image getImage() {		          		   
		return ResourceMgr.getImage(IMAGE_PROJECT);
	}
	
	public String decorateText(String text) {
       String prefProjDisplay = Preferences.getString(PREF_PROJECT_DISPLAY);
       if (prefProjDisplay.equals(PROJECT_DISPLAY_DIRLOC)) 
      	   return (String) getProperty("DisplayPath"); 
	   if (prefProjDisplay.equals(PROJECT_DISPLAY_NAMEATTR)) 
	       return super.getText();
	   if (prefProjDisplay.equals(PROJECT_DISPLAY_BOTH)) 
	  	   return (String) getProperty("DisplayPath") 
	    	              + " " 
	    	              + "["
	    	              + super.getText()
	    	              + "]";	
       return ResourceMgr.getString("Tree.Unknown");
	}
}
