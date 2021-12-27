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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.bind.Bind;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * Tests the value binding in the fluent databinding API in the {@link Bind}
 * class.
 */
public class BindValueTest extends AbstractDefaultRealmTestCase {

	@Test
	public void oneWayBindingsCreated() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();
		var context = new DataBindingContext();

		Binding binding = Bind.oneWay().modelToTarget().from(target).to(model).bind(context);

		assertTrue(context.getBindings().contains(binding));
		assertSame(target, binding.getModel());
		assertSame(model, binding.getTarget());
	}

	@Test
	public void twoWayBindingsCreated() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();
		var context = new DataBindingContext();

		Binding binding = Bind.twoWay().modelToTarget().from(model).to(target).bind(context);

		assertTrue(context.getBindings().contains(binding));
		assertSame(target, binding.getTarget());
		assertSame(model, binding.getModel());
	}

	@Test
	public void defaultDirectionIsTargetToModel() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		Binding binding = Bind.oneWay().from(target).to(model).bindWithNewContext();
		assertSame(target, binding.getTarget());
		assertSame(model, binding.getModel());
	}

	@Test
	public void oneWayUpdate() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		Bind.oneWay().from(target).to(model).bindWithNewContext();

		target.setValue("test1");
		assertEquals("test1", model.getValue());

		model.setValue("test2");
		assertEquals("test1", target.getValue());
	}

	@Test
	public void twoWayUpdate() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		Bind.twoWay().from(model).to(target).bindWithNewContext();

		target.setValue("test1");
		assertEquals("test1", model.getValue());

		model.setValue("test2");
		assertEquals("test2", target.getValue());
	}

	@Test
	public void validators() {
		// Setup
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		var interactions = new ArrayList<String>();

		Bind.twoWay() //
				.from(model) //
				.validateAfterGet(e -> {
					interactions.add(e + "1");
					return Status.OK_STATUS;
				}).validateAfterConvert(e -> {
					interactions.add(e + "2");
					return Status.OK_STATUS;
				}).validateBeforeSet(e -> {
					interactions.add(e + "3");
					return Status.OK_STATUS;
				}).to(target) //
				.validateAfterGet(e -> {
					interactions.add(e + "4");
					return Status.OK_STATUS;
				}).validateAfterConvert(e -> {
					interactions.add(e + "5");
					return Status.OK_STATUS;
				}).validateBeforeSet(e -> {
					interactions.add(e + "6");
					return Status.OK_STATUS;
				}).bindWithNewContext();

		// Clear list to avoid validation during initialisation
		interactions.clear();

		// Action
		model.setValue("model");

		// Verification
		assertEquals("model", target.getValue());
		assertEquals(List.of("model1", "model5", "model6"), interactions);

		// Setup
		interactions.clear();

		// Action
		target.setValue("target");

		// Verification
		assertEquals("target", target.getValue());
		System.out.println(interactions);
		assertEquals(List.of("target4", "target2", "target3"), interactions);
	}

	@Test
	public void twoWayValidator() {
		// Setup
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		var interactions = new ArrayList<String>();

		Bind.twoWay() //
				.from(target) //
				.validateTwoWay(e -> {
					interactions.add(e + "-from-end");
					return Status.OK_STATUS;
				})
				.to(model) //
				.validateTwoWay(e -> {
					interactions.add(e + "-to-end");
					return Status.OK_STATUS;
				}).bindWithNewContext();

		interactions.clear();

		// Action
		target.setValue("test1");

		// Verification
		assertEquals("test1", model.getValue());
		assertEquals(List.of("test1-from-end", "test1-to-end"), interactions);
		interactions.clear();

		// Action
		model.setValue("test2");

		// Verification
		assertEquals("test2", target.getValue());
		assertEquals(List.of("test2-to-end", "test2-from-end"), interactions);
	}

	@Test
	public void setValueTwice() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		try {
			Bind.twoWay().from(model).to(target).convertOnly().convertOnly();
			fail();
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void setNull() {
		try {
			Bind.twoWay().from((IObservableValue<Object>) null);
			fail();
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void oneWayConverter() {
		var target = new WritableValue<String>();
		var model = new WritableValue<Integer>();

		Bind.oneWay().from(target).convertTo(IConverter.create(Integer::parseInt)).to(model).bindWithNewContext();

		target.setValue("1");
		assertEquals(1, (int) model.getValue());

		model.setValue(2);
		assertEquals("1", target.getValue());
	}

	@Test
	public void twoWayConverter() {
		var target = new WritableValue<String>();
		var model = new WritableValue<Integer>();

		Bind.twoWay() //
				.from(target) //
				.convertTo(IConverter.create(Integer::parseInt)) //
				.convertFrom(IConverter.create(i -> i.toString())) //
				.to(model) //
				.bindWithNewContext();

		target.setValue("1");
		assertEquals(1, (int) model.getValue());

		model.setValue(2);
		assertEquals("2", target.getValue());
	}

	@Test
	public void twoWayDefaultConverter() {
		var target = new WritableValue<String>(null, String.class);
		var model = new WritableValue<Integer>(null, Integer.class);

		Bind.twoWay().from(target).defaultConvert().to(model).bindWithNewContext();

		target.setValue("1");
		assertEquals(1, (int) model.getValue());

		model.setValue(2);
		assertEquals("2", target.getValue());
	}

	@Test
	public void oneWayDefaultConverter() {
		var target = new WritableValue<String>(null, String.class);
		var model = new WritableValue<Integer>(null, Integer.class);

		Bind.oneWay().from(target).defaultConvert().to(model).bindWithNewContext();

		target.setValue("1");
		assertEquals(1, (int) model.getValue());

		model.setValue(2);
		assertEquals("1", target.getValue());
	}

	@Test
	public void updateOnlyOnRequest() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();
		var context = new DataBindingContext();

		Bind.twoWay().from(target).to(model).updateOnlyOnRequest().bind(context);

		target.setValue("test");
		assertEquals(null, model.getValue());

		context.updateModels();

		assertEquals("test", model.getValue());
	}

	@Test
	public void convertOnly() {
		var target = new WritableValue<String>();
		var model = new WritableValue<String>();

		var interactions = new ArrayList<String>();

		Bind.twoWay() //
				.from(target) //
				.convertTo(IConverter.create(e -> {
					interactions.add(e + "-to");
					return e + "-to";
				})) //
				.convertFrom(IConverter.create(e -> {
					interactions.add(e + "-from");
					return e + "-from";
				})) //
				.to(model) //
				.convertOnly() //
				.bindWithNewContext();

		interactions.clear();

		target.setValue("test1");
		assertEquals(null, model.getValue());

		model.setValue("test2");
		assertEquals("test2-from", target.getValue());

		assertEquals(List.of("test1-to", "test2-from"), interactions);
	}

}


