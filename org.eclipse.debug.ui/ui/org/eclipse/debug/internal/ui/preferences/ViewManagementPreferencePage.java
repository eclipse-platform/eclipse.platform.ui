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
package org.eclipse.debug.internal.ui.preferences;

import java.util.Iterator;

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.views.launch.LaunchViewContextListener;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * Preference page for configuring the debugger's automatic
 * view management.
 */
public class ViewManagementPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private CheckboxTableViewer fPerspectiveViewer;
	private Button fTrackViewsButton;
	private Button fResetViewsButton;
    // This flag indicates whether or not the user has pressed the reset button
	private boolean fResetPressed= false;
	private PerspectiveLabelProvider fLabelProvider= null;
    private SelectionListener fSelectionListener= new SelectionAdapter() {
    
        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source == fResetViewsButton) {
                handleResetPressed();
            } else if (source == fTrackViewsButton) {
                handleTrackViewsToggled();
            }
        }
    
    };
	
	public ViewManagementPreferencePage() {
		super();
		setTitle(DebugPreferencesMessages.ViewManagementPreferencePage_1); 
		setDescription(DebugPreferencesMessages.ViewManagementPreferencePage_0); 
		setPreferenceStore(DebugUITools.getPreferenceStore());
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.VIEW_MANAGEMENT_PREFERENCE_PAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		
		createPerspectiveViewer(composite);
		
		createViewTrackingOptions(composite);
		
		Dialog.applyDialogFont(composite);
		
		return composite;
	}

	/**
	 * @param composite
	 */
	private void createViewTrackingOptions(Composite composite) {
		fTrackViewsButton= new Button(composite, SWT.CHECK);
		fTrackViewsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fTrackViewsButton.setText(DebugPreferencesMessages.ViewManagementPreferencePage_3); 
		fTrackViewsButton.setSelection(DebugUITools.getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_TRACK_VIEWS));
        fTrackViewsButton.addSelectionListener(fSelectionListener);
		
		Label label= new Label(composite, SWT.WRAP);
		label.setText(DebugPreferencesMessages.ViewManagementPreferencePage_4); 
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fResetViewsButton= SWTUtil.createPushButton(composite, DebugPreferencesMessages.ViewManagementPreferencePage_5, null); 
		((GridData) fResetViewsButton.getLayoutData()).horizontalAlignment= GridData.BEGINNING;
		fResetViewsButton.addSelectionListener(fSelectionListener);
		updateResetButton();
	}
    
    private void handleResetPressed() {
        fResetPressed= true;
        fResetViewsButton.setEnabled(false);
    }
    
    protected void handleTrackViewsToggled() {
        if (fTrackViewsButton.getSelection()) {
            // When toggled on, possibly re-enable the reset button
            updateResetButton();
        } else {
            // When toggled off, disable the reset button
            fResetViewsButton.setEnabled(false);
        }
    }

	/**
	 * @param parent
	 */
	private void createPerspectiveViewer(Composite parent) {
		Label label= new Label(parent, SWT.WRAP);
		label.setText(DebugPreferencesMessages.ViewManagementPreferencePage_2); 
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Table table= new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fPerspectiveViewer= new CheckboxTableViewer(table);
		PerspectiveProvider provider= new PerspectiveProvider();
		fPerspectiveViewer.setContentProvider(provider);
		fLabelProvider= new PerspectiveLabelProvider();
		fPerspectiveViewer.setLabelProvider(fLabelProvider);
		fPerspectiveViewer.setInput(this);
		
		checkPerspectives(getPreferenceStore().getString(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES));
	}
	
	private void checkPerspectives(String perspectiveList) {
		fPerspectiveViewer.setAllChecked(false);
		IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
		Iterator perspectiveIds= LaunchViewContextListener.parseList(perspectiveList).iterator();
		while (perspectiveIds.hasNext()) {
			IPerspectiveDescriptor descriptor = registry.findPerspectiveWithId((String) perspectiveIds.next());
            if (descriptor != null) {
                fPerspectiveViewer.setChecked(descriptor, true);
            }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		StringBuffer buffer= new StringBuffer();
		Object[] descriptors = fPerspectiveViewer.getCheckedElements();
		for (int i = 0; i < descriptors.length; i++) {
			buffer.append(((IPerspectiveDescriptor) descriptors[i]).getId()).append(',');
		}
		
		getPreferenceStore().setValue(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, buffer.toString());
		boolean trackViews = fTrackViewsButton.getSelection();
        getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_TRACK_VIEWS, trackViews);
		if (fResetPressed || !trackViews) {
            // Reset if the user has pressed reset or chosen to no longer track views
			getPreferenceStore().setValue(LaunchViewContextListener.PREF_VIEWS_TO_NOT_OPEN, ""); //$NON-NLS-1$
			getPreferenceStore().setValue(LaunchViewContextListener.PREF_OPENED_VIEWS, ""); //$NON-NLS-1$
		}
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		checkPerspectives(getPreferenceStore().getDefaultString(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES));
		fTrackViewsButton.setSelection(getPreferenceStore().getDefaultBoolean(IInternalDebugUIConstants.PREF_TRACK_VIEWS));
		fResetPressed= false;
		updateResetButton();
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Updates enablement of the reset button.
	 * Enable if either persisted view collection is not empty.
     * Always disable if "track views" is turned off.
	 */
	private void updateResetButton() {
		boolean enableReset= !"".equals(getPreferenceStore().getString(LaunchViewContextListener.PREF_VIEWS_TO_NOT_OPEN)) || //$NON-NLS-1$
			!"".equals(getPreferenceStore().getString(LaunchViewContextListener.PREF_OPENED_VIEWS)); //$NON-NLS-1$
        // Only enable the button if there are views to clear, the user hasn't pressed the reset
        // button, and the option to "track views" is turned on.
		fResetViewsButton.setEnabled(enableReset && !fResetPressed && fTrackViewsButton.getSelection());
	}

	private static class PerspectiveProvider implements IStructuredContentProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fLabelProvider != null) {
			fLabelProvider.dispose();
		}
	}
}
