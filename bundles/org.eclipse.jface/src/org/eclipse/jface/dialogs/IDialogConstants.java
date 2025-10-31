/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

/**
 * Various dialog-related constants.
 * <p>
 * Within the dialog framework, all buttons are referred to by a button id.
 * Various common buttons, like "OK", "Cancel", and "Finish", have pre-assigned
 * button ids for convenience. If an application requires other dialog buttons,
 * they should be assigned application-specific button ids counting up from
 * <code>CLIENT_ID</code>.
 * </p>
 * <p>
 * Button label constants are also provided for the common buttons. JFace
 * automatically localizes these strings to the current locale; that is,
 * <code>YES_LABEL</code> would be bound to the string <code>"Si"</code> in
 * a Spanish locale, but to <code>"Oui"</code> in a French one.
 * </p>
 * <p>
 * All margins, spacings, and sizes are given in "dialog units" (DLUs), where
 * <ul>
 * <li>1 horizontal DLU = 1/4 average character width</li>
 * <li>1 vertical DLU = 1/8 average character height</li>
 * </ul>
 * </p>
 */
import org.eclipse.jface.resource.JFaceResources;

/**
 * IDialogConstants is the interface for common dialog strings and ids used throughout JFace. It is
 * recommended that you use these labels and ids wherever for consistency with the JFace dialogs.
 */
public interface IDialogConstants {
	// button ids

	// Note:  if new button ids are added, see
	// MessageDialogWithToggle.mapButtonLabelToButtonID(String, int)
	/**
	 * Button id for an "Ok" button (value 0).
	 */
	int OK_ID = 0;

	/**
	 * Button id for a "Cancel" button (value 1).
	 */
	int CANCEL_ID = 1;

	/**
	 * Button id for a "Yes" button (value 2).
	 */
	int YES_ID = 2;

	/**
	 * Button id for a "No" button (value 3).
	 */
	int NO_ID = 3;

	/**
	 * Button id for a "Yes to All" button (value 4).
	 */
	int YES_TO_ALL_ID = 4;

	/**
	 * Button id for a "Skip" button (value 5).
	 */
	int SKIP_ID = 5;

	/**
	 * Button id for a "Stop" button (value 6).
	 */
	int STOP_ID = 6;

	/**
	 * Button id for an "Abort" button (value 7).
	 */
	int ABORT_ID = 7;

	/**
	 * Button id for a "Retry" button (value 8).
	 */
	int RETRY_ID = 8;

	/**
	 * Button id for an "Ignore" button (value 9).
	 */
	int IGNORE_ID = 9;

	/**
	 * Button id for a "Proceed" button (value 10).
	 */
	int PROCEED_ID = 10;

	/**
	 * Button id for an "Open" button (value 11).
	 */
	int OPEN_ID = 11;

	/**
	 * Button id for a "Close" button (value 12).
	 */
	int CLOSE_ID = 12;

	/**
	 * Button id for a "Details" button (value 13).
	 */
	int DETAILS_ID = 13;

	/**
	 * Button id for a "Back" button (value 14).
	 */
	int BACK_ID = 14;

	/**
	 * Button id for a "Next" button (value 15).
	 */
	int NEXT_ID = 15;

	/**
	 * Button id for a "Finish" button (value 16).
	 */
	int FINISH_ID = 16;

	/**
	 * Button id for a "Help" button (value 17).
	 */
	int HELP_ID = 17;

	/**
	 * Button id for a "Select All" button (value 18).
	 */
	int SELECT_ALL_ID = 18;

	/**
	 * Button id for a "Deselect All" button (value 19).
	 */
	int DESELECT_ALL_ID = 19;

	/**
	 * Button id for a "Select types" button (value 20).
	 */
	int SELECT_TYPES_ID = 20;

	/**
	 * Button id for a "No to All" button (value 21).
	 */
	int NO_TO_ALL_ID = 21;

	/**
	 * Starting button id reserved for internal use by JFace (value 256). JFace
	 * classes make ids by adding to this number.
	 */
	int INTERNAL_ID = 256;

	/**
	 * Starting button id reserved for use by clients of JFace (value 1024).
	 * Clients of JFace should make ids by adding to this number.
	 */
	int CLIENT_ID = 1024;

	// button labels
	/**
	 * The label for OK buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String OK_LABEL = JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY);

	/**
	 * The label for cancel buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String CANCEL_LABEL = JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY);

	/**
	 * The label for yes buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.YES_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String YES_LABEL = JFaceResources.getString(IDialogLabelKeys.YES_LABEL_KEY);
	/**
	 * The label for no buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.NO_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String NO_LABEL = JFaceResources.getString(IDialogLabelKeys.NO_LABEL_KEY);

	/**
	 * The label for not to all buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.NO_TO_ALL_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String NO_TO_ALL_LABEL = JFaceResources.getString(IDialogLabelKeys.NO_TO_ALL_LABEL_KEY);

	/**
	 * The label for yes to all buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.YES_TO_ALL_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String YES_TO_ALL_LABEL = JFaceResources.getString(IDialogLabelKeys.YES_TO_ALL_LABEL_KEY);

	/**
	 * The label for skip buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.SKIP_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String SKIP_LABEL = JFaceResources.getString(IDialogLabelKeys.SKIP_LABEL_KEY);

	/**
	 * The label for stop buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.STOP_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String STOP_LABEL = JFaceResources.getString(IDialogLabelKeys.STOP_LABEL_KEY);

	/**
	 * The label for abort buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.ABORT_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String ABORT_LABEL = JFaceResources.getString(IDialogLabelKeys.ABORT_LABEL_KEY);

	/**
	 * The label for retry buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.RETRY_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String RETRY_LABEL = JFaceResources.getString(IDialogLabelKeys.RETRY_LABEL_KEY);

	/**
	 * The label for ignore buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.IGNORE_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String IGNORE_LABEL = JFaceResources.getString(IDialogLabelKeys.IGNORE_LABEL_KEY);

	/**
	 * The label for proceed buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.PROCEED_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String PROCEED_LABEL = JFaceResources.getString(IDialogLabelKeys.PROCEED_LABEL_KEY);

	/**
	 * The label for open buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.OPEN_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String OPEN_LABEL = JFaceResources.getString(IDialogLabelKeys.OPEN_LABEL_KEY);

	/**
	 * The label for close buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.CLOSE_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String CLOSE_LABEL = JFaceResources.getString(IDialogLabelKeys.CLOSE_LABEL_KEY);

	/**
	 * The label for show details buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.SHOW_DETAILS_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String SHOW_DETAILS_LABEL = JFaceResources.getString(IDialogLabelKeys.SHOW_DETAILS_LABEL_KEY);

	/**
	 * The label for hide details buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.HIDE_DETAILS_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String HIDE_DETAILS_LABEL = JFaceResources.getString(IDialogLabelKeys.HIDE_DETAILS_LABEL_KEY);

	/**
	 * The label for back buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.BACK_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String BACK_LABEL = JFaceResources.getString(IDialogLabelKeys.BACK_LABEL_KEY);

	/**
	 * The label for next buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.NEXT_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String NEXT_LABEL = JFaceResources.getString(IDialogLabelKeys.NEXT_LABEL_KEY);

	/**
	 * The label for finish buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.FINISH_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String FINISH_LABEL = JFaceResources.getString(IDialogLabelKeys.FINISH_LABEL_KEY);

	/**
	 * The label for help buttons.
	 * Using this static label string provides optimum performance by looking
	 * up the label only once when JFace is initialized.  However, clients that
	 * wish to support multiple locales in one system should instead use the pattern
	 * <code>JFaceResources.getString(IDialogLabelKeys.HELP_LABEL_KEY)</code>
	 * so that a locale other than the default may be consulted.
	 */
	String HELP_LABEL = JFaceResources.getString(IDialogLabelKeys.HELP_LABEL_KEY);

	// Margins, spacings, and sizes
	/**
	 * Vertical margin in dialog units (value 7).
	 */
	int VERTICAL_MARGIN = 7;

	/**
	 * Vertical spacing in dialog units (value 4).
	 */
	int VERTICAL_SPACING = 4;

	/**
	 * Horizontal margin in dialog units (value 7).
	 */
	int HORIZONTAL_MARGIN = 7;

	/**
	 * Horizontal spacing in dialog units (value 4).
	 */
	int HORIZONTAL_SPACING = 4;

	/**
	 * Height of button bar in dialog units (value 25).
	 */
	int BUTTON_BAR_HEIGHT = 25;

	/**
	 * Left margin in dialog units (value 20).
	 */
	int LEFT_MARGIN = 20;

	/**
	 * Button margin in dialog units (value 4).
	 */
	int BUTTON_MARGIN = 4;

	/**
	 * Button height in dialog units (value 14).
	 *
	 * @deprecated This constant is no longer in use.
	 * The button heights are now determined by the layout.
	 */
	@Deprecated(forRemoval = true, since = "2025-12")
	int BUTTON_HEIGHT = 14;

	/**
	 * Button width in dialog units (value 61).
	 */
	int BUTTON_WIDTH = 61;

	/**
	 * Indent in dialog units (value 21).
	 *
	 * @deprecated As of 3.9, this is deprecated as it is too big as DLU and got accidentally used
	 *             when setting pixel values
	 */
	@Deprecated(forRemoval = true, since = "2025-12")
	int INDENT = 21;

	/**
	 * Small indent in dialog units (value 7).
	 *
	 * @deprecated As of 3.9, this is deprecated as it is not clear what the meaning of a
	 *             "small indent" would be
	 */
	@Deprecated(forRemoval = true, since = "2025-12")
	int SMALL_INDENT = 7;

	/**
	 * Entry field width in dialog units (value 200).
	 */
	int ENTRY_FIELD_WIDTH = 200;

	/**
	 * Minimum width of message area in dialog units (value 300).
	 */
	int MINIMUM_MESSAGE_AREA_WIDTH = 300;
}
