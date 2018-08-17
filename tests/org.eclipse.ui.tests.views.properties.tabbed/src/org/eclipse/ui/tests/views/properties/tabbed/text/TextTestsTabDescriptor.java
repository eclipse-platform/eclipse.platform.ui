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

	@Override
	public String getCategory() {
		return "default"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return word + "@" + Integer.toHexString(word.hashCode());
	}

	@Override
	public String getLabel() {
		return word;
	}
}
