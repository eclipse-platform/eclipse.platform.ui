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
package org.eclipse.team.ui.synchronize.subscribers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Page that allows the user to select a set of resources that are managed by a subscriber 
 * participant. Callers can provide a scope hint to determine the initial selection for the
 * resource list. By default, the resources in the current selection are checked, otherwise
 * all resources are checked.
 * 
 * @see SubscriberRefreshWizard
 * @see ISynchronizeParticipant#createSynchronizeWizard()
 * @since 3.0
 */
public class GlobalRefreshResourceSelectionPage extends WizardPage {
	
	private SubscriberParticipant participant;
	
	// The scope hint for initial selection
	private int scopeHint;
	
	// Set of scope hint to determine the initial selection
	private Button participantScope;
	private Button selectedResourcesScope;
	private Button workingSetScope;
	private Button enclosingProjectsScope;
	private Button selectWorkingSetButton;
	
	// The checked tree viewer
	private ContainerCheckedTreeViewer fViewer;
	
	// Working set label and holder
	private Text workingSetLabel;
	private IWorkingSet workingSet;
	private List participantRoots;
	
	/**
	 * Content provider that accepts a <code>SubscriberParticipant</code> as input and
	 * returns the participants root resources.
	 */
	class MyContentProvider extends BaseWorkbenchContentProvider {
		public Object[] getChildren(Object element) {
			if(element instanceof SubscriberParticipant) {
				return ((SubscriberParticipant)element).getResources();
			}
			return super.getChildren(element);
		}
	}
	
	/**
	 * Label decorator that will display the full path for participant roots that are folders. This
	 * is useful for participants that have non-project roots.
	 */
	class MyLabelProvider extends LabelProvider {
		private LabelProvider workbenchProvider = new WorkbenchLabelProvider();
		public String getText(Object element) {
			if(element instanceof IContainer) {
				IContainer c = (IContainer)element;
				if(c.getType() != IResource.PROJECT && participantRoots.contains(c)) {
					return c.getFullPath().toString();
				}
			}
			return workbenchProvider.getText(element);
		}	
		public Image getImage(Object element) {
			return workbenchProvider.getImage(element);
		}
	}
		
	/**
	 * Create a new page for the given participant. The scope hint will determine the initial selection.
	 * 
	 * @param participant the participant to synchronize
	 * @param scopeHint a hint about the initial selection, can be one of:
	 * 	SubscriberRefreshWizard#SCOPE_WORKING_SET
	 * 	SubscriberRefreshWizard#SCOPE_SELECTED_RESOURCES 
	 * 	SubscriberRefreshWizard#SCOPE_ENCLOSING_PROJECT
	 *		SubscriberRefreshWizard#SCOPE_PARTICIPANT_ROOTS
	 */
	public GlobalRefreshResourceSelectionPage(SubscriberParticipant participant, int scopeHint) {
		super(Policy.bind("GlobalRefreshResourceSelectionPage.1")); //$NON-NLS-1$
		this.scopeHint = scopeHint;
		// Caching the roots so that the decorator doesn't have to recompute all the time.
		this.participantRoots = Arrays.asList(participant.getResources());
		setDescription(Policy.bind("GlobalRefreshResourceSelectionPage.2")); //$NON-NLS-1$
		setTitle(Policy.bind("GlobalRefreshResourceSelectionPage.3")); //$NON-NLS-1$
		this.participant = participant;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent2) {
		Composite top = new Composite(parent2, SWT.NULL);
		top.setLayout(new GridLayout());
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 50;
		top.setLayoutData(data);
		setControl(top);
		
		if (participant.getSubscriber().roots().length == 0) {
			Label l = new Label(top, SWT.NULL);
			l.setText(Policy.bind("GlobalRefreshResourceSelectionPage.4")); //$NON-NLS-1$
			setPageComplete(false);
		} else {
			Label l = new Label(top, SWT.NULL);
			l.setText(Policy.bind("GlobalRefreshResourceSelectionPage.5")); //$NON-NLS-1$
			
			// The viewer
			fViewer = new ContainerCheckedTreeViewer(top, SWT.BORDER);
			data = new GridData(GridData.FILL_HORIZONTAL);
			//data.widthHint = 200;
			data.heightHint = 100;
			fViewer.getControl().setLayoutData(data);
			fViewer.setContentProvider(new MyContentProvider());
			fViewer.setLabelProvider( new DecoratingLabelProvider(
					new MyLabelProvider(),
					PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
			fViewer.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					updateOKStatus();
				}
			});
			fViewer.setSorter(new ResourceSorter(ResourceSorter.NAME));
			fViewer.setInput(participant);
						
			// Scopes
			Group scopeGroup = new Group(top, SWT.NULL);
			scopeGroup.setText(Policy.bind("GlobalRefreshResourceSelectionPage.6")); //$NON-NLS-1$
			GridLayout layout = new GridLayout();
			layout.numColumns = 4;
			layout.makeColumnsEqualWidth = false;
			scopeGroup.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = 50;
			scopeGroup.setLayoutData(data);
			
			participantScope = new Button(scopeGroup, SWT.RADIO); 
			participantScope.setText(Policy.bind("GlobalRefreshResourceSelectionPage.7")); //$NON-NLS-1$
			participantScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateParticipantScope();
				}
			});
			
			selectedResourcesScope = new Button(scopeGroup, SWT.RADIO); 
			selectedResourcesScope.setText(Policy.bind("GlobalRefreshResourceSelectionPage.8")); //$NON-NLS-1$
			selectedResourcesScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateSelectedResourcesScope();
				}
			});
			
			enclosingProjectsScope = new Button(scopeGroup, SWT.RADIO); 
			enclosingProjectsScope.setText(Policy.bind("GlobalRefreshResourceSelectionPage.9")); //$NON-NLS-1$
			enclosingProjectsScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateEnclosingProjectScope();
				}
			});
			data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			data.horizontalIndent = 15;
			data.horizontalSpan = 2;
			enclosingProjectsScope.setLayoutData(data);
			
			workingSetScope = new Button(scopeGroup, SWT.RADIO); 
			workingSetScope.setText(Policy.bind("GlobalRefreshResourceSelectionPage.10")); //$NON-NLS-1$
			workingSetScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(workingSetScope.getSelection()) {
						updateWorkingSetScope();
					}
				}
			});
			
			workingSetLabel = new Text(scopeGroup, SWT.BORDER);
			workingSetLabel.setEditable(false);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			workingSetLabel.setLayoutData(data);
			
			Button selectWorkingSetButton = new Button(scopeGroup, SWT.NULL);
			selectWorkingSetButton.setText(Policy.bind("GlobalRefreshResourceSelectionPage.11")); //$NON-NLS-1$
			selectWorkingSetButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					selectWorkingSetAction();
				}			
			});
			data = new GridData(GridData.HORIZONTAL_ALIGN_END);
			selectWorkingSetButton.setLayoutData(data);
			Dialog.applyDialogFont(selectWorkingSetButton);
			
			workingSet = participant.getWorkingSet();
			updateWorkingSetLabel();
			initializeScopingHint();
		}
		Dialog.applyDialogFont(top);
	}
	
	/**
	 * Allow the finish button to be pressed if there are checked resources.
	 *
	 */
	protected void updateOKStatus() {	
		if(fViewer != null) {
			setPageComplete(areAnyElementsChecked() != null);
		} else {
			setPageComplete(false);
		}
	}
	
	/**
	 * Returns <code>true</code> if any of the root resources are grayed.
	 */
	private IResource areAnyElementsChecked() {
		TreeItem[] item = fViewer.getTree().getItems();
		List checked = new ArrayList();
		for (int i = 0; i < item.length; i++) {
			TreeItem child = item[i];
			if(child.getChecked() || child.getGrayed()) {
				return (IResource)child.getData();
			}
		}
		return null;
	}
	
	/**
	 * Return the list of top-most resources that have been checked.
	 * 
	 * @return  the list of top-most resources that have been checked or an
	 * empty list if nothing is selected.
	 */
	public IResource[] getCheckedResources() {
		TreeItem[] item = fViewer.getTree().getItems();
		List checked = new ArrayList();
		for (int i = 0; i < item.length; i++) {
			TreeItem child = item[i];
			collectCheckedItems(child, checked);
		}
		return (IResource[]) checked.toArray(new IResource[checked.size()]);
	}
	
	private void initializeScopingHint() {
		switch(scopeHint) {
			case SubscriberRefreshWizard.SCOPE_PARTICIPANT_ROOTS:
				participantScope.setSelection(true); 
				updateParticipantScope();
				break;
			case SubscriberRefreshWizard.SCOPE_WORKING_SET:
				workingSetScope.setSelection(true); 
				updateWorkingSetScope();
				break;
			default:
				if(workingSet != null) {
					workingSetScope.setSelection(true);
					updateWorkingSetScope();
				} else if(getResourcesFromSelection().length == 0) {
					participantScope.setSelection(true);
					updateParticipantScope();
				} else {
					selectedResourcesScope.setSelection(true);
					updateSelectedResourcesScope();
				}
		}
	}
	
	private void intializeSelectionInViewer(IResource[] resources) {
		if(resources.length > 0) {
//			fViewer.setExpandedElements(resources);
			fViewer.setSelection(new StructuredSelection(Arrays.asList(resources)), true);
		}
	}
	
	private void updateEnclosingProjectScope() {
		if(enclosingProjectsScope.getSelection()) {
			IResource[] selectedResources = getCheckedResources();
			List projects = new ArrayList();
			for (int i = 0; i < selectedResources.length; i++) {
				projects.add(selectedResources[i].getProject());
			}
			fViewer.setCheckedElements(projects.toArray());
			setPageComplete(projects.size() > 0);
		}
	}
	
	private void updateParticipantScope() {
		if(participantScope.getSelection()) {
			fViewer.setCheckedElements(participant.getSubscriber().roots());
			setPageComplete(true);
		}
	}
	
	private void updateSelectedResourcesScope() {
		if(selectedResourcesScope.getSelection()) {
			IResource[] resources = getResourcesFromSelection();
			fViewer.setCheckedElements(resources);
			setPageComplete(resources.length > 0);
			intializeSelectionInViewer(resources);
		}
	}
	
	private void selectWorkingSetAction() {
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog = manager.createWorkingSetSelectionDialog(getShell(), false);
		dialog.open();
		IWorkingSet[] sets = dialog.getSelection();
		if(sets != null) {
			workingSet = sets[0];
		} else {
			// dialog cancelled
			return;
		}
		updateWorkingSetScope();
		updateWorkingSetLabel();
		
		participantScope.setSelection(false);
		enclosingProjectsScope.setSelection(false);
		selectedResourcesScope.setSelection(false);
		workingSetScope.setSelection(true);
	}
	
	private void updateWorkingSetScope() {
		if(workingSet != null) {
				List resources = IDE.computeSelectedResources(new StructuredSelection(workingSet.getElements()));
				if(! resources.isEmpty()) {
					IResource[] resources2 = (IResource[])resources.toArray(new IResource[resources.size()]);
					fViewer.setCheckedElements(resources2);
					intializeSelectionInViewer(resources2);
					setPageComplete(true);
				}
		} else {
			fViewer.setCheckedElements(new Object[0]);
			setPageComplete(false);
		}
	}
	
	private IResource[] getResourcesFromSelection() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPart activePart = activeWorkbenchWindow.getPartService().getActivePart();
			if (activePart != null) {
				ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
				if (selectionProvider != null) {
					ISelection selection = selectionProvider.getSelection();
					if(selection instanceof IStructuredSelection) {
						return Utils.getResources(((IStructuredSelection)selection).toArray());
					}
				}
			}
		}
		return new IResource[0];
	}
	
	private void collectCheckedItems(TreeItem item, List checked) {
		if(item.getChecked() && !item.getGrayed()) {
			checked.add(item.getData());
		} else if(item.getGrayed()) {
			TreeItem[] children = item.getItems();
			for (int i = 0; i < children.length; i++) {
				TreeItem child = children[i];
				collectCheckedItems(child, checked);
			}
		}
	}
	
	private void updateWorkingSetLabel() {
		if (workingSet == null) {
			workingSetLabel.setText(Policy.bind("StatisticsPanel.noWorkingSet")); //$NON-NLS-1$
		} else {
			workingSetLabel.setText(workingSet.getName());
		}
	}
}
