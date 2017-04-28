/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import java.util.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ISelectionPolicy;

public class SubsetSelectionPolicy implements ISelectionPolicy {
	private Set<IContentType> subset;

	public SubsetSelectionPolicy(IContentType[] subset) {
		this.subset = new HashSet<>(Arrays.asList(subset));
	}

	@Override
	public IContentType[] select(IContentType[] candidates, boolean fileName, boolean content) {
		List<IContentType> result = new ArrayList<>(candidates.length);
		for (IContentType candidate : candidates) {
			if (subset.contains(candidate)) {
				result.add(candidate);
			}
		}
		return result.toArray(new IContentType[result.size()]);
	}
}
