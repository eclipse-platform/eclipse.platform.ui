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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.wizard.IWizardContainer;

import org.eclipse.ui.IEditorPart;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;

public class UIPerformChangeOperation extends PerformChangeOperation {

	private Display fDisplay;
	private IWizardContainer fWizardContainer;
	
	public UIPerformChangeOperation(Display display, Change change, IWizardContainer container) {
		super(change);
		fDisplay= display;
		fWizardContainer= container;
	}

	public UIPerformChangeOperation(Display display, CreateChangeOperation op, IWizardContainer container) {
		super(op);
		fDisplay= display;
		fWizardContainer= container;
	}
	
	protected void executeChange(final IProgressMonitor pm) throws CoreException {
		if (fDisplay != null && !fDisplay.isDisposed()) {
			final CoreException[] exception= new CoreException[1]; 
			Runnable r= new Runnable() {
				public void run() {
					IRewriteTarget[] targets= null;
					try {
						final Button cancel= getCancelButton();
						targets= getRewriteTargets();
						beginCompoundChange(targets);
						boolean enabled= true;
						if (cancel != null && !cancel.isDisposed()) {
							enabled= cancel.isEnabled();
							cancel.setEnabled(false);
						}
						try {
							UIPerformChangeOperation.super.executeChange(pm);
						} finally {
							if (cancel != null && !cancel.isDisposed()) {
								cancel.setEnabled(enabled);
							}
						}
					} catch (CoreException e) {
						exception[0]= e;
					} finally {
						if (targets != null)
							endCompoundChange(targets);
					}
				}
			};
			fDisplay.syncExec(r);
			if (exception[0] != null)
				throw new CoreException(exception[0].getStatus());
		} else {
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
	
	private Button getCancelButton() {
		if (fWizardContainer instanceof RefactoringWizardDialog2) {
			return ((RefactoringWizardDialog2)fWizardContainer).getCancelButton();
		} else if (fWizardContainer instanceof RefactoringWizardDialog) {
			return ((RefactoringWizardDialog)fWizardContainer).getCancelButton();
		}
		return null;
	}
}
