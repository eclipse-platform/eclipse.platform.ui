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

import org.eclipse.jface.text.IInformationControlCreator;


/**
 * A hover for line oriented diffs. It determines the text to show as hover for a certain line in the 
 * document.
 * 
 * @since 3.0
 */
public class LineChangeHover implements IAnnotationHover, IAnnotationHoverExtension {

	/** The lines which will be overlaid by the hover. */
	private Point fLineRange;

	/*
	 * @see IAnnotationHover#getHoverInfo(ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		String content= computeContent(sourceViewer, lineNumber);
		return formatSource(content);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getLineRange(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public Point getLineRange(ISourceViewer viewer, int line) {
		fLineRange= computeLineRange(viewer, line);
		return fLineRange;
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
			for (int pos= 0; pos < sb.length(); pos++) {
				if (sb.charAt(pos) == '\t')
					sb.replace(pos, pos + 1, "    "); //$NON-NLS-1$
			}
			return sb.toString(); //$NON-NLS-1$//$NON-NLS-2$
		}
		return content;
	}

	/**
	 * Computes the content of the hover for the document contained in <code>viewer</code> on 
	 * line <code>line</code>. 
	 * 
	 * @param viewer the connected viewer
	 * @param line the line for which to compute the hover info
	 * @return The hover content corresponding to the parameters 
	 * @see #getHoverInfo()
	 */
	private String computeContent(ISourceViewer viewer, int line) {
		Point contentRange= computeContentRange(viewer, line);
		if (contentRange == null || fLineRange == null)
			return null;
		ILineDiffer differ= getDiffer(viewer);
		if (differ == null)
			return null;
		// sanity test line argument
		if (line > fLineRange.y + 1 || line < fLineRange.x - 1)
			return null;

		final List lines= new LinkedList();
		for (int l= contentRange.x; l <= contentRange.y; l++) {
			lines.add(differ.getLineInfo(l));
		}
		final int max= viewer.getBottomIndex();
		return decorateText(lines, max - fLineRange.x + 1);
	}

	/**
	 * Takes a list of <code>ILineDiffInfo</code>s and computes a hover of at most <code>maxLines</code>.
	 * Added lines are prefixed with a <code>'+'</code>, changed lines with <code>'>'</code> and
	 * deleted lines with <code>'-'</code>.
	 * <p>Deleted and added lines can even each other out, so that a number of deleted lines get 
	 * displayed where - in the current document - the added lines are.
	 * 
	 * @param annotations a <code>List</code> of <code>ILineDiffInfo</code>
	 * @param maxLines the maximum number of lines. Note that adding up all annotations might give
	 * more than that due to deleted lines.
	 * @return a <code>String</code> suitable for hover display
	 */
	protected String decorateText(List annotations, int maxLines) {
		/* maxLines controls the size of the hover (not more than what fits into the display are of
		 * the viewer).
		 * added controls how many lines are added - added lines are 
		 */
		String text= new String();
		int added= 0;
		for (Iterator it= annotations.iterator(); it.hasNext();) {
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
		// trim ending line delimiter
		while (text.length() > 0 && Character.isWhitespace(text.charAt(text.length() - 1))) {
			text= text.substring(0, text.length() - 1);
		}
		return text;
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
			IAnnotationModel diffModel= ((IAnnotationModelExtension)model).getAnnotationModel(ILineDiffer.ID);
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
	 * @return the computed content range
	 */
	private Point computeContentRange(ISourceViewer viewer, int line) {
		ILineDiffer differ= getDiffer(viewer);
		if (differ == null)
			return null;

		Point lineRange= getLineRange(viewer, line);
		if (lineRange == null)
			return null;

		final int min= viewer.getTopIndex(); // the minimum line

		// here comes the hack: since we only get deleted lines *after* a line, we decrease one further if conditions met
		int l= lineRange.x - 1;
		ILineDiffInfo info= differ.getLineInfo(l);
		if (l >= min && info != null && info.getType() == ILineDiffInfo.UNCHANGED && info.getRemovedLinesBelow() > 0)
			return new Point(l, lineRange.y);
		else
			return lineRange;
	}

	/**
	 * Computes the block of lines which form a contiguous block of changes covering <code>line</code>.
	 * 
	 * @param viewer the source viewer showing
	 * @param line the line of interest
	 * @return a <code>Point</code> the x and y coordinates of which correspond to the first and last
	 * lines in the document that designate the block of changes covering <code>line</code>
	 */
	private Point computeLineRange(ISourceViewer viewer, int line) {
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
		final int min= viewer.getTopIndex(); // the minimum line

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

		int first= l + 1;

		// forward search
		final int max= viewer.getBottomIndex(); // the maximum line

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
