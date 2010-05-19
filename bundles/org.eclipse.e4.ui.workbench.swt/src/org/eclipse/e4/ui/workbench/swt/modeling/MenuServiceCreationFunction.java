package org.eclipse.e4.ui.workbench.swt.modeling;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;

public class MenuServiceCreationFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context) {
		try {
			return ContextInjectionFactory.make(MenuService.class, context);
		} catch (InjectionException ie) {
			// we won't report this at the moment.
			System.err.println("MenuService: " + context + ": " + ie);
		}
		return null;
	}

}
