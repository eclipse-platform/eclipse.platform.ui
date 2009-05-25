/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 93373 - [Intro] 
 *     		TipsAndTricksAction should not use magic numbers
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.PartEventAction;

/**
 * Launch the tips and tricks action.
 */
public class TipsAndTricksAction extends PartEventAction implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window this action is registered with.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create an instance of this class.
     * 
     * @param window the window
     */
    public TipsAndTricksAction(IWorkbenchWindow window) {
        super(IDEWorkbenchMessages.TipsAndTricks_text);
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setToolTipText(IDEWorkbenchMessages.TipsAndTricks_toolTip);
        window.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.TIPS_AND_TRICKS_ACTION);
        setActionDefinitionId(IWorkbenchCommandConstants.HELP_TIPS_AND_TRICKS);
        workbenchWindow.getPartService().addPartListener(this);
    }

    /**
     *	The user has invoked this action
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        // Ask the user to select a feature
        AboutInfo[] featureInfos = IDEWorkbenchPlugin.getDefault()
                .getFeatureInfos();
        ArrayList tipsAndTricksFeatures = new ArrayList(featureInfos.length);
        for (int i = 0; i < featureInfos.length; i++) {
            if (featureInfos[i].getTipsAndTricksHref() != null) {
				tipsAndTricksFeatures.add(featureInfos[i]);
			}
        }

        Shell shell = workbenchWindow.getShell();

        if (tipsAndTricksFeatures.size() == 0) {
            MessageDialog.openInformation(shell, IDEWorkbenchMessages.TipsAndTricksMessageDialog_title,
                    IDEWorkbenchMessages.TipsAndTricksMessageDialog_message);
            return;
        }

        AboutInfo[] features = new AboutInfo[tipsAndTricksFeatures.size()];
        tipsAndTricksFeatures.toArray(features);

        IProduct product = Platform.getProduct();
        FeatureSelectionDialog d = new FeatureSelectionDialog(shell, features,
                product == null ? null : product.getId(), IDEWorkbenchMessages.TipsAndTricksPageSelectionDialog_title,
                IDEWorkbenchMessages.TipsAndTricksPageSelectionDialog_message,
                IIDEHelpContextIds.TIPS_AND_TRICKS_PAGE_SELECTION_DIALOG);
        d.create();
        d.getOkButton().setEnabled(false);
        
        if (d.open() != Window.OK || d.getResult().length != 1) {
			return;
		}

        AboutInfo feature = (AboutInfo) d.getResult()[0];

        /**
         * Open the tips and trick help topic
         */
        if (feature != null) {
            final String href = feature.getTipsAndTricksHref();
            if (href != null) {
                BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
                    public void run() {
                        workbenchWindow.getWorkbench().getHelpSystem()
								.displayHelpResource(href);
                    }
                });
            } else {
                IStatus status = new Status(
                        IStatus.ERROR,
                        IDEWorkbenchPlugin.IDE_WORKBENCH,
                        IStatus.INFO,
                        IDEWorkbenchMessages.TipsAndTricksErrorDialog_noHref, null);
                ErrorDialog.openError(shell, IDEWorkbenchMessages.TipsAndTricksErrorDialog_title,
                        IDEWorkbenchMessages.TipsAndTricksErrorDialog_noHref,
                        status);
            }
        } else {
            IStatus status = new Status(IStatus.ERROR,
                    IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.INFO, IDEWorkbenchMessages.TipsAndTricksErrorDialog_noHref, null);
            ErrorDialog.openError(shell, IDEWorkbenchMessages.TipsAndTricksErrorDialog_title,
                    IDEWorkbenchMessages.TipsAndTricksErrorDialog_noFeatures, 
                    status);
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        workbenchWindow.getPartService().removePartListener(this);
        workbenchWindow = null;
    }
}
