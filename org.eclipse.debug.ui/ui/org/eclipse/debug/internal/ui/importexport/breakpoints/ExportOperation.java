/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * Performs the export operation for the breakpoint export wizard
 * 
 * @see WizardExportBreakpointsPage
 * @since 3.2
 */
public class ExportOperation implements IRunnableWithProgress {

	private Object[] fBreakpoints = null;
	private IPath fPath = null;
	private boolean fExists = false;
	
	/**
	 * Default constructor
	 * @param viewer the viewer where we get the breakpoints from to export
	 * @param path the path.file to export the breakpoints to
	 * @param autoOverwrite if we should automatically overwrite an existing file
	 */
	public ExportOperation(Object[] breakpoints, IPath path, boolean exists) {
		fBreakpoints = breakpoints;
		fPath = path;
		fExists = exists;
	}//end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		XMLMemento memento = XMLMemento.createWriteRoot(IImportExportConstants.IE_NODE_BREAKPOINTS); 
		IBreakpoint breakpoint = null;
		IMarker marker = null;
		IMemento root = null, child = null;
		IResource resource = null;
		monitor.beginTask(ImportExportMessages.ExportOperation_0, fBreakpoints.length);
		try {
			for (int i = 0; i < fBreakpoints.length; i++) {
				if(fBreakpoints[i] instanceof IBreakpoint) {
					breakpoint = (IBreakpoint)fBreakpoints[i];
				//in the event we are in workingset view, we can have multiple selection of the same breakpoint
				//so do a simple check for it
					marker = breakpoint.getMarker();
					root = memento.createChild(IImportExportConstants.IE_NODE_BREAKPOINT);
					root.putString(IImportExportConstants.IE_BP_ENABLED, Boolean.toString(breakpoint.isEnabled()));
					root.putString(IImportExportConstants.IE_BP_REGISTERED, Boolean.toString(breakpoint.isRegistered()));
					root.putString(IImportExportConstants.IE_BP_PERSISTANT, Boolean.toString(breakpoint.isPersisted()));
				//write out the resource info
					resource = marker.getResource();
					child = root.createChild(IImportExportConstants.IE_NODE_RESOURCE);
					child.putString(IImportExportConstants.IE_NODE_PATH, resource.getFullPath().toPortableString());
					child.putInteger(IImportExportConstants.IE_NODE_TYPE, resource.getType());
				//a generalized (name, value) pairing for attributes each stored as an ATTRIB element
					root = root.createChild(IImportExportConstants.IE_NODE_MARKER);
					root.putString(IImportExportConstants.IE_NODE_TYPE, marker.getType());
					root.putString(IImportExportConstants.TYPENAME, marker.getAttribute(IImportExportConstants.TYPENAME).toString());
					Object val = marker.getAttribute(IMarker.LINE_NUMBER);
					root.putString(IMarker.LINE_NUMBER, (val != null) ? val.toString() : null);
					val = marker.getAttribute(IImportExportConstants.CHARSTART); 
					root.putString(IImportExportConstants.CHARSTART, (val != null) ? val.toString() : null);
					for(java.util.Iterator iter = marker.getAttributes().keySet().iterator(); iter.hasNext();) {
						String iterval = iter.next().toString();
						if(!iterval.equals(IMarker.LINE_NUMBER) & !iterval.equals(IImportExportConstants.TYPENAME)) {
							child = root.createChild(IImportExportConstants.IE_NODE_ATTRIB);
							child.putString(IImportExportConstants.IE_NODE_NAME, iterval);
							child.putString(IImportExportConstants.IE_NODE_VALUE, marker.getAttribute(iterval).toString());
						}//end if
					}//end for
				}// end if
			}//end for
		}//end try
		catch(CoreException e) {throw new InvocationTargetException(e);}
		try {
			if(fExists) {
				memento.save(new FileWriter(fPath.toPortableString()));
			}//end if
			else {
				if(fPath.toFile().createNewFile()) {
					memento.save(new FileWriter(fPath.toPortableString()));
				}//end if
			}//end else
		}// end try
		catch (java.io.IOException ioe) {
			DebugPlugin.log(ioe);
			throw new InvocationTargetException(ioe);
		}//end catch
	}//end run
}//end class
