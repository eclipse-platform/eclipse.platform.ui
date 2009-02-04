package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.workbench.MPerspective;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.workbench.ui.internal.Workbench;

public interface ILegacyHook {
	public void init(Workbench e4Workbench, MApplication<MWorkbenchWindow> workbench);
	public void loadMenu(MMenu menuModel);
	public void loadPerspective(MPerspective<?> perspModel);
}
