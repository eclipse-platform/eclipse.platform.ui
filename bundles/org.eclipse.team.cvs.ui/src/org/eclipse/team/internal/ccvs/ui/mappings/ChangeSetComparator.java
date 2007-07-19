/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.util.Comparator;

public class ChangeSetComparator implements Comparator {
	private ChangeSetSorter fSorter= new ChangeSetSorter();

	public int compare(Object o1, Object o2) {
		return fSorter.compare(null, o1, o2);
	}
}
