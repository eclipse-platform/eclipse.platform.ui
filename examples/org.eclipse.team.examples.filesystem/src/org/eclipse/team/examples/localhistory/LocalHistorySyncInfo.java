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
package org.eclipse.team.examples.localhistory;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

public class LocalHistorySyncInfo extends SyncInfo {
	
	public LocalHistorySyncInfo(IResource local, IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local, null, remote, comparator);
	}

	protected int calculateKind() throws TeamException {
		if (getRemote() == null)
			return IN_SYNC;
		else
			return super.calculateKind();
	}
}