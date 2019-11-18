/*******************************************************************************
 *  Copyright (c) 2018, 2019 Angelo ZERR and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      Angelo Zerr <angelo.zerr@gmail.com> - Bug 527720 - [CodeMining] Line number in vertical ruler can be not synchronized with line header annotation
 *      Thomas Wolf <thomas.wolf@paranor.ch> - Bug 553133
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.JFaceTextUtil;

/**
 * Tracks line height changes of visible lines of a given {@link StyledText}.
 */
class VisibleLinesTracker implements PaintListener {

	private static final String DATA_KEY= VisibleLinesTracker.class.getName();

	/**
	 * The viewer to track.
	 */
	private final ITextViewer viewer;

	/**
	 * The previous bottom model line index.
	 */
	private int oldModelBottom= -1;

	/**
	 * The previous bottom widget line index.
	 */
	private int oldWidgetBottom= -1;

	/**
	 * The previous bottom line pixel.
	 */
	private int oldBottomPixel;

	/**
	 * List of handler to call when a visible line height change.
	 */
	private final LinkedHashSet<Consumer<StyledText>> handlers= new LinkedHashSet<>();

	/**
	 * Constructor to track line height change of visible lines of the {@link StyledText} of the
	 * given viewer.
	 *
	 * @param viewer the viewer to track
	 */
	private VisibleLinesTracker(ITextViewer viewer) {
		this.viewer= viewer;
	}

	@Override
	public void paintControl(PaintEvent e) {
		StyledText textWidget= viewer.getTextWidget();
		// track if bottom line index or bottom line pixel changed.
		if (oldModelBottom == -1) {
			oldWidgetBottom= JFaceTextUtil.getPartialBottomIndex(textWidget);
			oldModelBottom= JFaceTextUtil.widgetLine2ModelLine(viewer, oldWidgetBottom);
			oldBottomPixel= JFaceTextUtil.getLinePixel(textWidget, oldWidgetBottom);
			return;
		}
		int newWidgetBottom= JFaceTextUtil.getPartialBottomIndex(textWidget);
		int newModelBottom= JFaceTextUtil.widgetLine2ModelLine(viewer, newWidgetBottom);
		int newBottomPixel= JFaceTextUtil.getLinePixel(textWidget, newWidgetBottom);
		if (newWidgetBottom != oldWidgetBottom || newModelBottom != oldModelBottom || newBottomPixel != oldBottomPixel) {
			oldWidgetBottom= newWidgetBottom;
			oldModelBottom= newModelBottom;
			oldBottomPixel= newBottomPixel;
			handlers.forEach(handler -> handler.accept(textWidget));
		}
	}

	/**
	 * Track the line height change of the {@link StyledText} of the given handler an call the given
	 * handler.
	 *
	 * @param viewer the viewer to track
	 * @param handler the handler to call when line height change.
	 */
	static void track(ITextViewer viewer, Consumer<StyledText> handler) {
		StyledText textWidget= viewer != null ? viewer.getTextWidget() : null;
		if (textWidget == null) {
			return;
		}
		VisibleLinesTracker tracker= (VisibleLinesTracker) textWidget.getData(DATA_KEY);
		if (tracker == null) {
			tracker= new VisibleLinesTracker(viewer);
			textWidget.setData(DATA_KEY, tracker);
		}
		tracker.addHandler(handler);
	}

	/**
	 * Untrack the line height change of the {@link StyledText} of the given handler an call the
	 * given handler.
	 *
	 * @param viewer the viewer to track
	 * @param handler the handler to call when line height change.
	 */
	static void untrack(ITextViewer viewer, Consumer<StyledText> handler) {
		StyledText textWidget= viewer != null ? viewer.getTextWidget() : null;
		if (textWidget == null) {
			return;
		}
		VisibleLinesTracker tracker= (VisibleLinesTracker) textWidget.getData(DATA_KEY);
		if (tracker != null) {
			tracker.removeHandler(handler);
		}
	}

	/**
	 * Add the given handler.
	 *
	 * @param handler the handler to call when a visible line height change.
	 */
	private void addHandler(Consumer<StyledText> handler) {
		if (handlers.isEmpty()) {
			viewer.getTextWidget().addPaintListener(this);
		}
		handlers.add(handler);
	}

	/**
	 * Remove the given handler.
	 *
	 * @param handler the handler to call when a visible line height change.
	 */
	private void removeHandler(Consumer<StyledText> handler) {
		handlers.remove(handler);
		if (handlers.isEmpty()) {
			viewer.getTextWidget().removePaintListener(this);
		}
	}
}
