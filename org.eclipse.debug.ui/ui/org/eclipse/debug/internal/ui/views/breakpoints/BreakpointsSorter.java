/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class BreakpointsSorter extends ViewerSorter {
		/**
		 * @see ViewerSorter#isSorterProperty(Object, String)
		 */
		public boolean isSorterProperty(Object element,String propertyId) {
			return propertyId.equals(IBasicPropertyConstants.P_TEXT);
		}
		
		/**
		 * Returns a negative, zero, or positive number depending on whether
		 * the first element is less than, equal to, or greater than
		 * the second element.
		 * <p>
		 * Group breakpoints by debug model
		 * 	within debug model, group breakpoints by type 
		 * 		within type groups, sort by line number (if applicable) and then
		 * 		alphabetically by label
		 * 
		 * @param viewer the viewer
		 * @param e1 the first element
		 * @param e2 the second element
		 * @return a negative number if the first element is less than the 
		 *  second element; the value <code>0</code> if the first element is
		 *  equal to the second element; and a positive number if the first
		 *  element is greater than the second element
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
	
			IBreakpoint b1= (IBreakpoint)e1;
			IBreakpoint b2= (IBreakpoint)e2;
			String modelId1= b1.getModelIdentifier();
			String modelId2= b2.getModelIdentifier();
			int result= modelId1.compareTo(modelId2);
			if (result != 0) {
				return result;
			}
			String type1= ""; //$NON-NLS-1$
			String type2= ""; //$NON-NLS-1$
			IMarker marker1= b1.getMarker();
			if (!marker1.exists()) {
				return 0;
			}
			try {
				type1= marker1.getType();
			} catch (CoreException ce) {
				DebugUIPlugin.log(ce);
			}
			try {
				IMarker marker2= b2.getMarker();
				if (!marker2.exists()) {
					return 0;
				}
				type2= marker2.getType();	
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		
			result= type1.compareTo(type2);
			if (result != 0) {
				return result;
			}
			// model and type are the same
		
			ILabelProvider lprov = (ILabelProvider) ((StructuredViewer)viewer).getLabelProvider();
			String name1= lprov.getText(e1);
			String name2= lprov.getText(e2);
	
			boolean lineBreakpoint= false;
			try {
				lineBreakpoint= marker1.isSubtypeOf(IBreakpoint.LINE_BREAKPOINT_MARKER);
			} catch (CoreException ce) {
				DebugUIPlugin.log(ce);
			}
			if (lineBreakpoint) {
				return compareLineBreakpoints(b1, b2, name1,name2);
			} 
			
			return name1.compareTo(name2);		
		}
		
		protected int compareLineBreakpoints(IBreakpoint b1, IBreakpoint b2, String name1, String name2) {
			int colon1= name1.indexOf(':');
			if (colon1 != -1) {
				int colon2= name2.indexOf(':');
				if (colon2 != -1) {
					String upToColon1= name1.substring(0, colon1);
					if (name2.startsWith(upToColon1)) {
						int l1= 0;
						int l2= 0;
						try {
							l1= ((ILineBreakpoint)b1).getLineNumber();	
						} catch (CoreException e) {
							DebugUIPlugin.log(e);
						}
						try {
							l2= ((ILineBreakpoint)b2).getLineNumber();	
						} catch (CoreException e) {
							DebugUIPlugin.log(e);
						}
						return l1 - l2;
					}
				}
			}
			return name1.compareTo(name2);
		}
	}
