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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IUndoManagerListener;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

public class SaveListener implements IResourceChangeListener, IUndoManagerListener {
	
	private class DeltaVisitor implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource resource= delta.getResource();
			if (resource.getType() == IResource.FILE && delta.getKind() == IResourceDelta.CHANGED &&
					(delta.getFlags() & IResourceDelta.CONTENT) != 0) {
				IFile file= (IFile)resource;
				ContentStamps.increment(file);
			}
			return true;
		}
	}
	
	private DeltaVisitor fVisitor= new DeltaVisitor();
	private int fInChangeExecution;
	
	private static final SaveListener INSTANCE= new SaveListener();
	
	public static SaveListener getInstance() {
		return INSTANCE;
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta delta= event.getDelta();
			if (delta != null && fInChangeExecution == 0)
				delta.accept(fVisitor);
		} catch (CoreException e) {
			RefactoringCorePlugin.log(e);
		}
	}
	
	public void undoStackChanged(IUndoManager manager) {
	}
	public void redoStackChanged(IUndoManager manager) {
	}
	public void aboutToPerformChange(IUndoManager manager, Change change) {
		fInChangeExecution++;
	}
	public void changePerformed(IUndoManager manager, Change change) {
		fInChangeExecution--;
	}
	
	public void startup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		RefactoringCore.getUndoManager().addListener(this);
	}
	
	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		RefactoringCore.getUndoManager().removeListener(this);
	}
}
