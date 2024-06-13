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

import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;

import java.time.Duration;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Throttler;

import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;

/**
 * A sticky scrolling handler that retrieves stick lines from the {@link StickyLinesProvider} and
 * shows them in a {@link StickyScrollingControl} on top of the given source viewer.
 */
public class StickyScrollingHandler implements IViewportListener {

	private final static int THROTTLER_DELAY= 100;

	private ISourceViewer sourceViewer;

	private StickyScrollingControl stickyScrollingControl;

	private int tabWidth;

	private IPropertyChangeListener propertyChangeListener;

	private IPreferenceStore preferenceStore;

	private StickyLinesProvider stickyLinesProvider;

	private Throttler throttler;

	private int verticalOffset;

	/**
	 * Creates a StickyScrollingHandlerIndentation that will be linked to the given source viewer.
	 * The sticky scrolling will be computed by the default {@link StickyLinesProvider}.
	 * 
	 * @param sourceViewer The source viewer to link the handler
	 * @param verticalRuler The vertical ruler of the source viewer
	 * @param preferenceStore The preference store
	 */
	public StickyScrollingHandler(ISourceViewer sourceViewer, IVerticalRuler verticalRuler, IPreferenceStore preferenceStore) {
		this(sourceViewer, verticalRuler, preferenceStore, new StickyLinesProvider());
	}

	/**
	 * Creates a StickyScrollingHandlerIndentation that will be linked to the given source viewer.
	 * 
	 * @param sourceViewer The source viewer to link the handler
	 * @param verticalRuler The vertical ruler of the source viewer
	 * @param preferenceStore The preference store
	 * @param stickyLinesProvider The sticky scrolling computer
	 */
	public StickyScrollingHandler(ISourceViewer sourceViewer, IVerticalRuler verticalRuler, IPreferenceStore preferenceStore,
			StickyLinesProvider stickyLinesProvider) {
		this.sourceViewer= sourceViewer;

		throttler= new Throttler(sourceViewer.getTextWidget().getDisplay(), Duration.ofMillis(THROTTLER_DELAY), this::calculateAndShowStickyLines);
		this.stickyLinesProvider= stickyLinesProvider;

		StickyScrollingControlSettings settings= loadAndListenForProperties(preferenceStore);
		stickyScrollingControl= new StickyScrollingControl(sourceViewer, verticalRuler, settings, this);

		sourceViewer.addViewportListener(this);
	}

	private StickyScrollingControlSettings loadAndListenForProperties(IPreferenceStore store) {
		preferenceStore= store;
		propertyChangeListener= e -> {
			if (e.getProperty().equals(EDITOR_TAB_WIDTH) || e.getProperty().equals(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT)
					|| e.getProperty().equals(EDITOR_CURRENT_LINE_COLOR) || e.getProperty().equals(EDITOR_LINE_NUMBER_RULER)) {
				if (stickyScrollingControl != null && !sourceViewer.getTextWidget().isDisposed()) {
					StickyScrollingControlSettings settings= loadSettings(preferenceStore);
					stickyScrollingControl.applySettings(settings);
					stickyLinesProvider.setTabWidth(tabWidth);
				}
			}
		};
		store.addPropertyChangeListener(propertyChangeListener);
		return loadSettings(store);
	}

	private StickyScrollingControlSettings loadSettings(IPreferenceStore store) {
		tabWidth= store.getInt(EDITOR_TAB_WIDTH);

		int stickyScrollingMaxCount= store.getInt(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT);

		Color lineNumberColor= new Color(PreferenceConverter.getColor(store, EDITOR_LINE_NUMBER_RULER_COLOR));
		sourceViewer.getTextWidget().addDisposeListener(e -> lineNumberColor.dispose());

		Color stickyLineHoverColor= new Color(PreferenceConverter.getColor(store, EDITOR_CURRENT_LINE_COLOR));
		sourceViewer.getTextWidget().addDisposeListener(e -> stickyLineHoverColor.dispose());

		Color stickyLineBackgroundColor= sourceViewer.getTextWidget().getBackground();

		boolean showLineNumbers= store.getBoolean(EDITOR_LINE_NUMBER_RULER);

		return new StickyScrollingControlSettings(stickyScrollingMaxCount,
				lineNumberColor, stickyLineHoverColor, stickyLineBackgroundColor, showLineNumbers);
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
		List<StickyLine> stickyLines= stickyLinesProvider.get(verticalOffset, sourceViewer);
		stickyScrollingControl.setStickyLines(stickyLines);
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
