package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.ui.model.application.Menu;
import org.eclipse.e4.ui.model.workbench.Perspective;

public interface ILegacyHook {
	public void loadMenu(Menu menuModel);
	public void loadPerspective(Perspective<?> perspModel);
}
