/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;

/**
 * Action to toggle the use of contributed variables content providers on and off.
 * When on, all registered variables content providers for the current debug model
 * are used.  When off, the default content provider (that shows all children)
 * is used for all debug models.
 */
public class ToggleShowColumnsAction extends Action {
	
	private VariablesView fView;

	public ToggleShowColumnsAction(VariablesView view) {
		super(VariablesViewMessages.ToggleShowColumnsAction_0, IAction.AS_CHECK_BOX);
		setView(view);
		setToolTipText(VariablesViewMessages.ToggleShowColumnsAction_1);  
		setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_OBJS_COMMON_TAB));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".ToggleShowColumsAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.SHOW_COLUMNS_ACTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (!getView().isAvailable()) {
			return;
		}
		getView().setShowColumns(isChecked());	
		BusyIndicator.showWhile(getView().getViewer().getControl().getDisplay(), new Runnable() {
			public void run() {
				((AsynchronousTreeViewer)getView().getViewer()).refreshColumns();					
			}
		});		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#setChecked(boolean)
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
	}
	
	protected VariablesView getView() {
		return fView;
	}

	protected void setView(VariablesView view) {
		fView = view;
	}

}
