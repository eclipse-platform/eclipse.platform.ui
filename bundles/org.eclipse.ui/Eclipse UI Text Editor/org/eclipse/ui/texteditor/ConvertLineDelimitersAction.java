package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * An action to convert line delimiters of a text editor document to a particular line delimiter.
 */
public class ConvertLineDelimitersAction extends TextEditorAction {

	/** The target line delimiter. */
	private final String fLineDelimiter;

	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param lineDelimiter the target line delimiter to convert the editor's document to
	 */
	public ConvertLineDelimitersAction(ITextEditor editor, String lineDelimiter) {
		this(EditorMessages.getResourceBundle(), "dummy", editor, lineDelimiter); //$NON-NLS-1$
	}

	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param lineDelimiter the target line delimiter to convert the editor's document to
	 */
	public ConvertLineDelimitersAction(ResourceBundle bundle, String prefix, ITextEditor editor, String lineDelimiter) {
		super(bundle, prefix, editor);
		fLineDelimiter= lineDelimiter;
	}
		
	/*
	 * @see Action#run()
	 */
	public void run() {

		try {
			ITextEditor editor= getTextEditor();

			IDocumentProvider documentProvider= editor.getDocumentProvider();			
			IDocument document= documentProvider.getDocument(editor.getEditorInput());

			convert(document, fLineDelimiter);
			
		} catch (BadLocationException x) {
		}
	}

	/**
	 * Converts all line delimiters of the document to <code>lineDelimiter</code>.
	 */
	private static void convert(IDocument document, String lineDelimiter) throws BadLocationException {

		try {
			final int lineCount= document.getNumberOfLines();
			for (int i= 0; i < lineCount; i++) {
				final String delimiter= document.getLineDelimiter(i);
				if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(lineDelimiter)) {
					IRegion region= document.getLineInformation(i);
					document.replace(region.getOffset() + region.getLength(), delimiter.length(), lineDelimiter);
				}
			}

		} catch (BadLocationException e) {
			throw e;
		}
	}
}
