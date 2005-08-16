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
package org.eclipse.ltk.internal.ui.refactoring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.AbstractTextEditChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.core.refactoring.Change;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

public class TextEditChangePreviewViewer implements IChangePreviewViewer {

	private ComparePreviewer fViewer;
	
	private static class BufferChangeInput extends ChangePreviewViewerInput {
		TextEditBasedChangeGroup group;
		int surroundingLines;
		
		TextEditBasedChangeGroup[] groups;
		IRegion range;
		
		public BufferChangeInput(Change change) {
			super(change);
		}
	}
	
	private static class ComparePreviewer extends CompareViewerSwitchingPane {
		private CompareConfiguration fCompareConfiguration;
		private String fLabel;
		private ImageDescriptor fDescriptor;
		private Image fImage;
		public ComparePreviewer(Composite parent) {
			super(parent, SWT.BORDER | SWT.FLAT, true);
			fCompareConfiguration= new CompareConfiguration();
			fCompareConfiguration.setLeftEditable(false);
			fCompareConfiguration.setLeftLabel(RefactoringUIMessages.ComparePreviewer_original_source); 
			fCompareConfiguration.setRightEditable(false);
			fCompareConfiguration.setRightLabel(RefactoringUIMessages.ComparePreviewer_refactored_source); 
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (fImage != null && !fImage.isDisposed())
						fImage.dispose();
				}
			});
		}
		public void setLabel(String label) {
			fLabel= label;
		}
		public void setImageDescriptor(ImageDescriptor imageDescriptor) {
			fDescriptor= imageDescriptor;
		}
		protected Viewer getViewer(Viewer oldViewer, Object input) {
			return CompareUI.findContentViewer(oldViewer, (ICompareInput)input, this, fCompareConfiguration);
		}
		public void setText(String text) {
			if (fLabel != null) {
				super.setText(fLabel);
			} else {
				super.setText(text);
			}
			Image current= null;
			if (fDescriptor != null) {
				current= fImage;
				fImage= fDescriptor.createImage();
			} else {
				current= fImage;
				fImage= null;
			}
			setImage(fImage);
			if (current != null) {
				current.dispose();
			}
		}
	}
	
	private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {
		// we use an encoding that preserves Unicode across the stream
		private static final String ENCODING= "UTF-8";	//$NON-NLS-1$ 
		private String fContent;
		private String fType;
		public CompareElement(String content, String type) {
			fContent= content;
			fType= type;
		}
		public String getName() {
			return RefactoringUIMessages.ComparePreviewer_element_name; 
		}
		public Image getImage() {
			return null;
		}
		public String getType() {
			return fType;
		}
		public InputStream getContents() throws CoreException {
			try {
				return new ByteArrayInputStream(fContent.getBytes(ENCODING));
			} catch (UnsupportedEncodingException e) {
				return new ByteArrayInputStream(fContent.getBytes());
			}
		}
		public String getCharset() {
			return ENCODING;
		}
	}
	
	public static ChangePreviewViewerInput createInput(AbstractTextEditChange change) {
		return new ChangePreviewViewerInput(change);
	}
	
	public static ChangePreviewViewerInput createInput(Change change, TextEditBasedChangeGroup group, int surroundingLines) {
		BufferChangeInput input= new BufferChangeInput(change);
		input.group= group;
		input.surroundingLines= surroundingLines;
		return input;
	}
	
	public static ChangePreviewViewerInput createInput(Change change, TextEditBasedChangeGroup[] groups, IRegion range) {
		BufferChangeInput input= new BufferChangeInput(change);
		input.groups= groups;
		input.range= range;
		return input;
	}

	public void createControl(Composite parent) {
		fViewer= new ComparePreviewer(parent);
	}

	public Control getControl() {
		return fViewer;
	}

	public void setInput(ChangePreviewViewerInput input) {
		try {
			Change change= input.getChange();
			if (input instanceof BufferChangeInput) {
				BufferChangeInput extended= (BufferChangeInput)input;
				if (extended.group != null && extended.surroundingLines >= 0) {
					TextEditBasedChangeGroup group= extended.group;
					AbstractTextEditChange bufferChange= group.getTextEditChange();
					setInput(bufferChange, bufferChange.getCurrentContent(group.getRegion(), true, 2, new NullProgressMonitor()),
						bufferChange.getPreviewContent(new TextEditBasedChangeGroup[] { group }, group.getRegion(), true, 2, new NullProgressMonitor()),
						bufferChange.getTextType());
					return;
				} else if (extended.groups != null && extended.groups.length > 0 && extended.range != null) {
					AbstractTextEditChange bufferChange= extended.groups[0].getTextEditChange();
					setInput(bufferChange, bufferChange.getCurrentContent(extended.range, true, 0, new NullProgressMonitor()),
						bufferChange.getPreviewContent(extended.groups, extended.range, true, 0, new NullProgressMonitor()),
						bufferChange.getTextType());
					return;
				}
			} else if (change instanceof AbstractTextEditChange) {
				AbstractTextEditChange bufferChange= (AbstractTextEditChange)change;
				setInput(bufferChange, bufferChange.getCurrentContent(new NullProgressMonitor()), bufferChange.getPreviewContent(new NullProgressMonitor()), bufferChange.getTextType());
				return;
			} else {
				fViewer.setInput(null);
			}
		} catch (CoreException e) {
			RefactoringUIPlugin.log(e);
			fViewer.setInput(null);
		}
	}

	public void refresh() {
		fViewer.getViewer().refresh();
	}
	
	private void setInput(AbstractTextEditChange change, String left, String right, String type) {
		Object element= change.getModifiedElement();
		if (element instanceof IAdaptable) {
			IWorkbenchAdapter adapter= (IWorkbenchAdapter)((IAdaptable)element).getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				fViewer.setLabel(adapter.getLabel(element));
				fViewer.setImageDescriptor(adapter.getImageDescriptor(element));
			} else {
				fViewer.setLabel(null);
				fViewer.setImageDescriptor(null);
			}
		} else {
			fViewer.setLabel(null);
			fViewer.setImageDescriptor(null);
		}
		fViewer.setInput(new DiffNode( 
			new CompareElement(left, type), 
			new CompareElement(right, type)));
	}	
}
