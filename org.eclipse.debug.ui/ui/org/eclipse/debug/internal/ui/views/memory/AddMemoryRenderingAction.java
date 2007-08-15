/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     WindRiver - Bug 192028 [Memory View] Memory view does not 
 *                 display memory blocks that do not reference IDebugTarget     
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.swt.widgets.Shell;
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
		
		// pop up dialog and ask what renderings to create
		Shell shell = DebugUIPlugin.getShell();
		
		AddMemoryRenderingDialog dialog = new AddMemoryRenderingDialog(shell, fSite);
		dialog.open();
		
		// get a list of renderings to create
		Object[] renderings = dialog.getResult();
		
		IMemoryBlock blk = dialog.getMemoryBlock();
		
		if (blk == null)
			return;
		
		// ask for debug target and memory block retrieval
		
		// add memory renderings to Memory Rendering Manager
		for (int i=0; i<renderings.length; i++)
		{	
			if (renderings[i] instanceof IMemoryRenderingType)
			{
				try {
					IMemoryRendering rendering = ((IMemoryRenderingType)renderings[i]).createRendering();
					if (rendering != null)
					{
						rendering.init(fContainer, blk);
						fContainer.addMemoryRendering(rendering);
					}
				} catch (CoreException e) {
					MemoryViewUtil.openError(DebugUIMessages.AddMemoryRenderingAction_Add_rendering_failed, DebugUIMessages.AddMemoryRenderingAction_Unable_to_add_selected_renderings, e); 
				}
			}					
		}
	}
	
}
