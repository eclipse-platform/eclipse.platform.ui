/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Bug 538111 - [generic editor] Extension point for ICharacterPairMatcher
 */
package org.eclipse.ui.internal.genericeditor.preferences;

import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;

/**
 * Defines the constants used in the <code>org.eclipse.ui.themes</code>
 * extension contributed by this plug-in.
 *
 * @since 1.2
 */
public interface IGenericEditorThemeConstants {

	String ID_PREFIX = GenericEditorPlugin.BUNDLE_ID + "."; //$NON-NLS-1$

	/**
	 * Theme constant for the color used to highlight matching brackets.
	 */
	public final String EDITOR_MATCHING_BRACKETS_COLOR = ID_PREFIX
			+ GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;

}
