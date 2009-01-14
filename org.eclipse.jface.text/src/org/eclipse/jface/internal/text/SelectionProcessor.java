/*******************************************************************************
 * Copyright (c) 2009 Avaloq Evolution AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Eicher (Avaloq Evolution AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BlockTextSelection;
import org.eclipse.jface.text.IBlockTextSelection;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;

/**
 * Processes {@link ITextSelection}s.
 * 
 * @since 3.5
 */
public final class SelectionProcessor {
	private static class Implementation {
		/**
		 * Returns a text edit describing the text modification that would be executed if the given
		 * selection was replaced by <code>replacement</code>.
		 * 
		 * @param selection the selection to replace
		 * @param replacement the replacement text
		 * @return a text edit describing the operation needed to replace <code>selection</code>
		 * @throws BadLocationException if computing the edit failed
		 */
		TextEdit replace(ISelection selection, String replacement) throws BadLocationException {
			return new MultiTextEdit();
		}

		/**
		 * Returns the text covered by <code>selection</code>
		 * 
		 * @param selection the selection
		 * @return the text covered by <code>selection</code>
		 * @throws BadLocationException if computing the edit failed
		 */
		String getText(ISelection selection) throws BadLocationException {
			return ""; //$NON-NLS-1$
		}

		/**
		 * Returns <code>true</code> if the text covered by <code>selection</code> does not contain any
		 * characters. Note the difference to {@link ITextSelection#isEmpty()}, which returns
		 * <code>true</code> only for invalid selections.
		 * 
		 * @param selection the selection
		 * @return <code>true</code> if <code>selection</code> does not contain any text,
		 *         <code>false</code> otherwise
		 * @throws BadLocationException if accessing the document failed
		 */
		boolean isEmpty(ISelection selection) throws BadLocationException {
			return selection.isEmpty();
		}

		/**
		 * Returns <code>true</code> if <code>selection</code> covers text on two or more lines,
		 * <code>false</code> otherwise.
		 * 
		 * @param selection the selection
		 * @return <code>true</code> if <code>selection</code> covers text on two or more lines,
		 *         <code>false</code> otherwise
		 */
		boolean isMultiline(ISelection selection) {
			return false;
		}

		TextEdit delete(ISelection selection) throws BadLocationException {
			return replace(selection, ""); //$NON-NLS-1$
		}

		TextEdit backspace(ISelection selection) throws BadLocationException {
			return replace(selection, ""); //$NON-NLS-1$
		}

		/**
		 * Returns a selection similar to <code>selection</code> but {@linkplain #isEmpty(ISelection)
		 * empty}. Typically, the selection is reduced to its left-most offset.
		 * 
		 * @param selection the selection
		 * @return an empty variant of <code>selection</code>
		 * @throws BadLocationException if accessing the document failed
		 */
		ISelection makeEmpty(ISelection selection) throws BadLocationException {
			return selection;
		}

		/**
		 * Returns the text regions covered by the given selection.
		 * 
		 * @param selection the selection
		 * @return the text regions corresponding to <code>selection</code>
		 * @throws BadLocationException if accessing the document failed
		 */
		IRegion[] getRanges(ISelection selection) throws BadLocationException {
			return new IRegion[0];
		}
	}

	private final Implementation NULL_IMPLEMENTATION= new Implementation();

	private final Implementation RANGE_IMPLEMENTATION= new Implementation() {
		TextEdit replace(ISelection selection, String replacement) {
			ITextSelection ts= (ITextSelection)selection;
			return new ReplaceEdit(ts.getOffset(), ts.getLength(), replacement);
		}

		String getText(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			return ts.getText();
		}

		boolean isEmpty(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			return ts.getLength() == 0;
		}

		boolean isMultiline(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			return ts.getEndLine() > ts.getStartLine();
		}

		TextEdit delete(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			if (isEmpty(selection))
				return new DeleteEdit(ts.getOffset(), 1);
			return new DeleteEdit(ts.getOffset(), ts.getLength());
		}

		TextEdit backspace(ISelection selection) throws BadLocationException {
			ITextSelection ts= (ITextSelection)selection;
			if (isEmpty(selection))
				return new DeleteEdit(ts.getOffset() - 1, 1);
			return new DeleteEdit(ts.getOffset(), ts.getLength());
		}

		ISelection makeEmpty(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			return new TextSelection(fDocument, ts.getOffset(), 0);
		}

		IRegion[] getRanges(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			return new IRegion[] { new Region(ts.getOffset(), ts.getLength()) };
		}
	};

	private final Implementation COLUMN_IMPLEMENTATION= new Implementation() {
		TextEdit replace(ISelection selection, String replacement) throws BadLocationException {
			try {
				MultiTextEdit root;
				IBlockTextSelection cts= (IBlockTextSelection)selection;
				int startLine= cts.getStartLine();
				int endLine= cts.getEndLine();
				int startColumn= cts.getStartColumn();
				int endColumn= cts.getEndColumn();
				int visualStartColumn= computeVisualColumn(startLine, startColumn);
				int visualEndColumn= computeVisualColumn(endLine, endColumn);
				root= new MultiTextEdit();
				String[] delimiters= fDocument.getLegalLineDelimiters();

				int lastDelim= 0;
				for (int line= startLine; line <= endLine; line++) {
					String string;
					if (lastDelim == -1) {
						string= ""; //$NON-NLS-1$
					} else {
						int[] index= TextUtilities.indexOf(delimiters, replacement, lastDelim);
						if (index[0] == -1) {
							string= replacement.substring(lastDelim);
							lastDelim= -1;
						} else {
							string= replacement.substring(lastDelim, index[0]);
							lastDelim= index[0] + delimiters[index[1]].length();
						}
					}
					TextEdit replace= createReplaceEdit(line, visualStartColumn, visualEndColumn, string);
					root.addChild(replace);
				}
				while (lastDelim != -1) {
					// more stuff to insert
					String string;
					int[] index= TextUtilities.indexOf(delimiters, replacement, lastDelim);
					if (index[0] == -1) {
						string= replacement.substring(lastDelim);
						lastDelim= -1;
					} else {
						string= replacement.substring(lastDelim, index[0]);
						lastDelim= index[0] + delimiters[index[1]].length();
					}
					endLine++;
					TextEdit edit;
					if (endLine < fDocument.getNumberOfLines()) {
						edit= createReplaceEdit(endLine, visualStartColumn, visualEndColumn, string);
					} else {
						// insertion reaches beyond the last line
						int insertLocation= root.getExclusiveEnd();
						int spaces= visualStartColumn;
						char[] array= new char[spaces];
						Arrays.fill(array, ' ');
						string= fDocument.getLegalLineDelimiters()[0] + String.valueOf(array) + string;
						edit= new InsertEdit(insertLocation, string);
						insertLocation+= string.length();
					}
					root.addChild(edit);
				}
				return root;
			} catch (MalformedTreeException x) {
				Assert.isTrue(false);
				return null;
			}
		}

		String getText(ISelection selection) throws BadLocationException {
			IBlockTextSelection cts= (IBlockTextSelection)selection;
			StringBuffer buf= new StringBuffer(cts.getLength());
			int startLine= cts.getStartLine();
			int endLine= cts.getEndLine();
			int startColumn= cts.getStartColumn();
			int endColumn= cts.getEndColumn();
			int visualStartColumn= computeVisualColumn(startLine, startColumn);
			int visualEndColumn= computeVisualColumn(endLine, endColumn);

			for (int line= startLine; line <= endLine; line++) {
				appendColumnRange(buf, line, visualStartColumn, visualEndColumn);
				if (line != endLine)
					buf.append(fDocument.getLineDelimiter(line));
			}

			return buf.toString();
		}

		boolean isEmpty(ISelection selection) throws BadLocationException {
			IBlockTextSelection cts= (IBlockTextSelection)selection;
			int startLine= cts.getStartLine();
			int endLine= cts.getEndLine();
			int startColumn= cts.getStartColumn();
			int endColumn= cts.getEndColumn();
			int visualStartColumn= computeVisualColumn(startLine, startColumn);
			int visualEndColumn= computeVisualColumn(endLine, endColumn);
			return visualEndColumn == visualStartColumn;
		}

		boolean isMultiline(ISelection selection) {
			ITextSelection ts= (ITextSelection)selection;
			return ts.getEndLine() > ts.getStartLine();
		}

		TextEdit delete(ISelection selection) throws BadLocationException {
			if (isEmpty(selection)) {
				IBlockTextSelection cts= (IBlockTextSelection)selection;
				selection= new BlockTextSelection(fDocument, cts.getStartLine(), cts.getStartColumn(), cts.getEndLine(), cts.getEndColumn() + 1, fTabWidth);
			}
			return replace(selection, ""); //$NON-NLS-1$
		}

		TextEdit backspace(ISelection selection) throws BadLocationException {
			IBlockTextSelection cts= (IBlockTextSelection)selection;
			if (isEmpty(selection) && cts.getStartColumn() > 0) {
				selection= new BlockTextSelection(fDocument, cts.getStartLine(), cts.getStartColumn() - 1, cts.getEndLine(), cts.getEndColumn(), fTabWidth);
			}
			return replace(selection, ""); //$NON-NLS-1$
		}

		ISelection makeEmpty(ISelection selection) throws BadLocationException {
			IBlockTextSelection cts= (IBlockTextSelection)selection;
			int startLine= cts.getStartLine();
			int startColumn= cts.getStartColumn();
			int endLine= cts.getEndLine();
			int endColumn= computeCharacterColumn(cts.getEndLine(), computeVisualColumn(startLine, startColumn));
			return new BlockTextSelection(fDocument, startLine, startColumn, endLine, endColumn, fTabWidth);
		}

		IRegion[] getRanges(ISelection selection) throws BadLocationException {
			IBlockTextSelection cts= (IBlockTextSelection)selection;
			final int startLine= cts.getStartLine();
			final int endLine= cts.getEndLine();
			int visualStartColumn= computeVisualColumn(startLine, cts.getStartColumn());
			int visualEndColumn= computeVisualColumn(endLine, cts.getEndColumn());
			IRegion[] ranges= new IRegion[endLine - startLine + 1];

			for (int line= startLine; line <= endLine; line++) {
				int startColumn= computeCharacterColumn(line, visualStartColumn);
				int endColumn= computeCharacterColumn(line, visualEndColumn);
				IRegion lineInfo= fDocument.getLineInformation(line);
				int lineEnd= lineInfo.getLength();
				startColumn= Math.min(startColumn, lineEnd);
				endColumn= Math.min(endColumn, lineEnd);
				ranges[line - startLine]= new Region(lineInfo.getOffset() + startColumn, endColumn - startColumn);
			}

			return ranges;
		}

		private TextEdit createReplaceEdit(int line, int visualStartColumn, int visualEndColumn, String replacement) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			String content= fDocument.get(info.getOffset(), lineLength);
			int startColumn= -1;
			int endColumn= -1;
			int visual= 0;
			for (int offset= 0; offset < lineLength; offset++) {
				if (startColumn == -1 && visual >= visualStartColumn)
					startColumn= offset;
				if (visual == visualEndColumn) {
					endColumn= offset;
					break;
				}
				if (content.charAt(offset) == '\t')
					visual+= fTabWidth - visual % fTabWidth;
				else
					visual++;
			}
			if (startColumn == -1) {
				boolean materializeVirtualSpace= replacement.length() != 0;
				if (materializeVirtualSpace) {
					int spaces= visualStartColumn - visual;
					char[] array= new char[spaces];
					Arrays.fill(array, ' ');
					return new InsertEdit(info.getOffset() + lineLength, String.valueOf(array) + replacement);
				}
				return new MultiTextEdit();
			}
			if (endColumn == -1)
				endColumn= lineLength;
			return new ReplaceEdit(info.getOffset() + startColumn, endColumn - startColumn, replacement);
		}

		private void appendColumnRange(StringBuffer buf, int line, int visualStartColumn, int visualEndColumn) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			String content= fDocument.get(info.getOffset(), lineLength);
			int startColumn= -1;
			int endColumn= -1;
			int visual= 0;
			for (int offset= 0; offset < lineLength; offset++) {
				if (startColumn == -1 && visual >= visualStartColumn)
					startColumn= offset;
				if (visual == visualEndColumn) {
					endColumn= offset;
					break;
				}
				if (content.charAt(offset) == '\t')
					visual+= fTabWidth - visual % fTabWidth;
				else
					visual++;
			}
			if (startColumn != -1)
				buf.append(content.substring(startColumn, endColumn == -1 ? lineLength : endColumn));
			if (endColumn == -1) {
				int spaces= visualEndColumn - Math.max(visual, visualStartColumn);
				for (int i= 0; i < spaces; i++)
					buf.append(' ');
			}
		}

		private int computeVisualColumn(final int line, final int column) throws BadLocationException {
			int visualColumn= 0;
			IRegion info= fDocument.getLineInformation(line);
			int lineEnd= info.getLength();
			int to= Math.min(lineEnd, column);
			String content= fDocument.get(info.getOffset(), info.getLength());
			for (int offset= 0; offset < to; offset++) {
				if (content.charAt(offset) == '\t')
					visualColumn+= fTabWidth - visualColumn % fTabWidth;
				else
					visualColumn++;
			}
			if (column > lineEnd) {
				visualColumn+= column - lineEnd; // virtual spaces
			}
			return visualColumn;
		}

		private int computeCharacterColumn(int line, int visualColumn) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			String content= fDocument.get(info.getOffset(), lineLength);
			int column= -1;
			int visual= 0;
			for (int offset= 0; offset < lineLength; offset++) {
				if (column == -1 && visual >= visualColumn)
					return offset;
				if (content.charAt(offset) == '\t')
					visual+= fTabWidth - visual % fTabWidth;
				else
					visual++;
			}
			return lineLength + visualColumn - visual;
		}
	};

	private final IDocument fDocument;

	private final int fTabWidth;

	private IRewriteTarget fRewriteTarget;

	private ISelectionProvider fSelectionProvider;

	/**
	 * Creates a new processor on the given viewer.
	 * 
	 * @param viewer the viewer
	 */
	public SelectionProcessor(ITextViewer viewer) {
		this(viewer.getDocument(), viewer.getTextWidget().getTabs());
		if (viewer instanceof ITextViewerExtension) {
			ITextViewerExtension ext= (ITextViewerExtension)viewer;
			fRewriteTarget= ext.getRewriteTarget();
		}
		fSelectionProvider= viewer.getSelectionProvider();
	}

	/**
	 * Creates a new processor on the given document and using the given tab width.
	 * 
	 * @param document the document
	 * @param tabWidth the tabulator width in space equivalents
	 */
	public SelectionProcessor(IDocument document, int tabWidth) {
		Assert.isNotNull(document);
		Assert.isTrue(tabWidth > 0);
		fDocument= document;
		fTabWidth= tabWidth;
	}

	/**
	 * Returns a text edit describing the text modification that would be executed if the delete key
	 * was pressed on the given selection.
	 * 
	 * @param selection the selection to delete
	 * @return a text edit describing the operation needed to delete <code>selection</code>
	 * @throws BadLocationException if computing the edit failed
	 */
	public TextEdit delete(ISelection selection) throws BadLocationException {
		return getImplementation(selection).delete(selection);
	}

	/**
	 * Returns a text edit describing the text modification that would be executed if the backspace
	 * key was pressed on the given selection.
	 * 
	 * @param selection the selection to delete
	 * @return a text edit describing the operation needed to delete <code>selection</code>
	 * @throws BadLocationException if computing the edit failed
	 */
	public TextEdit backspace(ISelection selection) throws BadLocationException {
		return getImplementation(selection).backspace(selection);
	}

	/**
	 * Returns a text edit describing the text modification that would be executed if the given
	 * selection was replaced by <code>replacement</code>.
	 * 
	 * @param selection the selection to replace
	 * @param replacement the replacement text
	 * @return a text edit describing the operation needed to replace <code>selection</code>
	 * @throws BadLocationException if computing the edit failed
	 */
	public TextEdit replace(ISelection selection, String replacement) throws BadLocationException {
		return getImplementation(selection).replace(selection, replacement);
	}

	/**
	 * Returns the text covered by <code>selection</code>
	 * 
	 * @param selection the selection
	 * @return the text covered by <code>selection</code>
	 * @throws BadLocationException if computing the edit failed
	 */
	public String getText(ISelection selection) throws BadLocationException {
		return getImplementation(selection).getText(selection);
	}

	/**
	 * Returns <code>true</code> if the text covered by <code>selection</code> does not contain any
	 * characters. Note the difference to {@link ITextSelection#isEmpty()}, which returns
	 * <code>true</code> only for invalid selections.
	 * 
	 * @param selection the selection
	 * @return <code>true</code> if <code>selection</code> does not contain any text,
	 *         <code>false</code> otherwise
	 * @throws BadLocationException if accessing the document failed
	 */
	public boolean isEmpty(ISelection selection) throws BadLocationException {
		return getImplementation(selection).isEmpty(selection);
	}

	/**
	 * Returns <code>true</code> if <code>selection</code> covers text on two or more lines,
	 * <code>false</code> otherwise.
	 * 
	 * @param selection the selection
	 * @return <code>true</code> if <code>selection</code> covers text on two or more lines,
	 *         <code>false</code> otherwise
	 */
	public boolean isMultiline(ISelection selection) {
		return getImplementation(selection).isMultiline(selection);
	}

	/**
	 * Returns a selection similar to <code>selection</code> but {@linkplain #isEmpty(ISelection)
	 * empty}. Typically, the selection is reduced to its left-most offset.
	 * 
	 * @param selection the selection
	 * @return an empty variant of <code>selection</code>
	 * @throws BadLocationException if accessing the document failed
	 */
	public ISelection makeEmpty(ISelection selection) throws BadLocationException {
		return getImplementation(selection).makeEmpty(selection);
	}

	/**
	 * Convenience method that applies the edit returned from {@link #delete(ISelection)} to the
	 * underlying document.
	 * 
	 * @param selection the selection to delete
	 * @throws BadLocationException if accessing the document failed
	 */
	public void doDelete(ISelection selection) throws BadLocationException {
		TextEdit edit= delete(selection);
		ISelection empty= makeEmpty(selection);
		boolean complex= edit.hasChildren();
		if (complex && fRewriteTarget != null)
			fRewriteTarget.beginCompoundChange();
		edit.apply(fDocument, TextEdit.UPDATE_REGIONS);
		if (fSelectionProvider != null)
			fSelectionProvider.setSelection(empty);
		if (complex && fRewriteTarget != null)
			fRewriteTarget.endCompoundChange();

	}
	
	/**
	 * Returns the text regions covered by the given selection.
	 * 
	 * @param selection the selection
	 * @return the text regions corresponding to <code>selection</code>
	 * @throws BadLocationException if accessing the document failed
	 */
	public IRegion[] getRanges(ISelection selection) throws BadLocationException {
		return getImplementation(selection).getRanges(selection);
	}

	/**
	 * Returns the implementation.
	 * 
	 * @param selection the selection
	 * @return the corresponding processor implementation
	 */
	private Implementation getImplementation(ISelection selection) {
		if (selection instanceof IBlockTextSelection)
			return COLUMN_IMPLEMENTATION;
		else if (selection instanceof ITextSelection)
			return RANGE_IMPLEMENTATION;
		else
			return NULL_IMPLEMENTATION;
	}
}
