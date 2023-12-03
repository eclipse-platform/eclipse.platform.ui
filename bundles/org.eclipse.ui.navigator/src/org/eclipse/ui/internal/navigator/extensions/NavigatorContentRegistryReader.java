/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;

/**
 * Provides a common superclass for all consumers of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point.
 *
 * @since 3.2
 */
public class NavigatorContentRegistryReader extends RegistryReader implements
		INavigatorContentExtPtConstants {

	protected final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_MANAGER = NavigatorContentDescriptorManager
			.getInstance();

	protected NavigatorContentRegistryReader() {
		super(NavigatorPlugin.PLUGIN_ID, TAG_NAVIGATOR_CONTENT);
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		String elementName = element.getName();

		/* These are all of the valid root tags that exist */
		return TAG_ACTION_PROVIDER.equals(elementName)
				|| TAG_NAVIGATOR_CONTENT.equals(elementName)
				|| TAG_COMMON_WIZARD.equals(elementName)
				|| TAG_COMMON_FILTER.equals(elementName);
	}
}
