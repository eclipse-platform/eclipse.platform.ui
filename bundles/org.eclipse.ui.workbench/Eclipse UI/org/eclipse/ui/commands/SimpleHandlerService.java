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

package org.eclipse.ui.commands;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.internal.commands.util.Util;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class SimpleHandlerService extends AbstractHandlerService {
	
	private SortedMap handlerMap;

	/**
	 * TODO javadoc
	 */
	public SimpleHandlerService() {
		super();
		this.handlerMap = Collections.unmodifiableSortedMap(new TreeMap());
	}

	/**
	 * Returns the mapping of command ids to IHandler instances.
	 * 
	 * @return the mapping of command ids to IHandler instances.
	 */
	public SortedMap getHandlerMap() {
		return handlerMap;
	}

	/**
	 * Sets the mapping of command ids to IHandler instances.
	 *
	 * @param ids the mapping of command ids to IHandler instances.
	 */		
	public void setHandlerMap(SortedMap handlerMap)
		throws IllegalArgumentException {
		this.handlerMap = Util.safeCopy(handlerMap, String.class, IHandler.class);    
		fireHandlerServiceChanged(); 
    }
}
