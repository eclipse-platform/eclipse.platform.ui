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

import java.util.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ISelectionPolicy;

public class SubsetSelectionPolicy implements ISelectionPolicy {
	private Set subset;

	public SubsetSelectionPolicy(IContentType[] subset) {
		this.subset = new HashSet(Arrays.asList(subset));
	}

	public IContentType[] select(IContentType[] candidates, boolean fileName, boolean content) {
		List result = new ArrayList(candidates.length);
		for (int i = 0; i < candidates.length; i++)
			if (subset.contains(candidates[i]))
				result.add(candidates[i]);
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}
}
