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

package org.eclipse.ui.internal.handles;

import org.eclipse.ui.handles.IHandle;
import org.eclipse.ui.handles.IHandleEvent;

final class HandleEvent implements IHandleEvent {

	private IHandle handle;

	HandleEvent(IHandle handle)
		throws IllegalArgumentException {		
		super();
		
		if (handle == null)
			throw new IllegalArgumentException();
		
		this.handle = handle;
	}

	public IHandle getHandle() {
		return handle;
	}
}
