/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Action to open the help contents.
 * 
 * @since 3.0
 */
public class HelpContentsAction extends Action implements IWorkbenchAction {
    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Zero-arg constructor to allow cheat sheets to reuse this action.
     */
    public HelpContentsAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * Constructor for use by ActionFactory.
     */
    public HelpContentsAction(IWorkbenchWindow window) {
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.help.helpContents"); //$NON-NLS-1$

        // support for allowing a product to override the text for the action
        String overrideText = PrefUtil.getAPIPreferenceStore().getString(
                IWorkbenchPreferenceConstants.HELP_CONTENTS_ACTION_TEXT);
        if ("".equals(overrideText)) { //$NON-NLS-1$
            setText(WorkbenchMessages.getString("HelpContentsAction.text")); //$NON-NLS-1$ 
            setToolTipText(WorkbenchMessages
                    .getString("HelpContentsAction.toolTip")); //$NON-NLS-1$
        } else {
            setText(overrideText);
            setToolTipText(Action.removeMnemonics(overrideText));
        }
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS));
        WorkbenchHelp.setHelp(this, IHelpContextIds.HELP_CONTENTS_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        //This may take a while, so use the busy indicator
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
                WorkbenchHelp.displayHelp();
            }
        });
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow = null;
    }

}