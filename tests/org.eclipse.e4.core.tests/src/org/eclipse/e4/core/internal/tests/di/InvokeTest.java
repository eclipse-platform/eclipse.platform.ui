/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.di.annotations.Execute;

public class InvokeTest extends TestCase {

	/**
	 * Superclass
	 */
	static class TestSuperclass {
		public int saveCount = 0;
		@Execute
		void something() {
			saveCount++;
		}
	}

	/**
	 * Subclass
	 */
	static class TestSubclass extends TestSuperclass {
	}

	/**
	 * Checks that superclass methods are called
	 */
	public void testSuperclassMethods() {
		TestSubclass editor = new TestSubclass();
		ContextInjectionFactory.invoke(editor, Execute.class, EclipseContextFactory
				.create());
		assertEquals(1, editor.saveCount);
	}
}
