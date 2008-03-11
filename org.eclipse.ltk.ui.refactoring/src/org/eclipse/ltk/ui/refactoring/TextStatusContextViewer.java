/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An abstract base implementation of a status context viewer that presents
 * textual context information.
 * <p>
 * Subclasses need to implement {@link #createSourceViewer(Composite)} to create
 * the correct source viewer. They should use the method {@link #updateTitle(IAdaptable)}
 * and {@link #setInput(IDocument, IRegion)} to set the title text and image and to
 * populate the source viewer.
 * </p>
 * 
 * @since 3.0
 */
public abstract class TextStatusContextViewer implements IStatusContextViewer {

	private SourceViewer fSourceViewer;
	private ViewForm fForm;
	private CLabel fLabel;
	private Image fPaneImage;

	/**
	 * Returns the internal source viewer.
	 * 
	 * @return the internal source viewer or <code>null</code> if the
	 *  source viewer hasn't been created yet
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
	 * Updates the title image and text of the pane surrounding the source
	 * viewer. The image and text is determined by retrieving the <code>
	 * IWorkbenchAdapter</code> for the given element. If the element doen't
	 * provide a <code>IWorkbenchAdapter</code> or if the element is <code>
	 * null</code> the image is reset and a default label is shown.  
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
			title= RefactoringUIMessages.RefactoringStatusViewer_Problem_context; 
		fLabel.setText(title);
		if (fPaneImage != null) {
			fPaneImage.dispose();
			fPaneImage= null;
		}
		if (imageDescriptor != null) {
			fPaneImage= imageDescriptor.createImage(getControl().getDisplay());
		}
		fLabel.setImage(fPaneImage);
	}
	
	/**
	 * Sets the input of the source viewer to the given document and reveals the
	 * region determined by the given parameter region.
	 * 
	 * @param document the document to present
	 * @param region the region to reveal.
	 */
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

	/**
	 * {@inheritDoc}
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

		Dialog.applyDialogFont(parent);

		fSourceViewer= createSourceViewer(fForm);
		fSourceViewer.setEditable(false);
		fForm.setContent(fSourceViewer.getControl());
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return fForm;
	}
}

