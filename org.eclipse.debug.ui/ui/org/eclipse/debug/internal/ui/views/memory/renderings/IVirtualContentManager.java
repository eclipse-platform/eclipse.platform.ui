/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

public interface IVirtualContentManager {

	public int indexOf(Object key);
	
	public int columnOf(Object element, Object key);
	
	public Object getKey(int idx);
	
	public Object getKey(Object element);
	
	public Object getKey(int idx, int col);
	
	public void handleViewerChanged();
}
