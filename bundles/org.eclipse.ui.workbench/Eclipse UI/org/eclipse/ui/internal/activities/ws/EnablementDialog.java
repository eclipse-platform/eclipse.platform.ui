/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.NotDefinedException;

/**
 * Dialog that will prompt the user and confirm that they wish to activate a set
 * of activities.
 * 
 * @since 3.0
 */
public class EnablementDialog extends Dialog {

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(EnablementDialog.class.getName());

    private Button dontAskButton;
    
    private Collection activitiesToEnable = new HashSet(7);

    private Collection activityIds;

    private boolean dontAsk;
    
    /**
     * Create a new instance of the reciever.
     * 
     * @param parentShell the parent shell
     * @param activityIds the candidate activities
     */
    public EnablementDialog(Shell parentShell, Collection activityIds) {
        super(parentShell);
        this.activityIds = activityIds;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {            
		Composite composite = (Composite)super.createDialogArea(parent);
		Label text = new Label(composite, SWT.NONE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setFont(parent.getFont());
		IActivityManager manager = PlatformUI.getWorkbench().getActivitySupport().getActivityManager();
		
		if (activityIds.size() == 1) {
		    String activityId = (String) activityIds.iterator().next();
            activitiesToEnable.add(activityId);
		    
		    IActivity activity = manager.getActivity(activityId);
		    String activityText;
		    try {
		        activityText = activity.getName();
            } catch (NotDefinedException e) {
                activityText = activity.getId();
            }
			text.setText(
			        MessageFormat.format(
			                RESOURCE_BUNDLE
			                	.getString("requiresSingle"), //$NON-NLS-1$
			                new Object [] {activityText})); 

            text = new Label(composite, SWT.NONE);
            text.setText(RESOURCE_BUNDLE.getString("proceedSingle")); //$NON-NLS-1$
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setFont(parent.getFont());
		}
		else {
		    text.setText(RESOURCE_BUNDLE.getString("requiresMulti")); //$NON-NLS-1$
		    Set activityIdsCopy = new HashSet(activityIds);
		    CheckboxTableViewer viewer = new CheckboxTableViewer(composite);
		    viewer.setContentProvider(new ActivityContentProvider());
		    viewer.setLabelProvider(new ActivityLabelProvider(manager));
		    viewer.setInput(activityIdsCopy);
		    viewer.setCheckedElements(activityIdsCopy.toArray());
		    viewer.addCheckStateListener(new ICheckStateListener() {
                
                /* (non-Javadoc)
                 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
                 */
                public void checkStateChanged(CheckStateChangedEvent event) {
                    if (event.getChecked())
                        activitiesToEnable.add(event.getElement());
                    else
                        activitiesToEnable.remove(event.getElement());     
                    
                    getButton(Window.OK).setEnabled(!activitiesToEnable.isEmpty());
                }});
		    activitiesToEnable.addAll(activityIdsCopy);
		    
		    viewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    viewer.getControl().setFont(parent.getFont());
		    
			text = new Label(composite, SWT.NONE);
			text.setText(RESOURCE_BUNDLE.getString("proceedMulti")); //$NON-NLS-1$
			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			text.setFont(parent.getFont());
		}
		Label seperator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		seperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		dontAskButton = new Button(composite, SWT.CHECK);
        dontAskButton.setSelection(false);
        dontAskButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dontAskButton.setText(RESOURCE_BUNDLE.getString("dontAsk")); //$NON-NLS-1$
		dontAskButton.setFont(parent.getFont());

		return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(RESOURCE_BUNDLE.getString("title")); //$NON-NLS-1$
    }
    
    /** 
     * @return Returns whether the user has declared that there is to be no further 
     * prompting for the supplied activities
     */
    public boolean getDontAsk() {
        return dontAsk;
    }
    
    /**
     * @return Returns the activities to enable
     */
    public Collection getActivitiesToEnable() {
        return activitiesToEnable;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        dontAsk = dontAskButton.getSelection();
        super.okPressed();
    }
}