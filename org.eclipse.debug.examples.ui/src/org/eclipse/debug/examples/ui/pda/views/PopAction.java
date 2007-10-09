/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.ui.pda.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.examples.core.pda.model.PDADebugTarget;
import org.eclipse.debug.examples.ui.pda.DebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;


/**
 * Pops a selected value off the data stack. The selection does <b>not</b> have to be
 * the top element on the stack.
 */
public class PopAction extends Action implements ISelectionChangedListener {
    
    private DataStackView fView;

    /**
     * Constructs an action to pop values off the stack 
     */
    protected PopAction(DataStackView view) {
        super("Pop");
        ImageRegistry imageRegistry = DebugUIPlugin.getDefault().getImageRegistry();
        setImageDescriptor(imageRegistry.getDescriptor(DebugUIPlugin.IMG_ELCL_POP));
        setDisabledImageDescriptor(imageRegistry.getDescriptor(DebugUIPlugin.IMG_DLCL_POP));
        setToolTipText("Pop");
        setEnabled(false);
        view.getSite().getSelectionProvider().addSelectionChangedListener(this);
        fView = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection().isEmpty()) {
            setEnabled(false);
        } else {
            setEnabled(getDebugTarget().canPop());
        } 
    }

    public void run() {
        TreeViewer viewer = (TreeViewer)fView.getViewer();
        IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
        Object popee = selection.getFirstElement();
        try {
            PDADebugTarget target = getDebugTarget();
            IValue[] stack = target.getDataStack();
	        List restore = new ArrayList();
	        for (int i = 0; i < stack.length; i++) {
	            Object value = stack[i];
	            if (popee.equals(value)) {
	                // pop & stop
	                target.pop();
	                break;
	            } else {
	                // remember value to push back on
	                restore.add(target.pop());
	            }
	        }
	        while (!restore.isEmpty()) {
	            IValue value = (IValue) restore.remove(restore.size() - 1);
	            target.push(value.getValueString());
	        }
        } catch (DebugException e) {
        }
        viewer.refresh();
    }
    
    /**
     * Returns the debug target assocaited with the data view.
     * 
     * @return the debug target assocaited with the data view
     */
    protected PDADebugTarget getDebugTarget() {
        TreeViewer viewer = (TreeViewer)fView.getViewer();
        return (PDADebugTarget) viewer.getInput();
    }
}
