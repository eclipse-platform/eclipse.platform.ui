/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provides a custom dialog for displaying launch configurations seperated by project
 * in a tree view for the user to select
 * @since 3.2
 */
public class DeleteAssociatedLaunchConfigurationsDialog extends SelectionDialog {
	/**
	 * Class to provide content for the DeleteAssociatedLaunchConfigsDialog
	 */
	class DeleteContentProvider implements ITreeContentProvider {

		private HashMap fInputMap = new HashMap();
		
		public DeleteContentProvider(HashMap map) {
			if(map != null) {
				fInputMap = map;
			}
		}
		
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof IProject) {
				return ((ArrayList)fInputMap.get(parentElement)).toArray();
			}
			if(parentElement instanceof AdaptableList) {
				return fInputMap.keySet().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {return null;}

		public boolean hasChildren(Object element) {
			if(element instanceof IProject) {
				return true;
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
			fInputMap.clear();
			fInputMap = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		
	}
	
	/**
	 * Provides a custom viewer for the dialog whic allows us to have custom checked state handling
	 */
	class LCViewer extends CheckboxTreeViewer {

		/**
		 * Constructor
		 * @param parent the parent to add this viewer to
		 * @param style the style of the viewer
		 */
		public LCViewer(Composite parent, int style) {
			super(parent, style);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.CheckboxTreeViewer#getCheckedElements()
		 */
		public Object[] getCheckedElements() {
			Object[] items = super.getCheckedElements();
			//filter out the projects
			ArrayList list = new ArrayList();
			for (int i = 0; i < items.length; i++) {
				if(items[i] instanceof ILaunchConfiguration) {
					list.add(items[i]);
				}
			}
			return list.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.CheckboxTreeViewer#handleSelect(org.eclipse.swt.events.SelectionEvent)
		 */
		protected void handleSelect(SelectionEvent event) {
			if(event.detail == SWT.CHECK) {
				updateCheckedState((TreeItem)event.item);
			}
		}
		
		 /**
	     * Update the checked state up the given element and all of its children.
	     * 
	     * @param element
	     */
	    public void updateCheckedState(TreeItem item) {
	        Object element = item.getData();
	        if (element instanceof ILaunchConfiguration) {
            	TreeItem parent = item.getParentItem();
                TreeItem[] children = parent.getItems();
                int checked = 0;
                for (int i = 0; i < children.length; i++) {
					if(children[i].getChecked()) {
						checked++;
					}
				}
                if(checked == 0) {
	                setGrayChecked(parent.getData(), false);
                }
                else if(checked == children.length) {
                	parent.setGrayed(false);
	                parent.setChecked(true);
                }
                else {
                	setGrayChecked(parent.getData(), true);
                }
	        } 
	        else if (element instanceof IProject) {
	        	item.setGrayed(false);
	        	TreeItem[] children = item.getItems();
	        	for (int i = 0; i < children.length; i++) {
					setChecked(children[i].getData(), item.getChecked());
	        	}
	        }
	    }
	}
	
	private static final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".DELETE_ASSOCIATED_CONFIGS_DIALOG"; //$NON-NLS-1$
	private HashMap fMap = null;
	private Object fInput = null;
	private Button fPrefButton = null;
	private LCViewer fViewer = null;
	private Object[] fResult = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent shell for this dialog
	 * @param input the input for the viewer
	 * @param message the message for the top of the dialog
	 * @param map the map of project to listing of configs
	 */
	public DeleteAssociatedLaunchConfigurationsDialog(Shell parentShell, Object input, String message, HashMap map) {
		super(parentShell);
		super.setMessage(message);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fMap = map;
		fInput = input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(SETTINGS_ID);
		} 
		return section;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		IDialogSettings settings = getDialogBoundsSettings();
		if(settings != null) {
			try {
				int width = settings.getInt("DIALOG_WIDTH"); //$NON-NLS-1$
				int height = settings.getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
				if(width > 0 & height > 0) {
					return new Point(width, height);
				}
			}
			catch (NumberFormatException nfe) {
				return new Point(350, 400);
			}
		}
		return new Point(350, 400);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		SWTUtil.createLabel(comp, LaunchConfigurationsMessages.DeleteAssociatedLaunchConfigurationsDialog_0, 2);
		fViewer = new LCViewer(comp, SWT.BORDER);
		Tree tree = fViewer.getTree();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		tree.setLayoutData(gd);
		fViewer.setContentProvider(new DeleteContentProvider(fMap));
		fViewer.setInput(fInput);
		fViewer.setLabelProvider(DebugUITools.newDebugModelPresentation());
		fViewer.expandAll();
		Composite butcomp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		butcomp.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		butcomp.setLayoutData(gd);
		Button sall = SWTUtil.createPushButton(butcomp, LaunchConfigurationsMessages.DeleteAssociatedLaunchConfigurationsDialog_1, null);
		sall.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fViewer.setGrayedElements(new Object[0]);
				fViewer.setAllChecked(true);
			}			
		});
		Button dsall = SWTUtil.createPushButton(butcomp, LaunchConfigurationsMessages.DeleteAssociatedLaunchConfigurationsDialog_2, null);
		dsall.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fViewer.setAllChecked(false);
			}			
		});
		fPrefButton = new Button(comp, SWT.CHECK);
		fPrefButton.setText(LaunchConfigurationsMessages.DeleteAssociatedLaunchConfigurationsDialog_3);
		Dialog.applyDialogFont(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.DELETE_ASSOCIATED_LAUNCH_CONFIGS_DIALOG);
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getResult()
	 */
	public Object[] getResult() {
		return fResult;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ListDialog#okPressed()
	 */
	protected void okPressed() {
		fResult = fViewer.getCheckedElements();
		//set pref if selected
		boolean pref = fPrefButton.getSelection();
		if(pref) {
			IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore(); 
			store.setValue(IInternalDebugUIConstants.PREF_DELETE_CONFIGS_ON_PROJECT_DELETE, MessageDialogWithToggle.ALWAYS);
		}
		super.okPressed();
	}
}
