/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewContentProvider;
import org.eclipse.debug.ui.IDebugView;

/**
 * Provides contents for the registers view
 */
public class RegistersViewContentProvider extends VariablesViewContentProvider {

	public RegistersViewContentProvider(IDebugView view) {
		super(view);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IStackFrame) {
				children = ((IStackFrame)parent).getRegisterGroups();
			} else if (parent instanceof IRegisterGroup) {
				children = ((IRegisterGroup)parent).getRegisters();
			} else if (parent instanceof IVariable) {
				children = super.getChildren( parent );
			}
			if (children != null) {
				cache(parent, children);
				return children;
			}
		} catch (DebugException de) {
			if (getExceptionHandler() != null) {
				getExceptionHandler().handleException(de);
			} else {
				DebugUIPlugin.log(de);
			}
		}
		return new Object[0];
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		try {
			if (element instanceof IStackFrame) {
				return ((IStackFrame)element).hasRegisterGroups();
			}
			if (element instanceof IRegisterGroup) {
				return ((IRegisterGroup)element).hasRegisters();
			}
		} catch (DebugException de) {
			DebugUIPlugin.log(de);
			return false;
		}
		return super.hasChildren(element);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesViewContentProvider#setExceptionHandler(org.eclipse.debug.internal.ui.views.IDebugExceptionHandler)
	 */
	protected void setExceptionHandler(IDebugExceptionHandler handler) {
		super.setExceptionHandler(handler);
	}
}
