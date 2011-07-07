/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * Action that controls the preference for whether elements should be 
 * automatically expanded in the breadcrumb drop down viewers.
 * 
 * @since 3.5
 */
class BreadcrumbDropDownAutoExpandAction extends Action {

	private final LaunchView fLaunchView;

	/**
	 * Creates a new action to set the debug view mode.
	 * 
	 * @param view Reference to the debug view.
     * in auto mode.
	 */
	public BreadcrumbDropDownAutoExpandAction(LaunchView view) {
		super(IInternalDebugCoreConstants.EMPTY_STRING, AS_CHECK_BOX);
		fLaunchView = view;
				
		setText(LaunchViewMessages.BreadcrumbDropDownAutoExpandAction_label);
		setToolTipText(LaunchViewMessages.BreadcrumbDropDownAutoExpandAction_tooltip);  
		setDescription(LaunchViewMessages.BreadcrumbDropDownAutoExpandAction_description);  
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.DEBUG_VIEW_DROP_DOWN_AUTOEXPAND_ACTION);
		
		setChecked(fLaunchView.getBreadcrumbDropDownAutoExpand());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fLaunchView.setBreadcrumbDropDownAutoExpand(isChecked()); 
	}	
}

