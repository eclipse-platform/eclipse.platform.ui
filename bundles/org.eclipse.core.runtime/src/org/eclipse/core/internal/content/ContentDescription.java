/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

public class ContentDescription implements IContentDescription {
	private IContentType contentType;
	private Map properties = new HashMap(3);
	private boolean allOptions;

	ContentDescription(QualifiedName[] requested) {
		if (requested == null) {
			allOptions = true;
			return;
		}
		for (int i = 0; i < requested.length; i++)
			properties.put(requested[i], null);
	}

	private void assertMutable() {
		if (contentType != null)
			throw new IllegalStateException("Content description is immutable"); //$NON-NLS-1$
	}

	/**
	 * @see IContentDescription
	 */
	public IContentType getContentType() {
		return contentType;
	}

	/**
	 * @see IContentDescription
	 */
	public Object getProperty(QualifiedName key) {
		return properties.get(key);
	}

	public void setContentType(IContentType contentType) {
		assertMutable();
		this.contentType = contentType;
	}

	/**
	 * @see IContentDescription
	 */
	public void setProperty(QualifiedName key, Object value) {
		assertMutable();
		properties.put(key, value);
	}

	/**
	 * @see IContentDescription
	 */
	public boolean isRequested(QualifiedName propertyKey) {
		if (allOptions)
			return true;
		return properties.containsKey(propertyKey);
	}
}