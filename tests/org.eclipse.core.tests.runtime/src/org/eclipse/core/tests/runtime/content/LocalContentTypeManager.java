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
package org.eclipse.core.tests.runtime.content;

import org.eclipse.core.internal.content.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class LocalContentTypeManager extends ContentTypeManager {
	public LocalContentTypeManager() {
		// to increase visibility
	}

	public void addContentType(IContentType contentType) {
		super.addContentType(contentType);
		reorganize();
	}

	public IContentType createContentType(String namespace, String simpleId, String name, String[] fileExtensions, String[] fileNames, String baseTypeId, String defaultCharset) {
		return ContentType.createContentType(this, namespace, simpleId, name, (byte) 0, fileExtensions, fileNames, baseTypeId, defaultCharset, null);
	}

	protected ContentTypeBuilder createBuilder() {
		return new LocalContentTypeBuilder(this);
	}

	// to increase visibility
	public void startup() {
		super.startup();
	}

	public static IContentTypeManager getLocalContentTypeManager() {
		LocalContentTypeManager contentTypeManager = new LocalContentTypeManager();
		contentTypeManager.startup();
		return contentTypeManager;
	}
}