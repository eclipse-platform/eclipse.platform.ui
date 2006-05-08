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
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.tests.harness.util.CallHistory;

public class MockEditorActionBarContributor extends EditorActionBarContributor {
    protected CallHistory callHistory;

    protected IEditorPart target;

    protected int ACTION_COUNT = 5;

    protected MockAction[] actions;

    /**
     * Constructor for MockEditorActionBarContributor
     */
    public MockEditorActionBarContributor() {
        super();
        callHistory = new CallHistory(this);
    }

    public CallHistory getCallHistory() {
        return callHistory;
    }

    /**
     * @see IEditorActionBarContributor#init(IActionBars)
     */
    public void init(IActionBars bars) {
        callHistory.add("init");
        actions = new MockAction[ACTION_COUNT];
        for (int nX = 0; nX < ACTION_COUNT; nX++) {
            actions[nX] = new MockAction(Integer.toString(nX));
            if (nX % 2 > 0)
                actions[nX].setEnabled(false);
        }
        super.init(bars);
    }

    /**
     * @see EditorActionBarContributor#contributeToToolBar(IToolBarManager)
     */
    public void contributeToToolBar(IToolBarManager toolBarManager) {
        for (int i = 0; i < actions.length; ++i) {
            toolBarManager.add(actions[i]);
        }
    }

    /**
     * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
     */
    public void setActiveEditor(IEditorPart targetEditor) {
        callHistory.add("setActiveEditor");
        target = targetEditor;
    }

    /**
     * Returns the active editor.
     */
    public IEditorPart getActiveEditor() {
        return target;
    }

    /**
     * Returns the actions.
     */
    public MockAction[] getActions() {
        return actions;
    }

    /**
     * Set the enablement for all actions.
     */
    public void enableActions(boolean b) {
        for (int nX = 0; nX < ACTION_COUNT; nX++) {
            actions[nX].setEnabled(b);
        }
    }

}

