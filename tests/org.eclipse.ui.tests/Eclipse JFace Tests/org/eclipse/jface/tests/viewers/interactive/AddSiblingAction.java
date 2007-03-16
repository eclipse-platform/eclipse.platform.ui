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
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelChange;

public class AddSiblingAction extends TestSelectionAction {

    int fEventKind;

    public AddSiblingAction(String label, TestBrowser browser) {
        this(label, browser, TestModelChange.INSERT);
    }

    public AddSiblingAction(String label, TestBrowser browser, int eventKind) {
        super(label, browser);
        fEventKind = eventKind;
    }

    public void run(TestElement element) {
        element.getContainer().addChild(fEventKind);
    }
}
