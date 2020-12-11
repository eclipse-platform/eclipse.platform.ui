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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;

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

	private Map<Integer, Rectangle> oldVisibleLineBounds= Collections.emptyMap();

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
		Map<Integer, Rectangle> newBounds= getVisibleLineBounds();
		if (!oldVisibleLineBounds.equals(newBounds)) {
			oldVisibleLineBounds= newBounds;
			handlers.forEach(handler -> handler.accept(textWidget));
		}
	}

	private Map<Integer, Rectangle> getVisibleLineBounds() {
		StyledText textWidget= viewer.getTextWidget();
		if (textWidget.isDisposed() || !textWidget.isVisible()) {
			return Collections.emptyMap();
		}
		Map<Integer, Rectangle> res= new TreeMap<>();
		int lastVisibleLineIndex= textWidget.getLineIndex(textWidget.getClientArea().height);
		for (int widgetLine= textWidget.getLineIndex(0); widgetLine <= lastVisibleLineIndex; widgetLine++) {
			int widgetLineOffset= textWidget.getOffsetAtLine(widgetLine);
			res.put(Integer.valueOf(JFaceTextUtil.widgetLine2ModelLine(viewer, widgetLine)), //
					new Rectangle(0, textWidget.getLinePixel(widgetLine), 0, textWidget.getLineHeight(widgetLineOffset)));
		}
		return res;
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
