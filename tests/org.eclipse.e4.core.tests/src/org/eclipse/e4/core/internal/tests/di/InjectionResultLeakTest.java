/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di;

import java.lang.ref.WeakReference;
import javax.inject.Inject;
import javax.inject.Named;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;

/**
 * Test that we don't hold on to the values calculated during injection.
 * This test relies on VM performing garbage collection in System.gc().
 * The actual VM processing of GC requests is up to the implementation
 * so this test might not work on all VMs or might become invalid on 
 * future VMs. 
 */
public class InjectionResultLeakTest extends TestCase {

	static class PartConsumer {
		Object part;

		@Inject
		void setPart(@Optional @Named("testGC") Object part) {
			this.part = part;
		}
	}

	public void testLeaks() {
		IEclipseContext context = EclipseContextFactory.create();
		WeakReference<?> ref;

		{ // scope the "part" object to help GC
			Object part = new Object();
			ref = new WeakReference<Object>(part);
			assertEquals(part, ref.get());

			context.set("testGC", part);

			PartConsumer consumer = ContextInjectionFactory.make(
					PartConsumer.class, context);
			assertEquals(part, consumer.part);

			part = null; // another "let's help GC" statement
			context.remove("testGC");

			assertNull(consumer.part);
		}

		// gc a few times
		System.runFinalization();
		System.gc();
		System.runFinalization();
		System.gc();
		System.runFinalization();
		System.gc();
		System.runFinalization();
		System.gc();

		// partA should have been gc'd
		assertNull("The object should have been garbage collected", ref.get());
	}

}
