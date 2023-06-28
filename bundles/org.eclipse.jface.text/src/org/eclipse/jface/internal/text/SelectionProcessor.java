/*******************************************************************************
 * Copyright (c) 2009, 2021 Avaloq Evolution AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Eicher (Avaloq Evolution AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;

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
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.MultiStringMatcher;
import org.eclipse.jface.text.MultiStringMatcher.Match;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;

/**
 * Processes {@link ITextSelection}s.
 *
 * @since 3.5
 */
public final class SelectionProcessor {
	private static class Implementation<T extends ISelection> {
		/**
		 * Returns a text edit describing the text modification that would be executed if the given
		 * selection was replaced by <code>replacement</code>.
		 *
		 * @param selection the selection to replace
		 * @param replacement the replacement text
		 * @return a text edit describing the operation needed to replace <code>selection</code>
		 * @throws BadLocationException if computing the edit failed
		 */
		TextEdit replace(T selection, String replacement) throws BadLocationException {
			return new MultiTextEdit();
		}

		/**
		 * Returns the text covered by <code>selection</code>
		 *
		 * @param selection the selection
		 * @return the text covered by <code>selection</code>
		 * @throws BadLocationException if computing the edit failed
		 */
		String getText(T selection) throws BadLocationException {
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
		boolean isEmpty(T selection) throws BadLocationException {
			return selection.isEmpty();
		}

		/**
		 * Returns <code>true</code> if <code>selection</code> covers text on two or more lines,
		 * <code>false</code> otherwise.
		 *
		 * @param selection the selection
		 * @return <code>true</code> if <code>selection</code> covers text on two or more lines,
		 *         <code>false</code> otherwise
		 * @throws BadLocationException if selection is not a valid selection on the target document
		 */
		boolean isMultiline(T selection) throws BadLocationException {
			if (selection == null)
				throw new NullPointerException();
			return false;
		}

		TextEdit delete(T selection) throws BadLocationException {
			return replace(selection, ""); //$NON-NLS-1$
		}

		TextEdit backspace(T selection) throws BadLocationException {
			return replace(selection, ""); //$NON-NLS-1$
		}

		/**
		 * Returns a selection similar to <code>selection</code> but {@linkplain #isEmpty(ISelection)
		 * empty}. Typically, the selection is reduced to its left-most offset.
		 *
		 * @param selection the selection
		 * @param beginning <code>true</code> to collapse the selection to its smallest position
		 *            (i.e. its left-most offset), <code>false</code> to collapse it to its greatest
		 *            position (e.g its right-most offset)
		 * @return an empty variant of <code>selection</code>
		 * @throws BadLocationException if accessing the document failed
		 */
		T makeEmpty(T selection, boolean beginning) throws BadLocationException {
			return selection;
		}

		/**
		 * Returns the text regions covered by the given selection.
		 *
		 * @param selection the selection
		 * @return the text regions corresponding to <code>selection</code>
		 * @throws BadLocationException if accessing the document failed
		 */
		IRegion[] getRanges(T selection) throws BadLocationException {
			return new IRegion[0];
		}

		/**
		 * Returns the number of lines touched by <code>selection</code>.
		 *
		 * @param selection the selection
		 * @return the number of lines touched by <code>selection</code>
		 * @throws BadLocationException if accessing the document failed
		 */
		int getCoveredLines(T selection) throws BadLocationException {
			return 0;
		}

		/**
		 * Returns the selection after replacing <code>selection</code> by <code>replacement</code>.
		 *
		 * @param selection the selection to be replaced
		 * @param replacement the replacement text
		 * @return the selection that the user expects after the specified replacement operation
		 * @throws BadLocationException if accessing the document failed
		 */
		T makeReplaceSelection(T selection, String replacement) throws BadLocationException {
			return makeEmpty(selection, false);
		}

		/**
		 * Returns the selection after hitting backspace.
		 *
		 * @param selection the selection to be replaced
		 * @return the selection that the user expects after the specified backspace operation
		 * @throws BadLocationException if accessing the document failed
		 */
		public ISelection makeBackspaceSelection(T selection) throws BadLocationException {
			return makeEmpty(selection, true);
		}

		/**
		 * Returns the selection after hitting delete.
		 *
		 * @param selection the selection to be replaced
		 * @return the selection that the user expects after the specified backspace operation
		 * @throws BadLocationException if accessing the document failed
		 */
		public ISelection makeDeleteSelection(T selection) throws BadLocationException {
			return makeEmpty(selection, true);
		}
	}

	private final Implementation<ISelection> NULL_IMPLEMENTATION= new Implementation<>();

	private final Implementation<ITextSelection> RANGE_IMPLEMENTATION= new Implementation<>() {
		@Override
		TextEdit replace(ITextSelection selection, String replacement) {
			return new ReplaceEdit(selection.getOffset(), selection.getLength(), replacement);
		}

		@Override
		String getText(ITextSelection selection) {
			return selection.getText();
		}

		@Override
		boolean isEmpty(ITextSelection selection) {
			return selection.getLength() <= 0;
		}

		@Override
		boolean isMultiline(ITextSelection selection) throws BadLocationException {
			return fDocument.getLineOfOffset(selection.getOffset()) < fDocument.getLineOfOffset(selection.getOffset() + selection.getLength());
		}

		@Override
		TextEdit delete(ITextSelection selection) {
			return isEmpty(selection) ? new DeleteEdit(selection.getOffset(), 1) : new DeleteEdit(selection.getOffset(), selection.getLength());
		}

		@Override
		TextEdit backspace(ITextSelection selection) throws BadLocationException {
			return isEmpty(selection) ? new DeleteEdit(selection.getOffset() - 1, 1) : new DeleteEdit(selection.getOffset(), selection.getLength());
		}

		@Override
		ITextSelection makeEmpty(ITextSelection selection, boolean beginning) {
			return beginning ?
					new TextSelection(fDocument, selection.getOffset(), 0) : new TextSelection(fDocument, selection.getOffset() + selection.getLength(), 0);
		}

		@Override
		IRegion[] getRanges(ITextSelection selection) {
			return new IRegion[] { new Region(selection.getOffset(), selection.getLength()) };
		}

		@Override
		int getCoveredLines(ITextSelection selection) throws BadLocationException {
			return selection.getEndLine() - selection.getStartLine() + 1;
		}

		@Override
		ITextSelection makeReplaceSelection(ITextSelection selection, String replacement) {
			return new TextSelection(fDocument, selection.getOffset() + replacement.length(), 0);
		}

		@Override
		public ISelection makeBackspaceSelection(ITextSelection selection) throws BadLocationException {
			if (isEmpty(selection)) {
				return new TextSelection(Math.max(0, selection.getOffset() - 1), selection.getLength());
			}
			return makeEmpty(selection, true);
		}
	};

	private final Implementation<IMultiTextSelection> RANGES_IMPLEMENTATION= new Implementation<>() {

		private MultiTextEdit rangeEdits(IMultiTextSelection selection, Function<IRegion, TextEdit> regionToTextEdit) {
			MultiTextEdit res= new MultiTextEdit();
			Arrays.stream(selection.getRegions())
					.map(regionToTextEdit)
					.filter(Objects::nonNull)
					.forEach(res::addChild);
			return res;
		}

		@Override
		TextEdit replace(IMultiTextSelection selection, String replacement) {
			if (replacement.isBlank() || !replacement.contains(System.lineSeparator())) { // simple edit
				return rangeEdits(selection, region -> new ReplaceEdit(region.getOffset(), region.getLength(), replacement));
			} else { // paste
				MultiTextEdit root;
				root= new MultiTextEdit();
				String[] delimiters= fDocument.getLegalLineDelimiters();
				MultiStringMatcher delimiterMatcher= MultiStringMatcher.create(delimiters);

				int lastDelim= 0;
				for (IRegion region : selection.getRegions()) {
					String string;
					if (lastDelim == -1) {
						string= ""; //$NON-NLS-1$
					} else {
						Match m= delimiterMatcher.indexOf(replacement, lastDelim);
						if (m == null) {
							string= replacement.substring(lastDelim);
							lastDelim= -1;
						} else {
							string= replacement.substring(lastDelim, m.getOffset());
							lastDelim= m.getOffset() + m.getText().length();
						}
					}
					TextEdit replace= new ReplaceEdit(region.getOffset(), region.getLength(), string);
					root.addChild(replace);
				}
	//			while (lastDelim != -1) {
	//				// more stuff to insert
	//				String string;
	//				Match m= delimiterMatcher.indexOf(replacement, lastDelim);
	//				if (m == null) {
	//					string= replacement.substring(lastDelim);
	//					lastDelim= -1;
	//				} else {
	//					string= replacement.substring(lastDelim, m.getOffset());
	//					lastDelim= m.getOffset() + m.getText().length();
	//				}
	//				endLine++;
	//				TextEdit edit;
	//				if (endLine < fDocument.getNumberOfLines()) {
	//					edit= createReplaceEdit(endLine, visualStartColumn, visualEndColumn, string, delete);
	//				} else {
	//					// insertion reaches beyond the last line
	//					int insertLocation= root.getExclusiveEnd();
	//					int spaces= visualStartColumn;
	//					char[] array= new char[spaces];
	//					Arrays.fill(array, ' ');
	//					string= TextUtilities.getDefaultLineDelimiter(fDocument) + String.valueOf(array) + string;
	//					edit= new InsertEdit(insertLocation, string);
	//					insertLocation+= string.length();
	//				}
	//				root.addChild(edit);
	//			}
				return root;
			}
		}

		@Override
		String getText(IMultiTextSelection selection) throws BadLocationException {
			StringBuilder builder = new StringBuilder();
			for (IRegion region : selection.getRegions()) {
				builder.append(fDocument.get(region.getOffset(), region.getLength()));
			}
			return builder.toString();
		}

		@Override
		boolean isEmpty(IMultiTextSelection selection) {
			return Arrays.stream(selection.getRegions()).allMatch(r -> r.getLength() == 0);
		}

		@Override
		boolean isMultiline(IMultiTextSelection selection) throws BadLocationException {
			int line = -1;
			for (IRegion region : selection.getRegions()) {
				if (line == -1) {
					line = fDocument.getLineOfOffset(region.getOffset());
				} else if (
					line != fDocument.getLineOfOffset(region.getOffset()) ||
					line != fDocument.getLineOfOffset(region.getOffset() + region.getLength())) {
					return true;
				}
			}
			return false;
		}

		@Override
		TextEdit delete(IMultiTextSelection selection) {
			if (isEmpty(selection)) {
				return rangeEdits(selection, region -> new DeleteEdit(region.getOffset(), 1));
			}
			return rangeEdits(selection, region -> new DeleteEdit(region.getOffset(), region.getLength()));
		}

		@Override
		TextEdit backspace(IMultiTextSelection selection) throws BadLocationException {
			if (isEmpty(selection)) {
				return rangeEdits(selection, region -> region.getOffset() == 0 ? null : new DeleteEdit(region.getOffset() - 1, 1));
			}
			return rangeEdits(selection, region -> {
				if (region.getLength() > 0) {
					return new DeleteEdit(region.getOffset(), region.getLength());
				} else if (region.getOffset() > 0) {
					return new DeleteEdit(region.getOffset() - 1, 1);
				} else {
					return null;
				}
			});
		}

		@Override
		IMultiTextSelection makeEmpty(IMultiTextSelection selection, boolean beginning) {
			int[] deletedCount= new int[] { 0 };
			return new MultiTextSelection(fDocument, Arrays.stream(selection.getRegions()).map(region -> {
				Region res= beginning
						? new Region(region.getOffset() - deletedCount[0], 0)
						: new Region(region.getOffset() - deletedCount[0] + region.getLength(), 0);
				deletedCount[0]+= region.getLength();
				return res;
			}).toArray(Region[]::new));
		}

		@Override
		IRegion[] getRanges(IMultiTextSelection selection) {
			return selection.getRegions().clone();
		}

		@Override
		int getCoveredLines(IMultiTextSelection selection) throws BadLocationException {
			int res = 0;
			int lastLine = -1;
			for (IRegion region : selection.getRegions()) {
				if (lastLine == fDocument.getLineOfOffset(region.getOffset())) {
					res--; // ignore 1st line if already processed
				}
				res++; // at least 1 line for the range
				res+= (fDocument.getLineOfOffset(region.getOffset() + region.getLength()) - fDocument.getLineOfOffset(region.getOffset()));
				lastLine = fDocument.getLineOfOffset(region.getOffset() + region.getLength());
			}
			return res;
		}

		@Override
		IMultiTextSelection makeReplaceSelection(IMultiTextSelection selection, String replacement) {
			if (!replacement.contains(System.lineSeparator())) { // simple edit
				int[] offset= new int[] { 0 };
				return new MultiTextSelection(fDocument,
						Arrays.stream(selection.getRegions()).map(region -> {
							Region res= new Region(region.getOffset() + offset[0] + replacement.length(), 0);
							offset[0]+= (replacement.length() - region.getLength());
							return res;
						}).toArray(Region[]::new));
			} else { // paste
				TextEdit edit= replace(selection, replacement);
				if (edit instanceof MultiTextEdit) {
					int offsetDelta= 0;
					List<IRegion> afterEdit= new ArrayList<>(Math.min(edit.getLength(), selection.getLength()));
					for (int i= 0; i < Math.min(edit.getChildrenSize(), selection.getLength()); i++) {
						ReplaceEdit currentEdit= (ReplaceEdit) edit.getChildren()[i];
						offsetDelta+= currentEdit.getText().length() - currentEdit.getRegion().getLength();
						afterEdit.add(new Region(currentEdit.getOffset() + offsetDelta, 0));
					}
					return new MultiTextSelection(fDocument, afterEdit.toArray(IRegion[]::new));
				} else {
					return new TextSelection(fDocument, edit.getOffset() + replacement.length() - edit.getRegion().getLength(), 0);
				}
			}
		}

		@Override
		public ISelection makeBackspaceSelection(IMultiTextSelection selection) throws BadLocationException {
			int[] removedChars= { 0 };
			return new MultiTextSelection(fDocument,
					Arrays.stream(selection.getRegions()).map(region -> {
						int length= region.getLength() != 0 ? region.getLength() : (region.getOffset() != 0 ? 1 : 0);
						Region res= new Region(Math.max(0, region.getOffset() - removedChars[0] - (region.getLength() == 0 ? length : 0)), 0);
						removedChars[0]+= length;
						return res;
					}).toArray(Region[]::new));
		}

		@Override
		public ISelection makeDeleteSelection(IMultiTextSelection selection) throws BadLocationException {
			int[] removedChars= { 0 };
			return new MultiTextSelection(fDocument,
					Arrays.stream(selection.getRegions()).map(region -> {
						int length= region.getLength() != 0 ? region.getLength() : 1;
						Region res= new Region(Math.max(0, region.getOffset() - removedChars[0]), 0);
						removedChars[0]+= length;
						return res;
					}).toArray(Region[]::new));
		}
	};

		private final Implementation<IBlockTextSelection> COLUMN_IMPLEMENTATION= new Implementation<>() {
		private TextEdit replace(IBlockTextSelection selection, String replacement, boolean delete) throws BadLocationException {
			try {
				MultiTextEdit root;
				int startLine= selection.getStartLine();
				int endLine= selection.getEndLine();
				int startColumn= selection.getStartColumn();
				int endColumn= selection.getEndColumn();
				int visualStartColumn= computeVisualColumn(startLine, startColumn);
				int visualEndColumn= computeVisualColumn(endLine, endColumn);
				root= new MultiTextEdit();
				String[] delimiters= fDocument.getLegalLineDelimiters();
				MultiStringMatcher delimiterMatcher= MultiStringMatcher.create(delimiters);

				int lastDelim= 0;
				for (int line= startLine; line <= endLine; line++) {
					String string;
					if (lastDelim == -1) {
						string= ""; //$NON-NLS-1$
					} else {
						Match m= delimiterMatcher.indexOf(replacement, lastDelim);
						if (m == null) {
							string= replacement.substring(lastDelim);
							lastDelim= -1;
						} else {
							string= replacement.substring(lastDelim, m.getOffset());
							lastDelim= m.getOffset() + m.getText().length();
						}
					}
					TextEdit replace= createReplaceEdit(line, visualStartColumn, visualEndColumn, string, delete);
					root.addChild(replace);
				}
				while (lastDelim != -1) {
					// more stuff to insert
					String string;
					Match m= delimiterMatcher.indexOf(replacement, lastDelim);
					if (m == null) {
						string= replacement.substring(lastDelim);
						lastDelim= -1;
					} else {
						string= replacement.substring(lastDelim, m.getOffset());
						lastDelim= m.getOffset() + m.getText().length();
					}
					endLine++;
					TextEdit edit;
					if (endLine < fDocument.getNumberOfLines()) {
						edit= createReplaceEdit(endLine, visualStartColumn, visualEndColumn, string, delete);
					} else {
						// insertion reaches beyond the last line
						int insertLocation= root.getExclusiveEnd();
						int spaces= visualStartColumn;
						char[] array= new char[spaces];
						Arrays.fill(array, ' ');
						string= TextUtilities.getDefaultLineDelimiter(fDocument) + String.valueOf(array) + string;
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

		@Override
		TextEdit replace(IBlockTextSelection selection, String replacement) throws BadLocationException {
			return replace(selection, replacement, false);
		}

		@Override
		String getText(IBlockTextSelection selection) throws BadLocationException {
			StringBuilder buf= new StringBuilder(selection.getLength());
			int startLine= selection.getStartLine();
			int endLine= selection.getEndLine();
			int startColumn= selection.getStartColumn();
			int endColumn= selection.getEndColumn();
			int visualStartColumn= computeVisualColumn(startLine, startColumn);
			int visualEndColumn= computeVisualColumn(endLine, endColumn);

			for (int line= startLine; line <= endLine; line++) {
				appendColumnRange(buf, line, visualStartColumn, visualEndColumn);
				if (line != endLine)
					buf.append(fDocument.getLineDelimiter(line));
			}

			return buf.toString();
		}

		@Override
		boolean isEmpty(IBlockTextSelection selection) throws BadLocationException {
			int startLine= selection.getStartLine();
			int endLine= selection.getEndLine();
			int startColumn= selection.getStartColumn();
			int endColumn= selection.getEndColumn();
			int visualStartColumn= computeVisualColumn(startLine, startColumn);
			int visualEndColumn= computeVisualColumn(endLine, endColumn);
			return visualEndColumn == visualStartColumn;
		}

		@Override
		boolean isMultiline(IBlockTextSelection selection) {
			return selection.getEndLine() > selection.getStartLine();
		}

		@Override
		TextEdit delete(IBlockTextSelection selection) throws BadLocationException {
			if (isEmpty(selection)) {
				selection= new BlockTextSelection(fDocument, selection.getStartLine(), selection.getStartColumn(), selection.getEndLine(), selection.getEndColumn() + 1, fTabWidth);
			}
			return replace(selection, "", true); //$NON-NLS-1$
		}

		@Override
		TextEdit backspace(IBlockTextSelection selection) throws BadLocationException {
			if (isEmpty(selection) && selection.getStartColumn() > 0) {
				selection= new BlockTextSelection(fDocument, selection.getStartLine(), selection.getStartColumn() - 1, selection.getEndLine(), selection.getEndColumn(), fTabWidth);
			}
			return replace(selection, ""); //$NON-NLS-1$
		}

		@Override
		IBlockTextSelection makeEmpty(IBlockTextSelection selection, boolean beginning) throws BadLocationException {
			int startLine, startColumn, endLine, endColumn;
			if (beginning) {
				startLine= selection.getStartLine();
				startColumn= selection.getStartColumn();
				endLine= selection.getEndLine();
				endColumn= computeCharacterColumn(endLine, computeVisualColumn(startLine, startColumn));
			} else {
				endLine= selection.getEndLine();
				endColumn= selection.getEndColumn();
				startLine= selection.getStartLine();
				startColumn= computeCharacterColumn(startLine, computeVisualColumn(endLine, endColumn));
			}
			return new BlockTextSelection(fDocument, startLine, startColumn, endLine, endColumn, fTabWidth);
		}

		@Override
		IBlockTextSelection makeReplaceSelection(IBlockTextSelection selection, String replacement) throws BadLocationException {
			Match m= MultiStringMatcher.indexOf(replacement, 0, fDocument.getLegalLineDelimiters());
			int length= m != null ? m.getOffset() : replacement.length();

			int startLine= selection.getStartLine();
			int column= selection.getStartColumn() + length;
			int endLine= selection.getEndLine();
			int endColumn= computeCharacterColumn(endLine, computeVisualColumn(startLine, column));
			return new BlockTextSelection(fDocument, startLine, column, endLine, endColumn, fTabWidth);
		}

		@Override
		public ISelection makeBackspaceSelection(IBlockTextSelection selection) throws BadLocationException {
			if (!isEmpty(selection)) {
				return makeEmpty(selection, true);
			}
			int column= Math.max(0, selection.getStartColumn());
			return new BlockTextSelection(fDocument, selection.getStartLine(), column, selection.getEndLine(), column, fTabWidth);
		}

		@Override
		IRegion[] getRanges(IBlockTextSelection selection) throws BadLocationException {
			final int startLine= selection.getStartLine();
			final int endLine= selection.getEndLine();
			int visualStartColumn= computeVisualColumn(startLine, selection.getStartColumn());
			int visualEndColumn= computeVisualColumn(endLine, selection.getEndColumn());
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

		@Override
		int getCoveredLines(IBlockTextSelection selection) throws BadLocationException {
			return selection.getEndLine() - selection.getStartLine() + 1;
		}

		private TextEdit createReplaceEdit(int line, int visualStartColumn, int visualEndColumn, String replacement, boolean delete) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			String content= fDocument.get(info.getOffset(), lineLength);
			int startColumn= -1;
			int endColumn= -1;
			int visual= 0;
			for (int offset= 0; offset < lineLength; offset++) {
				if (startColumn == -1) {
					if (visual == visualStartColumn)
						if (!delete && isWider(content.charAt(offset), visual) && replacement.isEmpty())
							startColumn= offset - 1;
						else
							startColumn= offset;
					else if (visual > visualStartColumn) {
						if (isWider(content.charAt(offset - 1), visual))
							startColumn= offset - 1;
						else
							startColumn= offset;
					}
				}
				if (startColumn != -1) {
					if (visual == visualEndColumn) {
						endColumn= offset;
						break;
					} else if (visual > visualEndColumn) {
						if (!delete && isWider(content.charAt(offset - 1), visual))
							endColumn= offset - 1;
						else
							endColumn= offset;
						break;
					}
				}
				visual+= visualSizeIncrement(content.charAt(offset), visual);
			}
			if (startColumn == -1) {
				boolean materializeVirtualSpace= !replacement.isEmpty();
				if (materializeVirtualSpace) {
					int spaces= Math.max(0, visualStartColumn - visual);
					char[] array= new char[spaces];
					Arrays.fill(array, ' ');
					return new InsertEdit(info.getOffset() + lineLength, String.valueOf(array) + replacement);
				}
				return new MultiTextEdit();
			}
			if (endColumn == -1)
				endColumn= lineLength;
			if (replacement.isEmpty())
				return new DeleteEdit(info.getOffset() + startColumn, endColumn - startColumn);
			return new ReplaceEdit(info.getOffset() + startColumn, endColumn - startColumn, replacement);
		}

		private void appendColumnRange(StringBuilder buf, int line, int visualStartColumn, int visualEndColumn) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			String content= fDocument.get(info.getOffset(), lineLength);
			int startColumn= -1;
			int endColumn= -1;
			int visual= 0;
			for (int offset= 0; offset < lineLength; offset++) {
				if (startColumn == -1 && visual >= visualStartColumn)
					startColumn= offset;
				if (visual >= visualEndColumn) {
					endColumn= offset;
					break;
				}
				visual+= visualSizeIncrement(content.charAt(offset), visual);
			}
			if (startColumn != -1)
				buf.append(content.substring(startColumn, endColumn == -1 ? lineLength : endColumn));
			if (endColumn == -1) {
				int spaces= Math.max(0, visualEndColumn - Math.max(visual, visualStartColumn));
				for (int i= 0; i < spaces; i++)
					buf.append(' ');
			}
		}

		private int computeVisualColumn(final int line, final int column) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			int to= Math.min(lineLength, column);
			String content= fDocument.get(info.getOffset(), lineLength);
			int visual= 0;
			for (int offset= 0; offset < to; offset++)
				visual+= visualSizeIncrement(content.charAt(offset), visual);
			if (column > lineLength)
				visual+= column - lineLength; // virtual spaces
			return visual;
		}

		private int computeCharacterColumn(int line, int visualColumn) throws BadLocationException {
			IRegion info= fDocument.getLineInformation(line);
			int lineLength= info.getLength();
			String content= fDocument.get(info.getOffset(), lineLength);
			int visual= 0;
			for (int offset= 0; offset < lineLength; offset++) {
				if (visual >= visualColumn)
					return offset;
				visual+= visualSizeIncrement(content.charAt(offset), visual);
			}
			return lineLength + Math.max(0, visualColumn - visual);
		}

		private boolean isWider(char character, int visual) {
			return visualSizeIncrement(character, visual) > 1;
		}

		/**
		 * Returns the increment in visual length represented by <code>character</code> given the
		 * current visual length. The visual length is <code>1</code> unless <code>character</code>
		 * is a tabulator (<code>\t</code>).
		 *
		 * @param character the character the length of which to compute
		 * @param visual the current visual length
		 * @return the increment in visual length represented by <code>character</code>, which is in
		 *         <code>[0,fTabWidth]</code>
		 */
		private int visualSizeIncrement(char character, int visual) {
			if (character > 255 && fStyledText != null) {
				GC gc= null;
				try {
					gc= new GC(fStyledText);
					int charWidth= gc.stringExtent(new String(Character.toString(character))).x;
					int singleCharWidth= gc.stringExtent(" ").x; //$NON-NLS-1$
					return (int) Math.ceil((double) charWidth / singleCharWidth);
				} finally {
					if (gc != null)
						gc.dispose();
				}
			}
			if (character != '\t')
				return 1;
			if (fTabWidth <= 0)
				return 0;
			return fTabWidth - visual % fTabWidth;
		}
	};

	private final IDocument fDocument;

	private final int fTabWidth;

	private IRewriteTarget fRewriteTarget;

	private ISelectionProvider fSelectionProvider;

	private StyledText fStyledText;

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
		fStyledText= viewer.getTextWidget();
	}

	/**
	 * Creates a new processor on the given document and using the given tab width.
	 *
	 * @param document the document
	 * @param tabWidth the tabulator width in space equivalents, must be <code>&gt;=0</code>
	 */
	public SelectionProcessor(IDocument document, int tabWidth) {
		Assert.isNotNull(document);
		Assert.isTrue(tabWidth >= 0);
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
	 * Returns <code>true</code> if <code>selection</code> extends to two or more lines,
	 * <code>false</code> otherwise.
	 *
	 * @param selection the selection
	 * @return <code>true</code> if <code>selection</code> extends to two or more lines,
	 *         <code>false</code> otherwise
	 * @throws BadLocationException if <code>selection</code> is not valid regarding the target
	 *             document
	 */
	public boolean isMultiline(ISelection selection) throws BadLocationException {
		return getImplementation(selection).isMultiline(selection);
	}

	/**
	 * Returns a selection similar to <code>selection</code> but {@linkplain #isEmpty(ISelection)
	 * empty}. Typically, the selection is reduced to its extreme offsets.
	 *
	 * @param selection the selection
	 * @param beginning <code>true</code> to collapse the selection to its smallest position (i.e.
	 *            its left-most offset), <code>false</code> to collapse it to its greatest position
	 *            (e.g its right-most offset)
	 * @return an empty variant of <code>selection</code>
	 * @throws BadLocationException if accessing the document failed
	 */
	public ISelection makeEmpty(ISelection selection, boolean beginning) throws BadLocationException {
		return getImplementation(selection).makeEmpty(selection, beginning);
	}

	private ISelection makeReplaceSelection(ISelection selection, String replacement) throws BadLocationException {
		return getImplementation(selection).makeReplaceSelection(selection, replacement);
	}

	private ISelection makeBackspaceSelection(ISelection selection) throws BadLocationException {
		return getImplementation(selection).makeBackspaceSelection(selection);
	}

	private ISelection makeDeleteSelection(ISelection selection) throws BadLocationException {
		return getImplementation(selection).makeDeleteSelection(selection);
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
		boolean complex= edit.hasChildren();
		if (complex && fRewriteTarget != null)
			fRewriteTarget.beginCompoundChange();
		try {
			edit.apply(fDocument, TextEdit.UPDATE_REGIONS);
			if (fSelectionProvider != null) {
				ISelection empty= makeDeleteSelection(selection);
				fSelectionProvider.setSelection(empty);
			}
		} finally {
		if (complex && fRewriteTarget != null)
			fRewriteTarget.endCompoundChange();
		}
	}

	/**
	 * Convenience method that applies the edit returned from {@link #backspace(ISelection)} to the
	 * underlying document.
	 *
	 * @param selection the selection to delete
	 * @throws BadLocationException if accessing the document failed
	 */
	public void doBackspace(ISelection selection) throws BadLocationException {
		TextEdit edit= backspace(selection);
		boolean complex= edit.hasChildren();
		if (complex && fRewriteTarget != null)
			fRewriteTarget.beginCompoundChange();
		try {
			ISelection newSelection= makeBackspaceSelection(selection);
			edit.apply(fDocument, TextEdit.UPDATE_REGIONS);
			if (fSelectionProvider != null) {
				fSelectionProvider.setSelection(newSelection);
			}
		} finally {
			if (complex && fRewriteTarget != null)
				fRewriteTarget.endCompoundChange();
		}
	}

	/**
	 * Convenience method that applies the edit returned from {@link #replace(ISelection, String)}
	 * to the underlying document and adapts the selection accordingly.
	 *
	 * @param selection the selection to replace
	 * @param replacement the replacement text
	 * @throws BadLocationException if accessing the document failed
	 */
	public void doReplace(ISelection selection, String replacement) throws BadLocationException {
		TextEdit edit= replace(selection, replacement);
		boolean complex= edit.hasChildren();
		if (complex && fRewriteTarget != null)
			fRewriteTarget.beginCompoundChange();
		try {
			edit.apply(fDocument, TextEdit.UPDATE_REGIONS);

			if (fSelectionProvider != null) {
				ISelection empty= makeReplaceSelection(selection, replacement);
				fSelectionProvider.setSelection(empty);
			}
		} finally {
			if (complex && fRewriteTarget != null)
				fRewriteTarget.endCompoundChange();
		}
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
	 * Returns the number of lines touched by <code>selection</code>. Note that for linear
	 * selections, this is the number of contained delimiters plus 1.
	 *
	 * @param selection the selection
	 * @return the number of lines touched by <code>selection</code>
	 * @throws BadLocationException if accessing the document failed
	 */
	public int getCoveredLines(ISelection selection) throws BadLocationException {
		return getImplementation(selection).getCoveredLines(selection);
	}

	/**
	 * Returns the implementation.
	 *
	 * @param selection the selection
	 * @return the corresponding processor implementation
	 */
	@SuppressWarnings("unchecked")
	private <T extends ISelection> Implementation<T> getImplementation(ISelection selection) {
		if (selection instanceof IBlockTextSelection) {
			return (Implementation<T>) COLUMN_IMPLEMENTATION;
		} else if (selection instanceof IMultiTextSelection && ((IMultiTextSelection)selection).getRegions().length > 1) {
			return (Implementation<T>) RANGES_IMPLEMENTATION;
		} else if (selection instanceof ITextSelection) {
			return (Implementation<T>) RANGE_IMPLEMENTATION;
		}
		return (Implementation<T>) NULL_IMPLEMENTATION;
	}
}
