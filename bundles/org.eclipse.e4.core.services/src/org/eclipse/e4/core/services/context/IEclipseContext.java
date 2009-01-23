/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.services.context;

import org.eclipse.e4.core.services.context.spi.IComputedValue;

public interface IEclipseContext {

	public Object get(String name);
	
	public void set(String name, Object value);
	
	public void unset(String name);
	
	// TBD better to call this something other then "set" to avoid confusion between two set() methods
	// TBD "setCalculated"?
	public void set(String name, IComputedValue computedValue);

	public boolean isSet(String name);
	
	// TBD should this be a part of IEclipseContext or a separate convenience method?
	public void runAndTrack(final Runnable runnable, String name);
	
	// TBD add newChild()
	// TBD add dispose() ?
}
