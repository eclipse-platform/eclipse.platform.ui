/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.dependencies;

import java.util.Set;

/** 
 * Not to be implemented by clients.
 */

public interface IDependencySystem {
	public class CyclicSystemException extends Exception {}	
	public IResolutionDelta resolve() throws CyclicSystemException;	
	public IResolutionDelta resolve(boolean produceDelta) throws CyclicSystemException;
	/**
	 * Returns the delta for the last resolution operation (<code>null</code> if never 
	 * resolved or if last resolved with delta production disabled).
	 */
	public IResolutionDelta getLastDelta();
	public void addElements(IElement[] elementsToAdd);
	public void addElement(IElement element);
	public void removeElements(IElement[] elementsToRemove);
	public void removeElement(IElement element);
	public long getElementCount();
	public Set getResolved();
	public IElement getElement(Object id, Object identifier);
	public IElementSet getElementSet(Object id);	
	// factory methods 
	public IElement createElement(Object id, Object versionId, IDependency[] dependencies, boolean singleton);
	public IDependency createDependency(Object requiredObjectId, IMatchRule satisifactionRule, Object requiredVersionId, boolean optional);
	// global access to system version comparator
	public int compare(Object obj1, Object obj2);	
}