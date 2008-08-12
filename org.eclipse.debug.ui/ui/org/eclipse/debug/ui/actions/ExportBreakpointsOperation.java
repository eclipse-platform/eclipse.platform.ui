/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui.actions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
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
 * Exports breakpoints to a file or string buffer.
 * <p>
 * This class may be instantiated.
 * <p>
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExportBreakpointsOperation implements IRunnableWithProgress {

	private IBreakpoint[] fBreakpoints = null;
	/**
	 * Only one of file name or writer is used depending how the operation is
	 * created.
	 */
	private String fFileName = null;
	private StringWriter fWriter = null;
	
	/**
	 * Constructs an operation to export breakpoints to a file.
	 * 
	 * @param breakpoints the breakpoints to export
	 * @param fileName absolute path of file to export breakpoints to - the file
	 * 	will be overwritten if it already exists
	 */
	public ExportBreakpointsOperation(IBreakpoint[] breakpoints, String fileName) {
		fBreakpoints = breakpoints;
		fFileName = fileName;
	}

	/**
	 * Constructs an operation to export breakpoints to a string buffer. The buffer
	 * is available after the operation is run via {@link #getBuffer()}.
	 * 
	 * @param breakpoints the breakpoints to export
	 * @since 3.5
	 */
	public ExportBreakpointsOperation(IBreakpoint[] breakpoints) {
		fBreakpoints = breakpoints;
		fWriter = new StringWriter();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		XMLMemento memento = XMLMemento.createWriteRoot(IImportExportConstants.IE_NODE_BREAKPOINTS); 
		monitor.beginTask(ImportExportMessages.ExportOperation_0, fBreakpoints.length);
		try {
			for (int i = 0; i < fBreakpoints.length; i++) {
				IBreakpoint breakpoint = fBreakpoints[i];
				//in the event we are in working set view, we can have multiple selection of the same breakpoint
				//so do a simple check for it
				IMarker marker = breakpoint.getMarker();
				IMemento root = memento.createChild(IImportExportConstants.IE_NODE_BREAKPOINT);
				root.putString(IImportExportConstants.IE_BP_ENABLED, Boolean.toString(breakpoint.isEnabled()));
				root.putString(IImportExportConstants.IE_BP_REGISTERED, Boolean.toString(breakpoint.isRegistered()));
				root.putString(IImportExportConstants.IE_BP_PERSISTANT, Boolean.toString(breakpoint.isPersisted()));
				//write out the resource information
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
			Writer writer = fWriter;
			if (writer == null) {
				writer =new OutputStreamWriter(new FileOutputStream(fFileName), "UTF-8"); //$NON-NLS-1$
			} 
			memento.save(writer);
			writer.close();
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	/**
	 * Returns a string buffer containing a memento of the exported breakpoints
	 * or <code>null</code> if the operation was configured to export to a file.
	 * The memento can be used to import breakpoints into the workspace using an
	 * {@link ImportBreakpointsOperation}.
	 * 
	 * @return a string buffer containing a memento of the exported breakpoints
	 * or <code>null</code> if the operation was configured to export to a file
	 * @since 3.5
	 */
	public StringBuffer getBuffer() {
		if (fWriter != null) {
			return fWriter.getBuffer();
		}
		return null;
	}
	
	
}
