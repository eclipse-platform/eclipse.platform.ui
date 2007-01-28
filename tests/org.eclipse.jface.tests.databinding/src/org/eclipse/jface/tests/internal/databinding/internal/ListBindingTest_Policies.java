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

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.internal.databinding.ListBinding;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.internal.databinding.internal.Pipeline.TrackLastListener;
import org.eclipse.swt.widgets.Display;

/**
 * Asserts policies of ListBinding.
 * 
 * @since 3.2
 */
public class ListBindingTest_Policies extends TestCase {
	private WritableList target;
	private WritableList model;
	private DataBindingContext dbc;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));

		target = new WritableList(String.class);
		model = new WritableList(String.class);
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
		new ListBinding(target, model, new BindSpec()
				.setModelUpdatePolicy(null)).init(dbc);

		target.add("1");
		assertTrue("should be automatic", Arrays.equals(target.toArray(), model
				.toArray()));
	}

	public void testUpdateModelPolicyAutomatic() throws Exception {
		new ListBinding(target, model, new BindSpec()
				.setModelUpdatePolicy(BindSpec.POLICY_AUTOMATIC)).init(dbc);

		target.add("1");
		assertTrue("should be automatic", Arrays.equals(target.toArray(), model
				.toArray()));
	}

	public void testUpdateModelPolicyExplicit() throws Exception {
		ListBinding binding = new ListBinding( target, model,
				new BindSpec().setModelUpdatePolicy(BindSpec.POLICY_EXPLICIT));
		binding.init(dbc);

		target.add("1");

		assertFalse("should not be updated", Arrays.equals(target.toArray(),
				model.toArray()));

		binding.updateModelFromTarget();
		assertTrue("should be updated", Arrays.equals(target.toArray(), model
				.toArray()));
	}

	public void testUpdateTargetPolicyNull() throws Exception {
		new ListBinding( target, model, new BindSpec()
				.setTargetUpdatePolicy(null)).init(dbc);

		model.add("1");
		assertTrue("should be automatic", Arrays.equals(model.toArray(), target
				.toArray()));
	}

	public void testUpdateTargetPolicyAutomatic() throws Exception {
		new ListBinding(target, model, new BindSpec()
				.setTargetUpdatePolicy(BindSpec.POLICY_AUTOMATIC)).init(dbc);

		model.add("1");
		assertTrue("should be automatic", Arrays.equals(model.toArray(), target
				.toArray()));
	}

	public void testUpdateTargetPolicyExplicit() throws Exception {
		ListBinding binding = new ListBinding(target, model,
				new BindSpec().setTargetUpdatePolicy(BindSpec.POLICY_EXPLICIT));
		binding.init(dbc);

		model.add("1");

		assertFalse("should not be updated", Arrays.equals(model.toArray(),
				target.toArray()));

		binding.updateTargetFromModel();
		assertTrue("should be updated", Arrays.equals(model.toArray(), target
				.toArray()));
	}

	public void testTargetValidatePosition() throws Exception {
		TrackLastListener listener = new TrackLastListener();
		int position = BindingEvent.PIPELINE_BEFORE_CHANGE;

		final ListBinding listBinding = new ListBinding(target, model, new BindSpec()
				.setModelUpdatePolicy(BindSpec.POLICY_EXPLICIT)
				.setTargetValidatePolicy(new Integer(position)));
		listBinding.init(dbc);
		listBinding
				.addBindingEventListener(listener);

		String value = "value";
		target.add(value);

		assertFalse("target should not equal model", target.equals(model));
		assertEquals(position, listener.lastPosition);
	}

	public void testDefaultTargetValidationPosition() throws Exception {
		TrackLastListener listener = new TrackLastListener();

		final ListBinding listBinding = new ListBinding(target, model, new BindSpec()
				.setModelUpdatePolicy(BindSpec.POLICY_EXPLICIT));
		listBinding.init(dbc);
		listBinding
				.addBindingEventListener(listener);

		listener.lastPosition = -1;
		String value = "value";
		target.add(value);
		assertFalse("target should not equal model", target.equals(model));
		assertEquals(-1, listener.lastPosition);
	}
}
