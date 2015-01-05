/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920, 159768
 *     Matthew Hall - bug 260329
 *******************************************************************************/

package org.eclipse.core.tests.databinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class ListBindingTest extends AbstractDefaultRealmTestCase {
	private IObservableList target;
	private IObservableList model;
	private DataBindingContext dbc;

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		target = new WritableList(new ArrayList(), String.class);
		model = new WritableList(new ArrayList(), String.class);
		dbc = new DataBindingContext();
	}

	public void testUpdateModelFromTarget() throws Exception {
		Binding binding = dbc.bindList(target, model,
				new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST),
				new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST));

		target.add("1");
		List targetCopy = new ArrayList(target.size());
		targetCopy.addAll(target);

		model.add("2");

		assertFalse("target should not equal model", target.equals(model));
		binding.updateTargetToModel();
		assertEquals("target should not have changed", targetCopy, target);
		assertEquals("target != model", target, model);
	}

	public void testUpdateTargetFromModel() throws Exception {
		Binding binding = dbc.bindList(target, model,
				new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST),
				new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST));

		target.add("1");
		model.add("2");

		List modelCopy = new ArrayList(model.size());
		modelCopy.addAll(model);

		assertFalse("model should not equal target", model.equals(target));
		binding.updateModelToTarget();

		assertEquals("model should not have changed", modelCopy, model);
		assertEquals("model != target", model, target);
	}

	public void testGetTarget() throws Exception {
		Binding binding = dbc.bindList(target, model);
		assertEquals(target, binding.getTarget());
	}

	public void testGetModel() throws Exception {
		Binding binding = dbc.bindList(target, model);
		assertEquals(model, binding.getModel());
	}

	public void testStatusIsInstanceOfBindingStatus() throws Exception {
		Binding binding = dbc.bindList(target, model);
		assertTrue(binding.getValidationStatus().getValue() instanceof BindingStatus);
	}

	public void testAddValidationStatusContainsMultipleStatuses() throws Exception {
		UpdateListStrategy strategy = new UpdateListStrategy() {
			@Override
			protected IStatus doAdd(IObservableList observableList,
					Object element, int index) {
				super.doAdd(observableList, element, index);

				switch (index) {
				case 0:
					return ValidationStatus.error("");
				case 1:
					return ValidationStatus.info("");
				}

				return null;
			}
		};

		Binding binding = dbc.bindList(target, model, strategy, null);
		target.addAll(Arrays.asList(new String[] {"1", "2"}));

		IStatus status = (IStatus) binding.getValidationStatus().getValue();
		assertEquals("maximum status", IStatus.ERROR, status.getSeverity());
		assertTrue("multi status", status.isMultiStatus());

		IStatus[] children = status.getChildren();
		assertEquals("multi status children", 2, children.length);
		assertEquals("first status severity", IStatus.ERROR, children[0].getSeverity());
		assertEquals("second status severity", IStatus.INFO, children[1].getSeverity());
	}

	public void testRemoveValidationStatusContainsMultipleStatuses() throws Exception {
		List items = Arrays.asList(new String[] {"1", "2"});
		model.addAll(items);

		UpdateListStrategy strategy = new UpdateListStrategy() {
			int count;
			/* (non-Javadoc)
			 * @see org.eclipse.core.databinding.UpdateListStrategy#doRemove(org.eclipse.core.databinding.observable.list.IObservableList, int)
			 */
			@Override
			protected IStatus doRemove(IObservableList observableList, int index) {
				super.doRemove(observableList, index);

				switch (count++) {
				case 0:
					return ValidationStatus.error("");
				case 1:
					return ValidationStatus.info("");
				}

				return null;
			}
		};

		Binding binding = dbc.bindList(target, model, strategy, null);
		target.removeAll(items);

		IStatus status = (IStatus) binding.getValidationStatus().getValue();
		assertEquals("maximum status", IStatus.ERROR, status.getSeverity());
		assertTrue("multi status", status.isMultiStatus());

		IStatus[] children = status.getChildren();
		assertEquals("multi status children", 2, children.length);
		assertEquals("first status severity", IStatus.ERROR, children[0].getSeverity());
		assertEquals("second status severity", IStatus.INFO, children[1].getSeverity());
	}

	public void testAddOKValidationStatus() throws Exception {
		Binding binding = dbc.bindList(target, model);
		target.add("1");

		IStatus status = (IStatus) binding.getValidationStatus().getValue();
		assertTrue(status.isOK());
		assertEquals(0, status.getChildren().length);
	}

	public void testRemoveOKValidationStatus() throws Exception {
		model.add("1");
		Binding binding = dbc.bindList(target, model);

		target.remove("1");
		IStatus status = (IStatus) binding.getValidationStatus().getValue();
		assertTrue(status.isOK());
		assertEquals(0, status.getChildren().length);
	}
}
