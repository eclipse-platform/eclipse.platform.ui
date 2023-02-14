package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Shell;

public interface IScriptingSupport {
	public void openEditor(Shell shell, Object scope, IEclipseContext context);
}
