/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - bug 34548
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.outline;

import org.eclipse.ant.internal.ui.editor.xml.XmlElement;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ant.internal.ui.model.AntUIImages;

/**
 * Action delegate to launch Ant on a build file.
 */
public class AntRunTargetAction extends Action implements ISelectionChangedListener {

    private IEditorPart part;

    private RunTargetMode mode;

    private IFile selectedFile;

    private String selectedTarget;

    /**
     * Defines modes of operation for this action.
     */
    public interface RunTargetMode {
        String getText();

        String getToolTipText();

        String getId();

        boolean isDialogDisplayed();

        ImageDescriptor getImageDescriptor();
    }

    /**
     * Use this mode to run execute a target without prompting the user with
     * the Run Ant dialog.
     */
    public static final RunTargetMode MODE_IMMEDIATE_EXECUTE = new RunTargetMode() {

        public String getText() {
            return AntOutlineMessages
                    .getString("PopupMenu.runAntTargetNoDialog"); //$NON-NLS-1$
        }

        public String getToolTipText() {
            return AntOutlineMessages
                    .getString("PopupMenu.runAntTargetNoDialogTip"); //$NON-NLS-1$
        }

        public boolean isDialogDisplayed() {
            return false;
        }

        public String getId() {
            return IAntUIConstants.PLUGIN_ID + ".RunAntTargetNoDialogAction"; //$NON-NLS-1$
        }

        public ImageDescriptor getImageDescriptor() {
            return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_RUN);
        }
    };

    /**
     * Use this mode to display the dialog with the selected targets.
     */
    public static final RunTargetMode MODE_DISPLAY_DIALOG = new RunTargetMode() {

        public String getText() {
            return AntOutlineMessages.getString("PopupMenu.runAntTarget"); //$NON-NLS-1$
        }

        public String getToolTipText() {
            return AntOutlineMessages.getString("PopupMenu.runAntTargetTip"); //$NON-NLS-1$
        }

        public boolean isDialogDisplayed() {
            return true;
        }

        public String getId() {
            return IAntUIConstants.PLUGIN_ID + ".RunAntTargetAction"; //$NON-NLS-1$
        }

        public ImageDescriptor getImageDescriptor() {
            return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_TAB_ANT_TARGETS);
        }
    };

    /**
     * @param part
     * @param mode
     *            Use either {@link AntRunTargetAction#MODE_DISPLAY_DIALOG}or
     *            {@link AntRunTargetAction#MODE_IMMEDIATE_EXECUTE}
     */
    public AntRunTargetAction(IEditorPart part, RunTargetMode mode) {
        this.part = part;
        this.mode = mode;
        super.setId(mode.getId());
        super.setText(mode.getText());
        super.setToolTipText(mode.getToolTipText());
        super.setImageDescriptor(mode.getImageDescriptor());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        if (part != null && selectedFile != null) {
            AntLaunchShortcut shortcut = new AntLaunchShortcut();
            shortcut.setShowDialog(this.mode.isDialogDisplayed());
            shortcut.launch(selectedFile, ILaunchManager.RUN_MODE, selectedTarget);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {

        this.selectedFile = null;
        this.selectedTarget = null;

        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) event.getSelection();
            if (structuredSelection.size() == 1) {
                Object selectedResource = structuredSelection.getFirstElement();
                if (selectedResource instanceof XmlElement
                    && "target".equalsIgnoreCase(((XmlElement) selectedResource).getName())) { //$NON-NLS-1$
                    this.selectedTarget = ((XmlElement)selectedResource).getAttributeNamed("name").getValue(); //$NON-NLS-1$
                    if (part.getEditorInput() instanceof IFileEditorInput) {
                        this.selectedFile = ((IFileEditorInput) part.getEditorInput()).getFile();
                    }
                }
            }
        }
    }
}