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
	private String charset;
	private IContentType contentType;
	private boolean immutable;
	private int mark;
	private Map properties;
	ContentDescription() {
		super();
	}
	private void assertMutable() {
		//TODO: NLS this
		if (immutable)
			throw new IllegalStateException("Content description is immutable");
	}
	/**
	 * @see IContentDescription
	 */
	public String getCharset() {
		return charset;
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
	public int getMark() {
		return mark;
	}
	/**
	 * @see IContentDescription
	 */
	public Object getProperty(QualifiedName key) {
		if (properties == null)
			return null;
		return properties.get(key);
	}
	public void markAsImmutable() {
		assertMutable();
		immutable = true;
	}
	/**
	 * @see IContentDescription
	 */
	public void setCharset(String charset) {
		assertMutable();
		this.charset = charset;
	}
	public void setContentType(IContentType contentType) {
		assertMutable();
		this.contentType = contentType;
	}
	/**
	 * @see IContentDescription
	 */
	public void setMark(int mark) {
		assertMutable();
		this.mark = mark;
	}
	/**
	 * @see IContentDescription
	 */
	public void setProperty(QualifiedName key, Object value) {
		assertMutable();
		if (properties == null)
			properties = new HashMap();
		properties.put(key, value);
	}
}
