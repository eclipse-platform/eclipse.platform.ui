/*******************************************************************************
 * Copyright (c) 2008, 2008 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.views.properties.tabbed.text;

import org.eclipse.jface.viewers.IFilter;

/**
 * A section descriptor for the text test view that should be filtered and not
 * shown.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class TextTestsSectionDescriptor2 extends TextTestsSectionDescriptor {

	public TextTestsSectionDescriptor2(String word, String tabId) {
		super(word, tabId);
	}

	/*
	 * @see
	 * org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor#getFilter
	 * ()
	 */
	@Override
	public IFilter getFilter() {
		return new IFilter() {

			@Override
			public boolean select(Object toTest) {
				return false;
			}
		};
	}

}
