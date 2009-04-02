package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;

public class RealmFunction extends ContextFunction {

	public Object compute(IEclipseContext context, Object[] arguments) {
		return new LockRealm();
	}

}
