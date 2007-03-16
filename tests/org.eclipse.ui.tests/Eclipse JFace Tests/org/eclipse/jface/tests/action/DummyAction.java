/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.Action;


/**
 * A dummy action, used just for testing.
 */
class DummyAction extends Action {
    
    static int Count = 0;
    
    public DummyAction() {
        super("DummyAction " + ++Count);
    }
}
