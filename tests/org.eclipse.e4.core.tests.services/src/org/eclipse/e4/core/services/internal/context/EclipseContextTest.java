/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import junit.framework.TestCase;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IContextFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

public class EclipseContextTest extends TestCase {

	private static class ComputedValueBar extends ContextFunction {
		public Object compute(IEclipseContext context, Object[] arguments) {
			return context.get("bar");
		}
	}

	private static class ConcatFunction implements IContextFunction {
		public Object compute(IEclipseContext context, Object[] arguments) {
			String separator = (String) context.get("separator");
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) {
					result.append(separator);
				}
				result.append(arguments[i]);
			}
			return result.toString();
		}
	}

	private IEclipseContext context;
	private IEclipseContext parentContext;

	private int runCounter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.parentContext = EclipseContextFactory.create();
		this.parentContext.set(IContextConstants.DEBUG_STRING, getName() + "-parent");
		this.context = EclipseContextFactory.create(parentContext, null);
		context.set(IContextConstants.DEBUG_STRING, getName());
	}

	public void testContainsKey() {
		assertFalse("1.0", context.containsKey("function"));
		assertFalse("1.1", context.containsKey("separator"));
		
		context.set("function", new ConcatFunction());
		context.set("separator", ",");
		assertTrue("2.0", context.containsKey("function"));
		assertTrue("2.1", context.containsKey("separator"));
		
		//null value is still a value
		context.set("separator", null);
		assertTrue("3.0", context.containsKey("separator"));
		
		context.remove("separator");
		assertFalse("4.0", context.containsKey("separator"));
		
	}

	public void testFunctions() {
		context.set("function", new ConcatFunction());
		context.set("separator", ",");
		assertEquals("x", context.get("function", new String[] {"x"}));
		assertEquals("x,y", context.get("function", new String[] {"x", "y"}));
	}

	public void testGet() {
		assertNull(context.get("foo"));
		context.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		assertNull(parentContext.get("foo"));
		context.remove("foo");
		assertNull(context.get("foo"));
		parentContext.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		context.set("foo", new ComputedValueBar());
		assertNull(context.get("foo"));
		context.set("bar", "baz");
		assertEquals("baz", context.get("foo"));
	}

	public void testGetLocal() {
		assertNull(context.getLocal("foo"));
		context.set("foo", "bar");
		assertEquals("bar", context.getLocal("foo"));
		assertNull(parentContext.getLocal("foo"));
		context.remove("foo");
		assertNull(context.getLocal("foo"));
		parentContext.set("foo", "bar");
		assertNull(context.getLocal("foo"));
		context.set("foo", new ComputedValueBar());
		assertNull(context.getLocal("foo"));
		context.set("bar", "baz");
		assertEquals("baz", context.getLocal("foo"));
	}

	public void testRunAndTrack() {
		final Object[] value = new Object[1];
		context.runAndTrack(new Runnable() {
			public void run() {
				runCounter++;
				value[0] = context.get("foo");
			}
		});
		assertEquals(1, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", "bar");
		assertEquals(2, runCounter);
		assertEquals("bar", value[0]);
		context.remove("foo");
		assertEquals(3, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", new IContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				return context.get("bar");
			}
		});
		assertEquals(4, runCounter);
		assertEquals(null, value[0]);
		context.set("bar", "baz");
		assertEquals(5, runCounter);
		assertEquals("baz", value[0]);
		context.set("bar", "baf");
		assertEquals(6, runCounter);
		assertEquals("baf", value[0]);
		context.remove("bar");
		assertEquals(7, runCounter);
		assertEquals(null, value[0]);
		parentContext.set("bar", "bam");
		assertEquals(8, runCounter);
		assertEquals("bam", value[0]);
	}

}
