/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package org.eclipse.ui.tests.views.properties.tabbed.text;

import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;

/**
 * A section descriptor for the text test view.
 *
 * @author Anthony Hunter
 */
public class TextTestsSectionDescriptor extends AbstractSectionDescriptor {

	private String word;

	private String tabId;

	public TextTestsSectionDescriptor(String aWord, String aTabId) {
		super(null);
		this.word = aWord;
		this.tabId = aTabId;
	}

	@Override
	public String getId() {
		return word + "@" + Integer.toHexString(word.hashCode());
	}

	@Override
	public ISection getSectionClass() {
		return new TextTestsLabelSection(word);
	}

	@Override
	public String getTargetTab() {
		return tabId;
	}

}
