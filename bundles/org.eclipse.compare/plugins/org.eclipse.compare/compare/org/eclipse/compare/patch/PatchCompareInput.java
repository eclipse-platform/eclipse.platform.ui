/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.io.*;
import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.compare.internal.*;


/**
 * A PatchCompareInput uses a Patcher to 
 * patch selected workspace resources.
 */
/* package */ class PatchCompareInput extends CompareEditorInput {
		
	private DiffNode fRoot;
	private IResource fTarget;
	private Patcher fPatcher;
	
	/**
	 * Creates an compare editor input for the given selection.
	 */
	/* package */ PatchCompareInput(CompareConfiguration config, Patcher patcher, ISelection selection) {
		super(config);
		fPatcher= patcher;
		IResource[] s= Utilities.getResources(selection);
		if (s.length == 1)
			fTarget= s[0];	
	}
	
	/**
	 * Performs a two-way or three-way diff on the current selection.
	 */
	public Object prepareInput(IProgressMonitor pm) throws InvocationTargetException {
						
		CompareConfiguration cc= (CompareConfiguration) getCompareConfiguration();
		
		try {				
			Diff[] diffs= fPatcher.getDiffs();
			
			pm.beginTask(Utilities.getString("ResourceCompare.taskName"), diffs.length); //$NON-NLS-1$
		
			fRoot= new DiffNode(0);
			IContainer rootFolder= null;
			if (fTarget instanceof IContainer)
				rootFolder= (IContainer) fTarget;
				
			for (int i= 0; i < diffs.length; i++) {
				Diff diff= diffs[i];
				if (diff.fIsEnabled)
					createPath(fRoot, rootFolder, fPatcher.getPath(diff), diff);
				pm.worked(1);
			}
						
			fTarget.refreshLocal(IResource.DEPTH_INFINITE, pm);
			
			String leftLabel= fTarget.getName();
			cc.setLeftLabel(leftLabel);
			cc.setLeftImage(CompareUIPlugin.getImage(fTarget));
			
			String rightLabel= "Patch: " + fPatcher.getName();
			cc.setRightLabel(rightLabel);
			//cc.setRightImage(CompareUIPlugin.getImage(fRightResource));
			
			String format= Utilities.getString("ResourceCompare.twoWay.title"); //$NON-NLS-1$
			String title= MessageFormat.format(format, new String[] {leftLabel, rightLabel} );
			setTitle(title);

			return fRoot;
			
		} catch (CoreException ex) {
			throw new InvocationTargetException(ex);
		} finally {
			pm.done();
		}
	}
	
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		if (fRoot instanceof DiffNode) {
			try {
				commit(pm, (DiffNode) fRoot);
			} finally {	
				setDirty(false);
			}
		}
	}
	
	/*
	 * Recursively walks the diff tree and commits all changes.
	 */
	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		
		ITypedElement left= node.getLeft();
		if (left instanceof BufferedResourceNode)
			((BufferedResourceNode) left).commit(pm);
			
		ITypedElement right= node.getRight();
		if (right instanceof BufferedResourceNode)
			((BufferedResourceNode) right).commit(pm);

		IDiffElement[] children= node.getChildren();
		if (children != null) {
			for (int i= 0; i < children.length; i++) {
				IDiffElement element= children[i];
				if (element instanceof DiffNode)
					commit(pm, (DiffNode) element);
			}
		}
	}
	
	private void createPath(DiffContainer root, IContainer folder, IPath path, Diff diff) {
		if (path.segmentCount() > 1) {
			IFolder f= folder.getFolder(path.uptoSegment(1));
			IDiffElement child= root.findChild(path.segment(0));
			if (child == null) {
				ResourceNode rn= new ResourceNode(f);
				child= new DiffNode(root, Differencer.CHANGE, null, rn, rn);
			}
			if (child instanceof DiffContainer)
				createPath((DiffContainer)child, f, path.removeFirstSegments(1), diff);
		} else {
			// a leaf
			BufferedResourceNode rn= new BufferedResourceNode(folder.getFile(path));						
			new DiffNode(root, diff.getType(), null, rn, new PatchedResource(rn, diff, path, fPatcher));
		}
	}
}


