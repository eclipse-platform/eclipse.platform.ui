/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920, 159768
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
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
		Binding binding = dbc.bindList(target, model,
				null,
				null);
		assertEquals(target, binding.getTarget());
	}
	
	public void testGetModel() throws Exception {
		Binding binding = dbc.bindList(target, model,
				null,
				null);
		assertEquals(model, binding.getModel());
	}
}
