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

import java.util.ListIterator;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.preferences.Preferences;


public class TargetNode extends TreeNode {
    public TargetNode(String filename, Target target) { 
    	super(target.getName());
		setProperty("TargetObject", target);
		setProperty("BuildFile", filename);
		if (null != target.getDescription()) 
			setProperty("Description", target.getDescription());
		Project project = target.getProject();
        if (target.getName().equals(project.getDefaultTarget())) { 
        	setProperty("isDefaultTarget", "True");
        } else {
        	setProperty("isDefaultTarget", "False");
        }
		Vector topoSort = project.topoSort(target.getName(), project.getTargets());
		int n = topoSort.indexOf(target) + 1;
		while (topoSort.size() > n) 
			topoSort.remove(topoSort.size() - 1);
		topoSort.trimToSize();
	    setProperty("TopoVector", topoSort);        
    }

    public void setSelected() {
       if (isSelected())  
       	   setSelected(false);
       else 
       	   setSelected(true);    
    } 
    
	public void setSelected(boolean selected) {
	   Vector targetVector = (Vector) getRoot().getProperty("TargetVector");

       if (null != targetVector) {
          if (selected) {
           	 targetVector.add(this);
          } else {
        	 targetVector.remove(this);
          }
       }
	}
	 
	public boolean isSelected() { 
	   Vector targetVector = (Vector) getRoot().getProperty("TargetVector");
	   return targetVector.contains(this);
	}
			  
	public Image getImage() {	
		if (isSelected()) {
	      return ResourceMgr.getImage(IMAGE_TARGET_SELECTED);
		} else {        		   
		  return ResourceMgr.getImage(IMAGE_TARGET_DESELECTED);
		}
	}
	
	public String decorateText(String text) {
        // Text 
        text = ResourceMgr.getString("Tree.Unknown");
        String prefTargetDisplay = Preferences.getString(PREF_TARGET_DISPLAY);
        String description = (String) getProperty("Description");
        if (prefTargetDisplay.equals(TARGET_DISPLAY_DESCATTR) && null != description) 
           text = description;
	    if (prefTargetDisplay.equals(TARGET_DISPLAY_NAMEATTR)) 
	       text = super.getText();
	    if (prefTargetDisplay.equals(TARGET_DISPLAY_BOTH)) 
	       text = super.getText() 
	    	    + " " 
	    	    + "["
            	+ (null != description ? description : "")
	    	    + "]";
        // Decorate Text        
 		if (((String)getProperty("isDefaultTarget")).equals("True")) {
				text += " "
				  	 + "("
					 + ResourceMgr.getString("Tree.DefaultTarget")
					 + ")";
		}
		TreeNode treeRoot = getRoot();
		Vector targetVector = (Vector) treeRoot.getProperty("TargetVector");
		if (null == targetVector) {
			return text;
		}
		if (isSelected()) {
			int n = targetVector.indexOf(this);
			if (n >= 0) {
				text += " " + "[" + (n + 1) + "]";
			}
		} else { 		
			Target target = (Target) getProperty("TargetObject");
			ListIterator targets = targetVector.listIterator();
			while (targets.hasNext()) {
               TreeNode targetItem = (TreeNode) targets.next();
               Vector topoVector = (Vector) targetItem.getProperty("TopoVector");
			   if (topoVector.contains(target)) {
				  	text += " " + "[" + "*" + "]";
				  	return text;
			   }			   	    
			}	
		}
		return text;		
	}
}
