/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.leaks;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class Bug397302Tests {
	
	/**
	 * @since 3.5
	 *
	 */
	private static class TestListener implements ISourceProviderListener {
		
		private long callCount = 0;
		
		public long getCallCount() {
			return callCount;
		}

		/**
		 */
		public TestListener() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProviderListener#sourceChanged(int, java.lang.String, java.lang.Object)
		 */
		public void sourceChanged(int sourcePriority, String sourceName,
				Object sourceValue) {
			++callCount;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProviderListener#sourceChanged(int, java.util.Map)
		 */
		public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
			++callCount;
			}
	}

	private static final class TestSourceProvider extends AbstractSourceProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProvider#dispose()
		 */
		public void dispose() {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
		 */
		public Map getCurrentState() {
			return Collections.EMPTY_MAP;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
		 */
		public String[] getProvidedSourceNames() {
			return new String[] {};
		}

		/**
		 * 
		 */
		public void callOut() {
			this.fireSourceChanged(0, Collections.EMPTY_MAP);
		}
		
	}

	/**
	 * Reproduce the problem, as described in the bug report.
	 */
	@Test
	public void testBugAsDescribed() {
		final TestSourceProvider testSourceProvider = new TestSourceProvider();
		TestListener a = new TestListener();
		TestListener b = new TestListener();
		// keep weak references so we can check on the GC status later on...
		final WeakReference<TestListener> listenerARef = new WeakReference<TestListener>(a);
		final WeakReference<TestListener> listenerBRef = new WeakReference<TestListener>(b);
		
		// add listeners, call out the them, and verify that they got called.
		testSourceProvider.addSourceProviderListener(a);
		testSourceProvider.addSourceProviderListener(b);
		
		testSourceProvider.callOut();
		
		Assert.assertEquals(1, a.getCallCount());
		Assert.assertEquals(1, b.getCallCount());

		// remove listeners, call out to them, and verify that they no longer got called
		testSourceProvider.removeSourceProviderListener(a);
		testSourceProvider.removeSourceProviderListener(b);
		
		testSourceProvider.callOut();
		
		Assert.assertEquals(1, a.getCallCount());
		Assert.assertEquals(1, b.getCallCount());		

		// loose our strong references to a & b, force a GC, and see whether either gets leaked.
		// Test: The bug asserts that B has been leaked. Force a GC, and test whether
		// our weak references have gone null of not. If there is no leak, then both 
		// should be null.
		a = null;
		b = null;
		
		System.gc();
		
		Assert.assertNull("Reference A", listenerARef.get());
		Assert.assertNull("Reference B", listenerBRef.get());
		
		// Need this to prevent the above GC call from sweeping everything up before we're ready. 
		// See this only when NOT in debug.
		testSourceProvider.callOut();
		
	}

	/**
	 * Test that removal during call out does not cause problems.
	 */
	@Test
	public void testRemoveDuringCallOut() {
		final TestSourceProvider testSourceProvider = new TestSourceProvider();
		final TestListener testListener = new TestListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.tests.leaks.Bug397302Tests.TestListener#sourceChanged(int, java.util.Map)
			 */
			@Override
			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
				testSourceProvider.removeSourceProviderListener(this);
			}
			
		};
		testSourceProvider.addSourceProviderListener(testListener);
		
		// With improper protection, this was can through something like ConcurrentModificationException
		testSourceProvider.callOut();
		
	}
}
