/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.ui.editors.text.templates;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.0
 */
final class ContributionTemplateMessages extends NLS {

	private static final String BUNDLE_NAME= ContributionTemplateMessages.class.getName();

	private ContributionTemplateMessages() {
		// Do not instantiate
	}

	public static String ContributionTemplateStore_ignore_no_id;
	public static String ContributionTemplateStore_ignore_deleted;
	public static String ContributionTemplateStore_ignore_validation_failed;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ContributionTemplateMessages.class);
	}
}