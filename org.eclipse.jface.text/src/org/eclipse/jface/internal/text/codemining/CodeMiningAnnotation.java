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

import org.eclipse.jface.text.ITextViewer;
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

	private final List<ICodeMining> fMinings;

	private ITextViewer fViewer;

	private IProgressMonitor fMonitor;

	public CodeMiningAnnotation(Position position, ISourceViewer viewer) {
		super(position, viewer.getTextWidget());
		fMinings= new ArrayList<>();
		this.fViewer= viewer;
	}

	public void update(List<ICodeMining> minings, IProgressMonitor monitor) {
		disposeMinings();
		fMonitor= monitor;
		fMinings.addAll(minings);
	}

	@Override
	public void markDeleted(boolean deleted) {
		super.markDeleted(deleted);
		if (deleted) {
			disposeMinings();
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
		for (ICodeMining mining : minings) {
			if (!mining.isResolved()) {
				// Don't draw mining which is not resolved
				// then redraw the annotation when mining is ready.
				mining.resolve(fViewer, fMonitor).thenRun(() -> this.redraw());
				continue;
			}
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

}
