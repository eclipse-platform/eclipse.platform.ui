/*******************************************************************************
* Copyright (c) 2025 SAP SE
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/
package org.eclipse.jface.internal.text.codemining;

import static org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation.disposeMinings;
import static org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation.getMultilineHeight;
import static org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation.hasAtLeastOneResolvedMiningNotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextLineSpacingProvider;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineFooterAnnotation;

/**
 * Code Mining annotation at the end of the document.
 *
 */
public class CodeMiningDocumentFooterAnnotation extends LineFooterAnnotation implements ICodeMiningAnnotation {

	/**
	 * List of resolved minings which contains current resolved minings and last resolved minings
	 * and null if mining is not resolved.
	 */
	private volatile ICodeMining[] fResolvedMinings;

	/** Immutable snapshot of the current minings, replaced on each update. */
	private volatile List<ICodeMining> fMinings;

	/**
	 * List of bounds minings
	 */
	private final List<Rectangle> fBounds;

	/**
	 * The current progress monitor
	 */
	private IProgressMonitor fMonitor;

	/** The key of the line spacing provider of the footer that reserves space in a text widget. */
	private static final String LINE_SPACING_PROVIDER_KEY= CodeMiningDocumentFooterAnnotation.class.getName() + ".lineSpacingProvider"; //$NON-NLS-1$

	/** The height reserved below the last line of the text widget, <code>0</code> if none. */
	private int fReservedHeight;

	/** The line spacing provider that reserves the space, <code>null</code> if none. */
	private StyledTextLineSpacingProvider fLineSpacingProvider;

	/**
	 * Code mining annotation constructor.
	 *
	 * @param position the position
	 * @param viewer the viewer
	 * @param onMouseHover the consumer to be called on mouse hover. If set, the implementor needs
	 *            to take care of setting the cursor if wanted.
	 * @param onMouseOut the consumer to be called on mouse out. If set, the implementor needs to
	 *            take care of resetting the cursor.
	 * @param onMouseMove the consumer to be called on mouse move
	 */
	public CodeMiningDocumentFooterAnnotation(Position position, ISourceViewer viewer, Consumer<MouseEvent> onMouseHover, Consumer<MouseEvent> onMouseOut, Consumer<MouseEvent> onMouseMove) {
		super(position, viewer, onMouseHover, onMouseOut, onMouseMove);
		fResolvedMinings= null;
		fMinings= List.of();
		fBounds= new ArrayList<>();
	}

	@Override
	public int getHeight() {
		List<ICodeMining> minings= fMinings;
		return hasAtLeastOneResolvedMiningNotEmpty(minings, fResolvedMinings)
				? getMultilineHeight(null, minings, super.getTextWidget(), super.getHeight())
				: 0;
	}

	/**
	 * Reserves the space needed to display this footer below the last line of the text widget, as
	 * line spacing of that line: unlike a bottom margin, which StyledText keeps at the bottom of
	 * every view port, it scrolls with the document.
	 * <p>
	 * StyledText adds the line spacing below each row of a wrapped line, so no space is reserved
	 * while the last line is wrapped.
	 * </p>
	 */
	public void updateReservedHeight() {
		StyledText textWidget= getTextWidget();
		if (textWidget == null || textWidget.isDisposed()) {
			return;
		}
		int footerHeight= getHeight();
		int height= footerHeight > 0 && isLastLineWrapped(textWidget) ? 0 : footerHeight;
		if (height == fReservedHeight) {
			return;
		}
		// replace the provider, so that StyledText measures the last line again
		releaseReservedHeight(textWidget);
		if (height > 0) {
			fReservedHeight= height;
			fLineSpacingProvider= lineIndex -> lineIndex == textWidget.getLineCount() - 1
					? Integer.valueOf(textWidget.getLineSpacing() + height)
					: null;
			textWidget.setData(LINE_SPACING_PROVIDER_KEY, fLineSpacingProvider);
			textWidget.setLineSpacingProvider(fLineSpacingProvider);
		}
	}

	private static boolean isLastLineWrapped(StyledText textWidget) {
		int lastLineOffset= textWidget.getOffsetAtLine(textWidget.getLineCount() - 1);
		return textWidget.getLocationAtOffset(lastLineOffset).y != textWidget.getLocationAtOffset(textWidget.getCharCount()).y;
	}

	private void releaseReservedHeight(StyledText textWidget) {
		// a footer created for the new end of the document may have reserved the space already
		if (fLineSpacingProvider != null && textWidget != null && !textWidget.isDisposed()
				&& textWidget.getData(LINE_SPACING_PROVIDER_KEY) == fLineSpacingProvider) {
			textWidget.setData(LINE_SPACING_PROVIDER_KEY, null);
			textWidget.setLineSpacingProvider(null);
		}
		fLineSpacingProvider= null;
		fReservedHeight= 0;
	}

	@Override
	public void update(List<ICodeMining> minings, IProgressMonitor monitor) {
		List<ICodeMining> oldMinings= fMinings;
		ICodeMining[] resolvedMinings= fResolvedMinings;
		if (resolvedMinings == null || (resolvedMinings.length != minings.size())) {
			// size of resolved minings are different from size of minings to update, initialize it with size of minings to update
			resolvedMinings= new ICodeMining[minings.size()];
		}
		// fill valid resolved minings with old minings.
		int length= Math.min(oldMinings.size(), minings.size());
		for (int i= 0; i < length; i++) {
			ICodeMining mining= oldMinings.get(i);
			if (mining.getLabel() != null) {
				resolvedMinings[i]= mining;
			}
		}
		fResolvedMinings= resolvedMinings;
		fMonitor= monitor;
		fMinings= Collections.unmodifiableList(new ArrayList<>(minings));
		disposeMinings(oldMinings);
	}

	@Override
	public void markDeleted(boolean deleted) {
		super.markDeleted(deleted);
		if (deleted) {
			StyledText textWidget= getTextWidget();
			if (textWidget != null && !textWidget.isDisposed()) {
				// annotations are updated outside of the UI thread
				textWidget.getDisplay().execute(() -> releaseReservedHeight(textWidget));
			}
			List<ICodeMining> oldMinings= fMinings;
			fMinings= List.of();
			fResolvedMinings= null;
			disposeMinings(oldMinings);
		}
	}

	@Override
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		int singleLineHeight= super.getHeight();
		CodeMiningLineHeaderAnnotation.draw(fMinings, fBounds, singleLineHeight, fResolvedMinings, gc, textWidget, color, x, y, this::redraw);
	}

	@Override
	public void redraw() {
		// redraw codemining annotation is done only if all current minings are resolved.
		for (ICodeMining mining : fMinings) {
			if (!mining.isResolved()) {
				// one of mining is not resolved, resolve it and then redraw the annotation.
				mining.resolve(getViewer(), fMonitor).thenRunAsync(() -> {
					this.redraw();
				});
				return;
			}
		}
		// all minings are resolved, redraw the annotation
		super.redraw();
	}

	@Override
	public Consumer<MouseEvent> getAction(MouseEvent e) {
		ICodeMining mining= CodeMiningManager.getValidCodeMiningAtLocation(fResolvedMinings, fBounds, e.x, e.y);
		return mining != null ? mining.getAction() : null;
	}

	@Override
	public boolean isInVisibleLines() {
		return super.isInVisibleLines();
	}

	/**
	 * @since 3.31
	 */
	@Override
	public List<ICodeMining> getMinings() {
		return fMinings;
	}
}
