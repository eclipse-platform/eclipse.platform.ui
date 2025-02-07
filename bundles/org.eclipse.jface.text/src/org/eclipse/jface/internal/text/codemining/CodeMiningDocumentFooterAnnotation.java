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
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
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
	private ICodeMining[] fResolvedMinings;

	/**
	 * List of current resolved/unresolved minings
	 */
	private final List<ICodeMining> fMinings;

	/**
	 * List of bounds minings
	 */
	private final List<Rectangle> fBounds;

	/**
	 * The current progress monitor
	 */
	private IProgressMonitor fMonitor;

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
		fMinings= new ArrayList<>();
		fBounds= new ArrayList<>();
	}

	@Override
	public int getHeight() {
		return hasAtLeastOneResolvedMiningNotEmpty(fMinings, fResolvedMinings)
				? getMultilineHeight(null, fMinings, super.getTextWidget(), super.getHeight())
				: 0;
	}

	@Override
	public void update(List<ICodeMining> minings, IProgressMonitor monitor) {
		if (fResolvedMinings == null || (fResolvedMinings.length != minings.size())) {
			// size of resolved minings are different from size of minings to update, initialize it with size of minings to update
			fResolvedMinings= new ICodeMining[minings.size()];
		}
		// fill valid resolved minings with old minings.
		int length= Math.min(fMinings.size(), minings.size());
		for (int i= 0; i < length; i++) {
			ICodeMining mining= fMinings.get(i);
			if (mining.getLabel() != null) {
				fResolvedMinings[i]= mining;
			}
		}
		disposeMinings(fMinings);
		fMonitor= monitor;
		fMinings.addAll(minings);
	}

	@Override
	public void markDeleted(boolean deleted) {
		super.markDeleted(deleted);
		if (deleted) {
			disposeMinings(fMinings);
			fResolvedMinings= null;
		}
	}

	@Override
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		int singleLineHeight= super.getHeight();
		CodeMiningLineHeaderAnnotation.draw(fMinings, fBounds, singleLineHeight, fResolvedMinings, gc, textWidget, color, x, y, new Runnable() {

			@Override
			public void run() {
				redraw();
			}
		});
	}

	@Override
	public void redraw() {
		// redraw codemining annotation is done only if all current minings are resolved.
		List<ICodeMining> minings= new ArrayList<>(fMinings);
		for (ICodeMining mining : minings) {
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
}
