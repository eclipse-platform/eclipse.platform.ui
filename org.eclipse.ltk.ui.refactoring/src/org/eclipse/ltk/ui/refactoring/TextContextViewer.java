/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

public abstract class TextContextViewer  implements IStatusContextViewer {

	private SourceViewer fSourceViewer;
	private ViewForm fForm;
	private CLabel fLabel;
	private Image fPaneImage;

	/**
	 * Sets the title text of the pane surrounding the source viewer.
	 * @param text the pane's title text
	 */
	public void setTitleText(String text) {
		fLabel.setText(text);
	}
	
	/**
	 * Sets the title image of the pane surrounding the source viewer.
	 * 
	 * @param image the pane's image 
	 */
	public void setTitleImage(Image image) {
		fLabel.setImage(image);
	}

	/**
	 * Returns the internal source viewer.
	 * 
	 * @return the internal source viewer
	 */
	protected SourceViewer getSourceViewer() {
		return fSourceViewer;
	}
	
	/**
	 * Hook to create the source viewer used to present the textual context
	 * information.
	 * 
	 * @param parent the composite to be used as the source viewer's
	 *  parent
	 * @return the source viewer to be used
	 */
	protected abstract SourceViewer createSourceViewer(Composite parent);

	//---- Helper methods to populate viewer -------------------------------

	/**
	 * Updates the title image and text of the pane surounding the source
	 * viewer. The image and text is determined by retrieving the <code>
	 * IWorkbenchAdapter</code> for the given element. If the element doen't
	 * provide a <code>IWorkbenchAdapter</code> or if the element is <code>
	 * null</code> the image is resetted and a default label is shown.  
	 * 
	 * @param element the element providing the image and label for the title.
	 *  Can be <code>null</code> to reset the image and text
	 */
	protected void updateTitle(IAdaptable element) {
		String title= null;
		ImageDescriptor imageDescriptor= null;
		if (element != null) {
			IWorkbenchAdapter adapter= (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				title= adapter.getLabel(element);
				imageDescriptor= adapter.getImageDescriptor(element);
			}
		}
		if (title == null || title.length() == 0)
			title= RefactoringUIMessages.getString("RefactoringStatusViewer.Problem_context"); //$NON-NLS-1$
		setTitleText(title);
		if (fPaneImage != null) {
			fPaneImage.dispose();
			fPaneImage= null;
		}
		if (imageDescriptor != null) {
			fPaneImage= imageDescriptor.createImage(getControl().getDisplay());
		}
		setTitleImage(fPaneImage);
	}
	
	// this should be pushed down and not be API.
	protected IDocument getDocument(IFile file) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		try {
			try {
				manager.connect(path, new NullProgressMonitor());
				ITextFileBuffer buffer = manager.getTextFileBuffer(path);
				if (buffer != null) {
					return buffer.getDocument();
				}
			} finally {
				manager.disconnect(path, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			// fall through
		}
		return new Document("Couldn't read content of file");
	}

	protected void setInput(IDocument document, IRegion region) {
		Control ctrl= getControl();
		if (ctrl != null && ctrl.isDisposed())
			ctrl= null;
		try {
			if (ctrl != null)
				ctrl.setRedraw(false);
			fSourceViewer.setInput(document);
			if (region != null && document != null) {
				int offset= region.getOffset();
				int length= region.getLength();
				if (offset >= 0 && length >= 0) {
					fSourceViewer.setSelectedRange(offset, length);
					fSourceViewer.revealRange(offset, length);
				}
			}
		} finally {
			if (ctrl != null)
				ctrl.setRedraw(true);
		}
	}
	
	//---- Methods defined in IStatusContextViewer -------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IStatusContextViewer#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fForm= new ViewForm(parent, SWT.BORDER | SWT.FLAT);
		fForm.marginWidth= 0;
		fForm.marginHeight= 0;
		fLabel= new CLabel(fForm, SWT.NONE);
		fForm.setTopLeft(fLabel);
		fForm.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (fPaneImage != null)
					fPaneImage.dispose();
			}
		});
		
		fSourceViewer= createSourceViewer(fForm);
		fSourceViewer.setEditable(false);
		fForm.setContent(fSourceViewer.getControl());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IStatusContextViewer#getControl()
	 */
	public Control getControl() {
		return fForm;
	}
}

