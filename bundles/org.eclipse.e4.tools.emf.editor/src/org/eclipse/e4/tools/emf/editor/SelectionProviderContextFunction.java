package org.eclipse.e4.tools.emf.editor;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ISelectionProviderService;
import org.eclipse.e4.ui.services.IServiceConstants;

public class SelectionProviderContextFunction extends ContextFunction {

	@Override
	public Object compute(final IEclipseContext context) {
		return new ISelectionProviderService() {

			public void setSelection(Object selection) {
				context.set(IServiceConstants.SELECTION, selection);
			}
		};
	}
}