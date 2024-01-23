/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.time.Duration;
import java.util.Objects;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.notifications.NotificationPopup;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;

/**
 * Utility class to display a popup the first time the FindReplaceOverlay is
 * shown, informing the user about the new functionality. This class will track
 * whether the popup was already shown and will only show the Overlay on the
 * first time the popup was shown.
 */
class FindReplaceOverlayFirstTimePopup {

	private FindReplaceOverlayFirstTimePopup() {
	}

	private static final String PREFERENCE_NODE_NAME = "org.eclipse.ui.editors"; //$NON-NLS-1$
	private static final String SETTING_POPUP_WAS_SHOWN_BEFORE = "hasShownOverlayPopupBefore"; //$NON-NLS-1$
	/**
	 * How long to wait until the pop up should vanish in Ms.
	 */
	private static final Duration POPUP_VANISH_TIME = Duration.ofSeconds(6);
	private static final String USE_FIND_REPLACE_OVERLAY = "useFindReplaceOverlay"; //$NON-NLS-1$

	/**
	 * Returns the dialog settings object used to remember whether the popup was
	 * already shown or not.
	 *
	 * @return the dialog settings to be used
	 */
	private static IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceOverlayFirstTimePopup.class))
				.getDialogSettings();
		return settings;
	}

	private static void disableUseOverlayPreference() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(PREFERENCE_NODE_NAME); // $NON-NLS-1$
		preferences.putBoolean(USE_FIND_REPLACE_OVERLAY, false);
	}

	/**
	 * Displays a popup indicating that instead of the FindReplaceDialog, the
	 * FindReplaceOverlay is currently being used. Only displays the popup on the
	 * first time use of FindReplaceOverlay.
	 *
	 * The popup is bound to the bottom right corner of the principal computer
	 * Monitor.
	 *
	 * @param shellToUse the shell to bind the popup to
	 */
	public static void displayPopupIfNotAlreadyShown(Shell shellToUse) {
		IDialogSettings settings = getDialogSettings();

		if (!settings.getBoolean(SETTING_POPUP_WAS_SHOWN_BEFORE)) {
			settings.put(SETTING_POPUP_WAS_SHOWN_BEFORE, true);

			Display displayToUse = Objects.nonNull(shellToUse) ? shellToUse.getDisplay() : Display.getDefault();

			NotificationPopup.forDisplay(displayToUse).content(t -> createFirstTimeNotification(t))
					.title(FindReplaceMessages.FindReplaceOverlayFirstTimePopup_FindReplaceOverlayFirstTimePopup_title,
							true)
					.delay(POPUP_VANISH_TIME.toMillis()).open();
		}

	}

	private static Control createFirstTimeNotification(Composite composite) {
		Link messageBody = new Link(composite, SWT.WRAP);

		messageBody
				.setText(FindReplaceMessages.FindReplaceOverlayFirstTimePopup_FindReplaceOverlayFirstTimePopup_message);
		messageBody.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		messageBody.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			disableUseOverlayPreference();
			composite.getShell().close();
		}));

		return messageBody;
	}
}
