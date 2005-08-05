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
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

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
	 * @param document the document breakpoints are being set in or 
	 * <code>null</code> when the document should be derived from the
	 * given part
	 * @param rulerInfo specifies location the user has double-clicked 
	 */
	public ToggleBreakpointAction(IWorkbenchPart part, IDocument document, IVerticalRulerInfo rulerInfo) {
		super(ActionMessages.ToggleBreakpointAction_0); 
		fPart = part;
		fDocument = document;
		fRulerInfo = rulerInfo;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IDocument document= getDocument();
		if (document == null) {
			return;
		}
		IToggleBreakpointsTarget adapter = (IToggleBreakpointsTarget) fPart.getAdapter(IToggleBreakpointsTarget.class);
		if (adapter == null) {
			// attempt to force load adapter
			IAdapterManager manager = Platform.getAdapterManager();
			if (manager.hasAdapter(fPart, IToggleBreakpointsTarget.class.getName())) {
				adapter = (IToggleBreakpointsTarget) manager.loadAdapter(fPart, IToggleBreakpointsTarget.class.getName());
			}
		}
		if (adapter == null) {
			return;
		}
		int line = fRulerInfo.getLineOfLastMouseButtonActivity();
		
		// Test if line is valid 
		if (line == -1)
			return;

		/*
		 * XXX: remove once the following bug is fixed:
		 * 		https://bugs.eclipse.org/bugs/show_bug.cgi?id=99234
		 */ 
		if (line >= document.getNumberOfLines())
			return;
		
		try {
			IRegion region = document.getLineInformation(line);
			ITextSelection selection = new TextSelection(document, region.getOffset(), 0);
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
		DebugUIPlugin.errorDialog(fPart.getSite().getShell(), ActionMessages.ToggleBreakpointAction_1, ActionMessages.ToggleBreakpointAction_2, e); // 
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

	/**
	 * Returns the document on which this action operates.
	 * 
	 * @return the document or <code>null</code> if none
	 */
	private IDocument getDocument() {
		if (fDocument != null)
			return fDocument;
		
		if (fPart instanceof ITextEditor) {
			ITextEditor editor= (ITextEditor)fPart;
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null)
				return provider.getDocument(editor.getEditorInput());
		}
		
		IDocument doc = (IDocument) fPart.getAdapter(IDocument.class);
		if (doc != null) {
			return doc;
		}
		
		return null;
	}

}
