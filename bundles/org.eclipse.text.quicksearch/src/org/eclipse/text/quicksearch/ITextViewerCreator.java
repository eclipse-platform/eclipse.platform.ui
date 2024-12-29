/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;

/**
 * A factory object for instances of <code>ITextViewerHandle</code> that wraps text viewer used for presenting
 * quicksearch matches within content of their containing document.
 *
 * @see ITextViewerHandle
 * @since 1.3
 */
public interface ITextViewerCreator {

	/**
	 * Creates a new text viewer under the given SWT parent control and returns handle for it. This method <b>must</b>
	 * always create new text viewer & handle when called since each opened quick search dialog will call this method
	 * passing unique <code>parent</code> to display the viewer in the dialog. Dialog then re-uses returned handle
	 * (viewer) when this <code>ITextViewerCreator</code> is again chosen to present some (possibly other) quicksearch
	 * match. If other <code>ITextViewerCreator</code> contributor is chosen to present a match in the same dialog,
	 * this viewer is just made not visible (by means of using {@link StackLayout} in the parent composites hierarchy).
	 * <p>
	 * It's recommended to use {@link SourceViewerHandle} configured by {@link SourceViewerConfigurer} since they
	 * implement common aspects expected from quicksearch text viewers.
	 *
	 * @param parent the SWT parent control under which to create the viewer's SWT control
	 * @return a new text viewer handle
	 */
	ITextViewerHandle createTextViewer(Composite parent);

	/**
	 * Text viewer handle is a wrapper for a text viewer displayed in quicksearch dialog to present matches.
	 *
	 * @see ITextViewerCreator#createTextViewer(Composite)
	 */
	public interface ITextViewerHandle {

		/**
		 * Returns number of lines of text the client area of this handle's text viewer would display with its current
		 * size. Value is used to calculate regions subsequently passed to
		 * {@link #focusMatch(IRegion, IRegion, int, IRegion) focusMatch()}.
		 *
		 * @return number of visible text lines
		 * @see #focusMatch(IRegion, IRegion, int, IRegion)
		 */
		int getVisibleLines();

		/**
		 * Sets input to the text viewer of this handle. Only if different document is to be presented in this handle's
		 * viewer, this method is called with new input document. If different match in the already displayed document
		 * is to be presented, only {@link #focusMatch(IRegion, IRegion, int, IRegion) focusMatch()} is called.
		 *
		 * @param document the document to present in the viewer (content of the <code>file</code>)
		 * @param allMatchesStyles common text styles to apply in order to highlight all found matches in the document
		 * @param file the file contents of which is the <code>document</code>
		 * @see #focusMatch(IRegion, IRegion, int, IRegion)
		 */
		void setViewerInput(IDocument document, StyleRange[] allMatchesStyles, IFile file);

		/**
		 * Focuses on the specific match (selected amongst quicksearch results) within content of its containing
		 * document, which was previously set as viewer's input by call to
		 * {@link #setViewerInput(IDocument, StyleRange[], IFile) setViewerInput()}. This is expected to mean:<br>
		 * <ul>
		 * <li>limiting presented content of the input document (reachable by scrolling) to <code>visibleRegion</code>
		 * <li>presenting <code>revealedRange</code> part of the input document (vertical scrolling)
		 * <li>highlighting line of the match described by <code>matchLine</code>
		 * <li>making sure actual match described by <code>matchRange</code> is visible (horizontal scrolling)
		 * <li>positioning caret to the start of the match described by <code>matchRange</code>
		 * </ul>
		 *
		 * Passed regions are adjusted to have selected match vertically centered in the viewer of this handle since
		 * they are derived from the number of lines presented by the viewer (previous call to
		 * {@link #getVisibleLines()}).
		 * <p>
		 * All parameters are coordinates within the input document previously set by call to
		 * {@link #setViewerInput(IDocument, StyleRange[], IFile) setViewerInput()}.
		 *
		 * @param visibleRegion bounds of the portion of input document to present in the viewer
		 * @param revealedRange bounds of the part of input document to reveal (for vertical scroll)
		 * @param matchLine line number in the input document where match is located
		 * @param matchRange bounds of the focused match in the input document (for horizontal scroll)
		 * @see #getVisibleLines()
		 * @see #setViewerInput(IDocument, StyleRange[], IFile)
		 */
		void focusMatch(IRegion visibleRegion, IRegion revealedRange, int matchLine, IRegion matchRange);
	}

}
