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
package org.eclipse.e4.core.services.context.spi;

import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 * The base class for all context implementations. Clients may subclass this class.
 */
public class AbstractContext implements IEclipseContext {

	public Object get(String name) {
		return null;
	}

	public Object get(String name, Object[] arguments) {
		return get(name, null);
	}

	public Object getLocal(String name) {
		return null;
	}

	public boolean containsKey(String name) {
		return get(name) == null;
	}

	public void runAndTrack(Runnable runnable, String name) {
	}
	public void runAndTrack(IRunAndTrack runnable, Object[] args) {
	}

	public void set(String name, Object value) {
	}

	public void remove(String name) {
	}

}
