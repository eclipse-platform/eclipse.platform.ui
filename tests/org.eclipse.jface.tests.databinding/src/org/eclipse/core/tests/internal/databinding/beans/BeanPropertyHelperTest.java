/*******************************************************************************
 * Copyright (c) 2009, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 256150)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 445446
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyDescriptor;

import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class BeanPropertyHelperTest {
	@Test
	public void testGetPropertyDescriptor_ClassProperty()
			throws SecurityException, NoSuchMethodException {
		PropertyDescriptor pd = BeanPropertyHelper.getPropertyDescriptor(
				Bean.class, "value");
		assertEquals(Bean.class.getMethod("getValue"), pd.getReadMethod());
		assertEquals(Bean.class.getMethod("setValue", String.class), pd.getWriteMethod());
	}

	@Test
	public void testGetPropertyDescriptor_InterfaceProperty()
			throws SecurityException, NoSuchMethodException {
		PropertyDescriptor pd = BeanPropertyHelper.getPropertyDescriptor(
				IBean.class, "value");
		assertEquals(IBean.class.getMethod("getValue"), pd.getReadMethod());
		assertEquals(IBean.class.getMethod("setValue", String.class), pd.getWriteMethod());
	}

	@Test
	public void testGetPropertyDescriptor_SuperInterfaceProperty()
			throws SecurityException, NoSuchMethodException {
		PropertyDescriptor pd = BeanPropertyHelper.getPropertyDescriptor(
				IBeanExtension.class, "value");
		assertEquals(IBean.class.getMethod("getValue"), pd.getReadMethod());
		assertEquals(IBean.class.getMethod("setValue", String.class), pd.getWriteMethod());
	}

}
