/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search2.internal.ui.SearchMessages;

public class SelectSearchScopeDialog extends SelectionDialog {
    private final static int SIZING_SELECTION_WIDGET_WIDTH = 400;

    private final static int SIZING_SELECTION_WIDGET_HEIGHT = 300;

	private String fMessage;
	private IScopeDescription fScope;

	private CheckboxTreeViewer fTree;

	private IScopeDescription fInitialScope;

	protected SelectSearchScopeDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		setTitle(title);
		fMessage= message;
	}

    protected Control createDialogArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        Label label = new Label(composite, SWT.WRAP);
        label.setText(fMessage);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        label.setLayoutData(data);
        label.setFont(font);
        
        fTree = new CheckboxTreeViewer(composite);
        fTree.setUseHashlookup(true);
		final ITreeContentProvider treeContentProvider = new WorkbenchContentProvider();
        fTree.setContentProvider(treeContentProvider);
        final LabelProvider labelProvider= new WorkbenchLabelProvider();
        fTree.setLabelProvider(new DecoratingLabelProvider(labelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        fTree.setInput(ResourcesPlugin.getWorkspace().getRoot());
        fTree.setSorter(new RetrieverViewerSorter(labelProvider));

        data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        fTree.getControl().setLayoutData(data);
        fTree.getControl().setFont(font);

        fTree.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                handleCheckStateChange(event);
            }
        });

        fTree.addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                final Object element = event.getElement();
                if (fTree.getGrayed(element) == false)
                    BusyIndicator.showWhile(getShell().getDisplay(),
                            new Runnable() {
                                public void run() {
                                    setSubtreeChecked((IContainer) element,
                                            fTree.getChecked(element), false);
                                }
                            });
            }
        });

		// Add select / deselect all buttons for bug 46669
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(3, false));
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText(SearchMessages.SimpleResourceSelectionDialog_selectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				fTree.setGrayedElements(new Object[0]);
				fTree.setCheckedElements(treeContentProvider.getElements(fTree.getInput()));
				validateInput();
			}
		});
		selectAllButton.setFont(font);
		setButtonLayoutData(selectAllButton);

		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText(SearchMessages.SimpleResourceSelectionDialog_deselectAll);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				fTree.setGrayedElements(new Object[0]);
				fTree.setCheckedElements(new Object[0]);
				validateInput();
			}
		});
		deselectAllButton.setFont(font);
		setButtonLayoutData(deselectAllButton);
		
		Button selectWorkingSets= new Button(buttonComposite, SWT.PUSH);
		selectWorkingSets.setText(SearchMessages.SelectSearchScopeDialog_selectWorkingSets);
		selectWorkingSets.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				close();
				fScope= WorkingSetScopeDescription.createWithDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fInitialScope);
			}
		});

		initializeCheckedState();
		return composite;
    }
    
    /**
     * Called when the checked state of a tree item changes.
     * 
     * @param event the checked state change event.
     */
    private void handleCheckStateChange(final CheckStateChangedEvent event) {
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            public void run() {
                IResource resource = (IResource) event.getElement();
                boolean state = event.getChecked();

                fTree.setGrayed(resource, false);
                if (resource instanceof IContainer) {
                    setSubtreeChecked((IContainer) resource, state, true);
                }
                updateParentState(resource);
                validateInput();
            }
        });
    }

    /**
     * Sets the checked state of the container's members.
     * 
     * @param container the container whose children should be checked/unchecked
     * @param state true=check all members in the container. false=uncheck all 
     * 	members in the container.
     * @param checkExpandedState true=recurse into sub-containers and set the 
     * 	checked state. false=only set checked state of members of this container
     */
    private void setSubtreeChecked(IContainer container, boolean state,
            boolean checkExpandedState) {
        // checked state is set lazily on expand, don't set it if container is collapsed
        if (container.isAccessible() == false
                || (fTree.getExpandedState(container) == false && state && checkExpandedState)) {
            return;
        }
        IResource[] members = null;
        try {
            members = container.members();
        } catch (CoreException ex) {
        	SearchPlugin.log(ex.getStatus());
        }
        for (int i = members.length - 1; i >= 0; i--) {
            IResource element = members[i];
            boolean elementGrayChecked = fTree.getGrayed(element)
                    || fTree.getChecked(element);

            if (state) {
                fTree.setChecked(element, true);
                fTree.setGrayed(element, false);
            } else {
                fTree.setGrayChecked(element, false);
            }
            // unchecked state only needs to be set when the container is 
            // checked or grayed
            if (element instanceof IContainer && (state || elementGrayChecked)) {
                setSubtreeChecked((IContainer) element, state, true);
            }
        }
    }


    /**
     * Check and gray the resource parent if all resources of the 
     * parent are checked.
     * 
     * @param child the resource whose parent checked state should 
     * 	be set.
     */
    private void updateParentState(IResource child) {
        if (child == null || child.getParent() == null)
            return;

        IContainer parent = child.getParent();
        boolean childChecked = false;
        IResource[] members = null;
        try {
            members = parent.members();
        } catch (CoreException ex) {
        	SearchPlugin.log(ex.getStatus());
        }
        for (int i = members.length - 1; i >= 0; i--) {
            if (fTree.getChecked(members[i]) || fTree.getGrayed(members[i])) {
                childChecked = true;
                break;
            }
        }
        fTree.setGrayChecked(parent, childChecked);
        updateParentState(parent);
    }

    /**
     * Sets the checked state of tree items based on the initial 
     * working set, if any.
     */
    private void initializeCheckedState() {
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            public void run() {
            	List list= getInitialElementSelections();
                IResource[] items = (IResource[]) list.toArray(new IResource[list.size()]);
                fTree.setCheckedElements(items);
                for (int i = 0; i < items.length; i++) {
                    IResource resource = items[i];
                    IContainer container = null;

                    if (resource instanceof IContainer) {
                        container = (IContainer) resource;
                    } 
                    if (container != null) {
                        setSubtreeChecked(container, true, true);
                    }
                    if (resource != null && resource.isAccessible() == false) {
                        IProject project = resource.getProject();
                        if (fTree.getChecked(project) == false)
                            fTree.setGrayChecked(project, true);
                    } else {
                        updateParentState(resource);
                    }
                }
            }
        });
    }

	protected void validateInput() {
	}

	protected void okPressed() {
		Object[] checked= fTree.getCheckedElements();
		HashSet gray= new HashSet(Arrays.asList(fTree.getGrayedElements()));
		ArrayList result= new ArrayList();
		for (int i= 0; i < checked.length; i++) {
			Object object= checked[i];
			if (!gray.contains(object)) {
				result.add(object);
			}
		}
		setResult(result);
		super.okPressed();
	}

	public IScopeDescription getScopeDescription() {
		if (fScope == null) {
			Object[] selection= getResult();
			if (selection != null) {
				fScope= new SelectedResourcesScopeDescription((IResource[]) Arrays.asList(selection).toArray(new IResource[selection.length]), false);
			}
		}
		return fScope;
	}

	public void setInitialScope(IScopeDescription scope, IWorkbenchPage page) {
		fInitialScope= scope;
		setInitialSelections(scope.getRoots(page));
	}

}
