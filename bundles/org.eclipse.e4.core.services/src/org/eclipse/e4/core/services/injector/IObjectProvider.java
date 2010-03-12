/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.injector;

import org.eclipse.e4.core.services.context.IRunAndTrack;

/**
 * This interface describes an "object provider" - something that knows how to instantiate objects
 * corresponding to the key. NOTE: This is a preliminary form; this API will change.
 */
public interface IObjectProvider {

	public boolean containsKey(IObjectDescriptor key);

	public Object get(IObjectDescriptor key);

	// TBD arguments: do they make sense? May be just pass an Object?
	public IObjectDescriptor makeDescriptor(String description, Class clazz);

	// TBD replace this with events specific to injection, not context
	public void runAndTrack(final IRunAndTrack runnable, Object[] args);
}
