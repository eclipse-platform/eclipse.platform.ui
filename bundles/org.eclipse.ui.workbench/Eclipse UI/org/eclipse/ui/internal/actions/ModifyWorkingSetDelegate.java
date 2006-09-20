/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.QuickMenuCreator;

/**
 * @since 3.3
 * 
 */
public class ModifyWorkingSetDelegate extends
		AbstractWorkingSetPulldownDelegate implements IExecutableExtension, IActionDelegate2 {

	private class ModifyAction extends Action {

		private IWorkingSet set;

		private Collection selectedElements;

		/**
		 * @param set
		 * @param selectedElements
		 * @param add
		 */
		private ModifyAction(IWorkingSet set, Collection selectedElements) {
			super(set.getLabel(), IAction.AS_CHECK_BOX);
			this.set = set;
			this.selectedElements = selectedElements;
			setImageDescriptor(set.getImageDescriptor());
		}

		public void run() {

			Collection oldElements = Arrays.asList(set.getElements());
			Set newElements = new HashSet(oldElements.size()
					+ selectedElements.size());
			newElements.addAll(oldElements);
			if (add) {
				newElements.addAll(selectedElements);
			} else {
				newElements.removeAll(selectedElements);
			}
			set.setElements((IAdaptable[]) newElements
					.toArray(new IAdaptable[newElements.size()]));
		}
	}
	
	private QuickMenuCreator contextMenuCreator = new QuickMenuCreator() {
		protected void fillMenu(IMenuManager menu) {
			ModifyWorkingSetDelegate.this.fillMenu(menu);
		}
	};

	private static final Object SEPERATORMARKER = new Object();

	private boolean add = true;

	private List menuItems = new ArrayList();

	private IPropertyChangeListener listener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			refreshEnablement();
		}

		/**
		 * 
		 */
		private void refreshEnablement() {
			selectionChanged(actionProxy, getSelection());
		}
	};

	private IAction actionProxy;

	/**
	 * 
	 */
	public ModifyWorkingSetDelegate() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		contextMenuCreator.createMenu();
	}
	 
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		if (event.type == SWT.KeyDown || event.type == SWT.KeyUp)
			run(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.actions.AbstractWorkingSetPulldownDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		super.init(window);
		getWindow().getWorkbench().getWorkingSetManager()
				.addPropertyChangeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.actions.AbstractWorkingSetPulldownDelegate#dispose()
	 */
	public void dispose() {
		getWindow().getWorkbench().getWorkingSetManager()
				.removePropertyChangeListener(listener);
		super.dispose();
		contextMenuCreator.dispose();
	}
	
	public void fillMenu(Menu menu) {
		for (int i = 0; i < menuItems.size(); i++) {
			Object object = menuItems.get(i);
			if (object instanceof IAction) {
				ActionContributionItem item = new ActionContributionItem((IAction) object);
				item.fill(menu, -1);
			} else {
				Separator item = new Separator();
				item.fill(menu, -1);
			}
		}
	}
	private void fillMenu(IMenuManager menu) {
		for (int i = 0; i < menuItems.size(); i++) {
			Object object = menuItems.get(i);
			if (object instanceof IAction) {
				menu.add((IAction) object);
			} else {
				menu.add(new Separator());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.actions.AbstractWorkingSetPulldownDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction actionProxy, ISelection selection) {
		super.selectionChanged(actionProxy, selection);
		menuItems.clear();
		if (selection instanceof IStructuredSelection) {
			Collection selectedElements = ((IStructuredSelection) getSelection())
			.toList();
			// ensure every item is of type IAdaptable and is NOT an IWorkingSet (minimal fix for 157799)
			boolean minimallyOkay = true;
			for (Iterator i = selectedElements.iterator(); i
					.hasNext();) {
				Object object = i.next();
				if (!(object instanceof IAdaptable) || object instanceof IWorkingSet) {
					minimallyOkay = false;
					break;
				}
			}
			// only do this work if the selection is adaptable
			if (minimallyOkay) {
				IWorkingSet[][] typedSets = splitSets();

				for (int i = 0; i < typedSets.length; i++) {
					// add a seperator only if the last item is not a seperator
					if (menuItems.size() > 0
							&& menuItems.get(menuItems.size() - 1) != SEPERATORMARKER)
						menuItems.add(SEPERATORMARKER);
					IWorkingSet[] sets = typedSets[i];
					for (int j = 0; j < sets.length; j++) {
						IWorkingSet set = sets[j];

						Set existingElements = new HashSet();
						existingElements.addAll(Arrays
								.asList(set.getElements()));

						boolean visible = false;
						for (Iterator k = selectedElements.iterator(); k
								.hasNext();) {
							IAdaptable object = (IAdaptable) k.next();
							if (add) {
								if (!existingElements.contains(object)) {
									visible = true; // show if any element is
									// not
									// present in addition
									break;
								}
							} else {
								if (existingElements.contains(object)) {
									visible = true; // show if any element is
									// present in
									// removal
									break;
								}
							}
						}
						if (visible) {
							ModifyAction action = new ModifyAction(set,
									selectedElements);
							menuItems.add(action);
						}
					}
				}
			}
		}
		actionProxy.setEnabled(!menuItems.isEmpty()); // enable the item if
		// there are children to
		// show
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		if (data instanceof String) {
			add = Boolean.valueOf((String) data).booleanValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		this.actionProxy = action;
	}
}
