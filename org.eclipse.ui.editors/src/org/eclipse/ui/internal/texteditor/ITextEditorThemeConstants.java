/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;


/**
 * Defines the constants used in the <code>org.eclipse.ui.themes</code>
 * extension contributed by this plug-in.
 *
 * @since 3.3
 */
public interface ITextEditorThemeConstants {

	/**
	 * Theme constant for the color used to highlight the current line
	 * (value <code>"org.eclipse.ui.editors.currentLineColor"</code>).
	 */
	public final static String CURRENT_LINE_COLOR= "org.eclipse.ui.editors.currentLineColor"; //$NON-NLS-1$

	/**
	 * Theme constant for the color used to render line numbers inside the line number ruler
	 * (value <code>"org.eclipse.ui.editors.lineNumberRulerColor"</code>).
	 */
	public final static String LINE_NUMBER_RULER_COLOR= "org.eclipse.ui.editors.lineNumberRulerColor"; //$NON-NLS-1$

	/**
	 * Theme constant for the color used for hyperlinks
	 * (value <code>"org.eclipse.ui.editors.hyperlinkColor"</code>).
	 */
	public final static String HYPERLINK_COLOR= "org.eclipse.ui.editors.hyperlinkColor"; //$NON-NLS-1$

	/**
	 * Theme constant for the find scope background color
	 * (value <code>org.eclipse.ui.editors.findScope</code>).
	 */
	public final static String FIND_SCOPE_COLOR= "org.eclipse.ui.editors.findScope"; //$NON-NLS-1$

	/**
	 * Theme constant for the color used to render the print margin
	 * (value <code>"org.eclipse.ui.editors.printMarginColor"</code>).
	 */
	public final static String PRINT_MARGIN_COLOR= "org.eclipse.ui.editors.printMarginColor"; //$NON-NLS-1$

	/**
	 * Theme constant for the color used to render the editor background color
	 * (value <code>"org.eclipse.ui.editors.backgroundColor"</code>).
	 */
	public static final String PREFERENCE_COLOR_BACKGROUND= "org.eclipse.ui.editors.backgroundColor"; //$NON-NLS-1$

	/**
	 * Theme constant for the color used to render the editor foreground color
	 * (value <code>"org.eclipse.ui.editors.foregroundColor"</code>).
	 */
	public static final String PREFERENCE_COLOR_FOREGROUND= "org.eclipse.ui.editors.foregroundColor"; //$NON-NLS-1$

}
