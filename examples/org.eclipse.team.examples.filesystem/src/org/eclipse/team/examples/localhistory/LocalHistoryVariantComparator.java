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
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

public class LocalHistoryVariantComparator implements IResourceVariantComparator {
	public boolean compare(IResource local, IResourceVariant remote) {
		return false;
	}

	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		return false;
	}

	public boolean isThreeWay() {
		return false;
	}
}
