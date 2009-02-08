package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ComputedValue;

public class RealmValue extends ComputedValue {

	public Object compute(IEclipseContext context, Object[] arguments) {
		return new LockRealm();
	}

}
