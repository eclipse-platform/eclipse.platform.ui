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
package org.eclipse.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to toggle a breakpoint in a vertical ruler of a workbench part
 * containing a document. The part must provide an <code>IToggleBreakpointsTarget</code>
 * adapter which may optionally be an instance of an
 * <code>IToggleBreakpointsTargetExtension</code>.
 * <p>
 * Clients may instantiate this class. This class is not intended to be subclassed.
 * </p>
 * @since 3.1
 * @see org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate
 */
public class ToggleBreakpointAction extends Action {
	
	private IWorkbenchPart fPart;
	private IDocument fDocument;
	private IVerticalRulerInfo fRulerInfo;

	/**
	 * Constructs a new action to toggle a breakpoint in the given
	 * part containing the given document and ruler.
	 * 
	 * @param part the part in which to toggle the breakpoint - provides
	 *  an <code>IToggleBreakpointsTarget</code> adapter
	 * @param document the document breakpoints are being set in - used
	 *  to determine a line number/text range for the breakpoint. When
	 *  <code>null</code> this action will not run.
	 * @param rulerInfo specifies location the user has double-clicked 
	 */
	public ToggleBreakpointAction(IWorkbenchPart part, IDocument document, IVerticalRulerInfo rulerInfo) {
		super(ActionMessages.ToggleBreakpointAction_0); //$NON-NLS-1$
		fPart = part;
		fDocument = document;
		fRulerInfo = rulerInfo;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (fDocument == null) {
			return;
		}
		IToggleBreakpointsTarget adapter = (IToggleBreakpointsTarget) fPart.getAdapter(IToggleBreakpointsTarget.class);
		int line = fRulerInfo.getLineOfLastMouseButtonActivity();
		try {
			IRegion region = fDocument.getLineInformation(line);
			ITextSelection selection = new TextSelection(fDocument, region.getOffset(), 0);
			if (adapter instanceof IToggleBreakpointsTargetExtension) {
				IToggleBreakpointsTargetExtension extension = (IToggleBreakpointsTargetExtension) adapter;
				if (extension.canToggleBreakpoints(fPart, selection)) {
					extension.toggleBreakpoints(fPart, selection);
					return;
				}
			}
			if (adapter.canToggleLineBreakpoints(fPart, selection)) {
				adapter.toggleLineBreakpoints(fPart, selection);
			} else if (adapter.canToggleWatchpoints(fPart, selection)) {
				adapter.toggleWatchpoints(fPart, selection);
			} else if (adapter.canToggleMethodBreakpoints(fPart, selection)) {
				adapter.toggleMethodBreakpoints(fPart, selection);
			}
		} catch (BadLocationException e) {
			reportException(e);
		} catch (CoreException e) {
			reportException(e);
		}
	}
	
	/**
	 * Report an error to the user.
	 * 
	 * @param e underlying exception
	 */
	private void reportException(Exception e) {
		DebugUIPlugin.errorDialog(fPart.getSite().getShell(), ActionMessages.ToggleBreakpointAction_1, ActionMessages.ToggleBreakpointAction_2, e); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Disposes this action. Clients must call this method when
	 * this action is no longer needed.
	 */
	public void dispose() {
		fDocument = null;
		fPart = null;
		fRulerInfo = null;
	}
}
