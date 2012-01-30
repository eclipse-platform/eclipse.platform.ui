/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;

import org.eclipse.ui.dialogs.TypeFilteringDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;


/**
 * Dialog for selecting resources.
 *
 * @since 3.1
 */
class SelectResourcesDialog extends Dialog {

	interface IFilter {
		boolean accept(IResource resource);
	}

	private SelectResourcesBlock fResourceGroup;
    private List fAcceptedFileTypes = new ArrayList();
    private IResource[] fInput;
    private String fTitle;
    private String fInstruction;
    private Label fCountIndication;
	private IFilter fAcceptableLocationsFilter;


	public SelectResourcesDialog(Shell parentShell, String title, String instruction, IFilter acceptableLocationsFilter) {
		super(parentShell);
		fTitle= title;
		fInstruction= instruction;
		fAcceptableLocationsFilter= acceptableLocationsFilter;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 * @since 3.4
	 */
	protected boolean isResizable() {
		return true;
	}

	public void setInput(IResource[] input) {
		fInput= input;
	}

	public IResource[] getSelectedResources() {
		List items= fResourceGroup.getAllCheckedListItems();
		return (IResource[]) items.toArray(new IResource[items.size()]);
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		if (fTitle != null)
			newShell.setText(fTitle);
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite) super.createDialogArea(parent);
		Label label= new Label(composite, SWT.LEFT);
		label.setText(fInstruction);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fResourceGroup= new SelectResourcesBlock(composite, ResourcesPlugin.getWorkspace().getRoot(), getResourceProvider(IResource.FOLDER | IResource.PROJECT), WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), getResourceProvider(IResource.FILE), WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), SWT.NONE, useHeightHint(parent));
		fResourceGroup.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateSelectionCount();
			}
		});

		fCountIndication= new Label(composite, SWT.LEFT);
		fCountIndication.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createSelectionButtonGroup(composite);

		setInitialSelection();
		return composite;
	}

	private boolean useHeightHint(Composite parent) {
		int fontHeight= (parent.getFont().getFontData())[0].getHeight();
		int displayHeight= parent.getDisplay().getClientArea().height;
		return (displayHeight / fontHeight) > 50;
	}

    private ITreeContentProvider getResourceProvider(final int resourceType) {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
            	if (o instanceof IWorkspaceRoot) {
            		HashSet projects= new HashSet();
            		for (int i= 0; i < fInput.length; i++) {
            			IResource project= fInput[i].getProject();
            			if ((project.getType() & resourceType) > 0)
            				projects.add(project);
            		}
            		return projects.toArray();
            	}

                if (o instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) o).members();
                    } catch (CoreException e) {
                        //just return an empty set of children
                        return new Object[0];
                    }

                    //filter out the desired resource types
                    ArrayList results = new ArrayList();
                    for (int i = 0; i < members.length; i++) {
                        //And the test bits with the resource types to see if they are what we want
                        if ((members[i].getType() & resourceType) > 0 && (resourceType != IResource.FILE || fAcceptableLocationsFilter == null || fAcceptableLocationsFilter.accept(members[i]))) {
                            results.add(members[i]);
                        }
                    }
                    return results.toArray();
                }

                //input element case
                if (o instanceof ArrayList)
                	return ((ArrayList) o).toArray();

                return new Object[0];
            }
        };
    }

    protected final void createSelectionButtonGroup(Composite parent) {

		Font font= parent.getFont();

		// top level group
		Composite buttonComposite= new Composite(parent, SWT.NONE);
		buttonComposite.setFont(parent.getFont());

		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		layout.makeColumnsEqualWidth= true;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		Button selectButton= createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, TextEditorMessages.SelectResourcesDialog_selectAll, false);

		SelectionListener listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fResourceGroup.setAllSelections(true);
				updateSelectionCount();
			}
		};
		selectButton.addSelectionListener(listener);
		selectButton.setFont(font);
		setButtonLayoutData(selectButton);

		Button deselectButton= createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, TextEditorMessages.SelectResourcesDialog_deselectAll, false);

		listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fResourceGroup.setAllSelections(false);
				updateSelectionCount();
			}
		};
		deselectButton.addSelectionListener(listener);
		deselectButton.setFont(font);
		setButtonLayoutData(deselectButton);

		// types edit button
		Button selectTypesButton= createButton(buttonComposite, IDialogConstants.SELECT_TYPES_ID, TextEditorMessages.SelectResourcesDialog_filterSelection, false);

		listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSelectFileTypes();
			}
		};
		selectTypesButton.addSelectionListener(listener);
		selectTypesButton.setFont(font);
		setButtonLayoutData(selectTypesButton);
	}

    protected void handleSelectFileTypes() {
		Object[] acceptedFileTypes= queryFileTypes();
		if (acceptedFileTypes != null) {
			fAcceptedFileTypes= Arrays.asList(acceptedFileTypes);
			filterSelection();
		}
	}

    protected Object[] queryFileTypes() {
		TypeFilteringDialog dialog= new TypeFilteringDialog(getShell(), fAcceptedFileTypes);
		dialog.open();
		return dialog.getResult();
	}

    private void filterSelection() {

    	final IFilter filter= new IFilter() {
			public boolean accept(IResource resource) {
				return hasAcceptedFileType(resource);
			}
		};

		List list= fResourceGroup.getAllWhiteCheckedItems();
		final IResource[] resources= (IResource[]) list.toArray(new IResource[list.size()]);

        Runnable runnable = new Runnable() {
            public void run() {
            	setSelection(resources, filter);
			}
        };

        BusyIndicator.showWhile(getShell().getDisplay(), runnable);
    }

    protected boolean hasAcceptedFileType(IResource resource) {
		if (fAcceptedFileTypes == null)
			return true;

		String resourceName= resource.getName();
		int separatorIndex= resourceName.lastIndexOf("."); //$NON-NLS-1$
		if (separatorIndex == -1)
			return false;

		String extension= resourceName.substring(separatorIndex + 1);

		Iterator e= fAcceptedFileTypes.iterator();
		while (e.hasNext()) {
			if (extension.equalsIgnoreCase((String) e.next()))
				return true;
		}

		return false;
	}

	protected void setInitialSelection() {
		setSelection(fInput, fAcceptableLocationsFilter);
		selectAndReveal(fInput[0]);
	}

	protected void setSelection(IResource[] input, IFilter filter) {
		Map selectionMap= new Hashtable();
		for (int i= 0; i < input.length; i++) {
			IResource resource= input[i];
			if ((resource.getType() & IResource.FILE) > 0) {
				if (filter.accept(resource)) {
					List files= null;
					IContainer parent= resource.getParent();
					if (selectionMap.containsKey(parent))
						files= (List) selectionMap.get(parent);
					else
						files= new ArrayList();

					files.add(resource);
					selectionMap.put(parent, files);
				}
			} else
				setSelection(selectionMap, (IContainer) resource, filter);
		}
		fResourceGroup.updateSelections(selectionMap);
		updateSelectionCount();
	}

	private void setSelection(Map selectionMap, IContainer parent, IFilter filter) {
		try {

			IResource[] resources= parent.members();
			List selections= new ArrayList();

			for (int i= 0; i < resources.length; i++) {
				IResource resource= resources[i];
				if ((resource.getType() & IResource.FILE) > 0) {
					if (filter.accept(resource))
						selections.add(resource);
				} else {
					setSelection(selectionMap, (IContainer) resource, filter);
				}
			}

			if (!selections.isEmpty())
				selectionMap.put(parent, selections);

		} catch (CoreException x) {
			//Just return if we can't get any info
			return;
		}
	}

	private void selectAndReveal(IResource resource) {
		IContainer container= null;
		if ((IResource.FILE & resource.getType()) > 0)
			container= resource.getParent();
		else
			container= (IContainer) resource;
		fResourceGroup.selectAndReveal(container);
	}

	private void updateSelectionCount() {
		List listItems= fResourceGroup.getAllCheckedListItems();
		int checkedFiles= listItems == null ? 0 : listItems.size();
		StringBuffer buffer= new StringBuffer();
		switch (checkedFiles) {
			case 0:
				buffer.append(TextEditorMessages.SelectResourcesDialog_noFilesSelected);
				break;
			case 1:
				buffer.append(TextEditorMessages.SelectResourcesDialog_oneFileSelected);
				break;
			default:
				buffer.append(NLSUtility.format(TextEditorMessages.SelectResourcesDialog_nFilesSelected, new Integer(checkedFiles)));
		}
		fCountIndication.setText(buffer.toString());

		Button okButton= getButton(IDialogConstants.OK_ID);
		if (okButton != null)
			okButton.setEnabled(checkedFiles > 0);
	}
}
