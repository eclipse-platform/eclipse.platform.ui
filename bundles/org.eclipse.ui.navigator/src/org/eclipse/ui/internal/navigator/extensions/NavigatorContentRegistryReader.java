/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
public class NavigatorContentRegistryReader extends RegistryReader implements
		INavigatorContentExtPtConstants {

	protected final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_MANAGER = NavigatorContentDescriptorManager
			.getInstance();

	protected NavigatorContentRegistryReader() {
		super(NavigatorPlugin.PLUGIN_ID, TAG_NAVIGATOR_CONTENT);
	}

	protected boolean readElement(IConfigurationElement element) {
		String elementName = element.getName();

		/* These are all of the valid root tags that exist */
		return TAG_ACTION_PROVIDER.equals(elementName)
				|| TAG_NAVIGATOR_CONTENT.equals(elementName)
				|| TAG_COMMON_WIZARD.equals(elementName)
				|| TAG_COMMON_FILTER.equals(elementName);
	}
}
