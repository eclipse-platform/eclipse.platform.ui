/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;

/**
 * An action to delete a whole line, to the beginning or end of a line.
 */
public class DeleteLineAction extends TextEditorAction {

	/** Delete the whole line. */
	public static final int WHOLE= 0;
	/** Delete to the beginning of line. */
	public static final int TO_BEGINNING= 1;
	/** Delete to the end of line. */
	public static final int TO_END= 2;

	/** The type of deletion */
	private final int fType;

	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 */
	public DeleteLineAction(ResourceBundle bundle, String prefix, ITextEditor editor, int type) {
		super(bundle, prefix, editor);
		fType= type;		
	}

	private static IDocument getDocument(ITextEditor editor) {

		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider == null)
			return null;

		IDocument document= documentProvider.getDocument(editor.getEditorInput());
		if (document == null)
			return null;	
			
		return document;
	}
	
	private static ITextSelection getSelection(ITextEditor editor) {

		ISelectionProvider selectionProvider= editor.getSelectionProvider();
		if (selectionProvider == null)
			return null;
		
		ISelection selection= selectionProvider.getSelection();
		if (!(selection instanceof ITextSelection))
			return null;
		
		return (ITextSelection) selection;
	}
	
	/*
	 * @see IAction#run()
	 */
	public void run() {

		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;

		IDocument document= getDocument(editor);
		if (document == null)
			return;
			
		ITextSelection selection= getSelection(editor);
		if (selection == null)
			return;
		
		try {
			deleteLine(document, selection.getOffset(), fType);
		} catch (BadLocationException e) {
			// should not happen			
		}
	}
	
	private static void deleteLine(IDocument document, int position, int type) throws BadLocationException {

		int line= document.getLineOfOffset(position);
		int offset= 0;
		int length= 0;

		switch  (type) {
		case WHOLE:
			offset= document.getLineOffset(line);
			length= document.getLineLength(line);
			break;

		case TO_BEGINNING:
			offset= document.getLineOffset(line);
			length= position - offset;
			break;

		case TO_END:		
			offset= position;

			IRegion lineRegion= document.getLineInformation(line);
			int end= lineRegion.getOffset() + lineRegion.getLength();

			if (position == end) {
				String lineDelimiter= document.getLineDelimiter(line);
				length= lineDelimiter == null ? 0 : lineDelimiter.length();

			} else {
				length= end - offset;
			}
			break;
						
		default:
			return;
		}
		
		if (length != 0)
			document.replace(offset, length, ""); //$NON-NLS-1$
	}

}
