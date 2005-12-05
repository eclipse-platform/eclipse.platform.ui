/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;

import org.eclipse.search.core.text.TextSearchScope;

/**
 * The visitor that does the actual work.
 */
public class AmountOfWorkCalculator implements IResourceProxyVisitor {
	
	private final TextSearchScope fScope;
	
	private int fFileCount;

	public AmountOfWorkCalculator(TextSearchScope scope) {
		fScope= scope;
	}
		
	public boolean visit(IResourceProxy proxy) {
		boolean inScope= fScope.contains(proxy);
		
		if (inScope && proxy.getType() == IResource.FILE) {
			fFileCount++;
		}
		return inScope;
	}
	
	public int process() {
		fFileCount= 0;
		IResource[] roots= fScope.getRoots();
		for (int i= 0; i < roots.length; i++) {
			try {
				roots[i].accept(this, 0);
			} catch (CoreException ex) {
				// ignore
			}
		}
		return fFileCount;
	}
}
