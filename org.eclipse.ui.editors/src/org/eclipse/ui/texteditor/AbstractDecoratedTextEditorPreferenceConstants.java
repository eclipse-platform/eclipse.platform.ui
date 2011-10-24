/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.EditorsPluginPreferenceInitializer;

import org.eclipse.ui.texteditor.spelling.SpellingService;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Preference constants used in the extended text editor preference store.
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AbstractDecoratedTextEditorPreferenceConstants {


	/**
	 * Prevent initialization.
	 */
	private AbstractDecoratedTextEditorPreferenceConstants() {
	}

	/**
	 * A named preference that controls whether the current line highlighting is turned on or off
	 * (value <code>"currentLine"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CURRENT_LINE= "currentLine"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to highlight the current line
	 * (value <code>"currentLineColor"</code>).
	 * <p>
	 * The preference value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>.
	 * </p>
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see PreferenceConverter
	 */
	public final static String EDITOR_CURRENT_LINE_COLOR= "currentLineColor"; //$NON-NLS-1$

	/**
	 * A named preference that holds the number of spaces used per tab in the text editor.
	 * <p>
	 * Value is of type <code>int</code>: positive int value specifying the number of
	 * spaces per tab.
	 * </p>
	 */
	public final static String EDITOR_TAB_WIDTH= "tabWidth"; //$NON-NLS-1$

	/**
	 * A named preference that specifies if the editor uses spaces for tabs.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code>spaces instead of tabs are used
	 * in the editor. If <code>false</code> the editor inserts a tab character when pressing the tab
	 * key.
	 * </p>
	 */
	public final static String EDITOR_SPACES_FOR_TABS= "spacesForTabs"; //$NON-NLS-1$

	/**
	 * A named preference that holds the size of the editor's undo history.
	 * <p>
	 * Value is of type <code>int</code>: 0 or positive int value specifying the size of
	 * the editor's undo history.
	 * </p>
	 * @since 3.1
	 */
	public final static String EDITOR_UNDO_HISTORY_SIZE= "undoHistorySize"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the print margin is turned on or off
 	 * (value <code>"printMargin"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PRINT_MARGIN= "printMargin"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render the print margin
	 * (value <code>"printMarginColor"</code>).
	 * <p>
	 * The preference value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>.
	 * </p>
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see PreferenceConverter
	 */
	public final static String EDITOR_PRINT_MARGIN_COLOR= "printMarginColor"; //$NON-NLS-1$

	/**
	 * Print margin column
	 * (value <code>"printMarginColumn"</code>).
	 * <p>
	 * The preference value is of type <code>int</code>.
	 * </p>
	 */
	public final static String EDITOR_PRINT_MARGIN_COLUMN= "printMarginColumn"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the editor shows unknown
	 * indicators in text (squiggly lines).
	 * (value <code>"othersIndication"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @deprecated as of 3.0 there are no UNKNOWN annotations any more
	 */
	public final static String EDITOR_UNKNOWN_INDICATION= "othersIndication"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render unknown indicators
	 * (value <code>"othersIndicationColor"</code>).
	 * <p>
	 * The preference value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>.
	 * </p>
	 * @see #EDITOR_UNKNOWN_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see PreferenceConverter
	 * @deprecated As of 3.0, there are no UNKNOWN annotations any more
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_COLOR= "othersIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows unknown indicators
 	 * (value <code>"othersIndicationInOverviewRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @deprecated As of 3.0, there are no UNKNOWN annotations any more
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER= "othersIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the overview ruler is shown in the UI
 	 * (value <code>"overviewRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_OVERVIEW_RULER= "overviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the line number ruler is shown in the UI
	 * (value <code>"lineNumberRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_LINE_NUMBER_RULER= "lineNumberRuler"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render line numbers inside the line number ruler
	 * (value <code>"lineNumberColor"</code>).
	 * <p>
	 * The preference value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>.
	 * </p>
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see PreferenceConverter
	 * @see #EDITOR_LINE_NUMBER_RULER
	 */
	public final static String EDITOR_LINE_NUMBER_RULER_COLOR= "lineNumberColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether this plug-in's
	 * Annotations preference page is used to configure annotations.
	 * <p>
	 * Value is of type <code>boolean</code>.
	 * </p>
	 */
	public static final String USE_ANNOTATIONS_PREFERENCE_PAGE= "useAnnotationsPrefPage"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether this plug-in's
	 * Quick Diff preference page is used to configure Quick Diff.
	 * <p>
	 * Value is of type <code>boolean</code>.
	 * </p>
	 */
	public static final String USE_QUICK_DIFF_PREFERENCE_PAGE= "useQuickDiffPrefPage"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether quick diff colors are shown on the line number bar.
	 * <p>
	 * Value is of type <code>boolean</code>.
	 * </p>
	 */
	public static final String QUICK_DIFF_ALWAYS_ON= "quickdiff.quickDiff"; //$NON-NLS-1$

	/**
	 * A named preference that controls the default quick diff reference provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */
	public static final String QUICK_DIFF_DEFAULT_PROVIDER= "quickdiff.defaultProvider"; //$NON-NLS-1$

	/**
	 * A named preference that controls the default quick diff reference provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */
	public static final String QUICK_DIFF_CHARACTER_MODE= "quickdiff.characterMode"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether custom carets are used in the
	 * editor or not.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>false</code>, only
	 * the default caret is used in the editor.
	 * </p>
	 */
	public static final String EDITOR_USE_CUSTOM_CARETS= AbstractTextEditor.PREFERENCE_USE_CUSTOM_CARETS;

	/**
	 * A named preference that controls whether carets are drawn wide or not.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code>, the caret is
	 * twice as wide as the default caret.
	 * </p>
	 */
	public static final String EDITOR_WIDE_CARET= AbstractTextEditor.PREFERENCE_WIDE_CARET;

	/**
	 * A named preference that controls whether to use saturated colors in the overview ruler.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code>, saturated colors are used
	 * </p>
	 * 
	 * @since 3.8
	 * @see org.eclipse.jface.text.source.IOverviewRulerExtension#setUseSaturatedColors(boolean)
	 */
	public static final String USE_SATURATED_COLORS_IN_OVERVIEW_RULER= "Accessibility.UseSaturatedColors"; //$NON-NLS-1$;

	/**
	 * A named preference that holds the color used as the text selection foreground.
	 * This value has no effect if the system default color is used.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see PreferenceConverter
	 */
	public final static String EDITOR_SELECTION_FOREGROUND_COLOR= AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND;

	/**
	 * A named preference that describes if the system default selection foreground color
	 * is used as the text selection foreground.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR= AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT;

	/**
	 * A named preference that holds the color used as the text selection background.
	 * This value has no effect if the system default color is used.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see PreferenceConverter
	 */
	public final static String EDITOR_SELECTION_BACKGROUND_COLOR= AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND;

	/**
	 * A named preference that describes if the system default selection background color
	 * is used as the text selection background.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR= AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT;

	/**
	 * A named preference that controls if hyperlinks are turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	public static final String EDITOR_HYPERLINKS_ENABLED= AbstractTextEditor.PREFERENCE_HYPERLINKS_ENABLED;

	/**
	 * A named preference that controls the key modifier for hyperlinks.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	public static final String EDITOR_HYPERLINK_KEY_MODIFIER= AbstractTextEditor.PREFERENCE_HYPERLINK_KEY_MODIFIER;

	/**
	 * A named preference that controls the key modifier mask for hyperlinks.
	 * The value is only used if the value of <code>EDITOR_HYPERLINK_KEY_MODIFIER</code>
	 * cannot be resolved to valid SWT modifier bits.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 *
	 * @see #EDITOR_HYPERLINK_KEY_MODIFIER
	 * @since 3.1
	 */
	public static final String EDITOR_HYPERLINK_KEY_MODIFIER_MASK= AbstractTextEditor.PREFERENCE_HYPERLINK_KEY_MODIFIER_MASK;

	/**
	 * A named preference that holds the color used for hyperlinks.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 3.1
	 */
	public final static String EDITOR_HYPERLINK_COLOR= DefaultHyperlinkPresenter.HYPERLINK_COLOR;

	/**
	 * A named preference that holds the preference whether to use the native link color.
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.5
	 */
	public final static String EDITOR_HYPERLINK_COLOR_SYSTEM_DEFAULT= DefaultHyperlinkPresenter.HYPERLINK_COLOR_SYSTEM_DEFAULT;

	/**
	 * A named preference that controls disabling of the overwrite mode.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> As of 3.3, this preference can no longer
	 * be set via UI but is still honored by the code. A workspace that
	 * was started at least once with 3.3 has this preference set to <code>false</code>.
	 * Workspaces started with 3.4 keep their current preference.
	 *
	 * @since 3.1
	 */
	public static final String EDITOR_DISABLE_OVERWRITE_MODE= "disable_overwrite_mode"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether a confirmation
	 * dialog is shown before editing derived input.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String EDITOR_WARN_IF_INPUT_DERIVED= "warn_if_input_derived"; //$NON-NLS-1$

	/**
	 * A named preference that controls if smart home/end navigation is on or off
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String EDITOR_SMART_HOME_END= AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END;

	/**
	 * A named preference that controls the display of whitespace characters.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * <p>
	 * The following preferences can be used for fine-grained configuration when enabled.
	 * <ul>
	 * <li>{@link #EDITOR_SHOW_LEADING_SPACES}</li>
	 * <li>{@link #EDITOR_SHOW_ENCLOSED_SPACES}</li>
	 * <li>{@link #EDITOR_SHOW_TRAILING_SPACES}</li>
	 * <li>{@link #EDITOR_SHOW_LEADING_IDEOGRAPHIC_SPACES}</li>
	 * <li>{@link #EDITOR_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES}</li>
	 * <li>{@link #EDITOR_SHOW_TRAILING_IDEOGRAPHIC_SPACES}</li>
	 * <li>{@link #EDITOR_SHOW_LEADING_TABS}</li>
	 * <li>{@link #EDITOR_SHOW_ENCLOSED_TABS}</li>
	 * <li>{@link #EDITOR_SHOW_TRAILING_TABS}</li>
	 * <li>{@link #EDITOR_SHOW_CARRIAGE_RETURN}</li>
	 * <li>{@link #EDITOR_SHOW_LINE_FEED}</li>
	 * <li>{@link #EDITOR_WHITESPACE_CHARACTER_ALPHA_VALUE}</li>
	 * </ul>
	 * </p>
	 * 
	 * @since 3.3
	 */
	public static final String EDITOR_SHOW_WHITESPACE_CHARACTERS= AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS;

	/**
	 * A named preference that controls the display of leading Space characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_LEADING_SPACES= AbstractTextEditor.PREFERENCE_SHOW_LEADING_SPACES;

	/**
	 * A named preference that controls the display of enclosed Space characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_ENCLOSED_SPACES= AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_SPACES;

	/**
	 * A named preference that controls the display of trailing Space characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_TRAILING_SPACES= AbstractTextEditor.PREFERENCE_SHOW_TRAILING_SPACES;

	/**
	 * A named preference that controls the display of leading Ideographic Space characters. The
	 * value is used only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_LEADING_IDEOGRAPHIC_SPACES= AbstractTextEditor.PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES;

	/**
	 * A named preference that controls the display of enclosed Ideographic Space characters. The
	 * value is used only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES= AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES;

	/**
	 * A named preference that controls the display of trailing Ideographic Space characters. The
	 * value is used only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_TRAILING_IDEOGRAPHIC_SPACES= AbstractTextEditor.PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES;

	/**
	 * A named preference that controls the display of leading Tab characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_LEADING_TABS= AbstractTextEditor.PREFERENCE_SHOW_LEADING_TABS;

	/**
	 * A named preference that controls the display of enclosed Tab characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_ENCLOSED_TABS= AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_TABS;

	/**
	 * A named preference that controls the display of trailing Tab characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_TRAILING_TABS= AbstractTextEditor.PREFERENCE_SHOW_TRAILING_TABS;

	/**
	 * A named preference that controls the display of Carriage Return characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_CARRIAGE_RETURN= AbstractTextEditor.PREFERENCE_SHOW_CARRIAGE_RETURN;

	/**
	 * A named preference that controls the display of Line Feed characters. The value is used only
	 * if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_SHOW_LINE_FEED= AbstractTextEditor.PREFERENCE_SHOW_LINE_FEED;

	/**
	 * A named preference that controls the alpha value of whitespace characters. The value is used
	 * only if the value of {@link #EDITOR_SHOW_WHITESPACE_CHARACTERS} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 * 
	 * @since 3.7
	 */
	public static final String EDITOR_WHITESPACE_CHARACTER_ALPHA_VALUE= AbstractTextEditor.PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE;

	/**
	 * A named preference that controls the display of the range indicator.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	public static final String SHOW_RANGE_INDICATOR= "show_range_indicator"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the user is asked before switching the quick diff
	 * reference when showing revision information..
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 *
	 * @since 3.2
	 */
	public static final String REVISION_ASK_BEFORE_QUICKDIFF_SWITCH= "quickdiff.nowarn.before.switch"; //$NON-NLS-1$

	/**
	 * A named preference that controls the rendering mode of the revision ruler.
	 * <p>
	 * Value is of type <code>String</code> and should contain the name of a
	 * {@link org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension.RenderingMode}.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String REVISION_RULER_RENDERING_MODE= "revisionRulerRenderingMode"; //$NON-NLS-1$

	/**
	 * A named preference that controls the rendering of the author on the revision ruler.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String REVISION_RULER_SHOW_AUTHOR= "revisionRulerShowAuthor"; //$NON-NLS-1$

	/**
	 * A named preference that controls rendering of the revision on the revision ruler.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String REVISION_RULER_SHOW_REVISION= "revisionRulerShowRevision"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether text drag and drop is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String EDITOR_TEXT_DRAG_AND_DROP_ENABLED= AbstractTextEditor.PREFERENCE_TEXT_DRAG_AND_DROP_ENABLED;

	/**
	 * A named preference that defines whether the hint to make hover sticky should be shown.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.3
	 */
	public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE= "showTextHoverAffordance"; //$NON-NLS-1$

	/**
	 * A named preference that controls if hovers should automatically be closed
	 * when the mouse is moved into them, or when they should be enriched.
	 * <p>
	 * Value is of type <code>Integer</code> and maps to the following
	 * {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode}:
	 * </p>
	 * <ul>
	 * <li>-1: <code>null</code> (don't allow moving the mouse into a hover),</li>
	 * <li>0: {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode#AFTER_DELAY},</li>
	 * <li>1: {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode#IMMEDIATELY},</li>
	 * <li>2: {@link org.eclipse.jface.text.ITextViewerExtension8.EnrichMode#ON_CLICK}.</li>
	 * </ul>
	 *
	 * @since 3.4
	 */
	public static final String EDITOR_HOVER_ENRICH_MODE= AbstractTextEditor.PREFERENCE_HOVER_ENRICH_MODE;

	/**
  	* Initializes the given preference store with the default values.
	 *
  	* @param store the preference store to be initialized
  	*/
	public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_ANNOTATIONS_PREFERENCE_PAGE, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, false);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, true);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, false);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE, 200);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, false);

		if (!store.getBoolean(USE_QUICK_DIFF_PREFERENCE_PAGE)) {
			store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, true);
			store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE, false);
			store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER, "org.eclipse.ui.internal.editors.quickdiff.LastSaveReferenceProvider"); //$NON-NLS-1$
		}

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, true);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER, false);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR, new RGB(0, 0, 0));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_SATURATED_COLORS_IN_OVERVIEW_RULER, false);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR, true);

		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);

		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);

		String mod1Name= Action.findModifierString(SWT.MOD1);	// SWT.COMMAND on MAC; SWT.CONTROL elsewhere
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR_SYSTEM_DEFAULT, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, mod1Name);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK, SWT.MOD1);

		HyperlinkDetectorDescriptor[] descriptors= EditorsUI.getHyperlinkDetectorRegistry().getHyperlinkDetectorDescriptors();
		for (int i= 0; i < descriptors.length; i++) {
			int stateMask= computeStateMask(descriptors[i].getModifierKeys());
			if (stateMask == SWT.SHIFT) {
				EditorsPlugin.logErrorMessage("The '" + descriptors[i].getId() + "' hyperlink detector specifies 'Shift' as modifier. This is not allowed and hence replaced with the default modifier."); //$NON-NLS-1$ //$NON-NLS-2$
				stateMask= -1;
			}
			store.setDefault(descriptors[i].getId() + HyperlinkDetectorDescriptor.STATE_MASK_POSTFIX, stateMask);
		}

		boolean isInstalled= EditorsUI.getSpellingService().getSpellingEngineDescriptors().length > 0;
		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENABLED, isInstalled);
		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENGINE, ""); //$NON-NLS-1$

		store.setDefault(SHOW_RANGE_INDICATOR, true);
		store.setDefault(REVISION_ASK_BEFORE_QUICKDIFF_SWITCH, ""); //$NON-NLS-1$

		store.setDefault(AbstractTextEditor.PREFERENCE_RULER_CONTRIBUTIONS, ""); //$NON-NLS-1$
		store.setDefault(REVISION_RULER_RENDERING_MODE, IRevisionRulerColumnExtension.AGE.name());
		store.setDefault(REVISION_RULER_SHOW_AUTHOR, false);
		store.setDefault(REVISION_RULER_SHOW_REVISION, false);

		store.setDefault(EDITOR_WARN_IF_INPUT_DERIVED, true);
		store.setDefault(EDITOR_SMART_HOME_END, true);

		store.setDefault(EDITOR_SHOW_WHITESPACE_CHARACTERS, false);
		store.setDefault(EDITOR_SHOW_LEADING_SPACES, true);
		store.setDefault(EDITOR_SHOW_ENCLOSED_SPACES, true);
		store.setDefault(EDITOR_SHOW_TRAILING_SPACES, true);
		store.setDefault(EDITOR_SHOW_LEADING_IDEOGRAPHIC_SPACES, true);
		store.setDefault(EDITOR_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES, true);
		store.setDefault(EDITOR_SHOW_TRAILING_IDEOGRAPHIC_SPACES, true);
		store.setDefault(EDITOR_SHOW_LEADING_TABS, true);
		store.setDefault(EDITOR_SHOW_ENCLOSED_TABS, true);
		store.setDefault(EDITOR_SHOW_TRAILING_TABS, true);
		store.setDefault(EDITOR_SHOW_CARRIAGE_RETURN, true);
		store.setDefault(EDITOR_SHOW_LINE_FEED, true);
		store.setDefault(EDITOR_WHITESPACE_CHARACTER_ALPHA_VALUE, 80);

		store.setDefault(EDITOR_TEXT_DRAG_AND_DROP_ENABLED, true);
		store.setDefault(EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, true);
		store.setDefault(EDITOR_HOVER_ENRICH_MODE, 0);

		MarkerAnnotationPreferences.initializeDefaultValues(store);

		EditorsPluginPreferenceInitializer.setThemeBasedPreferences(store, false);
	}

	/**
	 * Computes the state mask out of the given modifiers string.
	 *
	 * @param modifiers a string containing modifiers
	 * @return the state mask
	 * @since 3.3
	 */
	private static final int computeStateMask(String modifiers) {
		if (modifiers == null)
			return -1;

		if (modifiers.length() == 0)
			return SWT.NONE;

		int stateMask= 0;
		StringTokenizer modifierTokenizer= new StringTokenizer(modifiers, ",;.:+-* "); //$NON-NLS-1$
		while (modifierTokenizer.hasMoreTokens()) {
			int modifier= findLocalizedModifier(modifierTokenizer.nextToken());
			if (modifier == 0 || (stateMask & modifier) == modifier)
				return -1;
			stateMask= stateMask | modifier;
		}
		return stateMask;
	}

	/**
	 * Maps the localized modifier name to a code in the same
	 * manner as #findModifier.
	 *
	 * @param modifierName the modifier name
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 * @since 3.3
	 */
	private static final int findLocalizedModifier(String modifierName) {
		if (modifierName == null)
			return SWT.NONE;

		if (modifierName.equalsIgnoreCase("M1")) //$NON-NLS-1$
			return SWT.MOD1;
		if (modifierName.equalsIgnoreCase("M2")) //$NON-NLS-1$
			return SWT.MOD2;
		if (modifierName.equalsIgnoreCase("M3")) //$NON-NLS-1$
			return SWT.MOD3;
		if (modifierName.equalsIgnoreCase("M4")) //$NON-NLS-1$
			return SWT.MOD4;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
			return SWT.CTRL;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
			return SWT.SHIFT;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
			return SWT.ALT;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
			return SWT.COMMAND;

		return SWT.NONE;
	}

}
