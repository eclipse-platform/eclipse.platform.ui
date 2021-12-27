/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.databinding.bind;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.bind.Bind;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * Tests the set binding in the fluent databinding API in the {@link Bind}
 * class.
 */
public class BindSetTest extends AbstractDefaultRealmTestCase {

	@Test
	public void oneWayBindingsCreated() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();
		var context = new DataBindingContext();

		Binding binding = Bind.oneWay().modelToTarget().from(target).to(model).bind(context);

		assertTrue(context.getBindings().contains(binding));
		assertSame(target, binding.getModel());
		assertSame(model, binding.getTarget());
	}

	@Test
	public void twoWayBindingsCreated() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();
		var context = new DataBindingContext();

		Binding binding = Bind.twoWay().modelToTarget().from(model).to(target).bind(context);

		assertTrue(context.getBindings().contains(binding));
		assertSame(target, binding.getTarget());
		assertSame(model, binding.getModel());
	}

	@Test
	public void defaultDirectionIsTargetToModel() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();

		Binding binding = Bind.oneWay().from(target).to(model).bindWithNewContext();
		assertSame(target, binding.getTarget());
		assertSame(model, binding.getModel());
	}

	@Test
	public void oneWayUpdate() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();

		Bind.oneWay().from(target).to(model).bindWithNewContext();

		target.add("test1");
		assertTrue(model.contains("test1"));

		model.add("test2");
		assertFalse(target.contains("test2"));
	}

	@Test
	public void twoWayUpdate() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();

		Bind.twoWay().from(target).to(model).bindWithNewContext();

		target.add("test1");
		assertTrue(model.contains("test1"));

		model.add("test2");
		assertTrue(target.contains("test2"));
	}

	@Test
	public void setSetTwice() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();

		try {
			Bind.twoWay().from(model).to(target).updateOnlyOnRequest().updateOnlyOnRequest();
			fail();
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void setNull() {
		try {
			Bind.twoWay().from((IObservableSet<Object>) null);
			fail();
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void oneWayConverter() {
		var target = new WritableSet<String>();
		var model = new WritableSet<Integer>();

		Bind.oneWay().from(target).convertTo(IConverter.create(Integer::parseInt)).to(model).bindWithNewContext();

		target.add("1");
		assertTrue(model.contains(1));
	}

	@Test
	public void twoWayConverter() {
		var target = new WritableSet<String>();
		var model = new WritableSet<Integer>();

		Bind.twoWay() //
				.from(target) //
				.convertTo(IConverter.create(Integer::parseInt)) //
				.convertFrom(IConverter.create(i -> i.toString())) //
				.to(model) //
				.bindWithNewContext();

		target.add("1");
		assertTrue(model.contains(1));

		model.add(2);
		assertTrue(target.contains("2"));
	}

	@Test
	public void twoWayDefaultConverter() {
		var target = WritableSet.withElementType(String.class);
		var model = WritableSet.withElementType(Integer.class);

		Bind.twoWay().from(target).defaultConvert().to(model).bindWithNewContext();

		target.add("1");
		assertTrue(model.contains(1));

		model.add(2);
		assertTrue(target.contains("2"));
	}

	@Test
	public void oneWayDefaultConverter() {
		var target = WritableSet.withElementType(String.class);
		var model = WritableSet.withElementType(Integer.class);

		Bind.oneWay().from(target).defaultConvert().to(model).bindWithNewContext();

		target.add("1");
		assertTrue(model.contains(1));

		model.add(2);
		assertFalse(target.contains("2"));
	}

	@Test
	public void updateOnlyOnRequest() {
		var target = new WritableSet<String>();
		var model = new WritableSet<String>();
		var context = new DataBindingContext();

		Bind.twoWay().from(target).to(model).updateOnlyOnRequest().bind(context);

		target.add("test");
		assertFalse(model.contains("test"));

		context.updateModels();

		assertTrue(model.contains("test"));
	}
}


