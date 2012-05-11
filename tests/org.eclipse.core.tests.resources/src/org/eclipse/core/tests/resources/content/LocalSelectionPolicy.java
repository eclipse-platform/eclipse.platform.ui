/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ISelectionPolicy;

/**
 * Selection policy that filters out any content types that do not
 * belong to the runtime plug-in or this test plug-in. 
 */
public class LocalSelectionPolicy implements ISelectionPolicy {
	public IContentType[] select(IContentType[] candidates, boolean fileName, boolean content) {
		List result = new ArrayList(candidates.length);
		for (int i = 0; i < candidates.length; i++) {
			String namespace = getNamespace(candidates[i].getId());
			if (namespace.equals(ContentTypeTest.PI_RESOURCES_TESTS) || namespace.equals(Platform.PI_RUNTIME))
				result.add(candidates[i]);
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}

	private static String getNamespace(String id) {
		int lastDot = id.lastIndexOf('.');
		if (lastDot <= 1)
			throw new IllegalArgumentException("lastDot ==" + lastDot);
		return id.substring(0, lastDot);
	}
}
