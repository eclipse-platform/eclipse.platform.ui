package org.eclipse.e4.core.services;

import org.osgi.framework.Bundle;


public interface IContributionFactorySpi {

	public Object create(Bundle bundle, String className,
			IServiceLocator serviceLocator);

	public Object call(Object object, String methodName,
			IServiceLocator serviceLocator, Object defaultValue);
}
