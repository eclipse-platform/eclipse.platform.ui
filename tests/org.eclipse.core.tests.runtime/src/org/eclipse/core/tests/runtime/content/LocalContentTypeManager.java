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
package org.eclipse.core.tests.runtime.content;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.internal.content.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * A content type manager that only sees content types contributed by 
 * org.eclipse.core.runtime and this plug-in.
 * 
 *  @see org.eclipse.core.tests.runtime.content.LocalContentTypeBuilder#getConfigurationElements()
 */
public class LocalContentTypeManager extends ContentTypeManager {
	public static IContentTypeManager getLocalContentTypeManager() {
		return new LocalContentTypeManager();
	}

	public IContentType findContentTypeFor(InputStream contents, IContentType[] subset) throws IOException {
		IContentType[] result = findContentTypesFor(contents, subset);
		return result.length > 0 ? result[0] : null;
	}

	public IContentType[] findContentTypesFor(InputStream contents, IContentType[] subset) throws IOException {
		return getCatalog().findContentTypesFor(contents, subset);
	}

	protected ContentTypeBuilder createBuilder(ContentTypeCatalog newCatalog) {
		return new LocalContentTypeBuilder(newCatalog);
	}

}
