/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.Comparator;

import com.ibm.icu.text.Collator;

import org.eclipse.ui.IWorkingSet;

public class WorkingSetComparator implements Comparator<IWorkingSet> {

	private Collator fCollator= Collator.getInstance();

	@Override
	public int compare(IWorkingSet o1, IWorkingSet o2) {
		String name1= o1.getLabel();
		String name2= o2.getLabel();
		return fCollator.compare(name1, name2);
	}
}
