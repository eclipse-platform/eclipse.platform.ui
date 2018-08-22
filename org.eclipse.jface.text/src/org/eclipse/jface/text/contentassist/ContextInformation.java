/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.jface.text.contentassist;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.Assert;



/**
 * A default implementation of the <code>IContextInformation</code> interface.
 */
public final class ContextInformation implements IContextInformation {

	/** The name of the context. */
	private final String fContextDisplayString;
	/** The information to be displayed. */
	private final String fInformationDisplayString;
	/** The image to be displayed. */
	private final Image fImage;

	/**
	 * Creates a new context information without an image.
	 *
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information
	 */
	public ContextInformation(String contextDisplayString, String informationDisplayString) {
		this(null, contextDisplayString, informationDisplayString);
	}

	/**
	 * Creates a new context information with an image.
	 *
	 * @param image the image to display when presenting the context information
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information,
	 *		may not be <code>null</code>
	 */
	public ContextInformation(Image image, String contextDisplayString, String informationDisplayString) {

		Assert.isNotNull(informationDisplayString);

		fImage= image;
		fContextDisplayString= contextDisplayString;
		fInformationDisplayString= informationDisplayString;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof IContextInformation) {
			IContextInformation contextInformation= (IContextInformation) object;
			boolean equals= fInformationDisplayString.equalsIgnoreCase(contextInformation.getInformationDisplayString());
			if (fContextDisplayString != null)
				equals= equals && fContextDisplayString.equalsIgnoreCase(contextInformation.getContextDisplayString());
			return equals;
		}
		return false;
	}

	@Override
	public int hashCode() {
	 	int low= fContextDisplayString != null ? fContextDisplayString.hashCode() : 0;
	 	return (fInformationDisplayString.hashCode() << 16) | low;
	}

	@Override
	public String getInformationDisplayString() {
		return fInformationDisplayString;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public String getContextDisplayString() {
		if (fContextDisplayString != null)
			return fContextDisplayString;
		return fInformationDisplayString;
	}
}
