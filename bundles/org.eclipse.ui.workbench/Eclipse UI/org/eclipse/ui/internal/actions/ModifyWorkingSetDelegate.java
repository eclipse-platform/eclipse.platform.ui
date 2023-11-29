/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Benjamin Muskalla - Bug 169023 [WorkingSets] "Add to working set"
 *     						drop down should include a "new working set" option
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.QuickMenuCreator;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.3
 */
public class ModifyWorkingSetDelegate extends AbstractWorkingSetPulldownDelegate
		implements IExecutableExtension, IActionDelegate2 {

	public static class NewWorkingSetAction extends Action {

		/**
		 * Create a new instance of this action.
		 */
		public NewWorkingSetAction() {
			super(WorkbenchMessages.NewWorkingSet);
		}

		@Override
		public void run() {
			IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
			IWorkingSetNewWizard wizard = manager.createWorkingSetNewWizard(null);
			// the wizard can never be null since we have at least a resource
			// working set
			// creation page
			WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);

			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
					IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);
			if (dialog.open() == Window.OK) {
				IWorkingSet workingSet = wizard.getSelection();
				if (workingSet != null) {
					manager.addWorkingSet(workingSet);
				}
			}
		}

	}

	private class ModifyAction extends Action {

		private IWorkingSet set;

		private IAdaptable[] selectedElements;

		private ModifyAction(IWorkingSet set, IAdaptable[] selectedElements) {
			super(set.getLabel(), IAction.AS_CHECK_BOX);
			this.set = set;
			this.selectedElements = selectedElements;
			setImageDescriptor(set.getImageDescriptor());
		}

		@Override
		public void run() {

			Collection oldElements = Arrays.asList(set.getElements());
			Set newElements = new HashSet(oldElements.size() + selectedElements.length);
			newElements.addAll(oldElements);
			List selectedAsList = Arrays.asList(selectedElements);
			if (add) {
				newElements.addAll(selectedAsList);
			} else {
				newElements.removeAll(selectedAsList);
			}
			set.setElements((IAdaptable[]) newElements.toArray(new IAdaptable[newElements.size()]));
		}
	}

	private QuickMenuCreator contextMenuCreator = new QuickMenuCreator() {
		@Override
		protected void fillMenu(IMenuManager menu) {
			ModifyWorkingSetDelegate.this.fillMenu(menu);
		}
	};

	private boolean add = true;

	private IPropertyChangeListener listener = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			refreshEnablement();
		}

		private void refreshEnablement() {
			selectionChanged(actionProxy, getSelection());
		}
	};

	private IAction actionProxy;

	public ModifyWorkingSetDelegate() {
	}

	@Override
	public void run(IAction action) {
		contextMenuCreator.createMenu();
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		if (event.type == SWT.KeyDown || event.type == SWT.KeyUp)
			run(action);
	}

	@Override
	public void init(IWorkbenchWindow window) {
		super.init(window);
		getWindow().getWorkbench().getWorkingSetManager().addPropertyChangeListener(listener);
	}

	@Override
	public void dispose() {
		getWindow().getWorkbench().getWorkingSetManager().removePropertyChangeListener(listener);
		super.dispose();
		contextMenuCreator.dispose();
	}

	@Override
	public void fillMenu(Menu menu) {
		for (Object object : getItems()) {
			if (object instanceof IAction) {
				ActionContributionItem item = new ActionContributionItem((IAction) object);
				item.fill(menu, -1);
			} else {
				IContributionItem item = (IContributionItem) object;
				item.fill(menu, -1);
			}
		}
		// create working set action only for add menu
		if (add) {
			IContributionItem item = null;
			if (menu.getItemCount() > 0) {
				item = new Separator();
				item.fill(menu, -1);
			}

			item = new ActionContributionItem(new NewWorkingSetAction());
			item.fill(menu, -1);
		}
	}

	/**
	 * Return the list of items to show in the submenu.
	 *
	 * @return the items to show in the submenu
	 */
	private List getItems() {
		List menuItems = new ArrayList();
		ISelection selection = getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			if (!add) {
				IAction emptyAction = new Action(WorkbenchMessages.NoApplicableWorkingSets) {
				};
				emptyAction.setEnabled(false);
				menuItems.add(emptyAction);
			}
			return menuItems;
		}

		IWorkingSet[][] typedSets = splitSets();
		Object[] selectedElements = ((IStructuredSelection) selection).toArray();

		// keep a tab of whether or not we need a separator. If a given type
		// of working set has contributed some items then this will be true
		// after the processing of the working set type. The next type will
		// then consult this field and add a separator before adding any
		// items of its own. In this way the list will never end with a
		// separator.
		boolean needsSeparator = false;

		for (IWorkingSet[] sets : typedSets) {
			int oldCount = menuItems.size();

			for (IWorkingSet set : sets) {
				Set existingElements = new HashSet();
				existingElements.addAll(Arrays.asList(set.getElements()));

				boolean visible = false;
				IAdaptable[] adaptables = new IAdaptable[selectedElements.length];
				System.arraycopy(selectedElements, 0, adaptables, 0, selectedElements.length);
				adaptables = set.adaptElements(adaptables);
				if (adaptables.length > 0 && add) {
					for (IAdaptable adaptable : adaptables) {
						if (!existingElements.contains(adaptable)) {
							// show if any element is not present in
							// addition
							visible = true;
							break;
						}
					}
				} else if (adaptables.length > 0) {
					for (IAdaptable adaptable : adaptables) {
						if (existingElements.contains(adaptable)) {
							visible = true; // show if any element
											// is present in removal
							break;
						}
					}
				}

				if (visible) {
					if (needsSeparator) {
						menuItems.add(new Separator());
						needsSeparator = false;
					}
					ModifyAction action = new ModifyAction(set, adaptables);
					menuItems.add(action);
				}
			}
			// we need a separator if we needed one before but never added it or
			// we've added new items to the list.
			needsSeparator |= menuItems.size() > oldCount;
		}
		if (menuItems.isEmpty() && !add) {
			IAction emptyAction = new Action(WorkbenchMessages.NoApplicableWorkingSets) {
			};
			emptyAction.setEnabled(false);
			menuItems.add(emptyAction);
		}
		return menuItems;
	}

	private void fillMenu(IMenuManager menu) {
		for (Object object : getItems()) {
			if (object instanceof IAction) {
				menu.add((IAction) object);
			} else {
				IContributionItem item = (IContributionItem) object;
				menu.add(item);
			}
		}
	}

	@Override
	public void selectionChanged(IAction actionProxy, ISelection selection) {
		super.selectionChanged(actionProxy, selection);
		if (selection instanceof IStructuredSelection) {
			// ensure every item is of type IAdaptable and is NOT an IWorkingSet (minimal
			// fix for 157799)
			boolean minimallyOkay = true;
			for (Object selectedElement : (IStructuredSelection) getSelection()) {
				if (!(selectedElement instanceof IAdaptable) || selectedElement instanceof IWorkingSet) {
					minimallyOkay = false;
					break;
				}
			}
			actionProxy.setEnabled(minimallyOkay);

		} else
			actionProxy.setEnabled(false);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof String) {
			add = Boolean.parseBoolean((String) data);
		}
	}

	@Override
	public void init(IAction action) {
		this.actionProxy = action;
	}
}
