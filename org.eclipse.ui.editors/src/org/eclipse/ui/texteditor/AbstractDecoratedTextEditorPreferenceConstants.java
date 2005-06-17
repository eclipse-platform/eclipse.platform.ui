/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;

import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * Preference constants used in the extended text editor preference store.
 *
 * @since 3.0
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
	 * A named preference that controls disabling of the overwrite mode.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	public static final String EDITOR_DISABLE_OVERWRITE_MODE= "disable_overwrite_mode"; //$NON-NLS-1$

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
  	* Initializes the given preference store with the default values.
	 *
  	* @param store the preference store to be initialized
  	*/
	public static void initializeDefaultValues(IPreferenceStore store) {

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_ANNOTATIONS_PREFERENCE_PAGE, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, false);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, true);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, new RGB(232, 242, 254));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE, 25);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, new RGB(176, 180 , 185));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, false);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, new RGB(120, 120, 120));

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

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR, true);

		PreferenceConverter.setDefault(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, new RGB(255, 255, 255));
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);
		PreferenceConverter.setDefault(store, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, new RGB(0, 0, 0));
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);

		String mod1Name= Action.findModifierString(SWT.MOD1);	// SWT.COMMAND on MAC; SWT.CONTROL elsewhere
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, mod1Name);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK, SWT.MOD1);
		PreferenceConverter.setDefault(store, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR, new RGB(0, 0, 255));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE, false);

		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENABLED, false);
		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENGINE, ""); //$NON-NLS-1$
		
		store.setDefault(SHOW_RANGE_INDICATOR, true);

		MarkerAnnotationPreferences.initializeDefaultValues(store);
	}
}
