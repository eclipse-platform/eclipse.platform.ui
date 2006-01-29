/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.IUpdatableCellProvider;
import org.eclipse.jface.internal.databinding.beans.JavaBeansReadableSet;
import org.eclipse.jface.internal.databinding.beans.JavaBeansUpdatableCellProvider;

/**
 * @since 3.2
 * 
 */
public class JavaBeans {

	public JavaBeans() {
	}

	public IReadableSet createReadableSet(Object bean,
			PropertyDescriptor propertyDescriptor, Class elementType) {
		return new JavaBeansReadableSet(bean, propertyDescriptor, elementType);
	}

	/**
	 * @param accountSet
	 * @param strings
	 * @return
	 */
	public IUpdatableCellProvider createUpdatableCellProvider(IReadableSet readableSet, String[] propertyNames) {
		return new JavaBeansUpdatableCellProvider(readableSet, propertyNames);
	}

}
