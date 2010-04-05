package org.eclipse.e4.core.commands.tests;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class TestUtil {
	public static IEclipseContext createContext(IEclipseContext parent, String name) {
		IEclipseContext wb = EclipseContextFactory.create(parent, null);
		wb.set(IContextConstants.DEBUG_STRING, name);
		return wb;
	}
}
