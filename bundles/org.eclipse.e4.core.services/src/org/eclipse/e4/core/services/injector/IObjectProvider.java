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
package org.eclipse.e4.core.services.injector;

/**
 * This interface describes an "object provider" - something that knows how to instantiate objects
 * corresponding to the key. NOTE: This is a preliminary form; this API will change.
 */
public interface IObjectProvider {

	public boolean containsKey(IObjectDescriptor key);

	public Object get(IObjectDescriptor key);

	public void setInjector(Injector injector);

	public String getKey(IObjectDescriptor key);

}
