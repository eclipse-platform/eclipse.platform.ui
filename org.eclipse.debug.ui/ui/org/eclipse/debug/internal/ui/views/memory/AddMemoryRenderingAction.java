/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     WindRiver - Bug 192028 [Memory View] Memory view does not 
 *                 display memory blocks that do not reference IDebugTarget     
 *     Ted Williams - WindRiver - Bug 215432 - [Memory View] Memory View: Workflow Enhancements
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.ui.PlatformUI;

/**
 * Toolbar "Add Memory Rendering Action" from Memory Rendering Pane
 */
public class AddMemoryRenderingAction extends AddMemoryBlockAction {
	
	private IMemoryRenderingContainer fContainer;
	
	public AddMemoryRenderingAction(IMemoryRenderingContainer container)
	{
		super(DebugUIMessages.AddMemoryRenderingAction_Add_renderings, AS_PUSH_BUTTON, container.getMemoryRenderingSite()); 
		setToolTipText(DebugUIMessages.AddMemoryRenderingAction_Add_renderings); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, DebugUIPlugin.getUniqueIdentifier() + ".AddRenderingContextAction_context"); //$NON-NLS-1$
		fContainer = container;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (fContainer instanceof RenderingViewPane)
			((RenderingViewPane) fContainer).showCreateRenderingTab();
	}
}

