/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164653, 159768
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class ValueBindingTest extends AbstractDefaultRealmTestCase {
	/**
	 * Bug 152543.
	 * 
	 * @throws Exception
	 */
	public void testNoUpdateTargetFromModel() throws Exception {
		try {
			new DataBindingContext().bindValue(new ObservableValueStub(),
					new ObservableValueStub(), new UpdateValueStrategy(
							UpdateValueStrategy.POLICY_NEVER),
					new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testValuePropagation() throws Exception {
		String initialValue = "value";

		WritableValue target = new WritableValue(null, String.class);
		WritableValue model = new WritableValue(initialValue, String.class);

		DataBindingContext dbc = new DataBindingContext();

		assertFalse(model.getValue().equals(target.getValue()));
		dbc.bindValue(target, model, null, null);

		assertEquals(target.getValue(), model.getValue());
	}

	public void testGetTarget() throws Exception {
		WritableValue target = new WritableValue();
		WritableValue model = new WritableValue();
		Binding valueBinding = new DataBindingContext().bindValue(target,
				model, null, null);

		assertEquals(target, valueBinding.getTarget());
	}

	public void testGetModel() throws Exception {
		WritableValue target = new WritableValue();
		WritableValue model = new WritableValue();
		Binding valueBinding = new DataBindingContext().bindValue(target,
				model, null, null);

		assertEquals(model, valueBinding.getModel());
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

		protected void doSetValue(Object value) {

		}
	}
}
