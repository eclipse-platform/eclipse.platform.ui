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
 * A source selection that the debugger has created in an editor.
 */
public class TextEditorSelection extends Decoration {
	
	protected ITextEditor fEditor;
	protected int fLine, fOffset, fLength;
	protected IThread fThread; 

	/**
	 * Constructs a new text selection decoration.
	 * 
	 * @param editor
	 * @param offset
	 * @param length
	 * @param thread
	 */
	public TextEditorSelection(ITextEditor editor, int lineNumber, int offset, int length, IThread thread) {
		fEditor = editor;
		fOffset = offset;
		fLength = length;
		fThread = thread;
		fLine = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.launch.Decoration#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}

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
		int offset= textSelection.getOffset();
		int length= textSelection.getLength();
		
		// Check to see if the current selection looks the same.
		// If not, it must be a user selection, which we leave alone.
		if (fOffset == offset && fLength == length) {
			ITextSelection nullSelection= getNullSelection(fLine, fOffset);
			fEditor.getSelectionProvider().setSelection(nullSelection);					
		}
		
	}
	
	/**
	 * Creates and returns an ITextSelection that is a zero-length selection located at the
	 * start line and start char.
	 */
	protected ITextSelection getNullSelection(final int startLine, final int startChar) {
		return new ITextSelection() {
			public int getStartLine() {
				return startLine;
			}
			public int getEndLine() {
				return startLine;
			}
			public int getOffset() {
				return startChar;
			}
			public String getText() {
				return ""; //$NON-NLS-1$
			}
			public int getLength() {
				return 0;
			}
			public boolean isEmpty() {
				return true;
			}
		};
	}	

}
