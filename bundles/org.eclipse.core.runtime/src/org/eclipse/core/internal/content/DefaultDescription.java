/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @see IContentType#getDefaultDescription
 */
public final class DefaultDescription implements IContentDescription {
	private ContentType contentType;

	public DefaultDescription(ContentType contentType) {
		this.contentType = contentType;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultDescription))
			return false;
		return contentType.getId().equals(((DefaultDescription) obj).getContentType().getId());
	}

	public String getCharset() {
		return (String) getProperty(CHARSET);
	}

	/**
	 * @see IContentDescription
	 */
	public IContentType getContentType() {
		//TODO performance: potential creation of garbage
		return new ContentTypeHandler(contentType, contentType.getCatalog().getGeneration());
	}

	public Object getProperty(QualifiedName key) {
		return contentType.getDefaultProperty(key);
	}

	public int hashCode() {
		return contentType.getId().hashCode();
	}

	public boolean isRequested(QualifiedName key) {
		return true;
	}

	public void setProperty(QualifiedName key, Object value) {
		throw new IllegalStateException();
	}

	public String toString() {
		return "{default} : " + contentType; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
