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
import org.eclipse.core.runtime.MultiStatus;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;

import org.eclipse.search.internal.core.SearchScope;

/**
 * The visitor that does the actual work.
 */
public class AmountOfWorkCalculator implements IResourceProxyVisitor {
	
	private SearchScope fScope;
	private int fFileCount;
	private boolean fVisitDerived;
	private final MultiStatus fStatus;

	AmountOfWorkCalculator(SearchScope scope, MultiStatus status, boolean visitDerived) {
		fStatus= status;
		fScope= scope;
		fVisitDerived= visitDerived;
	}
		
	public boolean visit(IResourceProxy proxy) {
		if (proxy.getType() != IResource.FILE) {
			return true;
		}
		
		if ((fVisitDerived || !proxy.isDerived()) && fScope.matchesFileName(proxy.getName())) {
			fFileCount++;
		}
		return true;	
	}
	
	public int process() {
		fFileCount= 0;
		IResource[] roots= fScope.getRootElements();
		for (int i= 0; i < roots.length; i++) {
			try {
				roots[i].accept(this, 0);
			} catch (CoreException ex) {
				fStatus.add(ex.getStatus());
			}
		}
		return fFileCount;
	}
}
