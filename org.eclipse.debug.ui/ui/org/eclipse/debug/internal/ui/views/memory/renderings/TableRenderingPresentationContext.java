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

package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.internal.ui.views.memory.MemoryViewPresentationContext;
import org.eclipse.ui.IWorkbenchPart;

public class TableRenderingPresentationContext extends MemoryViewPresentationContext{

	private TableRenderingContentInput fInput;
	private boolean fIsDynamicLoad;
	
	public TableRenderingPresentationContext(IWorkbenchPart part) {
		super(part);
	}
	
	public void setContentInput(TableRenderingContentInput input)
	{
		fInput = input;
	}
	
	public TableRenderingContentInput getInput()
	{
		return fInput;
	}
	
	public boolean isDynamicLoad()
	{
		return fIsDynamicLoad;
	}
	
	public void setDynamicLoad(boolean load)
	{
		fIsDynamicLoad = load;
	}
	
	public AbstractAsyncTableRendering getTableRendering()
	{
		if (getRendering() instanceof AbstractAsyncTableRendering)
			return (AbstractAsyncTableRendering)getRendering();
		
		return null;
	}

}
