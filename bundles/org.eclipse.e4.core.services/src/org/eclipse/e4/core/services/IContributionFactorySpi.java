package org.eclipse.e4.core.services;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.osgi.framework.Bundle;


public interface IContributionFactorySpi {

	public Object create(Bundle bundle, String className,
			IEclipseContext context);

	public Object call(Object object, String methodName,
			IEclipseContext context, Object defaultValue);
}
