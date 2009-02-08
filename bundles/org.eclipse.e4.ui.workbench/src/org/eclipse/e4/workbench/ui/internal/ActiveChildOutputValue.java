package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ComputedValue;
import org.eclipse.e4.ui.services.IServiceConstants;

public final class ActiveChildOutputValue extends ComputedValue {
	private final String attr;

	public ActiveChildOutputValue(String attr) {
		this.attr = attr;
	}

	public Object compute(IEclipseContext context, Object[] arguments) {
		IEclipseContext childContext = (IEclipseContext) context
				.getLocal(IServiceConstants.ACTIVE_CHILD);
		if (childContext != null) {
			return childContext.get(attr);
		} else if (context.containsKey(IServiceConstants.OUTPUTS)) {
			IEclipseContext outputs = (IEclipseContext) context
					.get(IServiceConstants.OUTPUTS);
			return outputs.get(attr);
		}
		return null;
	}
}