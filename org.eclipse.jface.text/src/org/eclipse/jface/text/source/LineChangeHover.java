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
package org.eclipse.jface.text.source;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;


/**
 * A hover for line oriented diffs. It determines the text to show as hover for a certain line in the 
 * document.
 * 
 * @since 3.0
 */
public class LineChangeHover implements IAnnotationHover, IAnnotationHoverExtension {

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int, int, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber, int first, int number) {
		int last= first + number - 1;
		String content= computeContent(sourceViewer, lineNumber, first, last);
		return formatSource(content);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		return getHoverInfo(sourceViewer, lineNumber, 0, Integer.MAX_VALUE);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getLineRange(org.eclipse.jface.text.source.ISourceViewer, int, int, int)
	 */
	public ITextSelection getLineRange(ISourceViewer viewer, int line, int first, int number) {
		int last= first + number - 1;
		Point lineRange= computeLineRange(viewer, line, first, last);
		if (viewer != null && lineRange != null && lineRange.x != -1 && lineRange.y != -1) {
			try {
				IDocument document= viewer.getDocument();
				if (document != null) {
					int offset= document.getLineOffset(lineRange.x);
					// make sure that content shifts due to deleted lines are shifted one down
					IRegion endLine= document.getLineInformation(Math.max(lineRange.x, lineRange.y));
					int endOffset= endLine.getOffset() + endLine.getLength();
					// consider add one to comprise last line
					int length= endOffset - offset + 1;
					return new TextSelection(document, offset, length); 
				}
			} catch (BadLocationException e) {
			}
		}
		return new TextSelection(0, 0);
	}

	/**
	 * Formats the source w/ syntax coloring etc. This implementation replaces tabs with spaces.
	 * May be overridden by subclasses.
	 * 
	 * @param content the hover content
	 * @return <code>content</code> reformatted
	 */
	protected String formatSource(String content) {
		if (content != null) {
			StringBuffer sb= new StringBuffer(content);
			final String tabReplacement= getTabReplacement();
			for (int pos= 0; pos < sb.length(); pos++) {
				if (sb.charAt(pos) == '\t')
					sb.replace(pos, pos + 1, tabReplacement);
			}
			return sb.toString();
		}
		return content;
	}

	/**
	 * Consults the preferences for the number of spaces a tab should be displayed as.
	 * 
	 * @return a String consisting of spaces that will replace one tab character in the 
	 * displayed source.
	 */
	protected String getTabReplacement() {
		return "    "; //$NON-NLS-1$
	}

	/**
	 * Computes the content of the hover for the document contained in <code>viewer</code> on 
	 * line <code>line</code>. 
	 * 
	 * @param viewer the connected viewer
	 * @param line the line for which to compute the hover info
	 * @param first the first line in <code>viewer</code>'s document to consider
	 * @param last the last line in <code>viewer</code>'s document to consider
	 * @return The hover content corresponding to the parameters 
	 * @see #getHoverInfo()
	 */
	private String computeContent(ISourceViewer viewer, int line, int first, int last) {
		Point contentRange= computeContentRange(viewer, line, first, last);
		if (contentRange == null)
			return null;
		ILineDiffer differ= getDiffer(viewer);
		if (differ == null)
			return null;
		// sanity test line argument
		if (line > contentRange.y + 1 || line < contentRange.x - 1)
			return null;

		final List lines= new LinkedList();
		for (int l= contentRange.x; l <= contentRange.y; l++) {
			lines.add(differ.getLineInfo(l));
		}
		final int max= viewer.getBottomIndex();
		return decorateText(lines, max - contentRange.x + 1);
	}

	/**
	 * Takes a list of <code>ILineDiffInfo</code>s and computes a hover of at most <code>maxLines</code>.
	 * Added lines are prefixed with a <code>'+'</code>, changed lines with <code>'>'</code> and
	 * deleted lines with <code>'-'</code>.
	 * <p>Deleted and added lines can even each other out, so that a number of deleted lines get 
	 * displayed where - in the current document - the added lines are.
	 * 
	 * @param diffInfos a <code>List</code> of <code>ILineDiffInfo</code>
	 * @param maxLines the maximum number of lines. Note that adding up all annotations might give
	 * more than that due to deleted lines.
	 * @return a <code>String</code> suitable for hover display
	 */
	protected String decorateText(List diffInfos, int maxLines) {
		/* maxLines controls the size of the hover (not more than what fits into the display are of
		 * the viewer).
		 * added controls how many lines are added - added lines are 
		 */
		String text= new String();
		int added= 0;
		for (Iterator it= diffInfos.iterator(); it.hasNext();) {
			ILineDiffInfo info= (ILineDiffInfo)it.next();
			String[] original= info.getOriginalText();
			int type= info.getType();
			int i= 0;
			if (type == ILineDiffInfo.ADDED)
				added++; //$NON-NLS-1$
			else if (type == ILineDiffInfo.CHANGED) {
				text += "> " + (original.length > 0 ? original[i++] : ""); //$NON-NLS-1$ //$NON-NLS-2$
				maxLines--;
			} else if (type == ILineDiffInfo.UNCHANGED) {
				maxLines++;
			}
			if (maxLines == 0)
				return trimTrailing(text);
			for (; i < original.length; i++) {
				text += "- " + original[i]; //$NON-NLS-1$
				added--;
				if (--maxLines == 0)
					return trimTrailing(text);
			}
		}
		text= text.trim();
		if (text.length() == 0 && added-- > 0)
			text += "+ "; //$NON-NLS-1$
		while (added-- > 0)
			text += "\n+ "; //$NON-NLS-1$
		return text;
	}

	/**
	 * Trims trailing spaces
	 * 
	 * @param text a <code>String</code>
	 * @return a copy of <code>text</code> with trailing spaces removed
	 */
	private String trimTrailing(String text) {
		int pos= text.length() - 1;
		while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
			pos--;
		}
		return text.substring(0, pos + 1);
	}

	/**
	 * Extracts the line differ - if any - from the viewer's document's annotation model.
	 * @param viewer the viewer
	 * @return a line differ for the document displayed in viewer, or <code>null</code>.
	 */
	private ILineDiffer getDiffer(ISourceViewer viewer) {
		// return the upper left corner of the first hover line
		IAnnotationModel model= viewer.getAnnotationModel();

		if (model == null)
			return null;
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModel diffModel= ((IAnnotationModelExtension)model).getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
			if (diffModel != null)
				model= diffModel;
		}
		if (model instanceof ILineDiffer)
			return (ILineDiffer)model;
		else
			return null;
	}

	/**
	 * Computes the content range, which is either equal to the line range or the line range plus
	 * one line at the start (due to <code>ILineDiffInfo</code> implementation).
	 * 
	 * @param viewer the connected viewer
	 * @param line the achor line
	 * @param first the first line in <code>viewer</code>'s document to consider
	 * @param last the last line in <code>viewer</code>'s document to consider
	 * @return the computed content range
	 */
	private Point computeContentRange(ISourceViewer viewer, int line, int first, int last) {
		ILineDiffer differ= getDiffer(viewer);
		if (differ == null)
			return null;

		Point lineRange= computeLineRange(viewer, line, first, last);
		if (lineRange == null)
			return null;

		// here comes the hack: since we only get deleted lines *after* a line, we decrease one further if conditions met
		int l= lineRange.x - 1;
		ILineDiffInfo info= differ.getLineInfo(l);
		if (l >= first - 1 && info != null && info.getType() == ILineDiffInfo.UNCHANGED && info.getRemovedLinesBelow() > 0)
			return new Point(l, lineRange.y);
		else
			return lineRange;
	}

	/**
	 * Computes the block of lines which form a contiguous block of changes covering <code>line</code>.
	 * 
	 * @param viewer the source viewer showing
	 * @param line the line which a hover is displayed for
	 * @param min the first line in <code>viewer</code>'s document to consider
	 * @param max the last line in <code>viewer</code>'s document to consider
	 * @return the selection in the document displayed in <code>viewer</code> containing <code>line</code> 
	 * that is covered by the hover information returned by the receiver.
	 */
	protected Point computeLineRange(ISourceViewer viewer, int line, int min, int max) {
		/* Algorithm: 
		 * All lines that have changes to themselves (added, changed) are taken that form a 
		 * contiguous block of lines that includes <code>line</code>.
		 * 
		 * If <code>line</code> is itself unchanged, if there is a deleted line either above or 
		 * below, or both, the lines +/- 1 from <code>line</code> are included in the search as well, 
		 * without applying this last rule to them, though. (I.e., if <code>line</code> is unchanged,
		 * but has a deleted line above, this one is taken in. If the line above has changes, the block
		 * is extended from there. If the line has no changes itself, the search stops).
		 * 
		 * The block never extends the visible line range of the viewer.
		 */

		ILineDiffer differ= getDiffer(viewer);
		if (differ == null)
			return null;

		// backward search

		int l= line;
		ILineDiffInfo info= differ.getLineInfo(l);
		// if this is a special case, we'll start the search one above line
		if (l >= min && info != null && info.getType() == ILineDiffInfo.UNCHANGED && info.getRemovedLinesAbove() > 0) {
			info= differ.getLineInfo(--l);
		}

		// search backwards until a line has no changes to itself
		while (l >= min && info != null && (info.getType() == ILineDiffInfo.CHANGED || info.getType() == ILineDiffInfo.ADDED)) {
			info= differ.getLineInfo(--l);
		}
		
		// correct overrun
//		int first= l < line ? l + 1 : l;
		int first= l + 1;

		// forward search

		l= line;
		info= differ.getLineInfo(l);
		// if this is a special case, we'll start the search one below line
		if (l <= max && info != null && info.getType() == ILineDiffInfo.UNCHANGED && info.getRemovedLinesBelow() > 0) {
			info= differ.getLineInfo(++l);
		}
		// search forward until a line has no changes to itself
		while (l <= max && info != null && (info.getType() == ILineDiffInfo.CHANGED || info.getType() == ILineDiffInfo.ADDED)) {
			info= differ.getLineInfo(++l);
		}
		
		// correct overrun
		int last= l - 1;

		return new Point(first, last);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getInformationControlCreator()
	 */
	public IInformationControlCreator getInformationControlCreator() {
		return null;
	}
}
