/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.serializers;

import java.util.ArrayList;
import java.util.List;

/**
 * CSS Serializer configuration used by {@link CSSSerializer} to filter the
 * attribute of the widget like Text[style='SWT.MULTI'].
 */
public class CSSSerializerConfiguration {

	private static final String[] EMPTY_STRING = new String[0];

	private List attributesFilter = null;

	/**
	 * Add attribute name <code>attributeName</code> to filter.
	 * 
	 * @param attributeName
	 */
	public void addAttributeFilter(String attributeName) {
		if (attributesFilter == null)
			attributesFilter = new ArrayList();
		attributesFilter.add(attributeName);
	}

	/**
	 * Return list of attribute name to filter.
	 * 
	 * @return
	 */
	public String[] getAttributesFilter() {
		if (attributesFilter != null)
			return (String[]) attributesFilter.toArray(EMPTY_STRING);
		return EMPTY_STRING;
	}

}
