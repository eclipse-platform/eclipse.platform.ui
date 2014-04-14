/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - 427887 Sort ws by index (defined by user)
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.workingsets;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.2
 *
 */
public class WorkingSetSorter extends ViewerSorter {
  
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof IWorkingSet && e2 instanceof IWorkingSet) {
			IWorkingSet set1 = (IWorkingSet) e1;
			IWorkingSet set2 = (IWorkingSet) e2;
			List<IWorkingSet> allWorkingSets = Arrays.asList(PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets());
			return allWorkingSets.indexOf(set1) - allWorkingSets.indexOf(set2);
		} else if (viewer instanceof StructuredViewer) {			
			 ILabelProvider labelProvider = (ILabelProvider) ((StructuredViewer)viewer).getLabelProvider();
			 String text1 = labelProvider.getText(e1);
			 String text2 = labelProvider.getText(e2);
			 if(text1 != null) {			
				 return text1.compareTo(text2);
			 }
		}
		return -1;
	}

}
