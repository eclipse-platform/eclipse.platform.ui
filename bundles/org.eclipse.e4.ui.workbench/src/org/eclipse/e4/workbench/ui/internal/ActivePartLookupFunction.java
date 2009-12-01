package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MPart;

/**
 *
 */
public class ActivePartLookupFunction extends ContextFunction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.
	 * eclipse.e4.core.services.context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		/**
		 * This is the specific implementation. TODO: generalize it
		 */
		MApplication app = (MApplication) context.get(MApplication.class.getName());
		if (app == null) {
			return null;
		}
		IEclipseContext current = app.getContext();
		if (current == null) {
			return null;
		}
		IEclipseContext next = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
		while (next != null) {
			current = next;
			next = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
		}
		return current.get(MPart.class.getName());
	}
}
