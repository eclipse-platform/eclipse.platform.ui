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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ITagOperation;
import org.eclipse.team.internal.ui.PixelConverter;
import org.eclipse.team.internal.ui.SWTUtils;
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
		
		final int width= convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);;
		
		final Label label = SWTUtils.createLabel(parent, Policy.bind("TagAction.enterTag")); //$NON-NLS-1$
		label.setLayoutData(SWTUtils.createGridData(width, SWT.DEFAULT, true, false));

		tagText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		tagText.setLayoutData(SWTUtils.createHFillGridData());
		tagText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					tagName = tagText.getText();
					updateEnablements();
				}
			}
		);
		
		moveTagButton= SWTUtils.createCheckBox(parent, Policy.bind("TagAction.moveTag")); //$NON-NLS-1$
		moveTagButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveTag = moveTagButton.getSelection();
			}
		});
		
		// Add F1 help
		WorkbenchHelp.setHelp(parent, IHelpContextIds.TAG_AS_VERSION_DIALOG);
	}

	public boolean shouldMoveTag()  {
		return moveTag;
	}
	
	/**
	 * @see DetailsDialog#createDropDownDialogArea(Composite)
	 */
	protected Composite createDropDownDialogArea(Composite parent) {
		
		final PixelConverter converter= SWTUtils.createDialogPixelConverter(parent);
		
		final Composite composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_DIALOG));
	    
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
