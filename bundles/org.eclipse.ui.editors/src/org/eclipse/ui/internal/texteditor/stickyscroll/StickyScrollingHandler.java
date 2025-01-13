/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

import static org.eclipse.ui.internal.texteditor.ITextEditorThemeConstants.STICKY_LINES_SEPARATOR_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Throttler;

import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.stickyscroll.IStickyLinesProvider.StickyLinesProperties;

/**
 * A sticky scrolling handler that retrieves stick lines from a {@link IStickyLinesProvider} and
 * shows them in a {@link StickyScrollingControl} on top of the given source viewer.
 */
public class StickyScrollingHandler implements IViewportListener {

	private final static int THROTTLER_DELAY= 100;

	private ISourceViewer sourceViewer;

	private StickyScrollingControl stickyScrollingControl;

	private IPropertyChangeListener propertyChangeListener;

	private IPreferenceStore preferenceStore;

	private IStickyLinesProvider stickyLinesProvider;

	private StickyLinesProperties stickyLinesProperties;

	private Throttler throttler;

	private int verticalOffset;

	/**
	 * Creates a StickyScrollingHandler that will be linked to the given source viewer. The sticky
	 * lines will be provided by the {@link DefaultStickyLinesProvider}.
	 * 
	 * @param sourceViewer The source viewer to link the handler with
	 * @param verticalRuler The vertical ruler of the source viewer
	 * @param preferenceStore The preference store
	 */
	public StickyScrollingHandler(ISourceViewer sourceViewer, IVerticalRuler verticalRuler, IPreferenceStore preferenceStore) {
		this(sourceViewer, verticalRuler, preferenceStore, new DefaultStickyLinesProvider());
	}

	/**
	 * Creates a StickyScrollingHandler that will be linked to the given source viewer. The sticky
	 * lines will be provided by the given <code>stickyLinesProvider</code>.
	 * 
	 * @param sourceViewer The source viewer to link the handler with
	 * @param verticalRuler The vertical ruler of the source viewer
	 * @param preferenceStore The preference store
	 * @param stickyLinesProvider The sticky scrolling provider
	 */
	public StickyScrollingHandler(ISourceViewer sourceViewer, IVerticalRuler verticalRuler, IPreferenceStore preferenceStore,
			IStickyLinesProvider stickyLinesProvider) {
		this.sourceViewer= sourceViewer;

		throttler= new Throttler(sourceViewer.getTextWidget().getDisplay(), Duration.ofMillis(THROTTLER_DELAY), this::calculateAndShowStickyLines);
		this.stickyLinesProvider= stickyLinesProvider;

		listenForPropertiesChanges(preferenceStore);
		stickyLinesProperties= loadStickyLinesProperties(preferenceStore);
		StickyScrollingControlSettings settings= loadControlSettings(preferenceStore);

		stickyScrollingControl= new StickyScrollingControl(sourceViewer, verticalRuler, settings, this);

		sourceViewer.addViewportListener(this);
	}

	private void listenForPropertiesChanges(IPreferenceStore store) {
		preferenceStore= store;
		propertyChangeListener= e -> {
			if (e.getProperty().equals(EDITOR_TAB_WIDTH) || e.getProperty().equals(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT)
					|| e.getProperty().equals(EDITOR_CURRENT_LINE_COLOR) || e.getProperty().equals(EDITOR_LINE_NUMBER_RULER)
					|| e.getProperty().equals(STICKY_LINES_SEPARATOR_COLOR)) {
				if (stickyScrollingControl != null && !sourceViewer.getTextWidget().isDisposed()) {
					StickyScrollingControlSettings settings= loadControlSettings(preferenceStore);
					stickyScrollingControl.applySettings(settings);
					stickyLinesProperties= loadStickyLinesProperties(preferenceStore);
				}
			}
		};
		store.addPropertyChangeListener(propertyChangeListener);
	}

	private StickyScrollingControlSettings loadControlSettings(IPreferenceStore store) {
		int stickyScrollingMaxCount= store.getInt(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT);

		Color lineNumberColor= new Color(PreferenceConverter.getColor(store, EDITOR_LINE_NUMBER_RULER_COLOR));
		Color stickyLineHoverColor= new Color(PreferenceConverter.getColor(store, EDITOR_CURRENT_LINE_COLOR));
		Color stickyLineBackgroundColor= sourceViewer.getTextWidget().getBackground();
		boolean showLineNumbers= store.getBoolean(EDITOR_LINE_NUMBER_RULER);
		Color stickyLineSeparatorColor= null;
		if (EditorsPlugin.getDefault() != null) {
			RGB rgb= PreferenceConverter.getColor(store, STICKY_LINES_SEPARATOR_COLOR);
			ISharedTextColors sharedTextColors= EditorsPlugin.getDefault().getSharedTextColors();
			stickyLineSeparatorColor= sharedTextColors.getColor(rgb);
		}
		return new StickyScrollingControlSettings(stickyScrollingMaxCount,
				lineNumberColor, stickyLineHoverColor, stickyLineBackgroundColor, stickyLineSeparatorColor, showLineNumbers);
	}

	private StickyLinesProperties loadStickyLinesProperties(IPreferenceStore store) {
		int tabWidth= store.getInt(EDITOR_TAB_WIDTH);
		return new StickyLinesProperties(tabWidth);
	}

	@Override
	public void viewportChanged(int newVerticalOffset) {
		if (this.verticalOffset == newVerticalOffset) {
			return;
		}
		verticalOffset= newVerticalOffset;
		throttler.throttledExec();
	}

	private void calculateAndShowStickyLines() {
		List<IStickyLine> stickyLines= Collections.emptyList();

		int startLine= sourceViewer.getTopIndex();

		if (startLine > 0) {
			stickyLines= stickyLinesProvider.getStickyLines(sourceViewer, sourceViewer.getTopIndex(), stickyLinesProperties);
		}

		if (stickyLines == null) {
			stickyLines= Collections.emptyList();
		}

		stickyLines= adaptStickyLinesToVisibleArea(stickyLines, startLine);

		stickyScrollingControl.setStickyLines(stickyLines);
	}

	private List<IStickyLine> adaptStickyLinesToVisibleArea(List<IStickyLine> stickyLines, int startLine) {
		if (stickyLines.isEmpty()) {
			return stickyLines;
		}

		LinkedList<IStickyLine> adaptedStickyLines= new LinkedList<>(stickyLines);

		int firstVisibleLine= startLine + adaptedStickyLines.size();
		int numberOfLines= sourceViewer.getDocument().getNumberOfLines();

		for (int i= startLine + 1; i <= firstVisibleLine && i < numberOfLines; i++) {
			List<IStickyLine> stickyLinesInLineI= stickyLinesProvider.getStickyLines(sourceViewer, i, stickyLinesProperties);

			if (stickyLinesInLineI.size() > adaptedStickyLines.size()) {
				adaptedStickyLines= new LinkedList<>(stickyLinesInLineI);
				firstVisibleLine= startLine + adaptedStickyLines.size();
			}

			while (stickyLinesInLineI.size() < adaptedStickyLines.size() && i < firstVisibleLine) {
				adaptedStickyLines.removeLast();
				firstVisibleLine--;
			}
		}

		return adaptedStickyLines;
	}

	/**
	 * Uninstalls the sticky scrolling handler from the source viewer. This completely disposes the
	 * {@link StickyScrollingControl} and removes all corresponding listeners.
	 */
	public void uninstall() {
		this.sourceViewer.removeViewportListener(this);
		preferenceStore.removePropertyChangeListener(propertyChangeListener);
		preferenceStore= null;
		throttler= null;

		stickyScrollingControl.dispose();
	}

}
