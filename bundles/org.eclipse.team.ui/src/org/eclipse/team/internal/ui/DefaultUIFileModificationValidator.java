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
package org.eclipse.team.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.core.DefaultFileModificationValidator;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;

/**
 * Override the default file modification validator to prompt to
 * make read-only files writtable
 */
public class DefaultUIFileModificationValidator extends DefaultFileModificationValidator {

    public static class FileListDialog extends DetailsDialog {

        private final IFile[] files;

        public static boolean openQuestion(Shell shell, IFile[] files) {
            FileListDialog dialog = new FileListDialog(shell, files);
            int code = dialog.open();
            return code == OK;
        }
        
        public FileListDialog(Shell parentShell, IFile[] files) {
            super(parentShell, Policy.bind("DefaultUIFileModificationValidator.0")); //$NON-NLS-1$
            this.files = files;
			setImageKey(DLG_IMG_WARNING);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
         */
        protected void createMainDialogArea(Composite parent) {
			createWrappingLabel(parent, Policy.bind("DefaultUIFileModificationValidator.1")); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createDropDownDialogArea(org.eclipse.swt.widgets.Composite)
         */
        protected Composite createDropDownDialogArea(Composite parent) {
			Composite composite = createComposite(parent);
			createWrappingLabel(composite, Policy.bind("DefaultUIFileModificationValidator.2")); //$NON-NLS-1$
			org.eclipse.swt.widgets.List fileList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);	 
			GridData data = new GridData ();		
			data.heightHint = 75;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			fileList.setLayoutData(data);
			fileList.setFont(parent.getFont());
			for (int i = 0; i < files.length; i++) {
				fileList.add(files[i].getFullPath().toString());
			}			
			return composite;
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
         */
        protected void updateEnablements() {
            // Nothing to do
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeCancelButton()
         */
        protected boolean includeCancelButton() {
            return false;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeOkButton()
         */
        protected boolean includeOkButton() {
            return false;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
            createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, true);
            super.createButtonsForButtonBar(parent);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#buttonPressed(int)
         */
        protected void buttonPressed(int id) {
            if (IDialogConstants.YES_ID == id)
                okPressed();
            else if (IDialogConstants.NO_ID == id)
                cancelPressed();
            else
                super.buttonPressed(id);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.core.DefaultFileModificationValidator#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
     */
    public IStatus validateEdit(final IFile[] allFiles, Object context) {
    	final IFile[] readOnlyFiles = getReadOnlyFiles(allFiles);
        if (readOnlyFiles.length > 0 && context != null && context instanceof Shell) {
            final Shell shell = (Shell)context;
            final boolean[] ok = new boolean[] { false };
            if (readOnlyFiles.length == 1) {
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        ok[0] = MessageDialog.openQuestion(shell, Policy.bind("DefaultUIFileModificationValidator.3"), Policy.bind("DefaultUIFileModificationValidator.4", readOnlyFiles[0].getFullPath().toString())); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                });
            } else {
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        ok[0] = FileListDialog.openQuestion(shell, readOnlyFiles);
                    }
                });
            }
            if (ok[0]) {
                setWritable(readOnlyFiles);
            };
        }
        return getStatus(readOnlyFiles);
    }
    
	private IFile[] getReadOnlyFiles(IFile[] files) {
		List result = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IFile file = files[i];
			if (file.isReadOnly()) {
				result.add(file);
			}
		}
		return (IFile[]) result.toArray(new IFile[result.size()]);
	}

	protected IStatus setWritable(final IFile[] files) {
        for (int i = 0; i < files.length; i++) {
        	IFile file = files[i];
        	ResourceAttributes attributes = file.getResourceAttributes();
        	if (attributes != null) {
        		attributes.setReadOnly(false);
        	}
        	try {
        		file.setResourceAttributes(attributes);
        	} catch (CoreException e) {
        		return e.getStatus();
        	}
        }
        return Status.OK_STATUS;
    }
}
