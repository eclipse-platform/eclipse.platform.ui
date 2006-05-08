/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import com.ibm.icu.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.activities.WorkbenchTriggerPointAdvisor;

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
    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(EnablementDialog.class.getName());

    private Button dontAskButton;

    private Set activitiesToEnable = new HashSet(7);

    private Collection activityIds;

    private boolean dontAsk;

    private Button detailsButton;

    boolean showDetails = false;

    private Composite detailsComposite;

    private Label detailsLabel;

    private String selectedActivity;

    private Text detailsText;

	private Properties strings;

    /**
     * Create a new instance of the reciever.
     * 
     * @param parentShell the parent shell
     * @param activityIds the candidate activities
     * @param strings string overrides
     */
    public EnablementDialog(Shell parentShell, Collection activityIds, Properties strings) {
        super(parentShell);
        this.activityIds = activityIds;
		this.strings = strings;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Font dialogFont = parent.getFont();
        composite.setFont(dialogFont);
        Label text = new Label(composite, SWT.NONE);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setFont(dialogFont);
        IActivityManager manager = PlatformUI.getWorkbench()
                .getActivitySupport().getActivityManager();

        if (activityIds.size() == 1) {
            String activityId = (String) activityIds.iterator().next();
            activitiesToEnable.add(activityId);
            selectedActivity = activityId;

            IActivity activity = manager.getActivity(activityId);
            String activityText;
            try {
                activityText = activity.getName();
            } catch (NotDefinedException e) {
                activityText = activity.getId();
            }
            text.setText(MessageFormat.format(RESOURCE_BUNDLE
                    .getString("requiresSingle"), //$NON-NLS-1$
                    new Object[] { activityText }));

            text = new Label(composite, SWT.NONE);
			text
					.setText(strings
							.getProperty(
									WorkbenchTriggerPointAdvisor.PROCEED_SINGLE,
									RESOURCE_BUNDLE
											.getString(WorkbenchTriggerPointAdvisor.PROCEED_SINGLE)));
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setFont(dialogFont);
        } else {
            text.setText(RESOURCE_BUNDLE.getString("requiresMulti")); //$NON-NLS-1$
            Set activityIdsCopy = new HashSet(activityIds);
            CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(
					composite, SWT.CHECK | SWT.BORDER | SWT.SINGLE);
            viewer.setContentProvider(new ActivityContentProvider());
            viewer.setLabelProvider(new ActivityLabelProvider(manager));
            viewer.setInput(activityIdsCopy);
            viewer.setCheckedElements(activityIdsCopy.toArray());
            viewer.addCheckStateListener(new ICheckStateListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
                 */
                public void checkStateChanged(CheckStateChangedEvent event) {
                    if (event.getChecked()) {
						activitiesToEnable.add(event.getElement());
					} else {
						activitiesToEnable.remove(event.getElement());
					}

                    getButton(Window.OK).setEnabled(
                            !activitiesToEnable.isEmpty());
                }
            });
            viewer.addSelectionChangedListener(new ISelectionChangedListener() {
                /* (non-Javadoc)
                 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
                 */
                public void selectionChanged(SelectionChangedEvent event) {
                    selectedActivity = (String) ((IStructuredSelection) event
                            .getSelection()).getFirstElement();
                    setDetails();
                }
            });
            activitiesToEnable.addAll(activityIdsCopy);

            viewer.getControl().setLayoutData(
                    new GridData(GridData.FILL_HORIZONTAL));
            viewer.getControl().setFont(dialogFont);

            text = new Label(composite, SWT.NONE);
            text.setText(strings.getProperty(WorkbenchTriggerPointAdvisor.PROCEED_MULTI, RESOURCE_BUNDLE
					.getString(WorkbenchTriggerPointAdvisor.PROCEED_MULTI))); 
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setFont(dialogFont);
        }
        Label seperator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        seperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        dontAskButton = new Button(composite, SWT.CHECK);
        dontAskButton.setSelection(false);
        dontAskButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dontAskButton.setText(strings.getProperty(
				WorkbenchTriggerPointAdvisor.DONT_ASK, RESOURCE_BUNDLE
						.getString(WorkbenchTriggerPointAdvisor.DONT_ASK)));
        dontAskButton.setFont(dialogFont);

        detailsComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        detailsComposite.setLayout(layout);
        detailsLabel = new Label(detailsComposite, SWT.NONE);
        detailsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        detailsLabel.setFont(dialogFont);

        detailsText = new Text(detailsComposite, SWT.WRAP | SWT.V_SCROLL
                | SWT.BORDER | SWT.READ_ONLY);
        detailsText.setLayoutData(new GridData(GridData.FILL_BOTH));
        detailsText.setFont(dialogFont);

        setDetails();

        GridData data = new GridData(GridData.FILL_BOTH);
        detailsComposite.setLayoutData(data);
        setDetailHints();

        return composite;
    }

    /**
     * Set the text of the detail label and text area.
     */
    protected void setDetails() {
        if (selectedActivity == null) {
            detailsLabel
					.setText(strings
							.getProperty(
									WorkbenchTriggerPointAdvisor.NO_DETAILS,
									RESOURCE_BUNDLE
											.getString(WorkbenchTriggerPointAdvisor.NO_DETAILS)));
            detailsText.setText(""); //$NON-NLS-1$
        } else {
            IActivity activity = PlatformUI.getWorkbench().getActivitySupport()
                    .getActivityManager().getActivity(selectedActivity);
            String name;
            try {
                name = activity.getName();
            } catch (NotDefinedException e1) {
                name = selectedActivity;
            }
            String desc;
            try {
                desc = activity.getDescription();
            } catch (NotDefinedException e) {
                desc = RESOURCE_BUNDLE.getString("noDescAvailable"); //$NON-NLS-1$
            }
            detailsLabel.setText(MessageFormat.format(RESOURCE_BUNDLE
                    .getString("detailsLabel"), new Object[] { name })); //$NON-NLS-1$
            detailsText.setText(desc);
        }
    }

    /**
     * 
     */
    protected void setDetailHints() {
        GridData data = (GridData) detailsComposite.getLayoutData();
        if (showDetails) {
            Composite parent = detailsComposite.getParent();
            data.widthHint = parent.getSize().x - ((GridLayout)parent.getLayout()).marginWidth * 2;
            data.heightHint = convertHeightInCharsToPixels(5);
        } else {
            data.widthHint = 0;
            data.heightHint = 0;
        }
    }

    /**
     * Set the label of the detail button based on whether we're currently showing the description text.
     */
    private void setDetailButtonLabel() {
        if (!showDetails) {
			detailsButton.setText(RESOURCE_BUNDLE.getString("showDetails")); //$NON-NLS-1$
		} else {
			detailsButton.setText(RESOURCE_BUNDLE.getString("hideDetails")); //$NON-NLS-1$        
		}
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
    public Set getActivitiesToEnable() {
        return activitiesToEnable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        dontAsk = dontAskButton.getSelection();
        super.okPressed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
                "", false); //$NON-NLS-1$
        setDetailButtonLabel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.DETAILS_ID) {
            detailsPressed();
            return;
        }
        super.buttonPressed(buttonId);
    }

    /**
     * Handles selection of the Details button.
     */
    private void detailsPressed() {
        showDetails = !showDetails;
        setDetailButtonLabel();
        setDetailHints();
        setDetails();
        ((Composite) getDialogArea()).layout(true);
        getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
}
