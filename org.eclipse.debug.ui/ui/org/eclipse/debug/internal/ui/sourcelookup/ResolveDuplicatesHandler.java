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
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Status handler to prompt for dupicate source element resolution.
 * 
 * @since 3.0
 */
public class ResolveDuplicatesHandler implements IStatusHandler {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		Object[] args = (Object[])source;
		List sources = (List) args[1];
		return resolveSourceElement(sources);
	}
	
	public Object resolveSourceElement(List sources) {
		Object file = null;
		sources = removeSourceNotFoundEditors(sources);
		if(sources.size() == 1) {
			return sources.get(0);
		} else if(sources.size() == 0) {
			return null;
		}
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(DebugUIPlugin.getShell(), new SourceElementLabelProvider());
		dialog.setMultipleSelection(false);
		dialog.setTitle(SourceLookupUIMessages.ResolveDuplicatesHandler_0); 
		dialog.setMessage(SourceLookupUIMessages.ResolveDuplicatesHandler_1); 
		dialog.setElements(sources.toArray());
		dialog.open();											
		if(dialog.getReturnCode() == Window.OK) {
			file = dialog.getFirstResult();		
		}
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
		while(iterator.hasNext()) {
			next = iterator.next();
			if (!(next instanceof CommonSourceNotFoundEditor)) {
				filteredList.add(next);
			}
		}
		if (filteredList.isEmpty() && sources.get(0) != null) {
			filteredList.add(sources.get(0));
		}
		return filteredList;
	}
}
