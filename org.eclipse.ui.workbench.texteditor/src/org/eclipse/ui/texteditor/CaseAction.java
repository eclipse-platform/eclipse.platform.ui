/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Action that converts the current selection to lower case.
 * @since 3.0
 */
public class CaseAction extends ResourceAction implements IUpdate {

	/** The editor we are working on. */
	private AbstractTextEditor fEditor;

	/** <code>true</code> if this action converts to upper case, <code>false</code> otherwise. */
	private boolean fToUpper;

	/**
	 * Creates and initializes the action for the given text editor.
	 * The action configures its visual representation from the given resource
	 * bundle. 
	 * 
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or  <code>null</code> if none
	 * @param editor the text editor
	 * @param toUpper <code>true</code> if this is an uppercase action, <code>false</code> otherwise.
	 * 
	 * @see ResourceAction#ResourceAction
	 */
	public CaseAction(ResourceBundle bundle, String prefix, AbstractTextEditor editor, boolean toUpper) {
		super(bundle, prefix);
		fEditor= editor;
		fToUpper= toUpper;
		update();
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (fEditor == null)
			return;
		
		if (!isEnabled())
			return;

		ISourceViewer viewer= fEditor.getSourceViewer();
		if (viewer == null)
			return;

		IDocument document= viewer.getDocument();
		if (document == null)
			return;
			
		StyledText st= viewer.getTextWidget();
		if (st == null)
			return;

		Point sel= viewer.getSelectedRange();
		if (sel == null)
			return;

		try {
			// if the selection is emtpy, we select the word / string using the viewer's 
			// doubleclick strategy
			if (sel.y == 0)  {
				
				// TODO find a better way to do this although there are multiple partitionings on a single document
				
//				String partition= getContentType(viewer, document, sel.x);
//				SourceViewerConfiguration svc= fEditor.getSourceViewerConfiguration(); // never null when viewer instantiated
//				ITextDoubleClickStrategy dcs= svc.getDoubleClickStrategy(viewer, partition);
//				if (dcs != null) {
//					dcs.doubleClicked(viewer);
//					sel= viewer.getSelectedRange();
//				}
 
				if (sel.y == 0)
					return;	// if the selection is still empty, we're done
			}

			String target= document.get(sel.x, sel.y);
			String replacement= (fToUpper ? target.toUpperCase() : target.toLowerCase());
			if (!target.equals(replacement)) {
				document.replace(sel.x, target.length(), replacement);
			}
		} catch (BadLocationException x) {
			// ignore and return
			return;
		}

		// reinstall selection and move it into view
		viewer.setSelectedRange(sel.x, sel.y);
		// don't use the viewer's reveal feature in order to avoid jumping around		
		st.showSelection();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		boolean enabled= true;
		if (fEditor != null) {
			if (fEditor.isEditorInputReadOnly()) {
				enabled= false;
			}
		} else {
			enabled= false;
		}
		setEnabled(enabled);
	}
}
