package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.OpenPerspectiveMenu;
import java.util.*;

public class WindowPerspectiveMenu extends OpenPerspectiveMenu {

	/**
	 * Constructor for WindowPerspectiveMenu.
	 * @param window
	 * @param input
	 */
	public WindowPerspectiveMenu(IWorkbenchWindow window, IAdaptable input) {
		super(window, input);
	}

	/* (non-Javadoc)
	 * Fills the menu with perspective items.
	 */
	public void fill(Menu menu, int index) 
	{
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean version2 = 
			store.getBoolean(IPreferenceConstants.REUSE_PERSPECTIVES);
		showActive(version2);
		super.fill(menu, index);
	}
}

