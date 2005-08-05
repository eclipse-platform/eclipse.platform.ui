/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * The Breakpoint working set page allows the user to create
 * and edit a Breakpoint working set.
 * 
 * @since 3.1
 */
public class BreakpointWorkingSetPage extends WizardPage implements IWorkingSetPage {

	final private static String PAGE_TITLE= DebugUIViewsMessages.BreakpointWorkingSetPage_0; 
	final private static String PAGE_ID= "breakpointWorkingSetPage"; //$NON-NLS-1$
	
	private Text fWorkingSetName;
	private CheckboxTreeViewer fTree;
	private ITreeContentProvider fTreeContentProvider;
	
	class ContentProvider implements ITreeContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			return element instanceof IBreakpointManager;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
	
	private boolean fFirstCheck;
	private IWorkingSet fWorkingSet;

	/**
	 * Default constructor.
	 */
	public BreakpointWorkingSetPage() {
		super(PAGE_ID, PAGE_TITLE, DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));
		setDescription(DebugUIViewsMessages.BreakpointWorkingSetPage_1); 
		fFirstCheck= true;
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label= new Label(composite, SWT.WRAP);
		label.setText(DebugUIViewsMessages.BreakpointWorkingSetPage_2); 
		GridData gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		fWorkingSetName= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fWorkingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		fWorkingSetName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			}
		);
		fWorkingSetName.setFocus();
		
		label= new Label(composite, SWT.WRAP);
		label.setText(DebugUIViewsMessages.BreakpointWorkingSetPage_3); 
		gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		fTree= new CheckboxTreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gd= new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint= convertHeightInCharsToPixels(15);
		fTree.getControl().setLayoutData(gd);
		
		fTreeContentProvider= new ContentProvider();
		fTree.setContentProvider(fTreeContentProvider);
				
		fTree.setLabelProvider(DebugUITools.newDebugModelPresentation());
		fTree.setSorter(new BreakpointsSorter());
		fTree.setUseHashlookup(true);
		fTree.setInput(DebugPlugin.getDefault().getBreakpointManager());
		fTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				validateInput();
			}
		});

		// Add select / deselect all buttons for bug 46669
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		Button selectAllButton = SWTUtil.createPushButton(buttonComposite, DebugUIViewsMessages.BreakpointWorkingSetPage_selectAll_label, null);
		selectAllButton.setToolTipText(DebugUIViewsMessages.BreakpointWorkingSetPage_selectAll_toolTip);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				fTree.setCheckedElements(fTreeContentProvider.getElements(fTree.getInput()));
				validateInput();
			}
		});
		
		Button deselectAllButton = SWTUtil.createPushButton(buttonComposite, DebugUIViewsMessages.BreakpointWorkingSetPage_deselectAll_label, null);
		deselectAllButton.setToolTipText(DebugUIViewsMessages.BreakpointWorkingSetPage_deselectAll_toolTip);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				fTree.setCheckedElements(new Object[0]);
				validateInput();
			}
		});
		
		if (fWorkingSet != null)
			fWorkingSetName.setText(fWorkingSet.getName());
		initializeCheckedState();
		validateInput();

		Dialog.applyDialogFont(composite);
	}

	/*
	 * Implements method from IWorkingSetPage
	 */
	public IWorkingSet getSelection() {
		return fWorkingSet;
	}

	/*
	 * Implements method from IWorkingSetPage
	 */
	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null"); //$NON-NLS-1$
		fWorkingSet= workingSet;
		if (getContainer() != null && getShell() != null && fWorkingSetName != null) {
			fFirstCheck= false;
			fWorkingSetName.setText(fWorkingSet.getName());
			initializeCheckedState();
			validateInput();
		}
	}

	/*
	 * Implements method from IWorkingSetPage
	 */
	public void finish() {
		String workingSetName= fWorkingSetName.getText();
		ArrayList elements= new ArrayList(10);
		findCheckedElements(elements, fTree.getInput());
		if (fWorkingSet == null) {
			IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
			fWorkingSet= workingSetManager.createWorkingSet(workingSetName, (IAdaptable[])elements.toArray(new IAdaptable[elements.size()]));
		} else {
			fWorkingSet.setName(workingSetName);
			fWorkingSet.setElements((IAdaptable[]) elements.toArray(new IAdaptable[elements.size()]));
		}
	}

	private void validateInput() {
		String errorMessage= null; 
		String newText= fWorkingSetName.getText();

		if (newText.equals(newText.trim()) == false)
			errorMessage = DebugUIViewsMessages.BreakpointWorkingSetPage_4; 
		if (newText.equals("")) { //$NON-NLS-1$
			if (fFirstCheck) {
				setPageComplete(false);
				fFirstCheck= false;
				return;
			}		
			errorMessage= DebugUIViewsMessages.BreakpointWorkingSetPage_5; 
		}

		fFirstCheck= false;

		if (errorMessage == null && (fWorkingSet == null || newText.equals(fWorkingSet.getName()) == false)) {
			IWorkingSet[] workingSets= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
			for (int i= 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage= DebugUIViewsMessages.BreakpointWorkingSetPage_6; 
				}
			}
		}

		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}
		
	private void findCheckedElements(List checkedResources, Object parent) {
		Object[] children= fTreeContentProvider.getChildren(parent);
		for (int i= 0; i < children.length; i++) {
			if (fTree.getGrayed(children[i]))
				findCheckedElements(checkedResources, children[i]);
			else if (fTree.getChecked(children[i]))
				checkedResources.add(children[i]);
		}
	}

	private void initializeCheckedState() {

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Object[] elements = new Object[0];
				if (fWorkingSet == null) {
					// Use current part's selection for initialization
					IWorkbenchPage page= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
					if (page == null) {
						return;
					}
					
					IWorkbenchPart part= page.getActivePart();
					if (part == null) {
						return;
					}
					
					ISelectionProvider provider = part.getSite().getSelectionProvider();
					if (provider == null) {
						return;
					}
					
					ISelection selection = provider.getSelection();
					if (selection instanceof IStructuredSelection) {
						IStructuredSelection ss = (IStructuredSelection) selection;
						List list = new ArrayList();
						Object[] objects = ss.toArray();
						for (int i= 0; i < objects.length; i++) {
							if (objects[i] instanceof IBreakpoint) {
								list.add(objects[i]);
							}
						}
						elements = list.toArray();
					}
				} else {
					elements= fWorkingSet.getElements();
				}

				fTree.setCheckedElements(elements);
			}
		});
	}
	
}
