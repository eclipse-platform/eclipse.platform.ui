/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui.actions;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.importexport.breakpoints.IImportExportConstants;
import org.eclipse.debug.internal.ui.importexport.breakpoints.ImportExportMessages;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * Exports breakpoints to a file. 
 * <p>
 * This class may be instantiated; not intended to be subclassed.
 * <p>
 * @since 3.2
 */
public class ExportBreakpointsOperation implements IRunnableWithProgress {

	private IBreakpoint[] fBreakpoints = null;
	private String fFileName = null;
	
	/**
	 * Constructs an operation to export breakpoints to a file.
	 * 
	 * @param breakpoints the breakpoints to export
	 * @param fileName absolute path of file to export breakpoints to - the file
	 * 	will be overritten if it already exists
	 */
	public ExportBreakpointsOperation(IBreakpoint[] breakpoints, String fileName) {
		fBreakpoints = breakpoints;
		fFileName = fileName;
	}//end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		XMLMemento memento = XMLMemento.createWriteRoot(IImportExportConstants.IE_NODE_BREAKPOINTS); 
		monitor.beginTask(ImportExportMessages.ExportOperation_0, fBreakpoints.length);
		try {
			for (int i = 0; i < fBreakpoints.length; i++) {
				IBreakpoint breakpoint = fBreakpoints[i];
				//in the event we are in workingset view, we can have multiple selection of the same breakpoint
				//so do a simple check for it
				IMarker marker = breakpoint.getMarker();
				IMemento root = memento.createChild(IImportExportConstants.IE_NODE_BREAKPOINT);
				root.putString(IImportExportConstants.IE_BP_ENABLED, Boolean.toString(breakpoint.isEnabled()));
				root.putString(IImportExportConstants.IE_BP_REGISTERED, Boolean.toString(breakpoint.isRegistered()));
				root.putString(IImportExportConstants.IE_BP_PERSISTANT, Boolean.toString(breakpoint.isPersisted()));
				//write out the resource info
				IResource resource = marker.getResource();
				IMemento child = root.createChild(IImportExportConstants.IE_NODE_RESOURCE);
				child.putString(IImportExportConstants.IE_NODE_PATH, resource.getFullPath().toPortableString());
				child.putInteger(IImportExportConstants.IE_NODE_TYPE, resource.getType());
				//a generalized (name, value) pairing for attributes each stored as an ATTRIB element
				root = root.createChild(IImportExportConstants.IE_NODE_MARKER);
				root.putString(IImportExportConstants.IE_NODE_TYPE, marker.getType());
				Object val = marker.getAttribute(IMarker.LINE_NUMBER);
				root.putString(IMarker.LINE_NUMBER, (val != null) ? val.toString() : null);
				val = marker.getAttribute(IImportExportConstants.CHARSTART); 
				root.putString(IImportExportConstants.CHARSTART, (val != null) ? val.toString() : null);
				for(java.util.Iterator iter = marker.getAttributes().keySet().iterator(); iter.hasNext();) {
					String iterval = iter.next().toString();
					if(!iterval.equals(IMarker.LINE_NUMBER)) {
						child = root.createChild(IImportExportConstants.IE_NODE_ATTRIB);
						child.putString(IImportExportConstants.IE_NODE_NAME, iterval);
						child.putString(IImportExportConstants.IE_NODE_VALUE, marker.getAttribute(iterval).toString());
					}
				}
			}
			memento.save(new FileWriter(fFileName));
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}
}
