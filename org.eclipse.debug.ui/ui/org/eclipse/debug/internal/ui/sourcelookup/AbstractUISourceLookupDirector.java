/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.window.Window;


/**
 * The UI implementation of an AbstractSourceLookupDirector.
 */
public abstract class AbstractUISourceLookupDirector extends AbstractSourceLookupDirector {	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupDirector#resolveSourceElement(org.eclipse.debug.core.model.IStackFrame, java.util.List)
	 */
	public Object resolveSourceElement(IStackFrame frame, List sources) {
		Object file = null;
		//TODO check if multiple source not found editors
		sources = removeSourceNotFoundEditors(sources);
		if(sources.size() == 1)
			return sources.get(0);
		else if(sources.size() == 0)
			return null;
		final MultipleSourceSelectionDialog dialog = new MultipleSourceSelectionDialog(DebugUIPlugin.getShell(), sources);
		DebugUIPlugin.getShell().getDisplay().syncExec(
			new Runnable() {
				public void run() {											
					dialog.open();											
				}						
			});	
		if(dialog.getReturnCode() == Window.OK)
			file = dialog.getSelection();		
		return file;
	}
	
	/**
	 * Remove extra source not found editors, if any.
	 * If multiple source not found editors and no "real" source inputs,
	 * return the first source not found editor.
	 * @param sources the list to be filtered
	 * @return the filtered list, may be empty
	 */
	private List removeSourceNotFoundEditors(List sources){
		Iterator iterator = sources.iterator();
		List filteredList = new ArrayList();
		Object next;
		while(iterator.hasNext())
		{
			next = iterator.next();
			if(next instanceof CommonSourceNotFoundEditor)
				filteredList.add(next);
		}
		if(sources.isEmpty() && sources.get(0) != null)
			filteredList.add(sources.get(0));
		return filteredList;
	}

}
