package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;
import org.eclipse.e4.ui.services.IServiceConstants;

public final class ActiveChildOutputValue implements IComputedValue {
	private final String attr;

	public ActiveChildOutputValue(String attr) {
		this.attr = attr;
	}

	public Object compute(IEclipseContext context, String[] arguments) {
		IEclipseContext childContext = (IEclipseContext) context
				.getLocal(IServiceConstants.ACTIVE_CHILD);
		if (childContext != null) {
			return childContext.get(attr);
		} else if (context.isSet(IServiceConstants.OUTPUTS)) {
			IEclipseContext outputs = (IEclipseContext) context
					.get(IServiceConstants.OUTPUTS);
			return outputs.get(attr);
		}
		return null;
	}
}