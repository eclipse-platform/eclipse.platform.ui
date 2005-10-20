/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jan 31, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.navigator.internal.deferred;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * @author mdelder
 */
public interface DeferredAdapterProvider extends ITreeContentProvider {

	public IDeferredElementAdapter[] getDeferredAdapters(Object element);

}