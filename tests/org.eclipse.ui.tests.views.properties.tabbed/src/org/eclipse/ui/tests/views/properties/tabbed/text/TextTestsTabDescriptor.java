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

import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;

/**
 * A tab descriptor for the text test view.
 *
 * @author Anthony Hunter
 */
public class TextTestsTabDescriptor extends AbstractTabDescriptor {

	private String word;

	public TextTestsTabDescriptor(String aWord) {
		super();
		this.word = aWord;
		getSectionDescriptors().add(new TextTestsSectionDescriptor(aWord, getId()));
		/* TextTestsSectionDescriptor2 added to the tests and it is filtered */
		getSectionDescriptors().add(new TextTestsSectionDescriptor2(aWord, getId()));
	}

	public String getCategory() {
		return "default"; //$NON-NLS-1$
	}

	public String getId() {
		return word + "@" + Integer.toHexString(word.hashCode());
	}

	public String getLabel() {
		return word;
	}
}
