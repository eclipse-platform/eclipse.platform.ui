/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal.search;

import java.util.*;

//import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.help.ui.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
//import org.eclipse.swt.events.*;
//import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

/**
 * Local Help participant in the federated search.
 */
public class EclipseOrgPage extends RootScopePage{
//    
//    // In 3.0 this class moved to org.eclipse.help.ide plug-in
//    // but user might have help working sets created in 2.1, with page ID from
//    // help.ui plug-in
//    public final static String PAGE_ID = HelpUIPlugin.PLUGIN_ID
//            + ".HelpWorkingSetPage"; //$NON-NLS-1$
    public final static String PAGE_TITLE = HelpUIResources
            .getString("WorkingSetPageTitle"); //$NON-NLS-1$
    public final static String PAGE_DESCRIPTION = HelpUIResources
            .getString("WorkingSetPageDescription"); //$NON-NLS-1$

    private Text workingSetName;
    CheckboxTreeViewer tree;
    private ITreeContentProvider treeContentProvider;
    private ILabelProvider elementLabelProvider;
    private boolean firstCheck;
    private WorkingSet workingSet;

    /**
     * Default constructor.
     */
    public EclipseOrgPage() {
        firstCheck = true;
    }

    protected Control createScopeContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        Label label = new Label(container, SWT.NULL);
        label.setText("Dummy content");
        return container;
    }
    
//    /* (non-Javadoc)
//     * @see org.eclipse.help.ui.RootScopePage#createScopeContents(org.eclipse.swt.widgets.Composite)
//     */
//    protected Control createScopeContents(Composite parent) {
//        Font font = parent.getFont();
//        initializeDialogUnits(parent);
//
//        Composite composite = new Composite(parent, SWT.NONE);
//        composite.setLayout(new GridLayout());
//        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
//        setControl(composite);
//
//        Label label = new Label(composite, SWT.WRAP);
//        label.setFont(font);
//        label.setText(HelpUIResources.getString("WorkingSetName")); //$NON-NLS-1$
//        GridData gd = new GridData(GridData.GRAB_HORIZONTAL
//                | GridData.HORIZONTAL_ALIGN_FILL
//                | GridData.VERTICAL_ALIGN_CENTER);
//        label.setLayoutData(gd);
//
//        workingSetName = new Text(composite, SWT.SINGLE | SWT.BORDER);
//        workingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
//                | GridData.HORIZONTAL_ALIGN_FILL));
//        workingSetName.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                validateInput();
//            }
//        });
//        workingSetName.setFocus();
//        workingSetName.setFont(font);
//
//        label = new Label(composite, SWT.WRAP);
//        label.setFont(font);
//        label.setText(HelpUIResources.getString("WorkingSetContent")); //$NON-NLS-1$
//        gd = new GridData(GridData.GRAB_HORIZONTAL
//                | GridData.HORIZONTAL_ALIGN_FILL
//                | GridData.VERTICAL_ALIGN_CENTER);
//        label.setLayoutData(gd);
//
//        tree = new CheckboxTreeViewer(composite, SWT.BORDER | SWT.H_SCROLL
//                | SWT.V_SCROLL);
//        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
//        gd.heightHint = convertHeightInCharsToPixels(15);
//        tree.getControl().setLayoutData(gd);
//        tree.getControl().setFont(font);
//
//        treeContentProvider = new HelpWorkingSetTreeContentProvider();
//        tree.setContentProvider(treeContentProvider);
//
//        elementLabelProvider = new HelpWorkingSetElementLabelProvider();
//        tree.setLabelProvider(elementLabelProvider);
//
//        tree.setUseHashlookup(true);
//
//        tree.setInput(BaseHelpSystem.getWorkingSetManager().getRoot());
//
//        tree.addCheckStateListener(new ICheckStateListener() {
//            public void checkStateChanged(CheckStateChangedEvent event) {
//                handleCheckStateChange(event);
//            }
//        });
//
//        tree.addTreeListener(new ITreeViewerListener() {
//            public void treeCollapsed(TreeExpansionEvent event) {
//            }
//            public void treeExpanded(TreeExpansionEvent event) {
//                final Object element = event.getElement();
//                if (tree.getGrayed(element) == false)
//                    BusyIndicator.showWhile(getShell().getDisplay(),
//                            new Runnable() {
//                                public void run() {
//                                    setSubtreeChecked(element, tree
//                                            .getChecked(element), false);
//                                }
//                            });
//            }
//        });
//
//        if (workingSet != null) {
//            workingSetName.setText(workingSet.getName());
//            // May need to reconcile working sets
////            HelpIdePlugin.getDefault().getWorkingSetSynchronizer()
////                    .addWorkingSet(workingSet);
//        }
//        initializeCheckedState();
//        validateInput();
//
//        // Set help for the page
//        //WorkbenchHelp.setHelp(tree, "help_workingset_page");    
//        
//        return composite;
//    }

    /* (non-Javadoc)
     * @see org.eclipse.help.ui.IScopeEditor#getScope()
     */
    public ISearchScope getScope() {
        String workingSetName = this.workingSetName.getText();
        ArrayList elements = new ArrayList(10);
        findCheckedElements(elements, tree.getInput());
        if (workingSet == null) {
            workingSet = new WorkingSet(
                    workingSetName,
                    (AdaptableHelpResource[]) elements
                            .toArray(new AdaptableHelpResource[elements.size()]));
        } else {
            workingSet.setName(workingSetName);
            workingSet.setElements((AdaptableHelpResource[]) elements
                    .toArray(new AdaptableHelpResource[elements.size()]));
        }
        return workingSet;
    }

//    /**
//     * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(org.eclipse.ui.IWorkingSet)
//     */
//    public void setSelection(IWorkingSet workingSet) {
//        Assert.isNotNull(workingSet, "Working set must not be null"); //$NON-NLS-1$
//        this.workingSet = workingSet;
//        if (getContainer() != null && getShell() != null
//                && workingSetName != null) {
//            firstCheck = false;
//            workingSetName.setText(workingSet.getName());
//            initializeCheckedState();
//            validateInput();
//        }
//    }

    void validateInput() {
        String errorMessage = null;
        String newText = workingSetName.getText();
        if (newText.equals(newText.trim()) == false)
            errorMessage = HelpUIResources.getString("WE030"); //$NON-NLS-1$
        if (newText.equals("")) { //$NON-NLS-1$
            if (firstCheck) {
                firstCheck = false;
                return;
            } else
                errorMessage = HelpUIResources.getString("WE031"); //$NON-NLS-1$
        }

        firstCheck = false;

        if (errorMessage == null
                && (workingSet == null || newText.equals(workingSet.getName()) == false)) {
            IWorkingSet[] workingSets = PlatformUI.getWorkbench()
                    .getWorkingSetManager().getWorkingSets();
            for (int i = 0; i < workingSets.length; i++) {
                if (newText.equals(workingSets[i].getName())) {
                    errorMessage = HelpUIResources.getString("WE032"); //$NON-NLS-1$
                }
            }
        }
        if (errorMessage == null && tree.getCheckedElements().length == 0)
            errorMessage = HelpUIResources.getString("WE033"); //$NON-NLS-1$
        
        setErrorMessage(errorMessage);
    }

    private void initializeCheckedState() {
        if (workingSet == null)
            return;

        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            public void run() {
                Object[] elements = workingSet.getElements();
                tree.setCheckedElements(elements);
                for (int i = 0; i < elements.length; i++) {
                    Object element = elements[i];
                    if (isExpandable(element))
                        setSubtreeChecked(element, true, true);
                    updateParentState(element, true);
                }
            }
        });
    }

    boolean isExpandable(Object element) {
        return treeContentProvider.hasChildren(element);
    }

    void updateParentState(Object child, boolean baseChildState) {
        if (child == null)
            return;

        Object parent = treeContentProvider.getParent(child);
        if (parent == null)
            return;

        boolean allSameState = true;
        Object[] children = null;
        children = treeContentProvider.getChildren(parent);

        for (int i = children.length - 1; i >= 0; i--) {
            if (tree.getChecked(children[i]) != baseChildState
                    || tree.getGrayed(children[i])) {
                allSameState = false;
                break;
            }
        }

        tree.setGrayed(parent, !allSameState);
        tree.setChecked(parent, !allSameState || baseChildState);

        updateParentState(parent, baseChildState);
    }

    void setSubtreeChecked(Object parent, boolean state,
            boolean checkExpandedState) {

        Object[] children = treeContentProvider.getChildren(parent);
        for (int i = children.length - 1; i >= 0; i--) {
            Object element = children[i];
            if (state) {
                tree.setChecked(element, true);
                tree.setGrayed(element, false);
            } else
                tree.setGrayChecked(element, false);
            if (isExpandable(element))
                setSubtreeChecked(element, state, checkExpandedState);
        }
    }

    private void findCheckedElements(java.util.List checkedResources,
            Object parent) {
        Object[] children = treeContentProvider.getChildren(parent);
        for (int i = 0; i < children.length; i++) {
            if (tree.getGrayed(children[i]))
                findCheckedElements(checkedResources, children[i]);
            else if (tree.getChecked(children[i]))
                checkedResources.add(children[i]);
        }
    }

    void handleCheckStateChange(final CheckStateChangedEvent event) {
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            public void run() {
                Object element = event.getElement();
                boolean state = event.getChecked();
                tree.setGrayed(element, false);
                if (isExpandable(element))
                    setSubtreeChecked(element, state, state);
                // only check subtree if state is set to true

                updateParentState(element, state);
                validateInput();
            }
        });
    }    
}
