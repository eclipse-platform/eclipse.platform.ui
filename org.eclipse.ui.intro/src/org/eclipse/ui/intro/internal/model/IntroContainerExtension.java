/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.internal.model;

import org.eclipse.core.runtime.*;

/**
 * An intro image element.
 */
public class IntroContainerExtension extends IntroElement {

	protected static final String CONTAINER_EXTENSION_ELEMENT = "extensionContent";

	private static final String PATH_ATTRIBUTE = "path";
	private static final String STYLE_ATTRIBUTE = "style";
	private static final String ALT_STYLE_ATTRIBUTE = "alt-style";

	private String path;
	private String style;
	private String altStyle;

	IntroContainerExtension(IConfigurationElement element) {
		super(element);
		path = element.getAttribute(PATH_ATTRIBUTE);
		style = element.getAttribute(STYLE_ATTRIBUTE);
		altStyle = element.getAttribute(ALT_STYLE_ATTRIBUTE);

		// Resolve.
		style = IntroModelRoot.getPluginLocation(style, element);
		altStyle = IntroModelRoot.getPluginLocation(altStyle, element);
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
	 */
	public int getType() {
		return IntroElement.CONTAINER_EXTENSION;
	}

	protected IConfigurationElement[] getChildren() {
		return getConfigurationElement().getChildren();
	}

	/**
	 * @return Returns the altStyle.
	 */
	protected String getAltStyle() {
		return altStyle;
	}

	/**
	 * @return Returns the style.
	 */
	protected String getStyle() {
		return style;
	}

}
