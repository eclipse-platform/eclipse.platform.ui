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

package org.eclipse.ui.internal.commands;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.commands.IHandler;

public final class SimpleHandlerService extends AbstractHandlerService {
	
	private SortedMap handlerMap;

	public SimpleHandlerService() {
		super();
		this.handlerMap = Collections.unmodifiableSortedMap(new TreeMap());
	}

	public SortedMap getHandlerMap() {
		return handlerMap;
	}
	
	public void setHandlerMap(SortedMap handlerMap)
		throws IllegalArgumentException {
		this.handlerMap = Util.safeCopy(handlerMap, String.class, IHandler.class);    
		fireHandlerServiceChanged(); 
    }
}
