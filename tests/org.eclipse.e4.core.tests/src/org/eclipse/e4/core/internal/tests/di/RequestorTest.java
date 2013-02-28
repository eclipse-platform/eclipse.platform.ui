package org.eclipse.e4.core.internal.tests.di;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.e4.core.internal.contexts.ContextObjectSupplier;
import org.eclipse.e4.core.internal.di.FieldRequestor;

public class RequestorTest extends TestCase {
	public Object field;
	public IEclipseContext context;

	public void testHashCode() throws Exception {
		Field field = getClass().getField("field");
		assertNotNull(field);
		FieldRequestor requestor = new FieldRequestor(field,
				InjectorFactory.getDefault(),
				ContextObjectSupplier.getObjectSupplier(context,
						InjectorFactory.getDefault()), null, this, false);
		int hash = requestor.hashCode();
		requestor.getReference().clear();
		assertEquals(hash, requestor.hashCode());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = EclipseContextFactory.create(getName());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		context.dispose();
	}


}
