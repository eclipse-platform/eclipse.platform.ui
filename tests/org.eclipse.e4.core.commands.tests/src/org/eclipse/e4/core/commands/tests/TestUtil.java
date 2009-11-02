package org.eclipse.e4.core.commands.tests;

import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

public class TestUtil {
	public static IEclipseContext createContext(IEclipseContext parent, String name) {
		IEclipseContext wb = EclipseContextFactory.create(parent, null);
		wb.set(IContextConstants.DEBUG_STRING, name);
		return wb;
	}
}
