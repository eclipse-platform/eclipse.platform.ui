/*******************************************************************************
 * Copyright (c) 2018, 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexander Kurtakov (Red Hat Inc.) - initial version
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.genericeditor.messages"; //$NON-NLS-1$
	public static String DefaultWordHighlightStrategy_OccurrencesOf;

	public static String TextViewer_open_hyperlink_error_title;
	public static String TextViewer_open_hyperlink_error_message;

	public static String GotoMatchingBracket_error_noMatchingBracket;
	public static String GotoMatchingBracket_error_bracketOutsideSelectedElement;
	public static String GenericEditorMergeViewer_title;

	public static String ContentAssistant;
	public static String ContentAssistant_autoActivation;
	public static String ContentAssistant_autoActivation_Tooltip;
	public static String ContentAssistant_autoActivationDelay;
	public static String ContentAssistant_autoActivationDelay_Tooltip;
	public static String ContentAssistant_autoActivationOnType;
	public static String ContentAssistant_autoActivationOnType_Tooltip;
	public static String ContentAssistant_autoActivationDelay_InvalidInput;
	public static String ContentAssistant_autoActivationDelay_EmptyInput;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
