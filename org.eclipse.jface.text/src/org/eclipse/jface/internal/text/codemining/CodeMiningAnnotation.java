/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide CodeMining support with CodeMiningManager - Bug 527720
 */
package org.eclipse.jface.internal.text.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;

/**
 * Code Mining annotation.
 *
 * @since 3.13
 */
public class CodeMiningAnnotation extends LineHeaderAnnotation {

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
	 * The viewer
	 */
	private ISourceViewer fViewer;

	/**
	 * The current progress monitor
	 */
	private IProgressMonitor fMonitor;

	/**
	 * Code mining annotation constructor.
	 *
	 * @param position the position
	 * @param viewer the viewer
	 */
	public CodeMiningAnnotation(Position position, ISourceViewer viewer) {
		super(position, viewer.getTextWidget());
		fResolvedMinings= null;
		fMinings= new ArrayList<>();
		this.fViewer= viewer;
	}

	/**
	 * Update code minings.
	 *
	 * @param minings the minings to update.
	 * @param monitor the monitor
	 */
	public void update(List<ICodeMining> minings, IProgressMonitor monitor) {
		disposeMinings();
		fMonitor= monitor;
		fMinings.addAll(minings);
		if (fResolvedMinings == null || (fResolvedMinings.length != fMinings.size())) {
			// size of resolved minings are different from size of minings to update, initialize it with size of minings to update
			fResolvedMinings= new ICodeMining[fMinings.size()];
		}
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
	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		gc.setForeground(color);
		gc.setBackground(textWidget.getBackground());
		List<ICodeMining> minings= new ArrayList<>(fMinings);
		int nbDraw= 0;
		int separatorWidth= -1;
		boolean redrawn= false;
		for (int i= 0; i < minings.size(); i++) {
			ICodeMining mining= minings.get(i);
			if (!mining.isResolved()) {
				// the mining is not resolved.
				if (!redrawn) {
					// redraw the annotation when mining is resolved.
					redraw();
					redrawn= true;
				}
				// try to get the last resolved mining.
				if (fResolvedMinings != null) {
					mining= fResolvedMinings[i];
				}
				if (mining == null) {
					// the last mining was not resolved, don't draw it.
					continue;
				}
			} else {
				// mining is resolved, update the resolved mining list
				fResolvedMinings[i]= mining;
			}
			// draw the mining
			if (nbDraw > 0) {
				gc.drawText(SEPARATOR, x, y);
				if (separatorWidth == -1) {
					separatorWidth= gc.stringExtent(SEPARATOR).x;
				}
				x+= separatorWidth;
			}
			x+= mining.draw(gc, textWidget, color, x, y).x;
			nbDraw++;
		}
	}

	@Override
	public void redraw() {
		// redraw codemining annotation is done only if all current minings are resolved.
		List<ICodeMining> minings= new ArrayList<>(fMinings);
		for (ICodeMining mining : minings) {
			if (!mining.isResolved()) {
				// one of mining is not resolved, resolve it and then redraw the annotation.
				mining.resolve(fViewer, fMonitor).thenRunAsync(() -> {
					this.redraw();
				});
				return;
			}
		}
		// all minings are resolved, redraw the annotation
		super.redraw();
	}

}
