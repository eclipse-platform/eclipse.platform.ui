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
package org.eclipse.ant.internal.ui.editor.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.debug.IAntDebugConstants;
import org.eclipse.ant.internal.ui.debug.model.AntDebugElement;
import org.eclipse.ant.internal.ui.debug.model.AntLineBreakpoint;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Run to line target for the Ant debugger
 */
public class RunToLineAdapter implements IRunToLineTarget {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		IEditorPart editorPart = (IEditorPart)part;
		IEditorInput input = editorPart.getEditorInput();
		String errorMessage = null;
		if (input == null) {
			errorMessage = AntEditorActionMessages.getString("RunToLineAdapter.0"); //$NON-NLS-1$
		} else {
			ITextEditor textEditor = (ITextEditor)editorPart;
			IDocumentProvider provider= textEditor.getDocumentProvider();
			IDocument document= provider.getDocument(input);
			
			if (document == null) {
				errorMessage = AntEditorActionMessages.getString("RunToLineAdapter.1"); //$NON-NLS-1$
			} else {
				
				ITextSelection textSelection = (ITextSelection) selection;
				int lineNumber = textSelection.getStartLine() + 1;
				
				IBreakpoint breakpoint= null;
				Map attributes = getRunToLineAttributes();
				IFile file = (IFile)input.getAdapter(IFile.class);
				if (file == null) {
				    errorMessage= AntEditorActionMessages.getString("RunToLineAdapter.2"); //$NON-NLS-1$
				} else {
				    breakpoint= new AntLineBreakpoint(file, lineNumber, attributes);//JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName[0], lineNumber[0], -1, -1, 1, false, attributes);
                    breakpoint.setPersisted(false);
				    errorMessage = AntEditorActionMessages.getString("RunToLineAdapter.3"); //$NON-NLS-1$
				    if (target instanceof IAdaptable) {
				        IDebugTarget debugTarget = (IDebugTarget) ((IAdaptable)target).getAdapter(IDebugTarget.class);
				        if (debugTarget != null) {
				            prepareSkipBreakpoints(breakpoint);
				            debugTarget.getDebugTarget().breakpointAdded(breakpoint);
				            target.resume();
				            return;
				        }
				    }
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, AntUIPlugin.getUniqueIdentifier(), AntUIPlugin.INTERNAL_ERROR,
				errorMessage, null));
	}

    private Map getRunToLineAttributes() {
        Map attributes = new HashMap();
        attributes.put(IMarker.TRANSIENT, Boolean.TRUE);
        attributes.put(IAntDebugConstants.ANT_RUN_TO_LINE, Boolean.TRUE); //$NON-NLS-1$
        return attributes;
    }
	
	/**
	 * Before resuming, check if breakpoints should be skipped during
	 * this operation. If so, disable the breakpoint manager and register
	 * a listener to reenable the manager when the run to line breakpoint
	 * is hit.
	 * 
	 * @param target the target that will be resumed for this action
	 * @param breakpoint the run to line breakpoint
	 */
	protected void prepareSkipBreakpoints(final IBreakpoint breakpoint) {
		final DebugPlugin plugin= DebugPlugin.getDefault();
		final IBreakpointManager manager = plugin.getBreakpointManager();
		if (!manager.isEnabled() || !DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE)) {
			// If the BP manager is already disabled, do nothing
			return;
		}
		manager.setEnabled(false);
		final IDebugEventSetListener debugEventListener= new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				for (int i = 0; i < events.length; i++) {
					DebugEvent event= events[i];
					Object source= event.getSource();
					if (source instanceof IThread && event.getKind() == DebugEvent.SUSPEND &&
							event.getDetail() == DebugEvent.BREAKPOINT) {
						IBreakpoint[] breakpoints = ((IThread) source).getBreakpoints();
						for (int j = 0; j < breakpoints.length; j++) {
							if (breakpoints[j] == breakpoint) {
								manager.setEnabled(true);
							}
						}
					} else if (source instanceof IDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
						// Clean up if the debug target terminates without
						// hitting the breakpoint.
						manager.setEnabled(true);
					}
				}
			}
		}; 
		plugin.addDebugEventListener(debugEventListener);
		// When the breakpoint manager is enabled or disabled (either by the
		// debug event listener or by the user), stop listening to debug events.
		manager.addBreakpointManagerListener(new IBreakpointManagerListener() {
			public void breakpointManagerEnablementChanged(boolean enabled) {
				plugin.removeDebugEventListener(debugEventListener);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
	    return target instanceof AntDebugElement;
	}
}
