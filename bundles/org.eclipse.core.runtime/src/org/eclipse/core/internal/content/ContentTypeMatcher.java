/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.io.*;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.*;

/**
 * @since 3.1
 */
class ContentTypeMatcher implements IContentTypeMatcher {

	private IContentTypeManager.ISelectionPolicy policy;

	public ContentTypeMatcher(IContentTypeManager.ISelectionPolicy policy) {
		this.policy = policy;
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType findContentTypeFor(InputStream contents, String fileName) throws IOException {
		IContentType[] all = findContentTypesFor(contents, fileName);
		return all.length > 0 ? all[0] : null;
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType findContentTypeFor(String fileName) {
		// basic implementation just gets all content types
		IContentType[] associated = findContentTypesFor(fileName);
		return associated.length == 0 ? null : associated[0];
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		return getCatalog().findContentTypesFor(contents, fileName, policy);
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType[] findContentTypesFor(String fileName) {
		return getCatalog().findContentTypesFor(fileName, policy);
	}

	private ContentTypeCatalog getCatalog() {
		return ContentTypeManager.getInstance().getCatalog();
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentDescription getDescriptionFor(InputStream contents, String fileName, QualifiedName[] options) throws IOException {
		return getCatalog().getDescriptionFor(contents, fileName, options, policy);
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentDescription getDescriptionFor(Reader contents, String fileName, QualifiedName[] options) throws IOException {
		return getCatalog().getDescriptionFor(contents, fileName, options, policy);
	}

}
