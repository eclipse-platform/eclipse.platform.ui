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
package org.eclipse.debug.ui.variables;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * The working set component allows the user to choose a working set from the
 * workspace
 */
public class WorkingSetComponent extends AbstractVariableComponent {

	private TableViewer viewer;
	private ILabelProvider labelProvider;
	
	/**
	 * Label provider that provides labels for working sets
	 */	
	private static class WorkingSetLabelProvider extends LabelProvider {
		private Map icons;

		public WorkingSetLabelProvider() {
			icons = new Hashtable(5);
		}
		
		public void dispose() {
			Iterator iterator = icons.values().iterator();
	
			while (iterator.hasNext()) {
				Image icon = (Image) iterator.next();
				icon.dispose();
			}
			super.dispose();
		}
		
		public Image getImage(Object object) {
			if(object instanceof IWorkingSet){
				IWorkingSet workingSet = (IWorkingSet) object; 
				ImageDescriptor imageDescriptor = workingSet.getImage();

				if (imageDescriptor == null) {
					return null;
				}
		
				Image icon = (Image) icons.get(imageDescriptor);
				if (icon == null) {
					icon = imageDescriptor.createImage();
					icons.put(imageDescriptor, icon);
				}
				return icon;
			}
			return super.getImage(object);
		}
		
		public String getText(Object element) {
			if (element instanceof IWorkingSet) {
				return ((IWorkingSet) element).getName();
			}
			return super.getText(element);
		}
	};
	
	/**
	 * Content provider that provides working sets.
	 */
	private static IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
		public Object[] getElements(Object inputElement) {
			return PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	};

	/**
	 * @see IVariableComponent#getControl()
	 */
	public Control getControl() {
		return mainGroup;
	}

	/**
	 * @see IVariableComponent#createContents(Composite, String, IGroupDialogPage)
	 */
	public void createContents(Composite parent, String varTag, IGroupDialogPage page) {
		super.createContents(parent, varTag, page); // Creates the main group and sets the page

		viewer = new TableViewer(mainGroup);
		labelProvider= new WorkingSetLabelProvider();
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(PlatformUI.getWorkbench());
		GridData data = new GridData(GridData.FILL_BOTH);
		Table table= viewer.getTable();
		table.setLayoutData(data);
		
		if (table.getItemCount() > 0) {
			table.setSelection(new TableItem[]{table.getItems()[0]});
		}
					
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
	}

	/**
	 * @see IVariableComponent#getVariableValue()
	 */
	public String getVariableValue() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Object element = selection.getFirstElement();
		if (element instanceof IWorkingSet) {
			return ((IWorkingSet) element).getName();
		}
		return null;
	}

	/**
	 * @see IVariableComponent#setVariableValue(String)
	 */
	public void setVariableValue(String varValue) {
		TableItem[] items = viewer.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			if (((IWorkingSet) items[i].getData()).getName().equals(varValue)) {
				viewer.setSelection(new StructuredSelection(items[i].getData()));
				break;
			}
		}
	}

	/**
	 * @see IVariableComponent#validate()
	 */
	public void validate() {
		boolean isValid= getVariableValue() != null;
		if (isValid) {
			getPage().setErrorMessage(null);
		} else {
			getPage().setErrorMessage(LaunchConfigurationsMessages.getString("WorkingSetComponent.A_specific_working_set_must_be_selected_from_the_list._1")); //$NON-NLS-1$
		}
		setIsValid(isValid);
		getPage().updateValidState();
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.variables.IVariableComponent#dispose()
	 */
	public void dispose() {
		labelProvider.dispose();
	}
}
