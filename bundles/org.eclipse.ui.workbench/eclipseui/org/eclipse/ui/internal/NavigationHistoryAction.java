/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The <code>NavigationHistoryAction</code> moves navigation history back and
 * forward.
 */
public class NavigationHistoryAction extends PageEventAction {

	private boolean recreateMenu;

	private boolean forward;

	private Menu historyMenu;

	private int MAX_HISTORY_LENGTH = 9;

	private class MenuCreator implements IMenuCreator {
		@Override
		public void dispose() {
		}

		@Override
		public Menu getMenu(Menu parent) {
			setMenu(new Menu(parent));
			fillMenu(historyMenu);
			initMenu();
			return historyMenu;
		}

		@Override
		public Menu getMenu(Control parent) {
			setMenu(new Menu(parent));
			fillMenu(historyMenu);
			initMenu();
			return historyMenu;
		}

	}

	private void setMenu(Menu menu) {
		historyMenu = menu;
	}

	private void initMenu() {
		historyMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				if (recreateMenu) {
					Menu m = (Menu) e.widget;
					MenuItem[] items = m.getItems();
					for (MenuItem item : items) {
						item.dispose();
					}
					fillMenu(m);
				}
			}
		});
	}

	private void fillMenu(Menu menu) {
		IWorkbenchPage page = getWorkbenchWindow().getActivePage();
		if (page == null) {
			return;
		}

		final NavigationHistory history = (NavigationHistory) getWorkbenchWindow().getActivePage()
				.getNavigationHistory();
		NavigationHistoryEntry[] entries;
		if (forward) {
			entries = history.getForwardEntries();
		} else {
			entries = history.getBackwardEntries();
		}
		int entriesCount[] = new int[entries.length];
		Arrays.fill(entriesCount, 1);
		entries = collapseEntries(entries, entriesCount);
		for (int i = 0; i < entries.length; i++) {
			if (i > MAX_HISTORY_LENGTH) {
				break;
			}
			String text = entries[i].getHistoryText();
			if (text != null) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setData(entries[i]);
				if (entriesCount[i] > 1) {
					text = NLS.bind(WorkbenchMessages.NavigationHistoryAction_locations, text,
							Integer.valueOf(entriesCount[i]));
				}
				item.setText(text);
				item.addSelectionListener(widgetSelectedAdapter(
						e -> history.shiftCurrentEntry((NavigationHistoryEntry) e.widget.getData(), forward)));
			}
		}
		recreateMenu = false;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (historyMenu != null) {
			for (int i = 0; i < historyMenu.getItemCount(); i++) {
				MenuItem menuItem = historyMenu.getItem(i);
				menuItem.dispose();
			}
			historyMenu.dispose();
			historyMenu = null;
		}
	}

	/**
	 * Create a new instance of <code>NavigationHistoryAction</code>
	 *
	 * @param window  the workbench window this action applies to
	 * @param forward if this action should move history forward of backward
	 */
	public NavigationHistoryAction(IWorkbenchWindow window, boolean forward) {
		super("", window); //$NON-NLS-1$
		ISharedImages sharedImages = window.getWorkbench().getSharedImages();
		if (forward) {
			setText(WorkbenchMessages.NavigationHistoryAction_forward_text);
			setToolTipText(WorkbenchMessages.NavigationHistoryAction_forward_toolTip);
			window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.NAVIGATION_HISTORY_FORWARD);
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
			setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY);
		} else {
			setText(WorkbenchMessages.NavigationHistoryAction_backward_text);
			setToolTipText(WorkbenchMessages.NavigationHistoryAction_backward_toolTip);
			window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.NAVIGATION_HISTORY_BACKWARD);
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
			setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY);
		}
		setEnabled(false);
		this.forward = forward;
		setMenuCreator(new MenuCreator());
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		super.pageClosed(page);
		setEnabled(false);
	}

	private NavigationHistoryEntry[] collapseEntries(NavigationHistoryEntry[] entries, int entriesCount[]) {
		ArrayList<NavigationHistoryEntry> allEntries = new ArrayList<>(Arrays.asList(entries));
		NavigationHistoryEntry previousEntry = null;
		int i = -1;
		for (Iterator<NavigationHistoryEntry> iter = allEntries.iterator(); iter.hasNext();) {
			NavigationHistoryEntry entry = iter.next();
			if (previousEntry != null) {
				String text = previousEntry.getHistoryText();
				if (text != null) {
					if (text.equals(entry.getHistoryText()) && previousEntry.editorInfo == entry.editorInfo) {
						iter.remove();
						entriesCount[i]++;
						continue;
					}
				}
			}
			previousEntry = entry;
			i++;
		}
		entries = new NavigationHistoryEntry[allEntries.size()];
		return allEntries.toArray(entries);
	}

	@Override
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		NavigationHistory nh = (NavigationHistory) page.getNavigationHistory();
		if (forward) {
			nh.setForwardAction(this);
		} else {
			nh.setBackwardAction(this);
		}
	}

	@Override
	public void run() {
		if (getWorkbenchWindow() == null) {
			// action has been disposed
			return;
		}
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			NavigationHistory nh = (NavigationHistory) page.getNavigationHistory();
			if (forward) {
				nh.forward();
			} else {
				nh.backward();
			}
			recreateMenu = true;
		}
	}

	public void update() {
		// Set the enabled state of the action and set the tool tip text. The tool tip
		// text is set to reflect the item that one will move back/forward to.
		WorkbenchPage page = (WorkbenchPage) getActivePage();
		if (page == null) {
			return;
		}
		NavigationHistory history = (NavigationHistory) page.getNavigationHistory();
		NavigationHistoryEntry[] entries;
		if (forward) {
			setEnabled(history.canForward());
			entries = history.getForwardEntries();
			if (entries.length > 0) {
				NavigationHistoryEntry entry = entries[0];
				String text = NLS.bind(WorkbenchMessages.NavigationHistoryAction_forward_toolTipName,
						entry.getHistoryText());
				setToolTipText(text);
			} else {
				setToolTipText(WorkbenchMessages.NavigationHistoryAction_forward_toolTip);
			}
		} else {
			setEnabled(history.canBackward());
			entries = history.getBackwardEntries();
			if (entries.length > 0) {
				NavigationHistoryEntry entry = entries[0];
				String text = NLS.bind(WorkbenchMessages.NavigationHistoryAction_backward_toolTipName,
						entry.getHistoryText());
				setToolTipText(text);
			} else {
				setToolTipText(WorkbenchMessages.NavigationHistoryAction_backward_toolTip);
			}
		}
		recreateMenu = true;
	}
}
