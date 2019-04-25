/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (bug 146488)
 *     Matthew Hall - initial API and implementation (bug 146488)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 1.1
 */
public class SetOnlyJavaBeanTest extends AbstractDefaultRealmTestCase {

	@Test
	public void testValidationError() throws Exception {
		Model model = new Model();
		model.setString("abc");

		Target target = new Target();

		IObservableValue<String> modelObservable = PojoProperties.value("string", String.class).observe(model);
		IObservableValue<String> targetObservable = PojoProperties.value("string", String.class).observe(target);

		DataBindingContext context = new DataBindingContext();
		context.bindValue(targetObservable, modelObservable,
				new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER), new UpdateValueStrategy<>());

		assertEquals("abc", target.string);

		modelObservable.setValue("xyz");

		assertEquals("xyz", target.string);
	}

	public static class Model {
		private String string;

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

	public static class Target {
		private String string;

		public void setString(String value) {
			this.string = value;
		}
	}
}