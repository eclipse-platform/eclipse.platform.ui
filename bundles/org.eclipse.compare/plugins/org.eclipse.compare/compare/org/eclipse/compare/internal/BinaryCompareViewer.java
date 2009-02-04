/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.io.*;
import java.util.ResourceBundle;
import com.ibm.icu.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
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
	private ICompareInput fInput;
	private ResourceBundle fBundle;
	private boolean fLeftIsLocal;

	private Composite fComposite;
	private Label fMessage;
	private Button fTextButton;
	
	
	public BinaryCompareViewer(Composite parent, final CompareConfiguration cc) {
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICompareContextIds.BINARY_COMPARE_VIEW);

		fBundle= ResourceBundle.getBundle(BUNDLE_NAME);
		
		fComposite= new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.VERTICAL;
		fComposite.setLayout(rowLayout);

		fMessage= new Label(fComposite, SWT.WRAP);
		fComposite.setData(CompareUI.COMPARE_VIEWER_TITLE, Utilities.getString(fBundle, "title")); //$NON-NLS-1$
		
		fLeftIsLocal= Utilities.getBoolean(cc, "LEFT_IS_LOCAL", false); //$NON-NLS-1$
		
		if (canShowAsText(cc)) {
			fTextButton = new Button(fComposite, SWT.PUSH);
			fTextButton.setText(Utilities.getString(fBundle, "compareAsText")); //$NON-NLS-1$
			fTextButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
						public void run() {
							handleShowAsText(cc);
						}
					});
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					// Do nothing
				}
			});
			fTextButton.setEnabled(false);
		}
	}

	private boolean canShowAsText(CompareConfiguration cc) {
		if (cc == null)
			return false;
		return Utilities.getAdapter(cc.getContainer(), ICompareAsText.class) != null;
	}

	protected void handleShowAsText(CompareConfiguration cc) {
		ICompareAsText comparer = (ICompareAsText)Utilities.getAdapter(cc.getContainer(), ICompareAsText.class);
		comparer.compareAsText(getInput());
	}

	public Control getControl() {
		return fComposite;
	}

	public void setInput(Object input) {
		if (fComposite != null && input instanceof ICompareInput) {
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
				Utilities.close(left);
				Utilities.close(right);			
			}
			if (message != null)
				fMessage.setText(message);
			if (fTextButton != null)
				fTextButton.setEnabled(true);
			fComposite.layout();
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
