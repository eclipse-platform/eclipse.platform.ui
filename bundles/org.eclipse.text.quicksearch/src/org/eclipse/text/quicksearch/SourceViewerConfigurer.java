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

import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.text.source.DiffPainter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.quicksearch.SourceViewerHandle.FixedLineHighlighter;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Implementation of source viewer factory for {@link SourceViewerHandle} that creates and does necessary setup of source viewer so that
 * it provides common aspects of quicksearch text viewers:
 * <ul>
 * <li>vertical ruler with line numbers supporting selected match line number highlighting
 * <li>selected match line highlighting
 * <li>current (caret position) line highlighting
 * <li>colors and fonts consistent with text viewers/editors preferences
 * </ul>
 *
 * Actual source viewer instance creation is delegated to provided {@link ISourceViewerCreator}.
 * @since 1.3
 */
public class SourceViewerConfigurer<T extends SourceViewer> implements ISourceViewerConfigurer<T> {

	public static final int VIEWER_STYLES = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.READ_ONLY;
	private static final String DISABLE_CSS = "org.eclipse.e4.ui.css.disabled"; //$NON-NLS-1$

	private final ISourceViewerCreator<T> fViewerCreator;
	private final IPropertyChangeListener fPropertyChangeListener = this::handlePreferenceStoreChanged;
	private final LineNumberChangeRulerColumn fLineNumberRulerColumn = new LineNumberChangeRulerColumn(EditorsUI.getSharedTextColors());
	private final FixedLineHighlighter fMatchLineHighlighter = new FixedLineHighlighter();

	protected final CompositeRuler fVerticalRuler = new CompositeRuler();
	protected final IPreferenceStore fPreferenceStore;
	protected T fSourceViewer;
	private Font fFont;

	/**
	 * Creates new instance that will use <code>viewerCreator</code> as a source viewer factory and common store
	 * to get preferences from for the viewer configuration.
	 * @param viewerCreator the factory for actual source viewer
	 */
	public SourceViewerConfigurer(ISourceViewerCreator<T> viewerCreator) {
		this(viewerCreator, EditorsUI.getPreferenceStore());
	}

	/**
	 * Creates new instance that will use <code>viewerCreator</code> as a source viewer factory and <code>store</code>
	 * to get preferences from for the viewer configuration.
	 * @param viewerCreator the factory for actual source viewer
	 * @param store the preference store to use for configuration
	 */
	public SourceViewerConfigurer(ISourceViewerCreator<T> viewerCreator, IPreferenceStore store) {
		Assert.isNotNull(viewerCreator);
		Assert.isNotNull(store);
		fViewerCreator = viewerCreator;
		fPreferenceStore = store;
	}

	@Override
	public T getSourceViewer(Composite parent) {
		fSourceViewer = fViewerCreator.createSourceViewer(parent, fVerticalRuler, VIEWER_STYLES);
		Assert.isNotNull(fSourceViewer);
		initialize();
		return fSourceViewer;
	}

	@Override
	public IChangeRulerColumn getChangeRulerColumn() {
		return fLineNumberRulerColumn;
	}

	@Override
	public FixedLineHighlighter getMatchLineHighlighter() {
		return fMatchLineHighlighter;
	}

	/**
	 * Initializes created source viewer to provide common aspects of quicksearch text viewers.
	 */
	protected void initialize() {
		fSourceViewer.getTextWidget().setData(DISABLE_CSS, Boolean.TRUE);
		fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);

		fSourceViewer.addVerticalRulerColumn(fLineNumberRulerColumn);

		initializeColors();
		initializeFont();

		fSourceViewer.getTextWidget().addLineBackgroundListener(fMatchLineHighlighter);

		var currentLineDecorations = getSourceViewerDecorationSupport();
		currentLineDecorations.install(fPreferenceStore);

		updateContributedRulerColumns((CompositeRuler) fVerticalRuler);

		fSourceViewer.getControl().addDisposeListener(e -> {
			currentLineDecorations.uninstall();
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
		});
	}

	/**
	 * Creates decoration support for the created source viewer. Default implementation returns {@link SourceViewerDecorationSupport}.
	 * @return decoration support for the source viewer
	 */
	protected SourceViewerDecorationSupport getSourceViewerDecorationSupport() {
		var support = new SourceViewerDecorationSupport(fSourceViewer, null, null, EditorsUI.getSharedTextColors());
		support.setCursorLinePainterPreferenceKeys(EDITOR_CURRENT_LINE, EDITOR_CURRENT_LINE_COLOR);
		return support;
	}

	/**
	 * Initializes the fore- and background colors of the created source viewer for both normal and selected text, color
	 * for line numbers (in vertical ruler) and color for line highlighting.
	 */
	protected void initializeColors() {

		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			ISharedTextColors sharedColors = EditorsUI.getSharedTextColors();
			var textWidget = fSourceViewer.getTextWidget();

			// ----------- foreground color --------------------
			Color color= store.getBoolean(PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_FOREGROUND));
			textWidget.setForeground(color);

			// ---------- background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_BACKGROUND));
			textWidget.setBackground(color);

			// ----------- selection foreground color --------------------
			color= store.getBoolean(PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_SELECTION_FOREGROUND));
			textWidget.setSelectionForeground(color);

			// ---------- selection background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: sharedColors.getColor(getColorFromStore(store, PREFERENCE_COLOR_SELECTION_BACKGROUND));
			textWidget.setSelectionBackground(color);

			fLineNumberRulerColumn.setBackground(textWidget.getBackground());

			// ----------- line numbers color --------------------
			var lineNumbersColor = getColorFromStore(store, EDITOR_LINE_NUMBER_RULER_COLOR);
			if (lineNumbersColor == null) {
				lineNumbersColor = new RGB(0, 0, 0);
			}
			fLineNumberRulerColumn.setForeground(sharedColors.getColor(lineNumbersColor));

			// ----------- line highlight (background) color --------------------
			color = sharedColors.getColor(getColorFromStore(store, EDITOR_CURRENT_LINE_COLOR));
			fLineNumberRulerColumn.setChangedColor(sharedColors.getColor(reverseInterpolateDiffPainterColor(textWidget.getBackground(), color)));
			if (fMatchLineHighlighter != null) {
				fMatchLineHighlighter.setHighlightColor(color);
			}
		}
	}

	/**
	 * Returns color that when set to {@link DiffPainter} makes it to paint quick diff annotation in a change ruler
	 * column with background color <code>backgroundColor</code> with color equal to <code>finalColor</code>.
	 * @param backgroundColor background color of the change ruler column
	 * @param finalColor final desired color of the diff annotation
	 * @return color to set to diff painter to make it to draw annotation with desired color
	 */
	@SuppressWarnings("restriction")
	public static RGB reverseInterpolateDiffPainterColor(Color backgroundColor, Color finalColor) {
		RGB baseRGB= finalColor.getRGB();
		RGB background= backgroundColor.getRGB();

		boolean darkBase= isDark(baseRGB);
		boolean darkBackground= isDark(background);
		if (darkBase && darkBackground)
			background= new RGB(255, 255, 255);
		else if (!darkBase && !darkBackground)
			background= new RGB(0, 0, 0);

		// reverse interpolate
		double scale = 0.6;
		double scaleInv = 1.0 - scale;
		return new RGB((int) ((baseRGB.red - scale * background.red) / scaleInv), (int) ((baseRGB.green - scale * background.green) / scaleInv), (int) ((baseRGB.blue - scale * background.blue) / scaleInv));
	}


	// copy-paste of org.eclipse.jface.internal.text.source.DiffPainter.isDark(RGB)
	private static boolean isDark(RGB rgb) {
		return greyLevel(rgb) > 128;
	}

	// copy-paste of org.eclipse.jface.internal.text.source.DiffPainter.greyLevel(RGB)
	private static double greyLevel(RGB rgb) {
		if (rgb.red == rgb.green && rgb.green == rgb.blue)
			return rgb.red;
		return (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
	}

	/**
	 * Gets color preference configured in <code>store</code> under <code>key</code>.
	 * @param store preference store
	 * @param key color preference key
	 * @return color preference
	 */
	protected RGB getColorFromStore(IPreferenceStore store, String key) {
		RGB rgb = null;
		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb = PreferenceConverter.getDefaultColor(store, key);
			} else {
				rgb = PreferenceConverter.getColor(store, key);
			}
		}
		return rgb;
	}

	private void initializeFont() {

		boolean isSharedFont= true;
		Font font= null;
		String symbolicFontName= getSymbolicFontName();

		if (symbolicFontName != null)
			font= JFaceResources.getFont(symbolicFontName);
		else if (fPreferenceStore != null) {
			// Backward compatibility
			if (fPreferenceStore.contains(JFaceResources.TEXT_FONT) && !fPreferenceStore.isDefault(JFaceResources.TEXT_FONT)) {
				FontData data= PreferenceConverter.getFontData(fPreferenceStore, JFaceResources.TEXT_FONT);

				if (data != null) {
					isSharedFont= false;
					font= new Font(fSourceViewer.getTextWidget().getDisplay(), data);
				}
			}
		}
		if (font == null)
			font= JFaceResources.getTextFont();

		if (!font.equals(fSourceViewer.getTextWidget().getFont())) {
			setFont(font);

			disposeFont();
			if (!isSharedFont)
				fFont= font;
		} else if (!isSharedFont) {
			font.dispose();
		}
	}

	// Sets the font for the source viewer sustaining selection and scroll position.
	private void setFont(Font font) {
		if (fSourceViewer.getDocument() != null) {

			ISelectionProvider provider= fSourceViewer.getSelectionProvider();
			ISelection selection= provider.getSelection();
			int topIndex= fSourceViewer.getTopIndex();

			Control parent= fSourceViewer.getControl();
			parent.setRedraw(false);

			fSourceViewer.getTextWidget().setFont(font);

			fVerticalRuler.setFont(font);

			provider.setSelection(selection);
			fSourceViewer.setTopIndex(topIndex);

			if (parent instanceof Composite composite) {
				composite.layout(true);
			}

			parent.setRedraw(true);
		} else {
			fSourceViewer.getTextWidget().setFont(font);
			fVerticalRuler.setFont(font);
		}
	}

	private void disposeFont() {
		if (fFont != null) {
			fFont.dispose();
			fFont= null;
		}
	}

	/**
	 * Returns the property preference key for the viewer font.
	 * <p>
	 * If this configurer provides non-null <code>{@link #getSymbolicFontName() symbolicFontName}</code> then this name
	 * is returned, otherwise {@link JFaceResources#TEXT_FONT} is returned.
	 * </p>
	 *
	 * @return key in the preference store for the font used in the source viewer
	 */
	private final String getFontPropertyPreferenceKey() {
		String symbolicFontName= getSymbolicFontName();
		if (symbolicFontName != null)
			return symbolicFontName;
		return JFaceResources.TEXT_FONT;
	}

	/**
	 * Returns custom symbolic name for the font to be used in the source viewer. By default returns <code>null</code>
	 * in which case default text viewer font is used.
	 * @return custom symbolic font name or <code>null</code> for default font use
	 */
	protected String getSymbolicFontName() {
		return null;
	}

	/**
	 * Returns preference store used for source viewer configuration.
	 *
	 * @return this configurer's preference store which may be <code>null</code>
	 */
	protected final IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}

	/**
	 * Adds additional ruler contributions to the vertical ruler.
	 * <p>
	 * Default implementation does nothing, clients may replace.</p>
	 *
	 * @param ruler the composite ruler to add contributions to
	 */
	protected void updateContributedRulerColumns(CompositeRuler ruler) {
		// no-op in default implementation
	}

	/**
	 * Handles a property change event describing a change of the preference store and updates the preference related
	 * source viewer properties.
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 *
	 * @param event the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property= event.getProperty();

		if (getFontPropertyPreferenceKey().equals(property)) {
			initializeFont();
			return;
		}

		if (property != null) {
			switch (property) {
			case PREFERENCE_COLOR_FOREGROUND:
			case PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT:
			case PREFERENCE_COLOR_BACKGROUND:
			case PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT:
			case PREFERENCE_COLOR_SELECTION_FOREGROUND:
			case PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT:
			case PREFERENCE_COLOR_SELECTION_BACKGROUND:
			case PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT:
			case EDITOR_LINE_NUMBER_RULER_COLOR:
				initializeColors();
				return;
			default:
				break;
			}
		}
	}

	/**
	 * Factory creating actual source viewer subsequently configured by {@link SourceViewerConfigurer} to provide
	 * common aspects of quicksearch text viewers.
	 * @see SourceViewerConfigurer
	 * @since 1.3
	 */
	public interface ISourceViewerCreator<T extends SourceViewer> {

		/**
		 * Creates new source viewer with <code>verticalRuler</code> and <code>styles</code> under passed
		 * <code>parent</code>.
		 * @param parent the parent SWT control for the viewer
		 * @param verticalRuler the vertical ruler to add to the created viewer
		 * @param styles the SWT styles for the viewer
		 * @return
		 */
		T createSourceViewer(Composite parent, CompositeRuler verticalRuler, int styles);
	}

}
