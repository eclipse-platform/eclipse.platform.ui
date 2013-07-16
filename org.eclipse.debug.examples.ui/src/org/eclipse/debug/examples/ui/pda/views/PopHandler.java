/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - bug fixing
 *******************************************************************************/
package org.eclipse.debug.examples.ui.pda.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.examples.core.pda.model.PDAThread;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Pops a selected value off the data stack. The selection does <b>not</b> have to be
 * the top element on the stack.
 */
public class PopHandler extends AbstractDataStackViewHandler {

    protected void doExecute(DataStackView view, PDAThread thread, ISelection selection) throws ExecutionException {
        TreeViewer viewer = (TreeViewer)view.getViewer();
        Object popee = selection instanceof IStructuredSelection 
            ? ((IStructuredSelection)selection).getFirstElement() : null;
        if (popee != null) {
            try {
                IValue[] stack = thread.getDataStack();
                List restore = new ArrayList();
                for (int i = 0; i < stack.length; i++) {
                    Object value = stack[i];
                    if (popee.equals(value)) {
                        // pop & stop
                        thread.popData();
                        break;
                    } else {
                        // remember value to push back on
                        restore.add(thread.popData());
                    }
                }
                while (!restore.isEmpty()) {
                    IValue value = (IValue) restore.remove(restore.size() - 1);
                    thread.pushData(value.getValueString());
                }
            } catch (DebugException e) {
				throw new ExecutionException("Failed to execute push command", e); //$NON-NLS-1$
            }
            viewer.refresh();
        }
    }

}
