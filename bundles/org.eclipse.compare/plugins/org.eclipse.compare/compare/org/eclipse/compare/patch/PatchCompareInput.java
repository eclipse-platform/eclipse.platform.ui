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


/* package */ class PatchCompareInput extends CompareEditorInput {
		
	private String fPatchName;
	private DiffNode fRoot;
	private IResource fResource;
	private Diff[] fDiffs;
	private int fStripPrefixSegments;

	
	/**
	 * Creates an compare editor input for the given selection.
	 */
	/* package */ PatchCompareInput(CompareConfiguration config, ISelection s, Diff[] diffs,
						String patchName, int strip) {
		super(config);
		fStripPrefixSegments= strip;
		IResource[] selection= Utilities.getResources(s);
		if (selection.length == 1)
			fResource= selection[0];		
		fDiffs= diffs;
		fPatchName= patchName;
	}
	
	/**
	 * Performs a two-way or three-way diff on the current selection.
	 */
	public Object prepareInput(IProgressMonitor pm) throws InvocationTargetException {
						
		CompareConfiguration cc= (CompareConfiguration) getCompareConfiguration();
		
		try {				
			pm.beginTask(Utilities.getString("ResourceCompare.taskName"), fDiffs.length); //$NON-NLS-1$
		
			fRoot= new DiffNode(0);
			IContainer rootFolder= null;
			if (fResource instanceof IContainer)
				rootFolder= (IContainer) fResource;
			
			// process diffs
			for (int i= 0; i < fDiffs.length; i++) {
				Diff diff= fDiffs[i];
				IPath path= diff.getPath();
				if (fStripPrefixSegments > 0 && fStripPrefixSegments < path.segmentCount())
					path= path.removeFirstSegments(fStripPrefixSegments);
				createPath(fRoot, rootFolder, path, diff);
				pm.worked(1);
			}			
			
			fResource.refreshLocal(IResource.DEPTH_INFINITE, pm);
			
			String leftLabel= fResource.getName();
			cc.setLeftLabel(leftLabel);
			cc.setLeftImage(CompareUIPlugin.getImage(fResource));
			
			String rightLabel= "Patch: " + fPatchName;
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

	/* package */ void createPath(DiffContainer root, IContainer folder, IPath path, Diff diff) {
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
			new DiffNode(root, diff.getType(), null, rn, new PatchedResource(rn, diff, path));
		}
	}
}


