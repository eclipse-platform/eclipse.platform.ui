/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

	public TextTestsSectionDescriptor(String aWord) {
		super(null);
		this.word = aWord;
	}

	public String getId() {
		return word;
	}

	public ISection getSectionClass() {
		return new TextTestsLabelSection(word);
	}

	public String getTargetTab() {
		return word;
	}

}
