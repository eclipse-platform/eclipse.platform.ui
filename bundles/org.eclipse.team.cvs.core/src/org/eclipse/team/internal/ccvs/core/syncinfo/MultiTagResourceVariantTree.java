/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.variants.ResourceVariantByteStore;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * A CVS resource variant tree that associates a different tag with each root project.
 */
public class MultiTagResourceVariantTree extends CVSResourceVariantTree {

	Map resources = new HashMap();
	
	public MultiTagResourceVariantTree(ResourceVariantByteStore cache, boolean cacheFileContentsHint) {
		super(cache, null, cacheFileContentsHint);
	}
	
	public void addResource(IResource resource, CVSTag tag) {
		Assert.isNotNull(resource);
		Assert.isNotNull(tag);
		resources.put(resource, tag);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.CVSResourceVariantTree#getTag(org.eclipse.core.resources.IResource)
	 */
	public CVSTag getTag(IResource resource) {
		return (CVSTag)resources.get(resource);
	}
}
