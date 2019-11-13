/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views.properties.tabbed;

/**
 * An abstract implementation of a section in a tab that overrides the tabs that
 * are provided by the tabbed property registry with a new list of tabs.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class AbstractOverridableTabListPropertySection
	extends AbstractPropertySection
	implements IOverridableTabList {

	@Override
	public ITabItem[] getTabs() {
		return new ITabItem[] {};
	}

	@Override
	public void selectTab(int tab) {
		/* no default implementation */
	}
}
