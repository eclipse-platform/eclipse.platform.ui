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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A text selection in an editor created by a maker (only the line number was available)
 */
public class MarkerTextSelection extends TextEditorSelection {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.launch.Decoration#remove()
	 */
	public void remove() {
		// Get the current text selection in the editor.  If there is none, 
		// then there's nothing to do
		ISelectionProvider provider = fEditor.getSelectionProvider();
		if (provider == null) {
			return;
		}
		ITextSelection textSelection= (ITextSelection)provider.getSelection();
		if (textSelection.isEmpty()) {
			return;
		}
		int startChar= textSelection.getOffset();
		int startLine= textSelection.getStartLine();
		
		// Check to see if the current selection looks the same as the last.
		// If not, it must be a user selection, which we leave alone.
		if (startLine == fLine) {
			ITextSelection nullSelection= getNullSelection(startLine, startChar);
			fEditor.getSelectionProvider().setSelection(nullSelection);					
		}
		
	}

	/**
	 * Constructs a new text selection decoration.
	 * 
	 * @param editor
	 * @param lineNumber
	 * @param thread
	 */
	public MarkerTextSelection(ITextEditor editor, int lineNumber, IThread thread) {
		super(editor, lineNumber, -1, -1, thread);
	}
}
