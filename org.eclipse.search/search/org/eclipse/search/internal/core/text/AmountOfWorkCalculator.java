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
package org.eclipse.search.internal.core.text;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.search.internal.core.ISearchScope;

/**
 * The visitor that does the actual work.
 */
public class AmountOfWorkCalculator extends TypedResourceVisitor {
	private ISearchScope fScope;
	private int fResult;
	private boolean fVisitDerived;

	AmountOfWorkCalculator(MultiStatus status, boolean visitDerived) {
		super(status);
		fVisitDerived= visitDerived;
	}
		
	protected boolean visitFile(IResourceProxy proxy) throws CoreException {
		if (shouldVisit(proxy))
			fResult++;
		return true;	
	}
	
	
	private boolean shouldVisit(IResourceProxy proxy) {
		if (!fScope.encloses(proxy))
			return false;
		if (fVisitDerived)
			return true;
		return !proxy.isDerived();
	}

	public int process(Collection projects, ISearchScope scope) {
		fResult= 0;
		fScope= scope;
		
		Iterator i= projects.iterator();
		while(i.hasNext()) {
			IProject project= (IProject)i.next();
			int save= fResult;
			try {
				project.accept(this, IResource.NONE);
			} catch (CoreException ex) {
				addToStatus(ex);
			}
			// Project doesn't contain any files that are in scope
			if (save == fResult)
				i.remove();	
		}
		
		return fResult;
	}
}
