/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 *    Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *       activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ICapabilityInstallWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.Category;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.registry.Capability;
import org.eclipse.ui.internal.ide.registry.CapabilityRegistry;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ProjectCapabilitySimpleSelectionPage extends WizardPage {
    private IWorkbench workbench;

    private IStructuredSelection selection;

    private IProject project;

    private TreeViewer viewer;

    private CapabilityRegistry reg;

    private Capability chosenCapability;

    private HashMap mapCapToWizard = new HashMap();

    /**
     * Creates an instance of this page.
     */
    public ProjectCapabilitySimpleSelectionPage(String pageName,
            IWorkbench workbench, IStructuredSelection selection,
            IProject project) {
        super(pageName);
        this.workbench = workbench;
        this.selection = selection;
        this.project = project;
        this.reg = IDEWorkbenchPlugin.getDefault().getCapabilityRegistry();
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage
     */
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /* (non-Javadoc)
     * Method declared on IDialogPage
     */
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        Composite topContainer = new Composite(parent, SWT.NONE);
        topContainer.setLayout(new GridLayout());
        topContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(topContainer, SWT.LEFT);
        label.setText(IDEWorkbenchMessages.ProjectCapabilitySelectionGroup_capabilities);
        GridData data = new GridData();
        data.verticalAlignment = SWT.TOP;
        label.setLayoutData(data);
        label.setFont(font);

        viewer = new TreeViewer(topContainer, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.getTree().setFont(font);
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setContentProvider(getContentProvider());
        viewer.setInput(reg);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                chosenCapability = null;
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) event
                            .getSelection();
                    if (sel != null && !sel.isEmpty()) {
                        Object result = sel.getFirstElement();
                        if (result instanceof Capability) {
                            chosenCapability = (Capability) result;
                        }
                    }
                }

                setPageComplete(validateChosenCapability());
            }
        });

        setControl(topContainer);
    }

    /**
     * Returns the content provider for the viewer
     */
    private IContentProvider getContentProvider() {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof CapabilityRegistry) {
                    ArrayList cats = reg.getUsedCategories();
                    if (reg.getMiscCategory() != null)
                        cats.add(reg.getMiscCategory());
                    return cats.toArray();
                }
                if (parentElement instanceof Category)
                    return ((Category) parentElement)
                            .getChildren(parentElement);
                return null;
            }

            public boolean hasChildren(Object element) {
                if (element instanceof CapabilityRegistry)
                    return true;
                if (element instanceof Category)
                    return ((Category) element).hasElements();
                return false;
            }
        };
    }

    /* (non-Javadoc)
     * Method declared on IWizardPage.
     */
    public IWizardPage getNextPage() {
        if (chosenCapability == null)
            return null;

        final IWizard[] wizard = new IWizard[1];
        wizard[0] = (IWizard) mapCapToWizard.get(chosenCapability);
        if (wizard[0] == null) {
            BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
                public void run() {
                    ICapabilityInstallWizard wiz;
                    wiz = chosenCapability.getInstallWizard();
                    if (wiz != null) {
                        wiz.init(workbench, selection, project);
                        wiz.addPages();
                        mapCapToWizard.put(chosenCapability, wiz);
                        wizard[0] = wiz;
                    }
                }
            });
        }

        if (wizard[0] == null)
            return null;

        IWizardPage page = wizard[0].getStartingPage();
        wizard[0] = null;
        return page;
    }

    private boolean validateChosenCapability() {
        if (chosenCapability == null) {
            setErrorMessage(null);
            return false;
        }

        Capability[] caps = reg.getProjectCapabilities(project);
        List existingCaps = Arrays.asList(caps);
        if (existingCaps.contains(chosenCapability)) {
            setErrorMessage(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_capabilityExist);
            return false;
        }

        String[] ids = reg.getPrerequisiteIds(chosenCapability);
        Capability[] prereqs = reg.findCapabilities(ids);
        for (int i = 0; i < prereqs.length; i++) {
            if (prereqs[i] == null) {
                setErrorMessage(NLS.bind(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_capabilityMissing, ids[i]));
                return false;
            }
            if (!existingCaps.contains(prereqs[i])) {
                setErrorMessage(NLS.bind(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_capabilityRequired, prereqs[i].getName()));
                return false;
            }
        }

        ids = reg.getMembershipSetIds(chosenCapability);
        List idsList = Arrays.asList(ids);
        for (int i = 0; i < caps.length; i++) {
            String[] setIds = reg.getMembershipSetIds(caps[i]);
            for (int j = 0; j < setIds.length; j++) {
                if (idsList.contains(setIds[j])) {
                    setErrorMessage(NLS.bind(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_capabilitySet, caps[i].getName()));
                    return false;
                }
            }
        }

        Capability[] newCaps = new Capability[caps.length + 1];
        System.arraycopy(caps, 0, newCaps, 0, caps.length);
        newCaps[caps.length] = chosenCapability;
        IStatus status = reg.validateCapabilities(newCaps);
        if (!status.isOK()) {
            setErrorMessage(status.getMessage());
            return false;
        }

        setErrorMessage(null);
        return true;
    }
}
