/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.di.annotations.Execute;

/**
* Tests that that no method is called, it the @Execute
* annotation is not present and that an exception is thrown from the DI
* framework
*/
public class InvokeTestNegative extends TestCase {

	/**
	 * Class to invoke for the test
	 */
	class TestSuperclass {
		public int saveCount = 0;
		// @Execute annotation missing on purpose
		void execute() {
			saveCount++;
		}
	}

	/**
	 * Checks that no methods is called
	 */
	public void testSuperclassMethods() {
		TestSuperclass editor = new TestSuperclass();
		try {
			ContextInjectionFactory.invoke(editor, Execute.class, EclipseContextFactory
					.create());
			   fail("Exception should have been thrown "); //$NON-NLS-1$
			} catch (Exception e) {
			   // expected
			}
	}

}
