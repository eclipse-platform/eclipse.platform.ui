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
package org.eclipse.update.search;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;


/**
 * This class can be added to the update search request
 * to filter out features that are not part of the specified set.
 * 
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see UpdateSearchRequest
 * @see IUpdateSearchFilter
 * @since 3.0
 */
public class VersionedIdentifiersFilter extends BaseFilter {
	private ArrayList vids;
	
	public VersionedIdentifiersFilter() {
		this(new VersionedIdentifier[0]);
	}
	
	public VersionedIdentifiersFilter(VersionedIdentifier[] vids) {
		this.vids = new ArrayList(vids.length);
		for (int i=0; i<vids.length; i++)
			this.vids.add(vids[i]);
	}
	
	public void add(VersionedIdentifier vid) {
		vids.add(vid);
	}
	
	public boolean accept(IFeatureReference match) {
		try {
			for (int i=0; i<vids.size(); i++) {
				VersionedIdentifier vid = (VersionedIdentifier)vids.get(i);
				// installed version is the same as the match - accept
				if (vid.equals(match.getVersionedIdentifier()))
					return true;
			}
			return false;
		} catch (CoreException e) {
			return false;
		}
	}
}
