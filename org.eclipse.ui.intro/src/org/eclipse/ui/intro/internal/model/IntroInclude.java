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
public class IntroInclude extends IntroElement {

	protected static final String INCLUDE_ELEMENT = "include";

	private static final String CONFIG_ID_ATTRIBUTE = "configId";
	private static final String PATH_ATTRIBUTE = "path";
	/**
	 * boolean attribute, default is false.
	 */
	private static final String MERGE_STYLE_ATTRIBUTE = "merge-style";

	private String configId;
	private String path;
	private boolean mergeStyle = false;

	IntroInclude(IConfigurationElement element) {
		super(element);
		configId = element.getAttribute(CONFIG_ID_ATTRIBUTE);
		path = element.getAttribute(PATH_ATTRIBUTE);
		String mergeStyleString = element.getAttribute(MERGE_STYLE_ATTRIBUTE);
		mergeStyle =
			(mergeStyleString != null
				&& mergeStyleString.equalsIgnoreCase("true"))
				? true
				: false;
	}

	/**
	 * @return Returns the configId.
	 */
	public String getConfigId() {
		return configId;
	}

	/**
	 * @return Returns the mergeStyle.
	 */
	public boolean getMergeStyle() {
		return mergeStyle;
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
		return IntroElement.INCLUDE;
	}

}
