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
package org.eclipse.ui.externaltools.internal.variable;


import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.group.IGroupDialogPage;

/**
 * The working set component allows the user to choose a working set from the
 * workspace
 */
public class WorkingSetComponent extends AbstractVariableComponent {

	private TableViewer viewer;
	/**
	 * Label provider that provides labels for working sets
	 */
	private static ILabelProvider labelProvider = new LabelProvider() {
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
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(PlatformUI.getWorkbench());
		GridData data = new GridData(GridData.FILL_BOTH);
		viewer.getTable().setLayoutData(data);
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
			getPage().setErrorMessage(ExternalToolsVariableMessages.getString("WorkingSetComponent.Must_Select")); //$NON-NLS-1$
		}
		setIsValid(isValid);
		getPage().updateValidState();
	}

}
