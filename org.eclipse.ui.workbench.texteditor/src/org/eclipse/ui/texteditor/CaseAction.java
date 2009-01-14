/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 145326 [typing] toUpperCase incorrect selection
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IBlockTextSelection;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Action that converts the current selection to lower case or upper case.
 * @since 3.0
 */
public class CaseAction extends TextEditorAction {

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
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public CaseAction(ResourceBundle bundle, String prefix, AbstractTextEditor editor, boolean toUpper) {
		super(bundle, prefix, editor);
		fToUpper= toUpper;
		update();
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;

		if (!validateEditorInputState())
			return;

		ISourceViewer viewer= ((AbstractTextEditor) editor).getSourceViewer();
		if (viewer == null)
			return;

		IDocument document= viewer.getDocument();
		if (document == null)
			return;

		StyledText st= viewer.getTextWidget();
		if (st == null)
			return;

		ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

		int adjustment= 0;
		try {
			if (JFaceTextUtil.isEmpty(viewer, selection))
				return;
			
			IRegion[] ranges= JFaceTextUtil.getCoveredRanges(viewer, selection);
			if (ranges.length > 1 && viewer instanceof ITextViewerExtension)
				((ITextViewerExtension) viewer).getRewriteTarget().beginCompoundChange();
			for (int i= 0; i < ranges.length; i++) {
				IRegion region= ranges[i];
				String target= document.get(region.getOffset(), region.getLength());
				String replacement= (fToUpper ? target.toUpperCase() : target.toLowerCase());
				if (!target.equals(replacement)) {
					document.replace(region.getOffset(), region.getLength(), replacement);
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=145326: replacement might be larger than the original
					adjustment= replacement.length() - target.length();
				}
			}
			if (ranges.length > 1 && viewer instanceof ITextViewerExtension)
				((ITextViewerExtension) viewer).getRewriteTarget().endCompoundChange();
		} catch (BadLocationException x) {
			// ignore and return
			return;
		}

		// reinstall selection and move it into view
		if (!(selection instanceof IBlockTextSelection))
			viewer.setSelectedRange(selection.getOffset(), selection.getLength() + adjustment);
		else
			viewer.getSelectionProvider().setSelection(selection);
		// don't use the viewer's reveal feature in order to avoid jumping around
		st.showSelection();
	}

}
