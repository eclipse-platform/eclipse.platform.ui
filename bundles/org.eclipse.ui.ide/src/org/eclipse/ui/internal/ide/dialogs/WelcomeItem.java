/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;

/**
 * Holds the information for an item appearing in the welcome editor
 */
public class WelcomeItem {
    private String text;

    private int[][] boldRanges;

    private int[][] helpRanges;

    private String[] helpIds;

    private String[] helpHrefs;

    private int[][] actionRanges;

    private String[] actionPluginIds;

    private String[] actionClasses;

    /**
     * Creates a new welcome item
     */
    public WelcomeItem(String text, int[][] boldRanges, int[][] actionRanges,
            String[] actionPluginIds, String[] actionClasses,
            int[][] helpRanges, String[] helpIds, String[] helpHrefs) {

        this.text = text;
        this.boldRanges = boldRanges;
        this.actionRanges = actionRanges;
        this.actionPluginIds = actionPluginIds;
        this.actionClasses = actionClasses;
        this.helpRanges = helpRanges;
        this.helpIds = helpIds;
        this.helpHrefs = helpHrefs;
    }

    /**
     * Returns the action ranges (character locations)
     */
    public int[][] getActionRanges() {
        return actionRanges;
    }

    /**
     * Returns the bold ranges (character locations)
     */
    public int[][] getBoldRanges() {
        return boldRanges;
    }

    /**
     * Returns the help ranges (character locations)
     */
    public int[][] getHelpRanges() {
        return helpRanges;
    }

    /**
     * Returns the text to display
     */
    public String getText() {
        return text;
    }

    /**
     * Returns true is a link (action or help) is present at the given character location
     */
    public boolean isLinkAt(int offset) {
        // Check if there is a link at the offset
        for (int i = 0; i < helpRanges.length; i++) {
            if (offset >= helpRanges[i][0]
                    && offset < helpRanges[i][0] + helpRanges[i][1]) {
                return true;
            }
        }

        // Check if there is an action link at the offset
        for (int i = 0; i < actionRanges.length; i++) {
            if (offset >= actionRanges[i][0]
                    && offset < actionRanges[i][0] + actionRanges[i][1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs a error to the workbench log
     */
    public void logActionLinkError(String actionPluginId, String actionClass) {
        IDEWorkbenchPlugin
                .log(IDEWorkbenchMessages.WelcomeItem_unableToLoadClass + actionPluginId + " " + actionClass); //$NON-NLS-1$
    }

    /**
     * Open a help topic
     */
    private void openHelpTopic(String topic, String href) {
        if (href != null) {
			PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(href);
		} else {
			PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(topic);
		}
    }

    /**
     * Run an action
     */
    private void runAction(String pluginId, String className) {
    	Bundle pluginBundle = Platform.getBundle(pluginId);
        if (pluginBundle == null) {
            logActionLinkError(pluginId, className);
            return;
        }
        Class actionClass;
        IAction action;
        try {
        	actionClass = pluginBundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            logActionLinkError(pluginId, className);
            return;
        }
        try {
            action = (IAction) actionClass.newInstance();
        } catch (InstantiationException e) {
            logActionLinkError(pluginId, className);
            return;
        } catch (IllegalAccessException e) {
            logActionLinkError(pluginId, className);
            return;
        } catch (ClassCastException e) {
            logActionLinkError(pluginId, className);
            return;
        }
        action.run();
    }

    /**
     * Triggers the link at the given offset (if there is one)
     */
    public void triggerLinkAt(int offset) {
        // Check if there is a help link at the offset
        for (int i = 0; i < helpRanges.length; i++) {
            if (offset >= helpRanges[i][0]
                    && offset < helpRanges[i][0] + helpRanges[i][1]) {
                // trigger the link
                openHelpTopic(helpIds[i], helpHrefs[i]);
                return;
            }
        }

        // Check if there is an action link at the offset
        for (int i = 0; i < actionRanges.length; i++) {
            if (offset >= actionRanges[i][0]
                    && offset < actionRanges[i][0] + actionRanges[i][1]) {
                // trigger the link
                runAction(actionPluginIds[i], actionClasses[i]);
                return;
            }
        }
    }
}
