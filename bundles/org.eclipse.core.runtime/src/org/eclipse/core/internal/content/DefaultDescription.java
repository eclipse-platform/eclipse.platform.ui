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

public final class DefaultDescription implements IContentDescription {
	private IContentType contentType;

	public DefaultDescription(ContentType contentType) {
		this.contentType = contentType;
	}

	public String getCharset() {
		return contentType.getDefaultCharset();
	}
	
	public IContentType getContentType() {
		return contentType;
	}

	public Object getProperty(QualifiedName key) {
		if (key == IContentDescription.CHARSET)
			return contentType.getDefaultCharset();
		return null;
	}

	public boolean isDefault() {
		return true;
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