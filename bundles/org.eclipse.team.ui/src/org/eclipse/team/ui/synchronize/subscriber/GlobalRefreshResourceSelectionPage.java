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
package org.eclipse.team.ui.synchronize.subscriber;

import java.util.*;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Page that allows the user to select a set of resources that are managed by a synchronize 
 * participant.
 * 
 * @since 3.0
 */
public class GlobalRefreshResourceSelectionPage extends WizardPage {
	
	private SubscriberParticipant participant;
	private Button participantScope;
	private Button selectedResourcesScope;
	private Button workingSetScope;
	private Button enclosingProjectsScope;
	private Button selectWorkingSetButton;
	private ContainerCheckedTreeViewer fViewer;
	private Text workingSetLabel;
	private IWorkingSet workingSet;
	private int scopeHint;

	class MyContentProvider extends BaseWorkbenchContentProvider {
		public Object[] getChildren(Object element) {
			if(element instanceof SubscriberParticipant) {
				return ((SubscriberParticipant)element).getResources();
			}
			return super.getChildren(element);
		}
	}
	
	class MyLabelProvider extends LabelProvider {
		private LabelProvider workbenchProvider = new WorkbenchLabelProvider();
		public String getText(Object element) {
			if(element instanceof IContainer) {
				IContainer c = (IContainer)element;
				List participantRoots = Arrays.asList(participant.getResources());
				if(participantRoots.contains(c) && c.getType() != IResource.PROJECT) {
					return c.getFullPath().toString();
				}
			}
			return workbenchProvider.getText(element);
		}	
		public Image getImage(Object element) {
			return workbenchProvider.getImage(element);
		}
	}
		
	public GlobalRefreshResourceSelectionPage(SubscriberParticipant participant, int scopeHint) {
		super("Synchronize");
		this.scopeHint = scopeHint;
		setDescription("Select the resource to synchronize");
		setTitle("Synchronize");
		setParticipant(participant);
	}
	
	public void setParticipant(SubscriberParticipant participant) {
		this.participant = participant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent2) {
		Composite top = new Composite(parent2, SWT.NULL);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(top);
		
		if (participant.getSubscriber().roots().length == 0) {
			Label l = new Label(top, SWT.NULL);
			l.setText("There are no resources associated with '" + participant.getName() + "''.");
		} else {
			Label l = new Label(top, SWT.NULL);
			l.setText("Available resources to Synchronize:");
			fViewer = new ContainerCheckedTreeViewer(top, SWT.BORDER) {
				
				};
			GridData data = new GridData(GridData.FILL_BOTH);
			data.widthHint = 250;
			data.heightHint = 200;
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
			scopeGroup.setText("Scope");
			GridLayout layout = new GridLayout();
			layout.numColumns = 4;
			layout.makeColumnsEqualWidth = false;
			scopeGroup.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			scopeGroup.setLayoutData(data);
			
			participantScope = new Button(scopeGroup, SWT.RADIO); 
			participantScope.setText("W&orkspace");
			participantScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateParticipantScope();
				}
			});
			
			selectedResourcesScope = new Button(scopeGroup, SWT.RADIO); 
			selectedResourcesScope.setText("&Selected Resources");
			selectedResourcesScope.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateSelectedResourcesScope();
				}
			});
			
			enclosingProjectsScope = new Button(scopeGroup, SWT.RADIO); 
			enclosingProjectsScope.setText("&Enclosing Projects");
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
			workingSetScope.setText("&Working Set: ");
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
			selectWorkingSetButton.setText("&Choose...");
			selectWorkingSetButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					selectWorkingSetAction();
					workingSetScope.setSelection(true);
					updateWorkingSetScope();
				}

			
			});
			data = new GridData(GridData.HORIZONTAL_ALIGN_END);
			selectWorkingSetButton.setLayoutData(data);
			Dialog.applyDialogFont(selectWorkingSetButton);
			
			workingSet = participant.getWorkingSet();
			updateWorkingSetLabel();
			initializeScopingHint();
		}
		
		updateOKStatus();
		Dialog.applyDialogFont(top);
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
				if(getResourcesFromSelection().length == 0) {
					participantScope.setSelection(true);
					updateParticipantScope();
				} else {
					selectedResourcesScope.setSelection(true);
					updateSelectedResourcesScope();
				}
		}
	}
	
	private void updateEnclosingProjectScope() {
		if(enclosingProjectsScope.getSelection()) {
			IResource[] selectedResources = getSelectedResources();
			List projects = new ArrayList();
			for (int i = 0; i < selectedResources.length; i++) {
				projects.add(selectedResources[i].getProject());
			}
			fViewer.setCheckedElements(projects.toArray());
			updateOKStatus();
		}
	}
	
	private void updateParticipantScope() {
		if(participantScope.getSelection()) {
			fViewer.setCheckedElements(participant.getSubscriber().roots());
			updateOKStatus();
		}
	}
	
	private void updateSelectedResourcesScope() {
		if(selectedResourcesScope.getSelection()) {
			fViewer.setCheckedElements(getResourcesFromSelection());
			updateOKStatus();
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
			workingSet = null;
		}
		workingSetScope.setSelection(true);
		updateWorkingSetScope();
		updateWorkingSetLabel();
	}
	
	private void updateWorkingSetScope() {
		if(workingSet != null) {
				List resources = IDE.computeSelectedResources(new StructuredSelection(workingSet.getElements()));
				if(! resources.isEmpty()) {
					fViewer.setCheckedElements((IResource[])resources.toArray(new IResource[resources.size()]));
				}
		} else {
			fViewer.setCheckedElements(new Object[0]);
		}
		updateOKStatus();
	}
	
	private IResource[] getResourcesFromSelection() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart().getSite().getSelectionProvider().getSelection();
		if(selection instanceof IStructuredSelection) {
			return Utils.getResources(((IStructuredSelection)selection).toArray());
		}
		return new IResource[0];
	}
	
	protected void updateOKStatus() {	
		if(fViewer != null) {
			setPageComplete(fViewer.getCheckedElements().length > 0);
		} else {
			setPageComplete(true);
		}
	}
	
	/**
	 * Return the set of root resources selected.
	 */
	public IResource[] getSelectedResources() {
		if(fViewer != null) {
			// Checked elements are ordered top-down.
			// Note: n^2!!!
			List resources = IDE.computeSelectedResources(new StructuredSelection(fViewer.getCheckedElements()));
			Map rootResources = new HashMap();
			for (Iterator it = resources.iterator(); it.hasNext();) {
				IResource element = (IResource) it.next();
				if(! rootResources.containsKey(element.getProject())) {
					List roots = new ArrayList();
					roots.add(element);
					rootResources.put(element.getProject(), roots);
				} else { 
					List r = (List)rootResources.get(element.getProject());
					boolean toAdd = true;
					for (Iterator it2 = r.iterator(); it2.hasNext();) {
						IResource e = (IResource) it2.next();
						if(e.getFullPath().isPrefixOf(element.getFullPath()))
							toAdd = false;
							break;
					}
					if(toAdd) {
						r.add(element);
					}
				}
			}	
			return (IResource[]) rootResources.keySet().toArray(new IResource[rootResources.size()]);
		} else {
			return new IResource[0];
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
