/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (bug 146488)
 *     Matthew Hall - initial API and implementation (bug 146488)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class SetOnlyJavaBeanTest extends AbstractDefaultRealmTestCase {

	public void testValidationError() throws Exception {
		Model model = new Model();
		model.setString("abc");

		Target target = new Target();

		IObservableValue modelObservable =
				PojoProperties.value("string").observe(model);
		IObservableValue targetObservable =
				PojoProperties.value("string").observe(target);

		DataBindingContext context = new DataBindingContext();
		context.bindValue(targetObservable, modelObservable,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER),
				new UpdateValueStrategy());

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