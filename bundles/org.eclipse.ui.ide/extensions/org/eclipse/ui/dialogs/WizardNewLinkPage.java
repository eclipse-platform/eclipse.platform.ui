/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * 	 	font should be activated and used by other components.
 *******************************************************************************/

package org.eclipse.ui.dialogs;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.dialogs.PathVariableSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * Standard resource link page for a wizard that creates a file or 
 * folder resource.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class WizardNewLinkPage extends WizardPage {
    private String initialLinkTarget;

    private int type;

    private boolean createLink = false;

    private IContainer container;

    // widgets
    private Text linkTargetField;

    private Button browseButton;

    private Button variablesButton;

    /**
     * Creates a new resource link wizard page. 
     *
     * @param pageName the name of the page
     * @param type specifies the type of resource to link to. 
     * 	<code>IResource.FILE</code> or <code>IResource.FOLDER</code>
     */
    public WizardNewLinkPage(String pageName, int type) {
        super(pageName);
        this.type = type;
        setPageComplete(true);
    }

    /* (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        initializeDialogUnits(parent);
        // top level group
        Composite topLevel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        topLevel.setLayout(layout);
        topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        topLevel.setFont(font);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(topLevel,
				IIDEHelpContextIds.NEW_LINK_WIZARD_PAGE);

        final Button createLinkButton = new Button(topLevel, SWT.CHECK);
        if (type == IResource.FILE) {
			createLinkButton.setText(IDEWorkbenchMessages.WizardNewLinkPage_linkFileButton);
		} else {
			createLinkButton.setText(IDEWorkbenchMessages.WizardNewLinkPage_linkFolderButton);
		}
        createLinkButton.setSelection(createLink);
        GridData data = new GridData();
        data.horizontalSpan = 3;
        createLinkButton.setLayoutData(data);
        createLinkButton.setFont(font);
        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                createLink = createLinkButton.getSelection();
                browseButton.setEnabled(createLink);
                variablesButton.setEnabled(createLink);
                linkTargetField.setEnabled(createLink);
                setPageComplete(validatePage());
            }
        };
        createLinkButton.addSelectionListener(listener);

        createLinkLocationGroup(topLevel, createLink);
        validatePage();

        setErrorMessage(null);
        setMessage(null);
        setControl(topLevel);
    }

    /**
     * Creates the link target location widgets.
     *
     * @param locationGroup the parent composite
     * @param enabled sets the initial enabled state of the widgets
     */
    private void createLinkLocationGroup(Composite locationGroup,
            boolean enabled) {
        Font font = locationGroup.getFont();
        Label fill = new Label(locationGroup, SWT.NONE);
        GridData data = new GridData();
        Button button = new Button(locationGroup, SWT.CHECK);
        data.widthHint = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        button.dispose();
        fill.setLayoutData(data);

        // link target location entry field
        linkTargetField = new Text(locationGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        linkTargetField.setLayoutData(data);
        linkTargetField.setFont(font);
        linkTargetField.setEnabled(enabled);
        linkTargetField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setPageComplete(validatePage());
            }
        });
        if (initialLinkTarget != null) {
            linkTargetField.setText(initialLinkTarget);
        }

        // browse button
        browseButton = new Button(locationGroup, SWT.PUSH);
        setButtonLayoutData(browseButton);
        browseButton.setFont(font);
        browseButton.setText(IDEWorkbenchMessages.WizardNewLinkPage_browseButton);
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleLinkTargetBrowseButtonPressed();
            }
        });
        browseButton.setEnabled(enabled);

        fill = new Label(locationGroup, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        fill.setLayoutData(data);

        // variables button
        variablesButton = new Button(locationGroup, SWT.PUSH);
        setButtonLayoutData(variablesButton);
        variablesButton.setFont(font);
        variablesButton.setText(IDEWorkbenchMessages.WizardNewLinkPage_variablesButton);
        variablesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleVariablesButtonPressed();
            }
        });
        variablesButton.setEnabled(enabled);
    }

    /**
     * Returns the link target location entered by the user. 
     *
     * @return the link target location entered by the user. null if the user
     * 	choose not to create a link.
     */
    public String getLinkTarget() {
        if (createLink && linkTargetField != null
                && linkTargetField.isDisposed() == false) {
            return linkTargetField.getText();
        }
        return null;
    }

    /**
     * Opens a file or directory browser depending on the link type.
     */
    private void handleLinkTargetBrowseButtonPressed() {
        String linkTargetName = linkTargetField.getText();
         String selection = null;
         IFileStore store = null;
        if (linkTargetName.length() > 0) {
            store = IDEResourceInfoUtils.getFileStore(linkTargetName);
            if (store == null || !store.fetchInfo().exists()) {
            	store = null;
            }
        }
        if (type == IResource.FILE) {
            FileDialog dialog = new FileDialog(getShell(), SWT.SHEET);
            if (store != null) {
                if (store.fetchInfo().isDirectory()) {
					dialog.setFilterPath(linkTargetName);
				} else {
					dialog.setFileName(linkTargetName);
				}
            }
            selection = dialog.open();
        } else {
            DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
            if (store != null) {
                if (!store.fetchInfo().isDirectory()) {
                    linkTargetName = store.getParent().getName();
                }
                if (linkTargetName != null) {
                    dialog.setFilterPath(linkTargetName);
                }
            }
            dialog.setMessage(IDEWorkbenchMessages.WizardNewLinkPage_targetSelectionLabel);
            selection = dialog.open();
        }
        if (selection != null) {
            linkTargetField.setText(selection);
        }
    }

    /**
     * Opens a path variable selection dialog
     */
    private void handleVariablesButtonPressed() {
        PathVariableSelectionDialog dialog = new PathVariableSelectionDialog(
                getShell(), type);

        if (dialog.open() == IDialogConstants.OK_ID) {
            String[] variableNames = (String[]) dialog.getResult();

            if (variableNames != null) {
                IPathVariableManager pathVariableManager = ResourcesPlugin
                        .getWorkspace().getPathVariableManager();
                IPath path = pathVariableManager.getValue(variableNames[0]);

                if (path != null) {
                    linkTargetField.setText(path.toOSString());
                }
            }
        }
    }

    /**
     * Sets the container to use for link validation.
     * This should be the parent of the new resource that is being 
     * linked.
     *
     * @param container the container to use for link validation.
     */
    public void setContainer(IContainer container) {
        this.container = container;
    }

    /**
     * Sets the value of the link target field
     * 
     * @param target the value of the link target field
     */
    public void setLinkTarget(String target) {
        initialLinkTarget = target;
        if (linkTargetField != null && linkTargetField.isDisposed() == false) {
            linkTargetField.setText(target);
        }
    }

    /**
     * Validates the type of the given file against the link type specified
     * during page creation.
     * 
     * @param linkTargetStore file to validate
     * @return boolean <code>true</code> if the link target type is valid
     * 	and <code>false</code> otherwise.
     */
    private boolean validateFileType(IFileStore linkTargetStore) {
        boolean valid = true;

        if (type == IResource.FILE && linkTargetStore.fetchInfo().isDirectory()) {
            setErrorMessage(IDEWorkbenchMessages.WizardNewLinkPage_linkTargetNotFile);
            valid = false;
        } else if (type == IResource.FOLDER
                && !linkTargetStore.fetchInfo().isDirectory()) {
            setErrorMessage(IDEWorkbenchMessages.WizardNewLinkPage_linkTargetNotFolder);
            valid = false;
        }
        return valid;
    }

    /**
     * Validates the name of the link target
     *
     * @param linkTargetName link target name to validate
     * @return boolean <code>true</code> if the link target name is valid
     * 	and <code>false</code> otherwise.
     */
    private boolean validateLinkTargetName(String linkTargetName) {
        boolean valid = true;

        if ("".equals(linkTargetName)) {//$NON-NLS-1$
            setErrorMessage(IDEWorkbenchMessages.WizardNewLinkPage_linkTargetEmpty);
            valid = false;
        } else {
            IPath path = new Path("");//$NON-NLS-1$
            if (path.isValidPath(linkTargetName) == false) {
                setErrorMessage(IDEWorkbenchMessages.WizardNewLinkPage_linkTargetInvalid);
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    private boolean validatePage() {
        boolean valid = true;
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

        if (createLink) {
            String linkTargetName = linkTargetField.getText();

            valid = validateLinkTargetName(linkTargetName);
            if (valid) {
                IFileStore linkTargetFile = IDEResourceInfoUtils.getFileStore(linkTargetName);
                if (linkTargetFile == null || !linkTargetFile.fetchInfo().exists()) {
                    setErrorMessage(IDEWorkbenchMessages.WizardNewLinkPage_linkTargetNonExistent);
                    valid = false;
                } else {
                    IStatus locationStatus = workspace.validateLinkLocation(
                            container, new Path(linkTargetName));

                    if (locationStatus.isOK() == false) {
                        setErrorMessage(IDEWorkbenchMessages.WizardNewLinkPage_linkTargetLocationInvalid);
                        valid = false;
                    } else {
                        valid = validateFileType(linkTargetFile);
                    }
                }
            }
        }
        // Avoid draw flicker by clearing error message
        // if all is valid.
        if (valid) {
            setMessage(null);
            setErrorMessage(null);
        }
        return valid;
    }
}
