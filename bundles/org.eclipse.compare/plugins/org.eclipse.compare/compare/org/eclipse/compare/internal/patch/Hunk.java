/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A Hunk describes a range of changed lines and some context lines.
 */
/* package */ class Hunk implements IWorkbenchAdapter, IAdaptable {
	
	static final int ADDED = 0x1;
	static final int DELETED = 0x2;
	static final int CHANGED = 0x4;
	static final int UNKNOWN = 0x8;
	
	Diff fParent;
	int fOldStart, fOldLength;
	int fNewStart, fNewLength;
	String[] fLines;
	boolean fMatches= false;
	private boolean fIsEnabled= true;
	boolean fHunkProblem= false;
	int hunkType;
	
	/* package */ Hunk(Diff parent, int[] oldRange, int[] newRange, List lines, boolean encounteredPlus, boolean encounteredMinus, boolean encounteredSpace) {
		
		fParent= parent;
		if (fParent != null)
			fParent.add(this);
		
		if (oldRange[0] > 0)
			fOldStart= oldRange[0]-1;	// line number start at 0!
		else
			fOldStart= 0;
		fOldLength= oldRange[1];
		if (newRange[0] > 0)
			fNewStart= newRange[0]-1;	// line number start at 0!
		else
			fNewStart= 0;
		fNewLength= newRange[1];
		
		fLines= (String[]) lines.toArray(new String[lines.size()]);
		
		if (encounteredSpace && (encounteredPlus || encounteredMinus)){
			hunkType = CHANGED;
		} else if (encounteredPlus && !encounteredMinus && !encounteredSpace){
			hunkType = ADDED;
		} else if (!encounteredPlus && encounteredMinus && !encounteredSpace) { 
			hunkType = DELETED;
		} else {
			hunkType = UNKNOWN;
		}
	}
		
	boolean isEnabled() {
		return fIsEnabled;
	}
	
	void setEnabled(boolean enable) {
		fIsEnabled= enable;
	}
	
	void reverse() {
		int t= fOldStart;
		fOldStart= fNewStart;
		fNewStart= t;
		
		t= fOldLength;
		fOldLength= fNewLength;
		fNewLength= t;
		
		//reverse hunk type
		if (hunkType == ADDED){
			hunkType = DELETED;
		} else if (hunkType == DELETED){
			hunkType = ADDED;
		}
		
		for (int i= 0; i < fLines.length; i++) {
			String line= fLines[i];
			char c= line.charAt(0);
			switch (c) {
			case '+':
				fLines[i]= '-' + line.substring(1);
				break;
			case '-':
				fLines[i]= '+' + line.substring(1);
				break;
			default:
				break;
			}
		}
	}

	/*
	 * Returns the contents of this hunk.
	 * Each line starts with a control character. Their meaning is as follows:
	 * <ul>
	 * <li>
	 * '+': add the line
	 * <li>
	 * '-': delete the line
	 * <li>
	 * ' ': no change, context line
	 * </ul>
	 */
	String getContent() {
		StringBuffer sb= new StringBuffer();
		for (int i= 0; i < fLines.length; i++) {
			String line= fLines[i];
			sb.append(line.substring(0, Patcher.length(line)));
			sb.append('\n');
		}
		return sb.toString();
	}
	
	/*
	 * Returns a descriptive String for this hunk.
	 * It is in the form old_start,old_length -> new_start,new_length.
	 */
	String getDescription() {
		StringBuffer sb= new StringBuffer();
		sb.append(Integer.toString(fOldStart));
		sb.append(',');
		sb.append(Integer.toString(fOldLength));
		sb.append(" -> "); //$NON-NLS-1$
		sb.append(Integer.toString(fNewStart));
		sb.append(',');
		sb.append(Integer.toString(fNewLength));
		return sb.toString();
	}
	
	String getRejectedDescription() {
		StringBuffer sb= new StringBuffer();
		sb.append("@@ -"); //$NON-NLS-1$
		sb.append(Integer.toString(fOldStart));
		sb.append(',');
		sb.append(Integer.toString(fOldLength));
		sb.append(" +"); //$NON-NLS-1$
		sb.append(Integer.toString(fNewStart));
		sb.append(',');
		sb.append(Integer.toString(fNewLength));
		sb.append(" @@"); //$NON-NLS-1$
		return sb.toString();
	}

	void reset(boolean problemEncountered) {
		fHunkProblem= problemEncountered;
	}

	//IWorkbenchAdapter methods
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		String label= getDescription();
		if (this.fHunkProblem)
			return NLS.bind(PatchMessages.Diff_2Args, new String[] {label, PatchMessages.PreviewPatchPage_NoMatch_error});
		return label;
	}

	public Object getParent(Object o) {
		return fParent;
	}

	//IAdaptable methods
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return this;
		return null;
	}
	
	protected boolean getHunkProblem() {
		return fHunkProblem;
	}

	int getHunkType() {
		return hunkType;
	}

	void setHunkType(int hunkType) {
		this.hunkType = hunkType;
	}

	public String[] getLines() {
		return fLines;
	}

	public void setParent(Diff diff) {
		this.fParent = diff;
		
	}
}
