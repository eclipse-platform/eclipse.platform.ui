/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 *
 */
public class NPETestScenario extends ScenariosTestCase {
	private Text text;

	private Person person;

	protected void setUp() throws Exception {
		super.setUp();
		person = new Person();
		text = new Text(getComposite(), SWT.BORDER);
	}

	/**
	 * Asserts the ability to have an initial value of <code>null</code> on
	 * the model and to update the value by changing the value of the view.
	 */
	public void test_InitialNullValue() {
		Person person = new Person();
		assertNull(person.getName());

		getDbc().bind(text, new Property(person, "name"), null);

		text.setText("Brad");
		text.notifyListeners(SWT.FocusOut, null);
		assertEquals("Brad", person.getName());
	}

	private static class Person {
		private String name;

		/**
		 * @return Returns the name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            The name to set.
		 */
		public void setName(String name) {
			this.name = name;
		}
	}
}
