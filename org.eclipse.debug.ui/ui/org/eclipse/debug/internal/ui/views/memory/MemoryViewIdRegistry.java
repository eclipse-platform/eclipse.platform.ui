/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.util.ArrayList;

/**
 * Class for managing the secondary ids for Memory View
 *
 */
public class MemoryViewIdRegistry{
	
	private static ArrayList fgRegistry;
	
	public static  void registerView(String secondaryId)
	{
		ArrayList registry = getRegistry();
		
		if (!registry.contains(secondaryId))
		{
			registry.add(secondaryId);
		}
	}
	
	public static  void deregisterView(String secondaryId)
	{
		ArrayList registry = getRegistry();
		
		if (registry.contains(secondaryId))
		{
			registry.remove(secondaryId);
		}
	}
	
	public static String getUniqueSecondaryId(String viewId)
	{
		int cnt = 0;
		String id = viewId + "." + cnt; //$NON-NLS-1$
		ArrayList registry = getRegistry();
		while (registry.contains(id))
		{
			cnt ++;
			id = viewId + "." + cnt; //$NON-NLS-1$
		}
		return id;
	}
	
	private static ArrayList getRegistry()
	{
		if (fgRegistry == null)
			fgRegistry = new ArrayList();
		
		return fgRegistry;
	}
}
