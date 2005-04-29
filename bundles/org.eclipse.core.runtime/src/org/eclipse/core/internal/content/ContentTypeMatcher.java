/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		ContentTypeCatalog currentCatalog = getCatalog();
		IContentType[] all = currentCatalog.findContentTypesFor(contents, fileName, policy);
		return all.length > 0 ? new ContentTypeHandler((ContentType) all[0], currentCatalog.getGeneration()) : null;
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType findContentTypeFor(String fileName) {
		// basic implementation just gets all content types
		ContentTypeCatalog currentCatalog = getCatalog();
		IContentType[] associated = currentCatalog.findContentTypesFor(fileName, policy);
		return associated.length == 0 ? null : new ContentTypeHandler((ContentType) associated[0], currentCatalog.getGeneration());
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		ContentTypeCatalog currentCatalog = getCatalog();
		IContentType[] types = currentCatalog.findContentTypesFor(contents, fileName, policy);
		IContentType[] result = new IContentType[types.length];
		int generation = currentCatalog.getGeneration();
		for (int i = 0; i < result.length; i++)
			result[i] = new ContentTypeHandler((ContentType) types[i], generation);
		return result;
	}

	/**
	 * @see IContentTypeMatcher
	 */
	public IContentType[] findContentTypesFor(String fileName) {
		ContentTypeCatalog currentCatalog = getCatalog();
		IContentType[] types = currentCatalog.findContentTypesFor(fileName, policy);
		IContentType[] result = new IContentType[types.length];
		int generation = currentCatalog.getGeneration();
		for (int i = 0; i < result.length; i++)
			result[i] = new ContentTypeHandler((ContentType) types[i], generation);
		return result;
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
