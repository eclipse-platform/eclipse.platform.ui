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
import org.eclipse.core.internal.content.ContentTypeBuilder;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class LocalContentTypeBuilder extends ContentTypeBuilder {
	public LocalContentTypeBuilder(ContentTypeManager catalog) {
		super(catalog);
	}

	/**
	 * To make testiing easier, ensures only content types / content interpreters contributed 
	 * by runtime and runtime.tests will be added to the catalog.
	 */
	protected IConfigurationElement[] getConfigurationElements() {
		IConfigurationElement[] allContentTypeCEs = super.getConfigurationElements();
		List selected = new ArrayList();
		for (int i = 0; i < allContentTypeCEs.length; i++) {
			String namespace = allContentTypeCEs[i].getDeclaringExtension().getNamespace();
			if (namespace.equals(Platform.PI_RUNTIME) || namespace.equals(RuntimeTest.PI_RUNTIME_TESTS))
				selected.add(allContentTypeCEs[i]);
		}
		return (IConfigurationElement[]) selected.toArray(new IConfigurationElement[selected.size()]);
	}
}