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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;

/**
 * Preference constants used in the Generic Editor preference store. Clients
 * should only read the Generic Editor preference store using these values.
 * Clients are not allowed to modify the preference store programmatically.
 * <p>
 * This class it is not intended to be instantiated or subclassed by clients.
 * </p>
 *
 * @since 1.2
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GenericEditorPreferenceConstants {

	private GenericEditorPreferenceConstants() {

	}

	/**
	 * A named preference that controls whether bracket matching highlighting is
	 * turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 1.2
	 */
	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to highlight matching brackets.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 *
	 * @since 1.2
	 */
	public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether bracket at caret location is
	 * highlighted or not.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 1.2
	 */
	public final static String EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION = "highlightBracketAtCaretLocation"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether enclosing bracket matching
	 * highlighting is turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 1.2
	 */
	public final static String EDITOR_ENCLOSING_BRACKETS = "enclosingBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether auto activation is enabled on the
	 * content assistant.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 1.3
	 */
	public final static String CONTENT_ASSISTANT_AUTO_ACTIVATION = "contentAssistant.autoActivation"; //$NON-NLS-1$
	public final static boolean CONTENT_ASSISTANT_AUTO_ACTIVATION_DEFAULT = true;

	/**
	 * A named preference that controls how long the auto activation delay is on the
	 * content assistant.
	 * <p>
	 * Value is of type <code>Integer</code> and in milliseconds.
	 * </p>
	 *
	 * @since 1.3
	 */
	public final static String CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY = "contentAssistant.autoActivationDelay"; //$NON-NLS-1$
	public final static int CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY_DEFAULT = 10;

	/**
	 * A named preference that controls whether auto activation on typing is enabled
	 * on the content assistant.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 1.3
	 */
	public final static String CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE = "contentAssistant.autoActivationOnType"; //$NON-NLS-1$
	public final static boolean CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE_DEFAULT = true;

	/**
	 * Returns the Generic Editor preference store.
	 *
	 * @return the Generic Editor preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return GenericEditorPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes the given preference store with the default values.
	 *
	 * @param store the preference store to be initialized
	 *
	 * @since 1.2
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
		store.setDefault(GenericEditorPreferenceConstants.EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION, false);
		store.setDefault(GenericEditorPreferenceConstants.EDITOR_ENCLOSING_BRACKETS, false);
		store.setDefault(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION,
				CONTENT_ASSISTANT_AUTO_ACTIVATION_DEFAULT);
		store.setDefault(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY,
				CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY_DEFAULT);
		store.setDefault(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE,
				CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE_DEFAULT);
		// Colors that are set by the current theme
		GenericEditorPluginPreferenceInitializer.setThemeBasedPreferences(store, false);
	}

}
