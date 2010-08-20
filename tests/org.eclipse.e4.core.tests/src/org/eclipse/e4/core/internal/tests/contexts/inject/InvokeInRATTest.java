/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;

/**
 * Tests for the context injection functionality using 2 contexts 
 */
public class InvokeInRATTest extends TestCase {
	
	static class TestHandler {
		
		public Object active;
		public Object selected;
		
		@CanExecute
		public void testEnablement(@Optional @Named("active") Object active, @Optional @Named("selected") Object selected) {
			this.active = active;
			this.selected = selected;
		}
	}
	
	public void testStaticInvoke() {
		IEclipseContext context = EclipseContextFactory.create();
		final int[] count = new int[1];
		
		context.runAndTrack(new RunAndTrack() {
			@Override
			public boolean changed(IEclipseContext context) {
				TestHandler handler = (TestHandler) context.get("handlerA");
				if (handler != null) {
					ContextInjectionFactory.invoke(handler, CanExecute.class, context);
					count[0]++;
				}
				return true; // continue to be notified
			}});
		
		// check that updates are propagated 
		context.set("active", new Integer(123));
		context.set("selected", "abc");
		TestHandler handler = new TestHandler();
		context.set("handlerA", handler);
		
		assertEquals(new Integer(123), handler.active);
		assertEquals("abc", handler.selected);
		
		// check handler replacement
		count[0] = 0;
		TestHandler newHandler = new TestHandler();
		context.set("handlerA", newHandler);
		assertEquals(1, count[0]);
		
		assertEquals(new Integer(123), newHandler.active);
		assertEquals("abc", newHandler.selected);
		
		// change values in the context; values should not be propagated to handlers 
		context.set("active", new Integer(456));
		context.set("selected", "xyz");
		
		assertEquals(new Integer(123), handler.active);
		assertEquals("abc", handler.selected);
		assertEquals(new Integer(123), newHandler.active);
		assertEquals("abc", newHandler.selected);
	}
}
