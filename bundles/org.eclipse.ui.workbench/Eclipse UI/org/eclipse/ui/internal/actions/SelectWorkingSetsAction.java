/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.AbstractWorkingSetDialog;
import org.eclipse.ui.internal.dialogs.WorkingSetFilter;
import org.eclipse.ui.internal.dialogs.WorkingSetLabelProvider;

/**
 * Action to select the visible working sets for a given workbench page.
 * 
 * @since 3.2
 */
public class SelectWorkingSetsAction extends AbstractWorkingSetPulldownDelegate  {

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
			super(set.getLabel(), IAction.AS_CHECK_BOX);
			setImageDescriptor(set.getImageDescriptor());
			this.set = set;
			setChecked(isWorkingSetEnabled(set));
		}

		public void runWithEvent(Event event) {
			
			Set newList = new HashSet(Arrays.asList(getWindow().getActivePage()
					.getWorkingSets()));

			if (isChecked()) {
				// if the primary modifier key is down then clear the list
				// first. this makes the selection exclusive rather than
				// additive.
				boolean modified = (event.stateMask & KeyLookupFactory
						.getDefault().formalModifierLookup(IKeyLookup.M1_NAME)) != 0;
				
				if (modified) 
					newList.clear();
				newList.add(set);
			} else {
				newList.remove(set);
			}

			getWindow().getActivePage().setWorkingSets(
					(IWorkingSet[]) newList.toArray(new IWorkingSet[newList
							.size()]));
		}
	}

	protected void fillMenu(Menu menu) {
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
		return getWindow().getActivePage().getWorkingSets();
	}

	private boolean isWorkingSetEnabled(IWorkingSet set) {
		IWorkingSet[] enabledSets = getEnabledSets();
		for (int i = 0; i < enabledSets.length; i++) {
			if (enabledSets[i].equals(set)) {
				return true;
			}
		}
		return false;
	}

	public void run(IAction action) {
		ConfigureWindowWorkingSetsDialog dialog = new ConfigureWindowWorkingSetsDialog(
				getWindow());
		if (dialog.open() == Window.OK) {

		}

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
		initializeDialogUnits(parent);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Composite viewerComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		viewerComposite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH + 300;  // fudge?  I like fudge.
		viewerComposite.setLayoutData(data);
		
		viewer = CheckboxTableViewer.newCheckList(viewerComposite, SWT.BORDER);
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

		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;

		viewer.getControl().setLayoutData(data);
		addModifyButtons(viewerComposite);
		
		addSelectionButtons(composite);
		
		availableWorkingSetsChanged();
		
		Dialog.applyDialogFont(composite);
		
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
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		}
		return null;
	}

	protected void availableWorkingSetsChanged() {
		viewer.setInput(window.getWorkbench().getWorkingSetManager()
				.getWorkingSets());
		super.availableWorkingSetsChanged();
	}
	
    /**
     * Called when the selection has changed.
     */
    void handleSelectionChanged() {
        updateButtonAvailability();
    }
    
    protected void configureShell(Shell shell) {
    		super.configureShell(shell);
    }

	protected void selectAllSets() {
		viewer.setCheckedElements(window.getWorkbench().getWorkingSetManager()
				.getWorkingSets());
		updateButtonAvailability();
	}

	protected void deselectAllSets() {
		viewer.setCheckedElements(new Object[0]);
		updateButtonAvailability();
	}
}
