/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.action.IStatusLineManager;

import org.eclipse.jface.text.IMarkRegionTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;

/**
 * Default implementation of <code>IMarkRegionTarget</code> using <code>ITextViewer</code>
 * and <code>IStatusLineManager</code>.
 *
 * @since 2.0
 */
public class MarkRegionTarget implements IMarkRegionTarget {

	/** The text viewer. */
	private final ITextViewer fViewer;
	/** The status line. */
	private final IStatusLineManager fStatusLine;

	/**
	 * Creates a MarkRegionTaret.
	 *
	 * @param viewer the text viewer
	 * @param manager the status line manager
	 */
	public MarkRegionTarget(ITextViewer viewer, IStatusLineManager manager) {
		fViewer= viewer;
		fStatusLine= manager;
	}

	/*
	 * @see IMarkregion#setMarkAtCursor(boolean)
	 */
	public void setMarkAtCursor(boolean set) {

		if (!(fViewer instanceof ITextViewerExtension))
			return;

		ITextViewerExtension viewerExtension= ((ITextViewerExtension) fViewer);

		if (set) {
			Point selection= fViewer.getSelectedRange();
			viewerExtension.setMark(selection.x);

			fStatusLine.setErrorMessage(""); //$NON-NLS-1$
			fStatusLine.setMessage(EditorMessages.Editor_mark_status_message_mark_set);

		} else {
			viewerExtension.setMark(-1);

			fStatusLine.setErrorMessage(""); //$NON-NLS-1$
			fStatusLine.setMessage(EditorMessages.Editor_mark_status_message_mark_cleared);
		}
	}

	/*
	 * @see IMarkregion#swapMarkAndCursor()
	 */
	public void swapMarkAndCursor() {

		if (!(fViewer instanceof ITextViewerExtension))
			return;

		ITextViewerExtension viewerExtension= ((ITextViewerExtension) fViewer);

		int markPosition= viewerExtension.getMark();
		if (markPosition == -1) {
			fStatusLine.setErrorMessage(EditorMessages.MarkRegionTarget_markNotSet);
			fStatusLine.setMessage(""); //$NON-NLS-1$
			return;
		}

		if (!isVisible(fViewer, markPosition)) {
			fStatusLine.setErrorMessage(EditorMessages.MarkRegionTarget_markNotVisible);
			fStatusLine.setMessage(""); //$NON-NLS-1$
			return;
		}

		Point selection= fViewer.getSelectedRange();
		viewerExtension.setMark(selection.x);

		fViewer.setSelectedRange(markPosition, 0);
		fViewer.revealRange(markPosition, 0);

		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(EditorMessages.Editor_mark_status_message_mark_swapped);
	}

	/**
	 * Tells whether the given offset is visible in the given text viewer.
	 *
	 * @param viewer the text viewer
	 * @param offset the offset to check
	 * @return <code>true</code> if the given offset is visible in the given text viewer
	 *
	 * @since 2.1
	 */
	protected final static boolean isVisible(ITextViewer viewer, int offset) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.modelOffset2WidgetOffset(offset) >= 0;
		}
		IRegion region= viewer.getVisibleRegion();
		int vOffset= region.getOffset();
		return (vOffset <= offset &&  offset <= vOffset + region.getLength());
	}
}
