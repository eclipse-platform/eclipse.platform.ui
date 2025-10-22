/*******************************************************************************
 * Copyright (c) 2009 Avaloq Evolution AG and others.
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
package org.eclipse.jface.text;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.SelectionProcessor;


/**
 * Standard implementation of {@link org.eclipse.jface.text.IBlockTextSelection}.
 *
 * @since 3.5
 */
public class BlockTextSelection extends TextSelection implements IBlockTextSelection {

	/** The start line. */
	private final int fStartLine;
	/** The start column. */
	private final int fStartColumn;
	/** The end line. */
	private final int fEndLine;
	/** The end column. */
	private final int fEndColumn;
	/** The tabulator width used to compute visual columns from character offsets. */
	private final int fTabWidth;

	/**
	 * Creates a column selection for the given lines and columns.
	 *
	 * @param document the document that this selection refers to
	 * @param startLine the start line
	 * @param startColumn the possibly virtual start column, measured in characters from the start
	 *        of <code>startLine</code>
	 * @param endLine the inclusive end line
	 * @param endColumn the exclusive and possibly virtual end column, measured in characters from
	 *        the start of <code>endLine</code>
	 * @param tabWidth the tabulator width used to compute the visual offsets from character offsets
	 */
	public BlockTextSelection(IDocument document, int startLine, int startColumn, int endLine, int endColumn, int tabWidth) {
		super(document, computeOffset(document, startLine, startColumn), computeOffset(document, endLine, endColumn) - computeOffset(document, startLine, startColumn));
		Assert.isLegal(startLine >= 0);
		Assert.isLegal(startColumn >= 0);
		Assert.isLegal(endLine >= startLine);
		Assert.isLegal(endColumn >= 0);
		Assert.isLegal(tabWidth >= 0);
		fStartLine= startLine;
		fStartColumn= startColumn;
		fEndLine= endLine;
		fEndColumn= endColumn;
		fTabWidth= tabWidth > 0	? tabWidth : 8; // seems to be the default when StyledText.getTabs returns 0
	}

	/**
	 * Returns the document offset for a given tuple of line and column count. If the column count
	 * points beyond the end of the line, the end of the line is returned (virtual location). If the
	 * line points beyond the number of lines, the end of the document is returned; if the line is
	 * &lt; zero, 0 is returned.
	 *
	 * @param document the document to get line information from
	 * @param line the line in the document, may be greater than the line count
	 * @param column the offset in the given line, may be greater than the line length
	 * @return the document offset corresponding to the line and column counts
	 */
	private static int computeOffset(IDocument document, int line, int column) {
		try {
			IRegion lineInfo= document.getLineInformation(line);
			int offsetInLine= Math.min(column, lineInfo.getLength());
			return lineInfo.getOffset() + offsetInLine;
		} catch (BadLocationException x) {
			if (line < 0) {
				return 0;
			}
			return document.getLength();
		}
	}

	@Override
	public int getStartLine() {
		return fStartLine;
	}

	@Override
	public int getStartColumn() {
		return fStartColumn;
	}

	@Override
	public int getEndLine() {
		return fEndLine;
	}

	@Override
	public int getEndColumn() {
		return fEndColumn;
	}

	@Override
	public String getText() {
		IDocument document= getDocument();
		if (document != null) {
			try {
				return new SelectionProcessor(document, fTabWidth).getText(this);
			} catch (BadLocationException x) {
				// ignore and default to super implementation
			}
		}
		return super.getText();
	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= super.hashCode();
		result= prime * result + fEndColumn;
		result= prime * result + fEndLine;
		result= prime * result + fStartColumn;
		result= prime * result + fStartLine;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		final BlockTextSelection other= (BlockTextSelection) obj;
		if (fEndColumn != other.fEndColumn) {
			return false;
		}
		if (fEndLine != other.fEndLine) {
			return false;
		}
		if (fStartColumn != other.fStartColumn) {
			return false;
		}
		if (fStartLine != other.fStartLine) {
			return false;
		}
		return true;
	}

	@Override
	public IRegion[] getRegions() {
		IDocument document= getDocument();
		if (document != null) {
			try {
				return new SelectionProcessor(document, fTabWidth).getRanges(this);
			} catch (BadLocationException x) {
				// default to single region behavior
			}
		}
		return new IRegion[] {new Region(getOffset(), getLength())};
	}

	/**
	 * @since 3.16
	 */
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("BlockTextSelection [offset: ").append(getOffset()); //$NON-NLS-1$
		sb.append(", startLine: ").append(fStartLine); //$NON-NLS-1$
		if (fEndLine != fStartLine) {
			sb.append(", endLine: ").append(fEndLine); //$NON-NLS-1$
		}
		sb.append(", startColumn: ").append(fStartColumn); //$NON-NLS-1$
		if (fEndColumn != fStartColumn) {
			sb.append(", endColumn").append(fEndColumn); //$NON-NLS-1$
		}
		if (getLength() != 0) {
			sb.append(", text: ").append(getText()); //$NON-NLS-1$
		}
		if (getDocument() != null) {
			sb.append(", document: ").append(getDocument()); //$NON-NLS-1$
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}
