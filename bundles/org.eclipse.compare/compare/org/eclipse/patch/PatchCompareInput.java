/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.patch;

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

	
	/**
	 * Creates an compare editor input for the given selection.
	 */
	/* package */ PatchCompareInput(CompareConfiguration config, ISelection s, Diff[] diffs, String patchName) {
		super(config);
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
				
				// strip of first component
				String path= diff.fOldName;
				int pos= path.indexOf('/');
				if (pos >= 0)
					path= path.substring(pos+1);
				
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
	
	/* package */ void createPath(DiffContainer root, IContainer folder, String path, Diff diff) {
		int pos= path.indexOf('/');
		if (pos >= 0) {
			String dir= path.substring(0, pos);
			IFolder f= folder.getFolder(new Path(dir));
			IDiffElement child= root.findChild(dir);
			if (child == null) {
				ResourceNode rn= new ResourceNode(f);
				child= new DiffNode(root, Differencer.CHANGE, null, rn, rn);
			}
			if (child instanceof DiffContainer)
				createPath((DiffContainer)child, f, path.substring(pos+1), diff);
		} else {
			// leaf
			IFile file= folder.getFile(new Path(path));
			BufferedResourceNode rn= new BufferedResourceNode(file);			
			
			new DiffNode(root, diff.getType(), null, rn, new PatchedResource(rn, diff, path));
		}
	}
}


