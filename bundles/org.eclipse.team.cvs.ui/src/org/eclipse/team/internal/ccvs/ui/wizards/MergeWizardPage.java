/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.tags.*;
import org.eclipse.team.internal.ui.SWTUtils;

public class MergeWizardPage extends CVSWizardPage {

    private Text endTagField;
    private Button endTagBrowseButton;
    private TagSource tagSource;
    private Text startTagField;
    private Button startTagBrowseButton;
    private TagRefreshButtonArea tagRefreshArea;
    private CVSTag startTag;
    private CVSTag endTag;
    private Button previewButton;
    private Button noPreviewButton;
    protected boolean preview = true;

    public MergeWizardPage(String pageName, String title, ImageDescriptor titleImage, String description, TagSource tagSource) {
        super(pageName, title, titleImage, description);
        this.tagSource = tagSource;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = SWTUtils.createGridComposite(parent, 1);
        
        Composite mainArea = createComposite(composite, 2, true);
        createEndTagArea(mainArea);
        createStartTagArea(mainArea);
        createPreviewOptionArea(mainArea);
        
        createTagRefreshArea(composite);

        Dialog.applyDialogFont(composite);
        setControl(composite);
    }

    private void createPreviewOptionArea(Composite mainArea) {
        previewButton = createRadioButton(mainArea, Policy.bind("MergeWizardPage.0"), 2); //$NON-NLS-1$
        noPreviewButton = createRadioButton(mainArea, Policy.bind("MergeWizardPage.1"), 2); //$NON-NLS-1$
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                preview  = previewButton.getSelection();
                updateEnablements();
            }
        };
        previewButton.setSelection(preview);
        noPreviewButton.setSelection(!preview);
        previewButton.addSelectionListener(selectionAdapter);
        noPreviewButton.addSelectionListener(selectionAdapter);
    }

    private void createTagRefreshArea(Composite parent) {
	    tagRefreshArea = new TagRefreshButtonArea(getShell(), getTagSource());
	    tagRefreshArea.setRunnableContext(getContainer());
	    tagRefreshArea.createArea(parent); 
    }

    private void createEndTagArea(Composite parent) {
        createWrappingLabel(parent, Policy.bind("MergeWizardPage.2"), 0, 2); //$NON-NLS-1$
        endTagField = createTextField(parent);
        endTagField.setLayoutData(SWTUtils.createHFillGridData());
        endTagField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateEndTag(endTagField.getText());
            }
        });
        final int endTagIncludeFlags = TagSelectionArea.INCLUDE_VERSIONS | TagSelectionArea.INCLUDE_BRANCHES | TagSelectionArea.INCLUDE_HEAD_TAG;
        TagContentAssistProcessor.createContentAssistant(endTagField, tagSource, endTagIncludeFlags);
        endTagBrowseButton = createPushButton(parent, Policy.bind("MergeWizardPage.3")); //$NON-NLS-1$
        endTagBrowseButton.setLayoutData(getButtonData());
        
        endTagBrowseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TagSelectionDialog dialog = new TagSelectionDialog(getShell(), getTagSource(), 
                        Policy.bind("MergeWizardPage.4"), //$NON-NLS-1$
                        Policy.bind("MergeWizardPage.5"), //$NON-NLS-1$
                        endTagIncludeFlags,
                        false, IHelpContextIds.MERGE_END_PAGE);
                if (dialog.open() == Dialog.OK) {
                    CVSTag selectedTag = dialog.getResult();
                    setEndTag(selectedTag);
                }
            }
        });
    }
    
    private GridData getButtonData() {
        final GridData gd= new GridData(SWT.FILL, SWT.FILL, false, false);
        gd.widthHint= convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        return gd;
    }

    private void createStartTagArea(Composite parent) {
        createWrappingLabel(parent, Policy.bind("MergeWizardPage.6"), 0, 2); //$NON-NLS-1$
        startTagField = createTextField(parent);
        startTagField.setLayoutData(SWTUtils.createHFillGridData());
        startTagField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateStartTag(startTagField.getText());
            }
        });
        TagContentAssistProcessor.createContentAssistant(startTagField, tagSource, TagSelectionArea.INCLUDE_VERSIONS);
        startTagBrowseButton = createPushButton(parent, Policy.bind("MergeWizardPage.7")); //$NON-NLS-1$
        startTagBrowseButton.setLayoutData(getButtonData());
        startTagBrowseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TagSelectionDialog dialog = new TagSelectionDialog(getShell(), getTagSource(), 
                        Policy.bind("MergeWizardPage.8"), //$NON-NLS-1$
                        Policy.bind("MergeWizardPage.9"), //$NON-NLS-1$
                        TagSelectionDialog.INCLUDE_VERSIONS,
                        false, IHelpContextIds.MERGE_START_PAGE);
                if (dialog.open() == Dialog.OK) {
                    CVSTag selectedTag = dialog.getResult();
                    setStartTag(selectedTag);
                }
            }
        });   
    }

    protected void updateEndTag(String text) {
        if (endTag == null || !endTag.getName().equals(text)) {
            CVSTag tag = getTagFor(text, false);
            setEndTag(tag);
        }
        updateEnablements();
    }
    
    protected void updateStartTag(String text) {
        if (startTag == null || !startTag.getName().equals(text)) {
            CVSTag tag = getTagFor(text, true);
            setStartTag(tag);
        }
        updateEnablements();
    }

    private CVSTag getTagFor(String text, boolean versionsOnly) {
        if (text.equals(CVSTag.DEFAULT.getName())) {
            if (versionsOnly) return null;
            return CVSTag.DEFAULT;
        }
        if (text.equals(CVSTag.BASE.getName())) {
            if (versionsOnly) return null;
            return CVSTag.BASE;
        }
        CVSTag[] tags;
        if (versionsOnly) {
            tags = tagSource.getTags(new int[] { CVSTag.VERSION, CVSTag.DATE });
        } else {
            tags = tagSource.getTags(new int[] { CVSTag.VERSION, CVSTag.BRANCH, CVSTag.DATE });
        }
        for (int i = 0; i < tags.length; i++) {
            CVSTag tag = tags[i];
            if (tag.getName().equals(text)) {
                return tag;
            }
        }
        return null;
    }

    protected void setEndTag(CVSTag selectedTag) {
        if (selectedTag == null || endTag == null || !endTag.equals(selectedTag)) {
	        endTag = selectedTag;
	        if (endTagField != null) {
	            String name = endTagField.getText();
	            if (endTag != null)
	                name = endTag.getName();
	            if (!endTagField.getText().equals(name))
	                endTagField.setText(name);
	            if (startTag == null && endTag != null && endTag.getType() == CVSTag.BRANCH) {
	                CVSTag tag = findCommonBaseTag(endTag);
	                if (tag != null) {
	                    setStartTag(tag);
	                }
	            }
	        }
	        updateEnablements();           
        }
    }

    protected void setStartTag(CVSTag selectedTag) {
        if (selectedTag == null || startTag != null || !endTag.equals(selectedTag)) {
	        startTag = selectedTag;
	        if (startTagField != null) {
	            String name = startTagField.getText();
	            if (startTag != null)
	                name = startTag.getName();
	            if (!startTagField.getText().equals(name))
	                startTagField.setText(name);
	        }
	        updateEnablements();
        }
    }
    
    private CVSTag findCommonBaseTag(CVSTag tag) {
        CVSTag[] tags = tagSource.getTags(CVSTag.VERSION);
        for (int i = 0; i < tags.length; i++) {
            CVSTag potentialMatch = tags[i];
            if (potentialMatch.getName().indexOf(tag.getName()) != -1) {
                return potentialMatch;
            }
        }
        return null;
    }

    private void updateEnablements() {
        if (endTag == null && endTagField.getText().length() > 0) {
            setErrorMessage(Policy.bind("MergeWizardPage.10")); //$NON-NLS-1$
        } else if (startTag == null && startTagField.getText().length() > 0) {
            setErrorMessage(Policy.bind("MergeWizardPage.11")); //$NON-NLS-1$
        } else if (endTag != null && startTag != null && startTag.equals(endTag)) {
            setErrorMessage(Policy.bind("MergeWizardPage.12")); //$NON-NLS-1$
        } else if (startTag == null && endTag != null && preview) {
            setErrorMessage(Policy.bind("MergeWizardPage.13")); //$NON-NLS-1$
        } else {
            setErrorMessage(null);
        }
        setPageComplete((startTag != null || !preview) && endTag != null && (startTag == null || !startTag.equals(endTag)));
    }

    protected TagSource getTagSource() {
         return tagSource;
    }

    private Button createPushButton(Composite parent, String label) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(label);
        b.setLayoutData(new GridData(GridData.END | GridData.HORIZONTAL_ALIGN_FILL));
        return b;
    }

    public CVSTag getStartTag() {
        return startTag;
    }
    
    public CVSTag getEndTag() {
        return endTag;
    }

    public boolean isPreview() {
        return preview;
    }
    
    public void setVisible(boolean visible) {
        endTagField.setFocus();
        super.setVisible(visible);
    }
}
