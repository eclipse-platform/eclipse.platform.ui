/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

/**
 * A simple compare viewer for binary files.
 * Shows the position of the first non-matching byte.
 */
public class BinaryCompareViewer extends AbstractViewer {

	private static final int EOF= -1;
	private Text fControl;
	private ICompareInput fInput;
	
	
	public BinaryCompareViewer(Composite parent, CompareConfiguration cc) {
		fControl= new Text(parent, SWT.NONE);
		fControl.setEditable(false);
		fControl.setData(CompareUI.COMPARE_VIEWER_TITLE, "Binary Compare");
	}

	public Control getControl() {
		return fControl;
	}

	public void setInput(Object input) {
		if (fControl != null && input instanceof ICompareInput) {
			fInput= (ICompareInput) input;
			
			InputStream left= null;
			InputStream right= null;
			
			try {
				left= getStream(fInput.getLeft());
				right= getStream(fInput.getRight());
				
				int pos= 0;
				while (true) {
					int l= left.read();
					int r= right.read();
					if (l != r) {
						fControl.setText("first bytes differ at position " + pos);
						break;
					}
					if (l == EOF)
						break;
					pos++;
				}
			} catch (CoreException ex) {
				fControl.setText("CoreException " + ex);
			} catch (IOException ex) {
				fControl.setText("IOException " + ex);
			} finally {
				if (left != null) {
					try {
						left.close();
					} catch (IOException ex) {
					}
				}
				if (right != null) {
					try {
						right.close();
					} catch (IOException ex) {
					}
				}			
			}
		}
	}

	public Object getInput() {
		return fInput;
	}

	private InputStream getStream(ITypedElement input) throws CoreException {
		if (input instanceof IStreamContentAccessor)
			return ((IStreamContentAccessor)input).getContents();
		return new ByteArrayInputStream(new byte[0]);
	}
}
