/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.wizards;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.registry.SynchronizeWizardDescription;
import org.eclipse.team.internal.ui.synchronize.SynchronizeManager;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Page that allows the user to select a set of resources that are managed
 * by a synchronize participant.
 * 
 * Remembers last participant
 * 
 * @since 3.0
 */
public class GlobalRefreshWizardSelectionPage extends WizardPage implements IDoubleClickListener, ISelectionChangedListener {
    
    private final static String DEFAULT_SELECTION= TeamUIPlugin.ID + "GlobalRefreshWizardSelectionPage.default_selection";

	private TableViewer fViewer;
	private IWizard wizard;
	private List createdImages;

	class MyContentProvider extends BaseWorkbenchContentProvider {
		public Object[] getChildren(Object element) {
			if(element instanceof SynchronizeManager) {
				SynchronizeManager manager = (SynchronizeManager)element;
				return manager.getWizardDescriptors();
			}
			return super.getChildren(element);
		}
	}
	
	class MyLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if(element instanceof SynchronizeWizardDescription) {
				SynchronizeWizardDescription descriptor = (SynchronizeWizardDescription)element;
				return descriptor.getName();
			}
			return null;
		}	
		
		public Image getImage(Object element) {
			if(element instanceof SynchronizeWizardDescription) {
				SynchronizeWizardDescription descriptor = (SynchronizeWizardDescription)element;
				ImageDescriptor d = descriptor.getImageDescriptor();
				if(createdImages == null) {
					createdImages = new ArrayList(3);
				}
				Image image = d.createImage();
				createdImages.add(image);
				return image;
			}
			return null;
		}
	}
		
	public GlobalRefreshWizardSelectionPage() {
		super(Policy.bind("GlobalRefreshParticipantSelectionPage.0")); //$NON-NLS-1$
		setDescription(Policy.bind("GlobalRefreshParticipantSelectionPage.1")); //$NON-NLS-1$
		setTitle(Policy.bind("GlobalRefreshParticipantSelectionPage.2")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if (createdImages != null) {
			for (Iterator it = createdImages.iterator(); it.hasNext();) {
				Image image = (Image) it.next();
				image.dispose();
			}
		}
	}
	
	/**
     * Save the page settings into the dialog settings
     */
    public void savePageSettings() {
        if (fViewer.getControl().isDisposed()) 
	        return;
	    
	    final IStructuredSelection selection= (IStructuredSelection)fViewer.getSelection();
	    final Object selected= selection.getFirstElement();
	    if (!(selected instanceof SynchronizeWizardDescription))
	        return;
	    getDialogSettings().put(DEFAULT_SELECTION, ((SynchronizeWizardDescription)selected).getId());
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent2) {
		Composite top = new Composite(parent2, SWT.NULL);
		top.setLayout(new GridLayout());
		setControl(top);
		
		Label l = new Label(top, SWT.NULL);
		l.setText(Policy.bind("GlobalRefreshParticipantSelectionPage.3")); //$NON-NLS-1$
		fViewer = new TableViewer(top, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		fViewer.getControl().setLayoutData(data);
		fViewer.setContentProvider(new MyContentProvider());
		fViewer.addDoubleClickListener(this);
		fViewer.setLabelProvider(new MyLabelProvider());
		fViewer.setSorter(new ResourceSorter(ResourceSorter.NAME));
		fViewer.setInput(TeamUI.getSynchronizeManager());
		fViewer.addSelectionChangedListener(this);
		
		final SynchronizeWizardDescription selected= getDefaultSelection();
		if (selected != null) {
		    fViewer.setSelection(new StructuredSelection(selected)); 
		} else {
		    final Object object= fViewer.getElementAt(0);
		    if (object != null)
		        fViewer.setSelection(new StructuredSelection(object));
		}
		fViewer.getTable().setFocus();
		Dialog.applyDialogFont(parent2);
	}
	
	private SynchronizeWizardDescription getDefaultSelection() {
	    
        if (!(TeamUI.getSynchronizeManager() instanceof SynchronizeManager))
            return null;

        final String defaultSelection= getDialogSettings().get(DEFAULT_SELECTION);
        if (defaultSelection == null) 
            return null;
        
        final SynchronizeManager syncManager= (SynchronizeManager)TeamUI.getSynchronizeManager(); 
        final SynchronizeWizardDescription [] wizards= syncManager.getWizardDescriptors();
        for (int i = 0; i < wizards.length; i++) {
            if (defaultSelection.equals(wizards[i].getId())) {
                return wizards[i];
            }
        }
        return null;
    }

    public void doubleClick(DoubleClickEvent event) {
		selectionChanged(
			new SelectionChangedEvent(
				event.getViewer(),
				event.getViewer().getSelection()));
		getContainer().showPage(getNextPage());
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		// Initialize the wizard so we can tell whether to enable the
		// Next button
		ISelection selection = event.getSelection();
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			wizard = null;
			setPageComplete(false);
			return;
		}
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (ss.size() != 1) {
			wizard = null;
			setPageComplete(false);
			return;
		}
		SynchronizeWizardDescription selectedDescriptor = (SynchronizeWizardDescription)ss.getFirstElement();
		try {
			wizard = selectedDescriptor.createWizard();
			wizard.addPages();		
			// Ask the container to update button enablement
			setPageComplete(true);
			setDescription(selectedDescriptor.getDescription());
		} catch (CoreException e) {
			Utils.handle(e);
			setPageComplete(false);
		}
	}
	
	public IWizard getSelectedWizard() {
		return this.wizard;
	}
	
	public IWizardPage getNextPage() {
		if (wizard == null) return null;
		return wizard.getStartingPage();
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fViewer.getTable().setFocus();
		}
	}
}
