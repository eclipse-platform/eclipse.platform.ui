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
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainerFactoryManager;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.internal.dialogs.ViewLabelProvider;

/**
 * Dialog which presents available breakpoint groupings to
 * the user and allows them to specify which they'd like
 * to use and in what order they should be applied.
 */
public class GroupBreakpointsByDialog extends Dialog {
	
	private BreakpointsView fView;
	
	// Table viewer that presents available containers
	private TableViewer fAvailableViewer;
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
	protected GroupBreakpointsByDialog(BreakpointsView view) {
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
		composite.setLayout(new GridLayout());
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= 400;
		data.heightHint= 400;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		initializeContent();
		
		createAvailableViewer(composite, labelProvider);
		createSelectedViewer(composite, labelProvider);
		
		updateViewers();
		Dialog.applyDialogFont(parentComposite);
		return parentComposite;
	}
	
	/**
	 * 
	 */
	private void initializeContent() {
		IBreakpointContainerFactory[] factories = BreakpointContainerFactoryManager.getDefault().getFactories();
		for (int i = 0; i < factories.length; i++) {
			fAvailableContainersProvider.addAvailable(factories[i]);
		}
		List activeFactories = fView.getBreakpointContainerFactories();
		Iterator iter = activeFactories.iterator();
		while (iter.hasNext()) {
			fSelectedContainersProvider.addSelected((IBreakpointContainerFactory) iter.next());
		}
	}

	/**
	 * @param parent
	 * @param labelProvider
	 * @param composite
	 */
	private void createSelectedViewer(Composite parent, ILabelProvider labelProvider) {
		Label label= new Label(parent, SWT.NONE);
		label.setText("Displayed Containers:");
		label.setLayoutData(new GridData());

		Composite selectedComposite= new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		layout.numColumns= 2;
		selectedComposite.setLayout(layout);
		selectedComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		selectedComposite.setFont(parent.getFont());
		
		fSelectedViewer= new TreeViewer(selectedComposite);
		fSelectedViewer.setContentProvider(fSelectedContainersProvider);
		fSelectedViewer.setLabelProvider(labelProvider);
		fSelectedViewer.setInput(new Object());
		Tree tree = fSelectedViewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(parent.getFont());
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
		
		Composite buttonComposite= new Composite(selectedComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
		buttonComposite.setLayoutData(new GridData());
		
		fRemoveButton= SWTUtil.createPushButton(buttonComposite, "&Remove", null);
		fRemoveButton.addSelectionListener(fSelectionListener);
		
		fMoveUpButton= SWTUtil.createPushButton(buttonComposite, "Move &Up", null);
		fMoveUpButton.addSelectionListener(fSelectionListener);
		
		fMoveDownButton= SWTUtil.createPushButton(buttonComposite, "Move &Down", null);
		fMoveDownButton.addSelectionListener(fSelectionListener);
	}

	/**
	 * @param parent
	 * @param labelProvider
	 * @param composite
	 */
	private void createAvailableViewer(Composite parent, ILabelProvider labelProvider) {
		Label label= new Label(parent, SWT.NONE);
		label.setText("Available Containers:");
		label.setLayoutData(new GridData());
		
		Composite availableComposite= new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		layout.numColumns= 2;
		availableComposite.setLayout(layout);
		availableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fAvailableViewer= new TableViewer(availableComposite);
		fAvailableViewer.setContentProvider(fAvailableContainersProvider);
		fAvailableViewer.setLabelProvider(labelProvider);
		fAvailableViewer.setInput(new Object());
		Table table = fAvailableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setFont(parent.getFont());
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
		
		Composite buttonComposite= new Composite(availableComposite, SWT.NONE);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData());
		
		fAddButton= SWTUtil.createPushButton(buttonComposite, "&Add", null);
		fAddButton.addSelectionListener(fSelectionListener);
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
		private HashMap fImageCache= new HashMap();
		
		public String getText(Object element) {
			if (element instanceof IBreakpointContainerFactory) {
				return ((IBreakpointContainerFactory) element).getLabel();
			}
			return super.getText(element);
		}
		public Image getImage(Object element) {
			if (element instanceof IBreakpointContainerFactory) {
				ImageDescriptor imageDescriptor = ((IBreakpointContainerFactory) element).getImageDescriptor();
				if (imageDescriptor != null) {
					Image image = (Image) fImageCache.get(imageDescriptor);
					if (image == null) {
						image= imageDescriptor.createImage();
						if (image != null) {
							fImageCache.put(imageDescriptor, image);
						}
					}
					return image;
				}
			}
			return super.getImage(element);
		}
		public void dispose() {
			Iterator imageIter = fImageCache.values().iterator();
			while (imageIter.hasNext()) {
				((Image) imageIter.next()).dispose();
			}
			super.dispose();
		}
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Group Breakpoints");
    }
}
