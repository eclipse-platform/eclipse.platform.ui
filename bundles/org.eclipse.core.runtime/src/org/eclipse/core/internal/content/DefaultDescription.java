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

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @see ContentType#getDefaultDescription
 */
public final class DefaultDescription implements IContentDescription {
	private ContentType contentType;
	private ContentTypeCatalog catalog;

	public DefaultDescription(ContentType contentType, ContentTypeCatalog catalog) {
		this.contentType = contentType;
		this.catalog = catalog;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultDescription))
			return false;
		return contentType.getId().equals(((DefaultDescription) obj).getContentType().getId());
	}

	public String getCharset() {
		return contentType.getDefaultCharset(catalog);
	}

	public IContentType getContentType() {
		return contentType;
	}

	public Object getProperty(QualifiedName key) {
		if (key == IContentDescription.CHARSET)
			return contentType.getDefaultCharset(catalog);
		return null;
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