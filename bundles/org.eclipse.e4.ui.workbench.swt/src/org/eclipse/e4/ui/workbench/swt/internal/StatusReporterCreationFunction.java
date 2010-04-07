package org.eclipse.e4.ui.workbench.swt.internal;

import org.eclipse.e4.core.services.log.Logger;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 *
 */
public class StatusReporterCreationFunction implements IContextFunction {

	public Object compute(IEclipseContext context, Object[] arguments) {
		try {
			return ContextInjectionFactory.make(WorkbenchStatusReporter.class,
					context);
		} catch (InvocationTargetException e) {
			Logger logger = (Logger) context.get(Logger.class.getName());
			if (logger != null) {
				logger.error(e);
			}
		} catch (InstantiationException e) {
			Logger logger = (Logger) context.get(Logger.class.getName());
			if (logger != null) {
				logger.error(e);
			}
		}
		return null;
	}

}
