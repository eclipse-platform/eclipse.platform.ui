/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - bug 34548
 * 	   Nico Seessle - bug 51319
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.outline;

import org.eclipse.ant.internal.ui.editor.model.AntProjectNode;
import org.eclipse.ant.internal.ui.editor.model.AntTargetNode;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * Action delegate to launch Ant on a build file.
 */
public class AntRunTargetAction extends Action {

    private AntEditorContentOutlinePage page;

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
            return AntOutlineMessages.getString("PopupMenu.runAntTargetNoDialog"); //$NON-NLS-1$
        }

        public String getToolTipText() {
            return AntOutlineMessages.getString("PopupMenu.runAntTargetNoDialogTip"); //$NON-NLS-1$
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
     * @param mode Use either {@link AntRunTargetAction#MODE_DISPLAY_DIALOG} or
     *            {@link AntRunTargetAction#MODE_IMMEDIATE_EXECUTE}
     */
    public AntRunTargetAction(AntEditorContentOutlinePage page, RunTargetMode mode) {
        this.page = page;
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
		selectedFile= null;
		selectedTarget= null;
		IEditorInput editorInput= page.getSite().getPage().getActiveEditor().getEditorInput();
		IPath filePath= null;
		if (editorInput instanceof IFileEditorInput) {
			selectedFile = ((IFileEditorInput) page.getSite().getPage().getActiveEditor().getEditorInput()).getFile();
	    } else if (editorInput instanceof ILocationProvider) {
	    	filePath= ((ILocationProvider)editorInput).getPath(editorInput);
	    }
		if (selectedFile == null && filePath == null) {
			return;
		}
		ISelection selection= page.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if (structuredSelection.size() == 1) {
				Object selectedResource = structuredSelection.getFirstElement();
				if (selectedResource instanceof AntTargetNode) {
					selectedTarget = ((AntTargetNode)selectedResource).getTarget().getName();
				} else if (selectedResource instanceof AntProjectNode) {
					selectedTarget = ""; //$NON-NLS-1$
				}
			}
		}
		
		if (selectedTarget == null) {
			return;
		}
		AntLaunchShortcut shortcut = new AntLaunchShortcut();
		shortcut.setShowDialog(mode.isDialogDisplayed());
		if (selectedFile != null) {
			shortcut.launch(selectedFile, ILaunchManager.RUN_MODE, selectedTarget);
		} else { //external buildfile
			shortcut.launch(filePath, ILaunchManager.RUN_MODE, selectedTarget);
		}
	}
}