/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *******************************************************************************/

package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 *
 */
public class NPETestScenario extends ScenariosTestCase {
	private Text text;

	Person person;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		person = new Person();
		text = new Text(getComposite(), SWT.BORDER);
	}

	/**
	 * Asserts the ability to have an initial value of <code>null</code> on the
	 * model and to update the value by changing the value of the view.
	 */
	public void test_InitialNullValue() {
		Person person = new Person();
		assertNull(person.getName());

		System.out
				.println("Expecting message about not being able to attach a listener");
		getDbc().bindValue(SWTObservables.observeText(text, SWT.Modify),
				BeansObservables.observeValue(person, "name"));

		text.setText("Brad");
		text.notifyListeners(SWT.FocusOut, null);
		assertEquals("Brad", person.getName());
	}

	static class Person {
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
