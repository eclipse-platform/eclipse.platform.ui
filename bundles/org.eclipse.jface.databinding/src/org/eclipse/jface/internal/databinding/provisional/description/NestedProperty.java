/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.description;


/**
 * TODO Javadoc
 * 
 * @since 1.0
 *
 */
public class NestedProperty  extends Property {

	private Class[] types;
	private Class prototypeClass;

	/**
	 * @param object
	 * @param properties
	 * @param types
	 */
	public NestedProperty(Object object, String[] properties, Class[] types) {
		super(object, properties);
		this.types = types;
	}
	
	/**
	 * @param object
	 * @param properties
	 * @param prototypeClass
	 */
	public NestedProperty(Object object, String properties, Class prototypeClass) {
		super(object, properties);
		this.prototypeClass = prototypeClass;		
	}

	/**
	 * @return the array of types
	 */
	public Class[] getTypes() {
		return types;
	}

	/**
	 * @return the prototype class
	 */
	public Class getPrototypeClass() {
		return prototypeClass;
	}
	
	
}
