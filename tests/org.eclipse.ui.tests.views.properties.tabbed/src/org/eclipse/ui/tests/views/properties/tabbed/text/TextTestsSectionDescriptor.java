/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public String getId() {
		return word + "@" + Integer.toHexString(word.hashCode());
	}

	public ISection getSectionClass() {
		return new TextTestsLabelSection(word);
	}

	public String getTargetTab() {
		return tabId;
	}

}
