/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.databinding.observable.value.AbstractObservableValue;
import org.eclipse.jface.internal.databinding.internal.ValueBinding;

/**
 * @since 3.2
 * 
 */
public class ValueBindingTest extends TestCase {
	/**
	 * Bug 152543.
	 * 
	 * @throws Exception
	 */
	public void testNoUpdateTargetFromModel() throws Exception {
		BindSpec spec = new BindSpec();
		spec.setUpdateModel(false);
		spec.setUpdateTarget(false);

		try {
			new ValueBinding(new DataBindingContext(),
					new ObservableValueStub(), new ObservableValueStub(), spec);
		} catch (Exception e) {
			fail();
		}
	}

	private static class ObservableValueStub extends AbstractObservableValue {
		protected Object doGetValue() {
			// do nothing
			return null;
		}

		public Object getValueType() {
			// do nothing
			return null;
		}
	}
}
