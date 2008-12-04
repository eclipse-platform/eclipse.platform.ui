package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.ui.model.application.Menu;
import org.eclipse.e4.ui.model.workbench.Perspective;
import org.eclipse.e4.workbench.ui.internal.Workbench;

public interface ILegacyHook {
	public void init(Workbench e4Workbench);
	public void loadMenu(Menu menuModel);
	public void loadPerspective(Perspective<?> perspModel);
}
