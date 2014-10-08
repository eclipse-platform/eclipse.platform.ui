/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.internal.util;

import java.util.ArrayList;

/**
 * This test pass verifies the initial focus of a dialog when it is given focus.
 */
public class FocusTestPass implements IDialogTestPass {
    private static final int CHECKLIST_SIZE = 1;

    @Override
	public String title() {
        return "Test Pass: Initial Focus";
    }

    @Override
	public String description() {
        return "Verify the initial focus of the dialogs.";
    }

    @Override
	public String label() {
        return "&Initial Focus";
    }

	@Override
	public ArrayList<String> checkListTexts() {
		ArrayList<String> list = new ArrayList<String>(CHECKLIST_SIZE);
        list.add("&1) the initial focus is appropriate.");
        return list;
    }

    @Override
	public String[] failureTexts() {
        String[] failureText = new String[CHECKLIST_SIZE];
        failureText[0] = "The initial focus is inappropriate.";
        return failureText;
    }

    @Override
	public String queryText() {
        return "Is the initial focus of the dialog correct?";
    }

    @Override
	public int getID() {
        return VerifyDialog.TEST_FOCUS;
    }
}
