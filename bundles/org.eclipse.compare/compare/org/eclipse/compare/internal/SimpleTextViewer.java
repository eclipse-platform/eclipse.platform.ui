/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.Document;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;


public class SimpleTextViewer extends AbstractViewer {
		
	private SourceViewer fSourceViewer;
	private ICompareInput fInput;
	
	
	SimpleTextViewer(Composite parent) {
		fSourceViewer= new SourceViewer(parent, null, SWT.H_SCROLL + SWT.V_SCROLL);
		fSourceViewer.setEditable(false);
	}
		
	public Control getControl() {
		return fSourceViewer.getTextWidget();
	}
	
	public void setInput(Object input) {
		if (input instanceof IStreamContentAccessor) {
			fSourceViewer.setDocument(new Document(getString(input)));
		} else if (input instanceof ICompareInput) {
			fInput= (ICompareInput) input;
			ITypedElement left= ((ICompareInput) fInput).getLeft();
			fSourceViewer.setDocument(new Document(getString(left)));
		}
	}
	
	public Object getInput() {
		return fInput;
	}
	
	private String getString(Object input) {
		
		if (input instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca= (IStreamContentAccessor) input;
			try {
				return Utilities.readString(sca.getContents());
			} catch (CoreException ex) {
			}
		}
		return ""; //$NON-NLS-1$
	}
}
