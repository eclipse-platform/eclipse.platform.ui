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
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IRewriteTarget;

import org.eclipse.ui.IEditorPart;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;


public class UIPerformChangeOperation extends PerformChangeOperation {

	public UIPerformChangeOperation(Change change) {
		super(change);
	}

	public UIPerformChangeOperation(CreateChangeOperation op) {
		super(op);
	}
	
	protected void executeChange(IProgressMonitor pm) throws CoreException {
		IRewriteTarget[] targets= null;
		try {
			targets= getRewriteTargets();
			beginCompoundChange(targets);
			super.executeChange(pm);
		} finally {
			if (targets != null)
				endCompoundChange(targets);
		}
	}

	private static void beginCompoundChange(IRewriteTarget[] targets) {
		for (int i= 0; i < targets.length; i++) {
			targets[i].beginCompoundChange();
		}
	}
	
	private static void endCompoundChange(IRewriteTarget[] targets) {
		for (int i= 0; i < targets.length; i++) {
			targets[i].endCompoundChange();
		}
	}
	
	private static IRewriteTarget[] getRewriteTargets() {
		IEditorPart[] editors= RefactoringUIPlugin.getInstanciatedEditors();
		List result= new ArrayList(editors.length);
		for (int i= 0; i < editors.length; i++) {
			IRewriteTarget target= (IRewriteTarget)editors[i].getAdapter(IRewriteTarget.class);
			if (target != null) {
				result.add(target);
			}
		}
		return (IRewriteTarget[]) result.toArray(new IRewriteTarget[result.size()]);
	}
}
