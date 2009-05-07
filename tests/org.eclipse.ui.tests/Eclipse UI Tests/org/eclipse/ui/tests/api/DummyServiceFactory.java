package org.eclipse.ui.tests.api;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * 
 * @since 3.5
 * @author Prakash G.R.
 *
 */
public class DummyServiceFactory extends AbstractServiceFactory {
	
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if(serviceInterface.equals(DummyService.class))
			return new DummyService();
		return null;
	}

}
