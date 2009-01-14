package org.eclipse.e4.core.services;

import org.osgi.framework.Bundle;


public interface IContributionFactorySpi {

	public Object create(Bundle bundle, String className,
			Context context);

	public Object call(Object object, String methodName,
			Context context, Object defaultValue);
}
