/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;


import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;



/**
 * Preference constants used in the extended text editor preference store.
 * @since 3.0
  */
public class ExtendedTextEditorPreferenceConstants {
	
	/**
	 * Prevent initialization.
	 */
	private ExtendedTextEditorPreferenceConstants() {
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
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_CURRENT_LINE_COLOR= "currentLineColor"; //$NON-NLS-1$

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
	 * @see org.eclipse.jface.preference.PreferenceConverter
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
	 * @since 2.1
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
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_COLOR= "othersIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows unknown indicators
 	 * (value <code>"othersIndicationInOverviewRuler"</code>). 
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
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
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @see #EDITOR_LINE_NUMBER_RULER
	 */
	public final static String EDITOR_LINE_NUMBER_RULER_COLOR= "lineNumberColor"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls whether quick diff colors are shown on the line number bar.
	 * <p>
	 * Value is of type <code>boolean</code>.
	 * @since 3.0
	 */
	public static final String QUICK_DIFF_ALWAYS_ON= "quickdiff.quickDiff"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls the default quick diff reference provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * @since 3.0
	 */
	public static final String QUICK_DIFF_DEFAULT_PROVIDER= "quickdiff.defaultProvider"; //$NON-NLS-1$

	/**
	 * A named preference that controls the default quick diff reference provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * @since 3.0
	 */
	public static final String QUICK_DIFF_CHARACTER_MODE= "quickdiff.characterMode"; //$NON-NLS-1$

	/**
  	* Initializes the given preference store with the default values.
	 * 
  	* @param store the preference store to be initialized
  	*/
	public static void initializeDefaultValues(IPreferenceStore store) {

		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, true);
		PreferenceConverter.setDefault(store, ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, new RGB(225, 235, 224));

		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, false);
		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);
		PreferenceConverter.setDefault(store, ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, new RGB(176, 180 , 185));

		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, false);
		PreferenceConverter.setDefault(store, ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, new RGB(0, 0, 0));
		store.setDefault(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, true);
		store.setDefault(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE, false);
		store.setDefault(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER, "org.eclipse.ui.internal.texteditor.quickdiff.LastSaveReferenceProvider"); //$NON-NLS-1$

		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, true);
		
		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION, false);
		store.setDefault(ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER, false);
		PreferenceConverter.setDefault(store, ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR, new RGB(0, 0, 0));
		
		MarkerAnnotationPreferences.initializeDefaultValues(store);
	}
}
