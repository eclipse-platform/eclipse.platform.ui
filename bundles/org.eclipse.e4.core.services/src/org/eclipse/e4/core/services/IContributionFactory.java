package org.eclipse.e4.core.services;

import org.eclipse.emf.common.util.URI;


public interface IContributionFactory {

	public Object call(Object object, String uri,
			String methodName, IServiceLocator serviceLocator, Object defaultValue);

	public Object create(String uri, IServiceLocator serviceLocator);
}
