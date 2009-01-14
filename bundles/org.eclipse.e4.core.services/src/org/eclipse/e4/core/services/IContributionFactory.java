package org.eclipse.e4.core.services;

public interface IContributionFactory {

	public Object call(Object object, String uri,
			String methodName, Context context, Object defaultValue);

	public Object create(String uri, Context context);
}
