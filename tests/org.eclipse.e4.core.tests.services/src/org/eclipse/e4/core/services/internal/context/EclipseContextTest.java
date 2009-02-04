package org.eclipse.e4.core.services.internal.context;

import junit.framework.TestCase;

import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;

public class EclipseContextTest extends TestCase {

	private static class ConcatFunction implements IComputedValue {
		public Object compute(IEclipseContext context, String[] arguments) {
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

	private static class ComputedValueBar implements IComputedValue {
		public Object compute(IEclipseContext context, String[] arguments) {
			return context.get("bar");
		}
	}

	private IEclipseContext context;
	private IEclipseContext parentContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.parentContext = EclipseContextFactory.create(getName() + "-parent");
		this.context = EclipseContextFactory.create(getName(), parentContext, null);
	}

	public void testGetLocal() {
		assertNull(context.getLocal("foo"));
		context.set("foo", "bar");
		assertEquals("bar", context.getLocal("foo"));
		assertNull(parentContext.getLocal("foo"));
		context.unset("foo");
		assertNull(context.getLocal("foo"));
		parentContext.set("foo", "bar");
		assertNull(context.getLocal("foo"));
		context.set("foo", new ComputedValueBar());
		assertNull(context.getLocal("foo"));
		context.set("bar", "baz");
		assertEquals("baz", context.getLocal("foo"));
	}

	public void testGet() {
		assertNull(context.get("foo"));
		context.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		assertNull(parentContext.get("foo"));
		context.unset("foo");
		assertNull(context.get("foo"));
		parentContext.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		context.set("foo", new ComputedValueBar());
		assertNull(context.get("foo"));
		context.set("bar", "baz");
		assertEquals("baz", context.get("foo"));
	}

	public void testFunctions() {
		context.set("function", new ConcatFunction());
		context.set("separator", ",");
		assertEquals("x", context.get("function", new String[] { "x" }));
		assertEquals("x,y", context.get("function", new String[] { "x", "y" }));
	}

	private int runCounter;

	public void testRunAndTrack() {
		final Object[] value = new Object[1];
		context.runAndTrack(new Runnable() {
			public void run() {
				runCounter++;
				value[0] = context.get("foo");
			}
		}, "runnable");
		assertEquals(1, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", "bar");
		assertEquals(2, runCounter);
		assertEquals("bar", value[0]);
		context.unset("foo");
		assertEquals(3, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", new IComputedValue() {
			public Object compute(IEclipseContext context, String[] arguments) {
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
		context.unset("bar");
		assertEquals(7, runCounter);
		assertEquals(null, value[0]);
		parentContext.set("bar", "bam");
		assertEquals(8, runCounter);
		assertEquals("bam", value[0]);
	}

}
