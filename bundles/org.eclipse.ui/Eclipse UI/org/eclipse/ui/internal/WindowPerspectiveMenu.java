package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.OpenPerspectiveMenu;
import java.util.*;

public class WindowPerspectiveMenu extends OpenPerspectiveMenu {

	private static ArrayList orderedShortcuts;
	private static ArrayList mruShortcuts;
	private static final int SHORTCUT_LIST_SIZE = 8;
	
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
		boolean smart = 
			store.getBoolean(IPreferenceConstants.REUSE_PERSPECTIVES);
		showActive(smart);
		super.fill(menu, index);
	}
	
	/**
	 * Runs an action for a particular perspective. Opens the persepctive supplied
	 * in a new window or a new page depending on the workbench preference.
	 *
	 * @param desc the selected perspective
	 */
	protected void run(IPerspectiveDescriptor desc) {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean smart = 
			store.getBoolean(IPreferenceConstants.REUSE_PERSPECTIVES);
		if (smart) {
			updateShortcuts(desc); // Do this before switching to get the original persp list.
			runReplaceCurrent(desc);
		} else {
			super.run(desc);
		}
	}
	
	/**
	 * Runs an action for a particular perspective. Check for shift or control events
	 * to decide which event to run.
	 *
	 * @param desc the selected perspective
	 * @param event SelectionEvent - the event send along with the selection callback
	 */
	protected void run(IPerspectiveDescriptor desc, SelectionEvent event) {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean smart = 
			store.getBoolean(IPreferenceConstants.REUSE_PERSPECTIVES);
		if (smart) {
			updateShortcuts(desc); // Do this before switching to get the original persp list.
			runReplaceCurrent(desc);
		} else {
			super.run(desc, event);
		}
	}
	
	/* (non-Javadoc)
	 * Returns the shortcut perspectives.
	 *
	 * The shortcut list is formed from the default perspective (dynamic) and
	 * the product perspectives (static).  For performance, we implement a
	 * shortcut cache which is only updated if the default perspective changes.
	 */
	protected ArrayList getShortcuts() 
	{
		if (orderedShortcuts != null)
			return orderedShortcuts;
		return super.getShortcuts();
	}	
	
	/**
	 * Update the shortcut list with the last one opened.
	 */
	private void updateShortcuts(IPerspectiveDescriptor desc) {
		// Bootstrap the shortcut list.
		if (orderedShortcuts == null) {
			orderedShortcuts = super.getShortcuts();
			mruShortcuts = (ArrayList)orderedShortcuts.clone();
		}

		// If the new desc is already in the shortcut list, just return.
		if (orderedShortcuts.contains(desc))
			return;
			
		// Add desc to shortcut lists.
		orderedShortcuts.add(desc); // insert at end to avoid reordering
		mruShortcuts.add(1, desc); // insert after default.
		
		// If the shortcut list is too long then remove the oldest ones.
		int size = mruShortcuts.size();
		int preferredSize = SHORTCUT_LIST_SIZE - 1;
		while (size > preferredSize) {
			Object old = mruShortcuts.get(size - 1);
			mruShortcuts.remove(old);
			orderedShortcuts.remove(old);
			-- size;
		}
	}
}

