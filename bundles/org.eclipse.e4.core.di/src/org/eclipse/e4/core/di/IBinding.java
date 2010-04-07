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

// TBD should this be an abstract base class?
// TBD support for multiple qualifiers
/**
 * Describes binding between object description and its implementation to be used by the dependency
 * injection.
 * 
 */
public interface IBinding {

	public IBinding named(String name);
	
	// TBD add qualified(Qualifier qualifier);

	public IBinding implementedBy(Class<?> clazz);

	public Class<?> getDescribedClass();

	public String getQualifierName();

	public Class<?> getImplementationClass();

}
