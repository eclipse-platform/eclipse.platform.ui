/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.util.Comparator;

import org.eclipse.help.AbstractTocProvider;


public class TocProviderComparator implements Comparator{

	public int compare(Object obj1,Object obj2)
	{
		AbstractTocProvider provider1 = (AbstractTocProvider)obj1;
		AbstractTocProvider provider2 = (AbstractTocProvider)obj2;
		
		if(provider1.getPriority()<provider2.getPriority())
			return -1;
		else if(provider1.getPriority()>provider2.getPriority())
			return 1;
		else
			return 0;
	}
}
