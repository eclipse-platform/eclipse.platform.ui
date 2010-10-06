package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 *
 */
public class ActivePartLookupFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context) {
		/**
		 * This is the specific implementation. TODO: generalize it
		 */
		MContext window = context.get(MWindow.class);
		if (window == null) {
			window = context.get(MApplication.class);
			if (window == null) {
				return null;
			}
		}
		IEclipseContext current = window.getContext();
		if (current == null) {
			return null;
		}
		return current.getActiveLeaf().get(MPart.class);
	}
}
