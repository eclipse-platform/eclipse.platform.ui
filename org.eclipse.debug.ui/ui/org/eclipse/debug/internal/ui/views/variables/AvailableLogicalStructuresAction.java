/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Drop down action that displays available logical structues for a selected
 * variable or expression.
 */
public class AvailableLogicalStructuresAction extends Action implements IMenuCreator, ISelectionChangedListener {
	
	private VariablesView fView;
	private Menu fMenu;
	private IValue fValue;
	private ILogicalStructureType[] fTypes;

	public AvailableLogicalStructuresAction(VariablesView view) {
		setView(view);
		setToolTipText(VariablesViewMessages.getString("AvailableLogicalStructuresAction.0")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_AVAILABLE_LOGICAL_STRUCTURES));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_AVAILABLE_LOGICAL_STRUCTURES));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_AVAILABLE_LOGICAL_STRUCTURES));
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.VARIABLES_SELECT_LOGICAL_STRUCTURE);
		setEnabled(false);
		getView().getSite().getSelectionProvider().addSelectionChangedListener(this);
		setMenuCreator(this);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
	}
	
	protected VariablesView getView() {
		return fView;
	}

	protected void setView(VariablesView view) {
		fView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
		getView().getSite().getSelectionProvider().removeSelectionChangedListener(this);
		fView= null;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		
		fMenu= new Menu(parent);
		ILogicalStructureType[] types = getTypes();
		boolean exist = false;
		Action firstAction = null;
		String firstKey = null;
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		if (types != null && types.length > 0) {
			for (int i = 0; i < types.length; i++) {
				Action action = new SelectLogicalStructureAction(getView(), types, i);
				String key = VariablesView.LOGICAL_STRUCTURE_TYPE_PREFIX + types[i].getId();
				if (i == 0) {
					firstAction = action;
					firstKey = key;
				}
				int value = store.getInt(key);
				exist = exist || value != 0;
				action.setChecked(value == 1);
				addActionToMenu(fMenu, action);
			}
		}
		if (!exist && firstAction != null) {
			firstAction.setChecked(true);
			store.setValue(firstKey, 1);
		}
		return fMenu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		setValue(null);
		setTypes(null);
		if (getView().isShowLogicalStructure()) {
			ISelection s = event.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				if (selection.size() == 1) {
					Object obj = selection.getFirstElement();
					IValue value = null;
					if (obj instanceof IVariable) {
						IVariable var = (IVariable) obj;
						try {
							value = var.getValue();
						} catch (DebugException e) {
						}
					} else if (obj instanceof IExpression) {
						IExpression expression = (IExpression)obj;
						value = expression.getValue();
					}
					if (value != null) {
						ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
						if (types.length > 0) {
							setTypes(types);
							setValue(value);
							setEnabled(true);
							return;
						}
					}
				}
			}
		}
		setEnabled(false);
	}
	
	protected ILogicalStructureType[] getTypes() {
		return fTypes;
	}
	
	private void setTypes(ILogicalStructureType[] types) {
		fTypes = types;
	}
	
	protected IValue getValue() {
		return fValue;
	}
	
	private void setValue(IValue value) {
		fValue = value;
	}

}
