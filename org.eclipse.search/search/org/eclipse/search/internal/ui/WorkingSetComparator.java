/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
