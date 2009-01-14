package org.eclipse.e4.core.services;



public interface IContributionFactory {

	public Object call(Object object, String uri,
			String methodName, IServiceLocator serviceLocator, Object defaultValue);

	public Object create(String uri, IServiceLocator serviceLocator);
}
