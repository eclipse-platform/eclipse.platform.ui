/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.preferences.Preferences;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.TargetNode;

public class AntViewFilter extends ViewerFilter implements IAntViewConstants {
     public AntViewFilter() { 
     	 super();
     }
     public boolean select(Viewer viewer, Object parentElement, Object element)  {
         if (element instanceof TargetNode) { 
        	if (TARGET_FILTER_DESCATTR.equals(Preferences.getString(PREF_TARGET_FILTER))) {
				if (null == ((TargetNode) element).getProperty("Description")) 
				   return false;
			}
         }
         return true;
     }
}
