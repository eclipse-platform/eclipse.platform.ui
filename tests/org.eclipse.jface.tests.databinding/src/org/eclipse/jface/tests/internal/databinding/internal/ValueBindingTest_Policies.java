/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
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

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackLastListener;
import org.eclipse.swt.widgets.Display;

/**
 * Asserts the policies of ValueBinding.
 * 
 * @since 3.2
 */
public class ValueBindingTest_Policies extends TestCase {
	private WritableValue target;
	private WritableValue model;
	private DataBindingContext dbc;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));

		target = new WritableValue(String.class);
		model = new WritableValue(String.class);
		dbc = new DataBindingContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
	}

	public void testUpdateModelPolicyNull() throws Exception {
		new ValueBinding(dbc, target, model, new BindSpec()
				.setModelUpdatePolicy(null));
		target.setValue("1");
		assertEquals("should be automatic", target.getValue(), model.getValue());
	}

	public void testUpdateModelPolicyAutomatic() throws Exception {
		new ValueBinding(dbc, target, model, new BindSpec()
				.setModelUpdatePolicy(BindSpec.POLICY_AUTOMATIC));
		target.setValue("1");
		assertEquals("should be automatic", target.getValue(), model.getValue());
	}

	public void testUpdateModelPolicyExplicit() throws Exception {
		ValueBinding binding = new ValueBinding(dbc, target, model,
				new BindSpec().setTargetUpdatePolicy(BindSpec.POLICY_EXPLICIT));

		model.setValue("1");
		assertFalse(model.getValue().equals(target.getValue()));

		binding.updateTargetFromModel();
		assertEquals(model.getValue(), target.getValue());
	}

	public void testUpdateTargetPolicyNull() throws Exception {
		new ValueBinding(dbc, target, model, new BindSpec()
				.setTargetUpdatePolicy(null));
		model.setValue("1");
		assertEquals("should be automatic", model.getValue(), target.getValue());
	}

	public void testUpdateTargetPolicyAutomatic() throws Exception {
		new ValueBinding(dbc, target, model, new BindSpec()
				.setTargetUpdatePolicy(BindSpec.POLICY_AUTOMATIC));
		model.setValue("1");
		assertEquals("should be automatic", model.getValue(), target.getValue());
	}

	public void testUpdateTargetPolicyDefault() throws Exception {
		TrackLastListener listener = new TrackLastListener();
		listener.lastPosition = -1;
		
		ValueBinding binding = new ValueBinding(dbc, target, model,
				new BindSpec().setModelUpdatePolicy(BindSpec.POLICY_EXPLICIT));
		binding.addBindingEventListener(listener);

		target.setValue("1");
		assertFalse(target.getValue().equals(model.getValue()));
		assertEquals("last position should be -1",
				-1, listener.lastPosition);

		binding.updateModelFromTarget();
		assertEquals(target.getValue(), model.getValue());
	}

	public void testTargetValidatePolicy() throws Exception {
		int position = BindingEvent.PIPELINE_AFTER_GET;
		TrackLastListener listener = new TrackLastListener();

		ValueBinding binding = new ValueBinding(dbc, target, model,
				new BindSpec().setModelUpdatePolicy(BindSpec.POLICY_EXPLICIT)
						.setTargetValidatePolicy(new Integer(position)));
		binding.addBindingEventListener(listener);

		String value = "1";
		target.setValue(value);

		assertFalse(target.getValue().equals(model.getValue()));
		assertEquals("last position", position, listener.lastPosition);
	}
}
