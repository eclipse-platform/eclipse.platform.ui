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
package org.eclipse.team.internal.ui.wizards;

import java.util.Arrays;
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
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Page that allows the user to select a set of resources that are managed
 * by a synchronize participant.
 * 
 * Remembers last selection
 * Remembers last working set
 * 
 * @since 3.0
 */
public class GlobalRefreshResourceSelectionPage extends WizardPage {

	private SubscriberParticipant participant;
	private Button selectOutgoingChanges;
	private ContainerCheckedTreeViewer fViewer;

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
				if(participantRoots.contains(c)) {
					return c.getFullPath().toString();
				}
			}
			return workbenchProvider.getText(element);
		}	
		public Image getImage(Object element) {
			return workbenchProvider.getImage(element);
		}
	}
	
	class MyContainerCheckedTree extends ContainerCheckedTreeViewer {

		public MyContainerCheckedTree(Composite parent, int style) {
			super(parent, style);
		}

		protected void doCheckStateChanged(Object element) {
			Widget item = findItem(element);
			if (item instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) item;
				treeItem.setGrayed(false);
				//updateChildrenItems(treeItem);
				doUpdateParentItems(treeItem.getParentItem());
			}
		}

		/**
		 * Updates the check / gray state of all parent items
		 */
		private void doUpdateParentItems(TreeItem item) {
			if (item != null) {
				Item[] children = getChildren(item);
				boolean containsChecked = false;
				boolean containsUnchecked = false;
				for (int i = 0; i < children.length; i++) {
					TreeItem curr = (TreeItem) children[i];
					containsChecked |= curr.getChecked();
					containsUnchecked |= (!curr.getChecked() || curr.getGrayed());
				}
				item.setChecked(containsChecked);
				item.setGrayed(containsChecked && containsUnchecked);
				doUpdateParentItems(item.getParentItem());
			}
		}
			
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.dialogs.ContainerCheckedTreeViewer#setExpanded(org.eclipse.swt.widgets.Item, boolean)
		 */
		protected void setExpanded(Item item, boolean expand) {
			((TreeItem) item).setExpanded(expand);
		}
	}
		
	public GlobalRefreshResourceSelectionPage(SubscriberParticipant participant) {
		super("Synchronize");
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
			fViewer.getControl().setLayoutData(data);
			fViewer.setContentProvider(new MyContentProvider());
			fViewer.setLabelProvider(new MyLabelProvider());
			fViewer.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					updateOKStatus();
				}
			});
			fViewer.setSorter(new ResourceSorter(ResourceSorter.NAME));
			fViewer.setInput(participant);
			
			Composite buttonComposote = new Composite(top, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 3;
			layout.makeColumnsEqualWidth = true;
			buttonComposote.setLayout(layout);
			data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			buttonComposote.setLayoutData(data);
			
			Button selectAll = new Button(buttonComposote, SWT.NULL);
			selectAll.setText("&Select All");
			selectAll.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fViewer.setCheckedElements(participant.getResources());
					updateOKStatus();
				}
			});
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			selectAll.setLayoutData(data);
			Dialog.applyDialogFont(selectAll);
			
			Button deSelectAll = new Button(buttonComposote, SWT.NULL);
			deSelectAll.setText("&Deselect All");
			deSelectAll.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					fViewer.setCheckedElements(new Object[0]);
					updateOKStatus();
				}
			});
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			deSelectAll.setLayoutData(data);
			Dialog.applyDialogFont(deSelectAll);
			
			Button selectWorkingSetButton = new Button(buttonComposote, SWT.NULL);
			selectWorkingSetButton.setText("&Working Set...");
			selectWorkingSetButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
					IWorkingSetSelectionDialog dialog = manager.createWorkingSetSelectionDialog(getShell(), false);
					dialog.open();
					IWorkingSet[] workingSet = dialog.getSelection();
					if(workingSet != null) {
						for (int i = 0; i < workingSet.length; i++) {
							IWorkingSet set = workingSet[i];
							List resources = IDE.computeSelectedResources(new StructuredSelection(set.getElements()));
							if(! resources.isEmpty()) {
								fViewer.setCheckedElements((IResource[])resources.toArray(new IResource[resources.size()]));
							}
						}
						updateOKStatus();
					}
				}
			});
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			selectWorkingSetButton.setLayoutData(data);
			Dialog.applyDialogFont(selectWorkingSetButton);
		}
		updateOKStatus();
		Dialog.applyDialogFont(top);
	}

	protected void updateOKStatus() {	
		if(fViewer != null) {
			setPageComplete(fViewer.getCheckedElements().length > 0);
		} else {
			setPageComplete(true);
		}
	}
	
	public IResource[] getSelectedResources() {
		if(fViewer != null) {
			List resources = IDE.computeSelectedResources(new StructuredSelection(fViewer.getCheckedElements()));
			return (IResource[]) resources.toArray(new IResource[resources.size()]);
		} else {
			return null;
		}
	}
}
