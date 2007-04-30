/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.ITextEditorThemeConstants;
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
	 * @since 3.3
	 */
	public static final String EDITOR_SHOW_WHITESPACE_CHARACTERS= AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS;

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
  	* Initializes the given preference store with the default values.
	 *
  	* @param store the preference store to be initialized
  	*/
	public static void initializeDefaultValues(IPreferenceStore store) {
		ColorRegistry registry= null;
		if (PlatformUI.isWorkbenchRunning())
			registry= PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_ANNOTATIONS_PREFERENCE_PAGE, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, false);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, true);
		setDefaultAndFireEvent(store,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR,
				findRGB(registry, ITextEditorThemeConstants.CURRENT_LINE_COLOR, new RGB(232, 242, 254)));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, false);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE, 200);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, false);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);
		
		setDefaultAndFireEvent(store, 
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, 
				findRGB(registry,ITextEditorThemeConstants.PRINT_MARGIN_COLOR, new RGB(176, 180 , 185)));

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, false);
		setDefaultAndFireEvent(store,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR,
				findRGB(registry, ITextEditorThemeConstants.LINE_NUMBER_RULER_COLOR, new RGB(120, 120, 120)));

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

		PreferenceConverter.setDefault(store,AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,new RGB(255, 255, 255));
		
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);
		PreferenceConverter.setDefault(store,AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,new RGB(0, 0, 0));
		
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);

		String mod1Name= Action.findModifierString(SWT.MOD1);	// SWT.COMMAND on MAC; SWT.CONTROL elsewhere
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED, true);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, mod1Name);
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK, SWT.MOD1);
		
		setDefaultAndFireEvent(store, 
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR, 
				findRGB(registry,ITextEditorThemeConstants.HYPERLINK_COLOR, new RGB(0, 0, 255)));

		HyperlinkDetectorDescriptor[] descriptors= EditorsUI.getHyperlinkDetectorRegistry().getHyperlinkDetectorDescriptors();
		for (int i= 0; i < descriptors.length; i++) {
			int stateMask= computeStateMask(descriptors[i].getModifierKeys());
			if (stateMask == SWT.SHIFT) {
				EditorsPlugin.logErrorMessage("The '" + descriptors[i].getId() + "' hyperlink detector specifies 'Shift' as modifier. This is not allowed and hence replaced with the default modifier."); //$NON-NLS-1$ //$NON-NLS-2$
				stateMask= -1;
			}
			store.setDefault(descriptors[i].getId() + HyperlinkDetectorDescriptor.STATE_MASK_POSTFIX, stateMask);
		}

		store.setToDefault(EDITOR_DISABLE_OVERWRITE_MODE);

		/*
		 * As of 3.3 we enabled spell checking per default
		 * but do not want this to impact our performance tests. For this
		 * reason we disable it when running the UI test application.
		 */ 
		boolean isInTestMode= System.getProperty("eclipse.perf.dbloc") != null; //$NON-NLS-1$
		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENABLED, !isInTestMode);
		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENGINE, ""); //$NON-NLS-1$
		
		store.setDefault(SHOW_RANGE_INDICATOR, true);
		store.setDefault(REVISION_ASK_BEFORE_QUICKDIFF_SWITCH, ""); //$NON-NLS-1$
		
		setDefaultAndFireEvent(store, 
				AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE, 
				findRGB(registry, ITextEditorThemeConstants.FIND_SCOPE_COLOR, new RGB(185, 176 , 180)));
		
		store.setDefault(AbstractTextEditor.PREFERENCE_RULER_CONTRIBUTIONS, ""); //$NON-NLS-1$
		store.setDefault(REVISION_RULER_RENDERING_MODE, IRevisionRulerColumnExtension.AGE.name());
		store.setDefault(REVISION_RULER_SHOW_AUTHOR, false);
		store.setDefault(REVISION_RULER_SHOW_REVISION, false);

		store.setDefault(EDITOR_WARN_IF_INPUT_DERIVED, true);
		store.setDefault(EDITOR_SMART_HOME_END, true);
		store.setDefault(EDITOR_SHOW_WHITESPACE_CHARACTERS, false);
		store.setDefault(EDITOR_TEXT_DRAG_AND_DROP_ENABLED, true);
		store.setDefault(EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, true);
		
		MarkerAnnotationPreferences.initializeDefaultValues(store);
	}

	/**
	 * Sets the default value and fires a property
	 * change event if necessary.
	 * 
	 * @param store	the preference store
	 * @param key the preference key
	 * @param newValue the new value
	 * @since 3.3
	 */
	private static void setDefaultAndFireEvent(IPreferenceStore store, String key, RGB newValue) {
		RGB oldValue= null;
		if (store.isDefault(key))
			oldValue= PreferenceConverter.getDefaultColor(store, key);
		
		PreferenceConverter.setDefault(store, key, newValue);
		
		if (oldValue != null && !oldValue.equals(newValue))
			store.firePropertyChangeEvent(key, oldValue, newValue);
	}
	
	/**
	 * Returns the RGB for the given key in the given color registry.
	 * 
	 * @param registry the color registry
	 * @param key the key for the constant in the registry
	 * @param defaultRGB the default RGB if no entry is found
	 * @return RGB the RGB
	 * @since 3.3
	 */
	private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
		if (registry == null)
			return defaultRGB;
		
		RGB rgb= registry.getRGB(key);
		if (rgb != null)
			return rgb;
		return defaultRGB;
	}

	/**
	 * Computes the state mask out of the given modifiers string.
	 *
	 * @param modifiers a string containing modifiers
	 * @return the state mask
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
