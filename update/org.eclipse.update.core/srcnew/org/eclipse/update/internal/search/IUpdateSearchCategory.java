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
package org.eclipse.update.internal.search;


public interface IUpdateSearchCategory {
/**
 * Returns the unique identifier of this search category.
 */
	public String getId();
/**
 * Accepts the identifier assigned to this category during
 * the registry reading.
 */
	public void setId(String id);
	
	IUpdateSearchQuery [] getQueries();
}