/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainerFactoryManager;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.dialogs.ViewLabelProvider;

/**
 * 
 */
public class ShowBreakpointsByDialog extends Dialog {
	
	private BreakpointsView fView;
	
	// List viewer that presents available containers
	private ListViewer fAvailableViewer;
	private AvailableContainersProvider fAvailableContainersProvider= new AvailableContainersProvider();
	
	// Tree viewer that presents selected containers
	private TreeViewer fSelectedViewer;
	private SelectedContainerProvider fSelectedContainersProvider= new SelectedContainerProvider();
	
	private List fResult= new ArrayList();

	private Button fAddButton;
	private Button fRemoveButton;
	private Button fMoveUpButton;
	private Button fMoveDownButton;
	
	private SelectionAdapter fSelectionListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Object source= e.getSource();
			if (source == fAddButton) {
				handleAddPressed();
			} else if (source == fRemoveButton) {
				handleRemovePressed();
			} else if (source == fMoveUpButton) {
				handleMoveUpPressed();
			} else if (source == fMoveDownButton) {
				handleMoveDownPressed();
			}
		}
	};

	/**
	 * @param parentShell
	 */
	protected ShowBreakpointsByDialog(BreakpointsView view) {
		super(view.getSite().getShell());
		fView= view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		ILabelProvider labelProvider= new BreakpointContainerFactoryLabelProvider();
		
		Composite parentComposite= (Composite) super.createDialogArea(parent);
		Composite composite= new Composite(parentComposite, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		composite.setLayout(layout);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= 400;
		data.heightHint= 400;
		composite.setLayoutData(data);
		
		IBreakpointContainerFactory[] factories = BreakpointContainerFactoryManager.getDefault().getFactories();
		for (int i = 0; i < factories.length; i++) {
			fAvailableContainersProvider.addAvailable(factories[i]);
		}
		List activeFactories = fView.getBreakpointContainerFactories();
		Iterator iter = activeFactories.iterator();
		while (iter.hasNext()) {
			fSelectedContainersProvider.addSelected((IBreakpointContainerFactory) iter.next());
		}
		
		Label label= new Label(composite, SWT.NONE);
		label.setText("Available Containers:");
		data= new GridData();
		data.horizontalSpan= 2;
		label.setLayoutData(data);
		
		fAvailableViewer= new ListViewer(composite);
		fAvailableViewer.setContentProvider(fAvailableContainersProvider);
		fAvailableViewer.setLabelProvider(labelProvider);
		fAvailableViewer.setInput(new Object());
		fAvailableViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		fAvailableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleAddPressed();
			}
		});
		fAvailableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateAddButton();
			}
		});
		
		fAddButton= new Button(composite, SWT.PUSH);
		fAddButton.setText("Add");
		fAddButton.addSelectionListener(fSelectionListener);
		
		label= new Label(composite, SWT.NONE);
		label.setText("Displayed Containers:");
		data= new GridData();
		data.horizontalSpan= 2;
		label.setLayoutData(data);
		
		fSelectedViewer= new TreeViewer(composite);
		fSelectedViewer.setContentProvider(fSelectedContainersProvider);
		fSelectedViewer.setLabelProvider(labelProvider);
		fSelectedViewer.setInput(new Object());
		fSelectedViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fSelectedViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                handleRemovePressed();
            }
        });
		fSelectedViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelectedButtons();
			}
		});
		
		Composite buttonComposite= new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
		buttonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fRemoveButton= new Button(buttonComposite, SWT.PUSH);
		fRemoveButton.setText("Remove");
		fRemoveButton.addSelectionListener(fSelectionListener);
		
		fMoveUpButton= new Button(buttonComposite, SWT.PUSH);
		fMoveUpButton.setText("Move Up");
		fMoveUpButton.addSelectionListener(fSelectionListener);
		
		fMoveDownButton= new Button(buttonComposite, SWT.PUSH);
		fMoveDownButton.setText("Move Down");
		fMoveDownButton.addSelectionListener(fSelectionListener);
		
		updateViewers();
		
		return parentComposite;
	}
	
	public List getSelectedContainers() {
		return fResult;
	}
	
	protected void okPressed() {
		Object[] factories= fSelectedContainersProvider.getElements(null);
		while (factories.length > 0) {
			Object factory= factories[0];
			fResult.add(factory);
			factories= fSelectedContainersProvider.getChildren(factory);
		}
		super.okPressed();
	}
	
	public void handleAddPressed() {
		IStructuredSelection selection= (IStructuredSelection) fAvailableViewer.getSelection();
		if (selection.size() < 1) {
			return;
		}
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			fSelectedContainersProvider.addSelected((IBreakpointContainerFactory) iter.next());
		}
		updateViewers();
	}
	
	public void handleRemovePressed() {
		IStructuredSelection selection= (IStructuredSelection) fSelectedViewer.getSelection();
		if (selection.size() < 1) {
			return;
		}
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			fAvailableContainersProvider.addAvailable((IBreakpointContainerFactory) iter.next());
		}
		updateViewers();
	}
	
	/**
	 * Moves each selected item up in the tree of selected containers
	 */
	public void handleMoveUpPressed() {
		IStructuredSelection selection = (IStructuredSelection) fSelectedViewer.getSelection();
		Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			fSelectedContainersProvider.moveUp(iter.next());
		}
		updateViewers();
	}
	
	/**
	 * Moves each selected item down in the tree of selected containers
	 */
	public void handleMoveDownPressed() {
		IStructuredSelection selection = (IStructuredSelection) fSelectedViewer.getSelection();
		Object[] elements= selection.toArray();
		for (int i= elements.length - 1; i >= 0; i--) {
			fSelectedContainersProvider.moveDown(elements[i]);
		}
		updateViewers();
	}
	
	public void updateViewers() {
		fAvailableViewer.refresh();
		fSelectedViewer.refresh();
		fSelectedViewer.expandAll();
		updateAddButton();
		updateSelectedButtons();
	}
	
	public void updateSelectedButtons() {
		updateRemoveButton();
		updateMoveUpButton();
		updateMoveDownButton();
	}
	
	public void updateAddButton() {
		fAddButton.setEnabled(fAvailableContainersProvider.getElements(null).length > 0);
	}
	
	public void updateRemoveButton() {
		fRemoveButton.setEnabled(fSelectedContainersProvider.getElements(null).length > 0);
	}
	
	public void updateMoveUpButton() {
		boolean enabled= true;
		IStructuredSelection selection = (IStructuredSelection) fSelectedViewer.getSelection();
		if (selection.size() == 0) {
			enabled= false;
		} else {
			Object firstSelected= selection.getFirstElement();
			Object parent= fSelectedContainersProvider.getParent(firstSelected);
			if (!(parent instanceof IBreakpointContainerFactory)) {
				enabled= false;
			}
		}
		fMoveUpButton.setEnabled(enabled);
	}
	
	public void updateMoveDownButton() {
		boolean enabled= true;
		IStructuredSelection selection = (IStructuredSelection) fSelectedViewer.getSelection();
		if (selection.size() == 0) {
			enabled= false;
		} else {
			Object lastSelected= selection.toList().get(selection.size() - 1);
			Object[] children= fSelectedContainersProvider.getChildren(lastSelected);
			if (children.length < 1) {
				enabled= false;
			}
		}
		fMoveDownButton.setEnabled(enabled);
	}
	
	private class AvailableContainersProvider implements IStructuredContentProvider {
		protected List availableFactories= new ArrayList();
		
		public void addAvailable(IBreakpointContainerFactory factory) {
			availableFactories.add(factory);
			fSelectedContainersProvider.selectedFactories.remove(factory);
		}
		
		public Object[] getElements(Object inputElement) {
			return availableFactories.toArray();
		}
		
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Content provider that returns the selected breakpoint container factories
	 * as a tree.
	 */
	private class SelectedContainerProvider implements ITreeContentProvider {
		protected List selectedFactories= new ArrayList();
		
		public void addSelected(IBreakpointContainerFactory factory) {
			selectedFactories.add(factory);
			fAvailableContainersProvider.availableFactories.remove(factory);
		}
		
		public void moveUp(Object object) {
			int index = selectedFactories.indexOf(object);
			if (index > 0) {
				selectedFactories.remove(object);
				selectedFactories.add(index - 1, object);
			}
		}
		
		public void moveDown(Object object) {
			int index = selectedFactories.indexOf(object);
			if (index < selectedFactories.size() - 1) {
				selectedFactories.remove(object);
				selectedFactories.add(index + 1, object);
			}
		}

		public Object[] getChildren(Object parentElement) {
			// A factory's "child" is the next factory in the list
			int index = selectedFactories.indexOf(parentElement);
			if (index < selectedFactories.size() - 1) {
				return new Object[] { selectedFactories.get(index + 1) };
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			// A factory's "parent" is the factory before it
			int index = selectedFactories.indexOf(element);
			if (index <= 0 || selectedFactories.size() <= 1) {
				return null;
			}
			return selectedFactories.get(index - 1);
		}

		public boolean hasChildren(Object element) {
			// A factory has "children" if there are more
			// factories after it.
			int index = selectedFactories.indexOf(element);
			return index != -1 && index < selectedFactories.size() - 1;
		}

		public Object[] getElements(Object inputElement) {
			if (selectedFactories.size() > 0) {
				return new Object[] { selectedFactories.get(0) };
			}
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class BreakpointContainerFactoryLabelProvider extends ViewLabelProvider {
		public String getText(Object element) {
			if (element instanceof IBreakpointContainerFactory) {
				return ((IBreakpointContainerFactory) element).getLabel();
			}
			return null;
		}
	}
}
