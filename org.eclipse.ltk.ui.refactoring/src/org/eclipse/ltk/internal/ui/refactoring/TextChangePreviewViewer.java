/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

public class TextChangePreviewViewer implements IChangePreviewViewer {

	private ComparePreviewer fViewer;
	
	private static class TextEditChangeInput extends ChangePreviewViewerInput {
		TextEditChangeGroup group;
		int surroundingLines;
		
		TextEditChangeGroup[] groups;
		IRegion range;
		
		public TextEditChangeInput(Change change) {
			super(change);
		}
	}
	
	private static class ComparePreviewer extends CompareViewerSwitchingPane {
		private CompareConfiguration fCompareConfiguration;
		public ComparePreviewer(Composite parent) {
			super(parent, SWT.BORDER | SWT.FLAT, true);
			fCompareConfiguration= new CompareConfiguration();
			fCompareConfiguration.setLeftEditable(false);
			fCompareConfiguration.setLeftLabel(RefactoringUIMessages.getString("ComparePreviewer.original_source")); //$NON-NLS-1$
			fCompareConfiguration.setRightEditable(false);
			fCompareConfiguration.setRightLabel(RefactoringUIMessages.getString("ComparePreviewer.refactored_source")); //$NON-NLS-1$
		}
		protected Viewer getViewer(Viewer oldViewer, Object input) {
			return CompareUI.findContentViewer(oldViewer, (ICompareInput)input, this, fCompareConfiguration);
		}
		public void setText(String text) {
			/*
			Object input= getInput();
			if (input instanceof CompareInput) {
				CompareInput cInput= (CompareInput)input;
				setImage(fLabelProvider.getImage(cInput.getChangeElement()));
				super.setText(fLabelProvider.getText(cInput.getChangeElement()));
			} else {
				super.setText(text);
				setImage(null);
			}
			*/
			super.setText(text);
		}
	}
	
	private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {
		private static final String ENCODING= "UTF-8";	//$NON-NLS-1$ // we use an encoding that preserves Unicode across the stream
		private String fContent;
		private String fType;
		public CompareElement(String content, String type) {
			fContent= content;
			fType= type;
		}
		public String getName() {
			return RefactoringUIMessages.getString("ComparePreviewer.element_name"); //$NON-NLS-1$
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
	
	public static ChangePreviewViewerInput createInput(TextChange change) {
		return new ChangePreviewViewerInput(change);
	}
	
	public static ChangePreviewViewerInput createInput(Change change, TextEditChangeGroup group, int surroundingLines) {
		TextEditChangeInput result= new TextEditChangeInput(change);
		result.group= group;
		result.surroundingLines= surroundingLines;
		return result;
	}
	
	public static ChangePreviewViewerInput createInput(Change change, TextEditChangeGroup[] groups, IRegion range) {
		TextEditChangeInput result= new TextEditChangeInput(change);
		result.groups= groups;
		result.range= range;
		return result;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fViewer= new ComparePreviewer(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#getControl()
	 */
	public Control getControl() {
		return fViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#setInput(org.eclipse.jdt.internal.ui.refactoring.ChangeElement)
	 */
	public void setInput(ChangePreviewViewerInput input) {
		try {
			Change change= input.getChange();
			if (input instanceof TextEditChangeInput) {
				TextEditChangeInput edi= (TextEditChangeInput)input;
				if (edi.group != null && edi.surroundingLines >= 0) {
					TextEditChangeGroup editChange= edi.group;
					TextChange textChange= editChange.getTextChange();
					setInput(textChange.getCurrentContent(editChange.getRegion(), true, 2),
						textChange.getPreviewContent(new TextEditChangeGroup[] { editChange }, editChange.getRegion(), true, 2),
						textChange.getTextType());
					return;
				} else if (edi.groups != null && edi.groups.length > 0 && edi.range != null) {
					TextChange textChange= edi.groups[0].getTextChange();
					setInput(textChange.getCurrentContent(edi.range, true, 0),
						textChange.getPreviewContent(edi.groups, edi.range, true, 0),
						textChange.getTextType());
					return;
				}
			} else if (change instanceof TextChange) {
				TextChange textChange= (TextChange)change;
				setInput(textChange.getCurrentContent(), textChange.getPreviewContent(), textChange.getTextType());
				return;
			} else {
				fViewer.setInput(null);
			}
		} catch (CoreException e) {
			RefactoringUIPlugin.log(e);
			fViewer.setInput(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#refresh()
	 */
	public void refresh() {
		fViewer.getViewer().refresh();
	}
	
	private void setInput(String left, String right, String type) {
		fViewer.setInput(new DiffNode( 
			new CompareElement(left, type), 
			new CompareElement(right, type)));
	}	
}
