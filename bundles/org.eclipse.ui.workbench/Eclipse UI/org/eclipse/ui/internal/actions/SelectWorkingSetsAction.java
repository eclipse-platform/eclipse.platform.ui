/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.AbstractWorkingSetDialog;
import org.eclipse.ui.internal.dialogs.WorkingSetFilter;
import org.eclipse.ui.internal.dialogs.WorkingSetLabelProvider;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * Action to select the visible working sets for a given workbench page.
 * 
 * @since 3.2
 */
public class SelectWorkingSetsAction implements IWorkbenchWindowActionDelegate,
		IWorkbenchWindowPulldownDelegate2 {

	private class ManageWorkingSetsAction extends Action {

		ManageWorkingSetsAction() {
			super(WorkbenchMessages.Edit);
		}

		public void run() {
			SelectWorkingSetsAction.this.run(this);
		}
	}

	private class ToggleWorkingSetAction extends Action {
		private IWorkingSet set;

		ToggleWorkingSetAction(IWorkingSet set) {
			super(set.getName(), IAction.AS_CHECK_BOX);
			setImageDescriptor(set.getImage());
			this.set = set;
			setChecked(isWorkingSetEnabled(set));
		}

		public void run() {
			Set newList = new HashSet(Arrays.asList(window.getActivePage()
					.getWorkingSets()));

			if (isChecked())
				newList.add(set);
			else
				newList.remove(set);

			window.getActivePage().setWorkingSets(
					(IWorkingSet[]) newList.toArray(new IWorkingSet[newList
							.size()]));
		}
	}

	private Menu menubarMenu, toolbarMenu;

	private IWorkbenchWindow window;

	public void dispose() {
		if (menubarMenu != null) {
			menubarMenu.dispose();
			menubarMenu = null;
		}
		if (toolbarMenu != null) {
			toolbarMenu.dispose();
			toolbarMenu = null;
		}
	}

	private void fillMenu(Menu menu) {
		IWorkingSet[][] typedSets = splitSets();

		for (int i = 0; i < typedSets.length; i++) {
			IWorkingSet[] sets = typedSets[i];
			for (int j = 0; j < sets.length; j++) {
				IWorkingSet set = sets[j];

				// only add visible sets
				// if (set.isVisible()) {
				ActionContributionItem item = new ActionContributionItem(
						new ToggleWorkingSetAction(set));
				item.fill(menu, -1);
				// }
			}
			Separator separator = new Separator();
			separator.fill(menu, -1);
		}

		ActionContributionItem item = new ActionContributionItem(
				new ManageWorkingSetsAction());
		item.fill(menu, -1);

	}

	private IWorkingSet[] getEnabledSets() {
		return window.getActivePage().getWorkingSets();
	}

	public Menu getMenu(Control parent) {
		if (toolbarMenu != null)
			toolbarMenu.dispose();
		toolbarMenu = new Menu(parent);
		initMenu(toolbarMenu);
		return toolbarMenu;
	}

	public Menu getMenu(Menu parent) {
		if (menubarMenu != null)
			menubarMenu.dispose();
		menubarMenu = new Menu(parent);
		initMenu(menubarMenu);
		return menubarMenu;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Creates the menu for the action
	 */
	private void initMenu(Menu menu) {
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu) e.widget;
				MenuItem[] items = m.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}

		});
	}

	private boolean isWorkingSetEnabled(IWorkingSet set) {
		IWorkingSet[] enabledSets = getEnabledSets();
		for (int i = 0; i < enabledSets.length; i++) {
			if (enabledSets[i].equals(set))
				return true;
		}
		return false;
	}

	public void run(IAction action) {
		ConfigureWindowWorkingSetsDialog dialog = new ConfigureWindowWorkingSetsDialog(
				window);
		if (dialog.open() == Window.OK) {

		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * Split the working sets known by the manager into arrays based on their
	 * defining page Id.
	 * 
	 * @return an array of arrays
	 */
	private IWorkingSet[][] splitSets() {
		IWorkingSet[] allSets = window.getWorkbench().getWorkingSetManager()
				.getWorkingSets();

		Map map = new HashMap();
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
				.getWorkingSetRegistry();

		for (int i = 0; i < allSets.length; i++) {
			String setType = allSets[i].getId();
			if (WorkbenchActivityHelper.filterItem(registry
					.getWorkingSetDescriptor(setType)))
				continue;
			List setsOfType = (List) map.get(setType);
			if (setsOfType == null) {
				setsOfType = new ArrayList();
				map.put(setType, setsOfType);
			}
			setsOfType.add(allSets[i]);
		}

		IWorkingSet[][] typedSets = new IWorkingSet[map.keySet().size()][];
		int i = 0;
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			List setsOfType = (List) map.get(iter.next());
			typedSets[i] = new IWorkingSet[setsOfType.size()];
			setsOfType.toArray(typedSets[i++]);
		}
		return typedSets;
	}
}

class ConfigureWindowWorkingSetsDialog extends AbstractWorkingSetDialog {

	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

    private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;
    
	private IWorkbenchWindow window;

	private CheckboxTableViewer viewer;

	protected ConfigureWindowWorkingSetsDialog(IWorkbenchWindow window) {
		super(window.getShell(), null);
		this.window = window;
		setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title_multiSelect);
		setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message_multiSelect);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(new WorkingSetLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.addFilter(new WorkingSetFilter(null));
		viewer.setInput(window.getWorkbench().getWorkingSetManager()
				.getWorkingSets());
		viewer.setCheckedElements(window.getActivePage().getWorkingSets());
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged();
            }
        });

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;

		viewer.getControl().setLayoutData(data);

		addModifyButtons(composite);
		return composite;
	}

	protected void okPressed() {
		Object[] selection = viewer.getCheckedElements();
		IWorkingSet[] workingSets = new IWorkingSet[selection.length];
		System.arraycopy(selection, 0, workingSets, 0, selection.length);
		window.getActivePage().setWorkingSets(workingSets);
		super.okPressed();
	}

	protected List getSelectedWorkingSets() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection)
			return ((IStructuredSelection) selection).toList();
		return null;
	}

	protected void availableWorkingSetsChanged() {
		viewer.setInput(window.getWorkbench().getWorkingSetManager()
				.getWorkingSets());
	}
	
    /**
     * Called when the selection has changed.
     */
    void handleSelectionChanged() {
        updateButtonAvailability();
    }
}
