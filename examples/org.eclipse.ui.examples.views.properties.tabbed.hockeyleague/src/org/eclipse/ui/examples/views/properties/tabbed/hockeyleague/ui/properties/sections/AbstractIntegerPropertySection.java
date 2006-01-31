/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections;

/**
 * An abstract implementation of a section for a field with a String property
 * value.
 * 
 * @author Anthony Hunter
 */
public abstract class AbstractIntegerPropertySection
	extends AbstractTextPropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTextPropertySection#isEqual(java.lang.String)
	 */
	protected boolean isEqual(String newText) {
		try {
			Integer.parseInt(newText);
		} catch (NumberFormatException e) {
			refresh();
			return true;
		}
		Integer integer = new Integer(Integer.parseInt(newText)); 
		return getFeatureInteger().equals(integer);
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTextPropertySection#getFeatureAsText()
	 */
	protected String getFeatureAsText() {
		return getFeatureInteger().toString();
	}

	/**
	 * Get the text value of the feature for the text field for the section.
	 * 
	 * @return the text value of the feature.
	 */
	protected abstract Integer getFeatureInteger();

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTextPropertySection#getFeatureValue(java.lang.String)
	 */
	protected Object getFeatureValue(String newText) {
		return new Integer(Integer.parseInt(newText));
	}
}