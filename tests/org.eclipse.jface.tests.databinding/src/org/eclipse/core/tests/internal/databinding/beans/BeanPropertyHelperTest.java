/*******************************************************************************
 * Copyright (c) 2009, 2014 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 256150)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 445446
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;

/**
 * @since 3.2
 *
 */
public class BeanPropertyHelperTest extends TestCase {
	public void testGetPropertyDescriptor_ClassProperty()
			throws SecurityException, NoSuchMethodException {
		PropertyDescriptor pd = BeanPropertyHelper.getPropertyDescriptor(
				Bean.class, "value");
		assertEquals(Bean.class.getMethod("getValue"), pd.getReadMethod());
		assertEquals(
				Bean.class.getMethod("setValue", new Class[] { String.class }),
				pd.getWriteMethod());
	}

	public void testGetPropertyDescriptor_InterfaceProperty()
			throws SecurityException, NoSuchMethodException {
		PropertyDescriptor pd = BeanPropertyHelper.getPropertyDescriptor(
				IBean.class, "value");
		assertEquals(IBean.class.getMethod("getValue"), pd.getReadMethod());
		assertEquals(
				IBean.class.getMethod("setValue", new Class[] { String.class }),
				pd.getWriteMethod());
	}

	public void testGetPropertyDescriptor_SuperInterfaceProperty()
			throws SecurityException, NoSuchMethodException {
		PropertyDescriptor pd = BeanPropertyHelper.getPropertyDescriptor(
				IBeanExtension.class, "value");
		assertEquals(IBean.class.getMethod("getValue"), pd.getReadMethod());
		assertEquals(
				IBean.class.getMethod("setValue", new Class[] { String.class }),
				pd.getWriteMethod());
	}

}
