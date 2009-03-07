/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

/**
 * The FilterSelectionAction opens the filters dialog.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class FilterSelectionAction extends ResourceNavigatorAction {
    private static final String FILTER_TOOL_TIP = ResourceNavigatorMessages.FilterSelection_toolTip;

    private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessages.FilterSelection_message;

    private static final String FILTER_TITLE_MESSAGE = ResourceNavigatorMessages.FilterSelection_title;

    /**
     * Creates the action.
     * 
     * @param navigator the resource navigator
     * @param label the label for the action
     */
    public FilterSelectionAction(IResourceNavigator navigator, String label) {
        super(navigator, label);
        setToolTipText(FILTER_TOOL_TIP);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                INavigatorHelpContextIds.FILTER_SELECTION_ACTION);
        setEnabled(true);
    }

    /*
     * Implementation of method defined on <code>IAction</code>.
     */
    public void run() {
        IResourceNavigator navigator = getNavigator();
        ResourcePatternFilter filter = navigator.getPatternFilter();
        FiltersContentProvider contentProvider = new FiltersContentProvider(
                filter);

        ListSelectionDialog dialog = new ListSelectionDialog(getShell(),
                getViewer(), contentProvider, new LabelProvider(),
                FILTER_SELECTION_MESSAGE);

        dialog.setTitle(FILTER_TITLE_MESSAGE);
        dialog.setInitialSelections(contentProvider.getInitialSelections());
        dialog.open();
        if (dialog.getReturnCode() == Window.OK) {
            Object[] results = dialog.getResult();
            String[] selectedPatterns = new String[results.length];
            System.arraycopy(results, 0, selectedPatterns, 0, results.length);
            filter.setPatterns(selectedPatterns);
            navigator.setFiltersPreference(selectedPatterns);
            Viewer viewer = getViewer();
            viewer.getControl().setRedraw(false);
            viewer.refresh();
            viewer.getControl().setRedraw(true);
        }
    }

}
