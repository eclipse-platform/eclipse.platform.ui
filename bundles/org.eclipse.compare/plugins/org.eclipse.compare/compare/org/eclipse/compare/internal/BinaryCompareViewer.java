/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.text.MessageFormat;

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

	private static final String BUNDLE_NAME= "org.eclipse.compare.internal.BinaryCompareViewerResources"; //$NON-NLS-1$

	private static final int EOF= -1;
	private Text fControl;
	private ICompareInput fInput;
	private ResourceBundle fBundle;
	private boolean fLeftIsLocal;
	
	
	public BinaryCompareViewer(Composite parent, CompareConfiguration cc) {
		
		fBundle= ResourceBundle.getBundle(BUNDLE_NAME);

		fControl= new Text(parent, SWT.NONE);
		fControl.setEditable(false);
		fControl.setData(CompareUI.COMPARE_VIEWER_TITLE, Utilities.getString(fBundle, "title")); //$NON-NLS-1$
		
		fLeftIsLocal= Utilities.getBoolean(cc, "LEFT_IS_LOCAL", false); //$NON-NLS-1$
	}

	public Control getControl() {
		return fControl;
	}

	public void setInput(Object input) {
		if (fControl != null && input instanceof ICompareInput) {
			fInput= (ICompareInput) input;
			
			InputStream left= null;
			InputStream right= null;
			
			String message= null;
			try {
				left= getStream(fInput.getLeft());
				right= getStream(fInput.getRight());
				
				if (left != null && right != null) {
					int pos= 0;
					while (true) {
						int l= left.read();
						int r= right.read();
						if (l != r) {
							String format= Utilities.getString(fBundle, "diffMessageFormat"); //$NON-NLS-1$
							message= MessageFormat.format(format, new String[] { Integer.toString(pos) } );
							break;
						}
						if (l == EOF)
							break;
						pos++;
					}
				} else if (left == null && right == null) {
					message= Utilities.getString(fBundle, "deleteConflictMessage"); //$NON-NLS-1$
				} else if (left == null) {
					if (fLeftIsLocal)
						message= Utilities.getString(fBundle, "deletedMessage"); //$NON-NLS-1$
					else
						message= Utilities.getString(fBundle, "addedMessage"); //$NON-NLS-1$
				} else if (right == null) {
					if (fLeftIsLocal)
						message= Utilities.getString(fBundle, "addedMessage"); //$NON-NLS-1$
					else
						message= Utilities.getString(fBundle, "deletedMessage"); //$NON-NLS-1$
				}
			} catch (CoreException ex) {
				message= Utilities.getString(fBundle, "errorMessage"); //$NON-NLS-1$
			} catch (IOException ex) {
				message= Utilities.getString(fBundle, "errorMessage"); //$NON-NLS-1$
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
			if (message != null)
				fControl.setText(message);				
		}
	}

	public Object getInput() {
		return fInput;
	}

	private InputStream getStream(ITypedElement input) throws CoreException {
		if (input instanceof IStreamContentAccessor)
			return ((IStreamContentAccessor)input).getContents();
		return null;
	}
}
