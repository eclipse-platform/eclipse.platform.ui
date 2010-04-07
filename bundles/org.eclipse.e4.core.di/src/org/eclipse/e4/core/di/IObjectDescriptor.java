/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IObjectDescriptor {

	// TBD rename getDesiredClass()
	// TBD is this needed if we can get a Type?
	public Class<?> getElementClass();

	public Type getElementType();

	public boolean isOptional();

	public boolean hasQualifier(Class<? extends Annotation> clazz);

	public Annotation[] getQualifiers();

	public Object getQualifier(Class<? extends Annotation> clazz);
	
}
