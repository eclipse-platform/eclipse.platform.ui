/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.actions;

import org.eclipse.ant.internal.launching.debug.IAntDebugConstants;
import org.eclipse.ant.internal.launching.debug.model.AntLineBreakpoint;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

public class ToggleLineBreakpointAction implements IToggleBreakpointsTarget {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		IEditorPart editorPart = (IEditorPart)part;
		IEditorInput editorInput = editorPart.getEditorInput();
		IResource resource= null;
		if (editorInput instanceof IFileEditorInput) {
			resource= ((IFileEditorInput)editorInput).getFile();
		}
		if (resource == null) {
			Display.getCurrent().beep();
            return;
		}
		
		ITextSelection textSelection = (ITextSelection) selection;
		int lineNumber = textSelection.getStartLine();
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IAntDebugConstants.ID_ANT_DEBUG_MODEL);
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			if (resource.equals(breakpoint.getMarker().getResource())) {
				if (((ILineBreakpoint)breakpoint).getLineNumber() == (lineNumber + 1)) {
					DebugUITools.deleteBreakpoints(new IBreakpoint[] { breakpoint }, part.getSite().getShell(), null);
					return;
				}
			}
		}
		// create line breakpoint (doc line numbers start at 0)
		new AntLineBreakpoint(resource, lineNumber + 1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}
}
