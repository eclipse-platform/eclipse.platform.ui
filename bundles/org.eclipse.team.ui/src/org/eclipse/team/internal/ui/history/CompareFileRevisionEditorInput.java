/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.IProgressMonitor;

public class CompareFileRevisionEditorInput extends CompareEditorInput {

	private ITypedElement left;
	private ITypedElement right;
	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 */
	public CompareFileRevisionEditorInput(FileRevisionTypedElement left, FileRevisionTypedElement right) {
		super(new CompareConfiguration());
		this.left = left;
		this.right = right;
	}

	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		return new DiffNode(left,right);
	}

}
