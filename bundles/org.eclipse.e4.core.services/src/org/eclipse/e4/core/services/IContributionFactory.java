package org.eclipse.e4.core.services;

import org.eclipse.e4.core.services.context.IEclipseContext;

public interface IContributionFactory {

	public Object call(Object object, String uri,
			String methodName, IEclipseContext context, Object defaultValue);

	public Object create(String uri, IEclipseContext context);
}
