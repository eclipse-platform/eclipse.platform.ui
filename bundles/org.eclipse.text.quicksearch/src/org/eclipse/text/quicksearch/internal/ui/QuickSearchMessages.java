/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.osgi.util.NLS;

public final class QuickSearchMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.text.quicksearch.internal.ui.QuickSearchMessages";//$NON-NLS-1$

	private QuickSearchMessages() {
		// Do not instantiate
	}

	public static String QuickSearchDialog_switchButtonTooltip;
	public static String QuickSearchDialog_defaultViewer;

	static {
		NLS.initializeMessages(BUNDLE_NAME, QuickSearchMessages.class);
	}
}
