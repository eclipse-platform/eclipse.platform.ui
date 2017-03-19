/*******************************************************************************
 * Copyright (c) 2016 Conrad Groth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Conrad Groth - initial implementation for bug 491678
 *******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.eclipse.core.databinding.UpdateSetStrategy.POLICY_NEVER;
import static org.eclipse.core.databinding.UpdateSetStrategy.POLICY_UPDATE;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.SetBinding;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Assert;

public class SetBindingTest extends AbstractDefaultRealmTestCase {
	private IObservableSet<String> target;
	private IObservableSet<String> model;
	private DataBindingContext dbc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		target = new WritableSet<>();
		model = new WritableSet<>();
		dbc = new DataBindingContext();
	}

	@Override
	public void tearDown() throws Exception {
		dbc.dispose();
		model.dispose();
		target.dispose();
	}

	public void testUpdateModelFromTarget() throws Exception {
		target.add("1");

		dbc.bindSet(target, model, new UpdateSetStrategy(UpdateSetStrategy.POLICY_NEVER), new UpdateSetStrategy());

		assertEquals("target != model", target, model);
	}

	public void testUpdateTargetFromModel() throws Exception {
		model.add("1");

		dbc.bindSet(target, model, new UpdateSetStrategy(), new UpdateSetStrategy(UpdateSetStrategy.POLICY_NEVER));

		assertEquals("model != target" + model + target, model, target);
	}

	/**
	 * test for bug 491678
	 */
	public void testAddListenerAndInitialSyncAreUninterruptable() {
		Policy.setLog(new ILogger() {
			@Override
			public void log(IStatus status) {
				if (!status.isOK()) {
					Assert.fail("The databinding logger has the not-ok status " + status);
				}
			}
		});

		model.add("first");
		new SetBinding(target, model, new UpdateSetStrategy(), new UpdateSetStrategy());
		model.remove("first");
	}

	/**
	 * test for bug 491678
	 */
	public void testTargetValueIsSyncedToModelIfModelWasNotSyncedToTarget() {
		target.add("first");
		dbc.bindSet(target, model, new UpdateSetStrategy(POLICY_UPDATE), new UpdateSetStrategy(POLICY_NEVER));
		assertEquals(model.size(), target.size());
	}

}
