package org.eclipse.e4.tools.emf.ui.common;

import java.util.Map;
import org.eclipse.swt.widgets.Shell;

public interface IScriptingSupport {
	public void openEditor(Shell shell, Object scope, Map<String, Object> additionalData);
}
