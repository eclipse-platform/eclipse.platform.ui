/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.tags;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ITagOperation;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class TagAsVersionDialog extends DetailsDialog {

    private static final int TAG_AREA_HEIGHT_HINT = 200;
    
	private ITagOperation operation;
	
	private Text tagText;
	private Button moveTagButton;
	
	private String tagName = ""; //$NON-NLS-1$
	private boolean moveTag = false;

    private TagSource tagSource;

    private TagSelectionArea tagArea;
	
	public TagAsVersionDialog(Shell parentShell, String title, ITagOperation operation) {
		super(parentShell, title);
		this.tagSource = operation.getTagSource();
		this.operation = operation;
	}
	
	/**
	 * @see DetailsDialog#createMainDialogArea(Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		// create message
		Label label = new Label(parent, SWT.WRAP);
		label.setText(Policy.bind("TagAction.enterTag")); //$NON-NLS-1$
		GridData data = new GridData(
			GridData.GRAB_HORIZONTAL |
			GridData.HORIZONTAL_ALIGN_FILL |
			GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);;
		label.setLayoutData(data);

		tagText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		tagText.setLayoutData(new GridData(
			GridData.GRAB_HORIZONTAL |
			GridData.HORIZONTAL_ALIGN_FILL));
		tagText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					tagName = tagText.getText();
					updateEnablements();
				}
			}
		);
		
		moveTagButton = new Button(parent, SWT.CHECK);
		moveTagButton.setText(Policy.bind("TagAction.moveTag")); //$NON-NLS-1$
		moveTagButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		moveTagButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveTag = moveTagButton.getSelection();
			}
		});
		
		// Add F1 help
		WorkbenchHelp.setHelp(parent, IHelpContextIds.TAG_AS_VERSION_DIALOG);
		Dialog.applyDialogFont(parent);
	}

	public boolean shouldMoveTag()  {
		return moveTag;
	}
	
	/**
	 * @see DetailsDialog#createDropDownDialogArea(Composite)
	 */
	protected Composite createDropDownDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
	    Composite composite = new Composite(parent, SWT.NONE);
	    GridLayout layout = new GridLayout();
	    layout.marginHeight = 0;
	    layout.marginWidth = 0;
	    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	    composite.setLayout(layout);
	    final GridData gridData = new GridData(GridData.FILL_BOTH);
	    gridData.heightHint = TAG_AREA_HEIGHT_HINT;
	    composite.setLayoutData(gridData);
		
		tagArea = new TagSelectionArea(getShell(), tagSource, TagSelectionArea.INCLUDE_VERSIONS, null);
		tagArea.setTagAreaLabel(Policy.bind("TagAction.existingVersions"));  //$NON-NLS-1$
		tagArea.setIncludeFilterInputArea(false);
		tagArea.createArea(composite);
		tagArea.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(TagSelectionArea.SELECTED_TAG)) {
                    CVSTag tag = tagArea.getSelection();
                    if (tag != null) {
                        tagText.setText(tag.getName());
                    }
                } else if (event.getProperty().equals(TagSelectionArea.OPEN_SELECTED_TAG)) {
                    CVSTag tag = tagArea.getSelection();
                    if (tag != null) {
                        tagText.setText(tag.getName());
                        okPressed();
                    }
                }
            }
        });
		return composite;
	}
	
	/**
	 * Validates tag name
	 */
	protected void updateEnablements() {
		String message = null;
		if(tagName.length() == 0) {
			message = ""; //$NON-NLS-1$
		} else {		
			IStatus status = CVSTag.validateTagName(tagName);
			if (!status.isOK()) {
				message = status.getMessage();
			}
		}
		setPageComplete(message == null);
		setErrorMessage(message);
		if (tagArea != null) {
		    tagArea.setFilter(tagName);
		}
	}
	
	/**
	 * Returns the tag name entered into this dialog
	 */
	public String getTagName() {
		return tagName;
	}
	
	/**
	 * @return
	 */
	public ITagOperation getOperation() {
		operation.setTag(new CVSTag(tagName, CVSTag.VERSION));
		if (moveTag) {
			operation.moveTag();
		}
		return operation;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#isMainGrabVertical()
     */
    protected boolean isMainGrabVertical() {
        return false;
    }

}
