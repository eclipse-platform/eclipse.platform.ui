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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class LocalContentTypeManager extends ContentTypeManager {
	public LocalContentTypeManager() {
		// to increase constructor's visibility
	}
	/**
	 * Ensures only content types / content interpreters contributed by runtime and runtime.tests
	 * will be added to the catalog.
	 */
	protected void startup() {
		IExtensionRegistry registry = InternalPlatform.getDefault().getRegistry();
		IExtensionPoint contentTypesXP = registry.getExtensionPoint(IPlatform.PI_RUNTIME, PT_CONTENTTYPES);
		IExtension[] allExtensions = contentTypesXP.getExtensions();
		List selectedElements = new ArrayList();
		for (int i = 0; i < allExtensions.length; i++) {
			String namespace = allExtensions[i].getNamespace();
			if (!namespace.equals(IPlatform.PI_RUNTIME) && !namespace.equals(RuntimeTest.PI_RUNTIME_TESTS))
				continue;
			IConfigurationElement[] contentTypes = allExtensions[i].getConfigurationElements();
			for (int j = 0; j < contentTypes.length; j++)
				registerContentType(contentTypes[j]);			
		}
		validateContentTypes();
	}
	public void addContentType(IContentType contentType) {
		super.addContentType(contentType);
		validateContentTypes();
	}
	public IContentType createContentType(String namespace, String simpleId, String name, String[] fileExtensions, String[] fileNames, String baseTypeId) {
		return super.createContentType(namespace, simpleId, name, fileExtensions, fileNames, baseTypeId);
	}
}
