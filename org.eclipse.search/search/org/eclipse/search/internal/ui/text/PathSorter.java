/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class PathSorter extends NameSorter {
	protected int compare(IResource left, IResource right) {
		IPath path1= left.getFullPath();
		IPath path2= right.getFullPath();
		int segmentCount= Math.min(path1.segmentCount(), path2.segmentCount());
		for (int i= 0; i < segmentCount; i++) {
			int value= collator.compare(path1.segment(i), path2.segment(i));
			if (value != 0)
				return value;
		}
		return path1.segmentCount() - path2.segmentCount();
	}
}
