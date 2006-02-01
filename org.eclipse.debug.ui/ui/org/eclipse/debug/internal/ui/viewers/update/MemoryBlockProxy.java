/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;

public class MemoryBlockProxy extends EventHandlerModelProxy  {
	
	private IMemoryBlock fMemoryBlock;
	private DebugEventHandler fDebugEventHandler = new DebugEventHandler(this)  {

		protected boolean handlesEvent(DebugEvent event) {
			if (event.getKind() == DebugEvent.CHANGE && event.getSource() == fMemoryBlock)
				return true;
			
			Object src = event.getSource();
			if (src instanceof IDebugElement)
			{
				if (event.getKind() == DebugEvent.SUSPEND && ((IDebugElement)src).getDebugTarget() == fMemoryBlock.getDebugTarget())
					return true;
			}
			return false;
		}

		protected void handleChange(DebugEvent event) {
			if (event.getDetail() == DebugEvent.STATE)
			{	
				// TODO:  test state change
				ModelDelta delta = new ModelDelta(fMemoryBlock, IModelDelta.STATE);
				fireModelChanged(delta);
			}
			else
			{
				ModelDelta delta = new ModelDelta(fMemoryBlock, IModelDelta.CONTENT);
				fireModelChanged(delta);
			}
		}

		protected void handleSuspend(DebugEvent event) {
			ModelDelta delta = new ModelDelta(fMemoryBlock, IModelDelta.CONTENT);
			fireModelChanged(delta);
		}

		public synchronized void dispose() {
			super.dispose();
		}};
	
	public MemoryBlockProxy(IMemoryBlock mb)
	{
		fMemoryBlock = mb;
	}


	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[]{fDebugEventHandler};
	}


}
