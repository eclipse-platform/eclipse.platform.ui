/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;


/**
 * Preference constants used in the default text editor preference store.
 *
 * @since 2.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
  */
public class TextEditorPreferenceConstants {

	/**
	 * Prevent initialization.
	 */
	private TextEditorPreferenceConstants() {
	}

	/**
	 * A named preference that controls whether the current line highlighting is turned on or off
	 * (value <code>"currentLine"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
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
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
	 */
	public final static String EDITOR_CURRENT_LINE_COLOR= "currentLineColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the print margin is turned on or off
 	 * (value <code>"printMargin"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
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
	 * @deprecated As of 3.0,, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
	 */
	public final static String EDITOR_PRINT_MARGIN_COLOR= "printMarginColor"; //$NON-NLS-1$

	/**
	 * Print margin column
	 * (value <code>"printMarginColumn"</code>).
	 * <p>
	 * The preference value is of type <code>int</code>.
	 * </p>
	 * @deprecated As of 3.0,, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
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
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
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
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_COLOR= "othersIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows unknown indicators
 	 * (value <code>"othersIndicationInOverviewRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER= "othersIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the overview ruler is shown in the UI
 	 * (value <code>"overviewRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
	 */
	public final static String EDITOR_OVERVIEW_RULER= "overviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the line number ruler is shown in the UI
	 * (value <code>"lineNumberRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
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
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants}
	 */
	public final static String EDITOR_LINE_NUMBER_RULER_COLOR= "lineNumberColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_PROBLEM_INDICATION= "problemIndication"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_PROBLEM_INDICATION_COLOR= "problemIndicationColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_WARNING_INDICATION= "warningIndication"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_WARNING_INDICATION_COLOR= "warningIndicationColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_INFO_INDICATION= "infoIndication"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_INFO_INDICATION_COLOR= "infoIndicationColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_TASK_INDICATION= "taskIndication"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_TASK_INDICATION_COLOR= "taskIndicationColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_BOOKMARK_INDICATION= "bookmarkIndication"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_BOOKMARK_INDICATION_COLOR= "bookmarkIndicationColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_SEARCH_RESULT_INDICATION= "searchResultIndication"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_SEARCH_RESULT_INDICATION_COLOR= "searchResultIndicationColor"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER= "errorIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER= "warningIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_INFO_INDICATION_IN_OVERVIEW_RULER= "infoIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER= "taskIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER= "bookmarkIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * @deprecated As of 2.1, provided by <code>org.eclipse.ui.editors.markerAnnotationSpecification</code> extension point
	 */
	public final static String EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER= "searchResultIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
  	* Initializes the given preference store with the default values.
	 *
  	* @param store the preference store to be initialized
  	*/
	public static void initializeDefaultValues(IPreferenceStore store) {

		// set defaults from AbstractDecoratedTextEditor
		AbstractDecoratedTextEditorPreferenceConstants.initializeDefaultValues(store);

		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, true);
	}
}
