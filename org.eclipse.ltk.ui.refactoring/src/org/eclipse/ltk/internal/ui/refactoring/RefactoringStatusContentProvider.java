/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RefactoringStatusContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			return ((RefactoringStatus)obj).getEntries();
		}
}

