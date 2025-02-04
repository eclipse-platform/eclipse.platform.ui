/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide CodeMining support with CodeMiningManager - Bug 527720
 */
package org.eclipse.jface.internal.text.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;

/**
 * Code Mining annotation in line content.
 *
 * @since 3.13
 */
public class CodeMiningLineContentAnnotation extends LineContentAnnotation implements ICodeMiningAnnotation {

	private static final String SEPARATOR= " | "; //$NON-NLS-1$

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

	private final boolean afterPosition;

	/**
	 * Code mining annotation constructor.
	 *
	 * @param position the position
	 * @param viewer   the viewer
	 */
	public CodeMiningLineContentAnnotation(Position position, ISourceViewer viewer) {
		this(position, viewer, false);
	}

	/**
	 * Code mining annotation constructor.
	 *
	 * @param position the position
	 * @param viewer the viewer
	 */
	public CodeMiningLineContentAnnotation(Position position, ISourceViewer viewer, boolean afterPosition) {
		this(position, viewer, afterPosition, null, null, null);
	}

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
	public CodeMiningLineContentAnnotation(Position position, ISourceViewer viewer, boolean afterPosition, Consumer<MouseEvent> onMouseHover, Consumer<MouseEvent> onMouseOut,
			Consumer<MouseEvent> onMouseMove) {
		super(position, viewer, onMouseHover, onMouseOut, onMouseMove);
		fResolvedMinings= null;
		fMinings= new ArrayList<>();
		fBounds= new ArrayList<>();
		this.afterPosition= afterPosition;
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
				// mining was resolved without an error.
				fResolvedMinings[i]= mining;
			}
		}
		disposeMinings();
		fMonitor= monitor;
		fMinings.addAll(minings);
	}

	@Override
	public void markDeleted(boolean deleted) {
		super.markDeleted(deleted);
		if (deleted) {
			disposeMinings();
			fResolvedMinings= null;
		}
	}

	private void disposeMinings() {
		fMinings.stream().forEach(ICodeMining::dispose);
		fMinings.clear();
	}

	@Override
	protected int drawAndComputeWidth(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		List<ICodeMining> minings= new ArrayList<>(fMinings);
		int nbDraw= 0;
		int originalX= x;
		int separatorWidth= -1;
		boolean redrawn= false;
		fBounds.clear();
		for (int i= 0; i < minings.size(); i++) {
			ICodeMining mining= minings.get(i);
			// try to get the last resolved mining.
			ICodeMining lastResolvedMining= (fResolvedMinings != null && fResolvedMinings.length > i) ? fResolvedMinings[i] : null;
			if (mining.getLabel() != null) {
				// mining is resolved without error, update the resolved mining list
				fResolvedMinings[i]= mining;
			} else if (!mining.isResolved()) {
				// the mining is not resolved, draw the last resolved mining
				mining= lastResolvedMining;
				if (!redrawn) {
					// redraw the annotation when mining is resolved.
					redraw();
					redrawn= true;
				}
			} else {
				// the mining is resolved with error, draw the last resolved mining
				mining= lastResolvedMining;
			}
			if (!CodeMiningManager.isValidMining(mining)) {
				// ignore the draw of mining
				continue;
			}
			// draw the mining
			if (nbDraw > 0) {
				gc.drawText(SEPARATOR, x, y);
				if (separatorWidth == -1) {
					separatorWidth= gc.stringExtent(SEPARATOR).x;
				}
				x+= separatorWidth;
			}
			@SuppressWarnings("null")
			Point loc= mining.draw(gc, textWidget, color, x, y);
			fBounds.add(new Rectangle(x, y, loc.x, loc.y));
			x+= loc.x;
			nbDraw++;
		}
		return x - originalX;
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

	public final boolean isAfterPosition() {
		return afterPosition;
	}
}
