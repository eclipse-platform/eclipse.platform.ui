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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.Assert;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

/* package */ class TextEditChangeElement extends ChangeElement {
	
	private static final ChangeElement[] fgChildren= new ChangeElement[0];
	
	private TextEditChangeGroup fChange;
	
	public TextEditChangeElement(ChangeElement parent, TextEditChangeGroup change) {
		super(parent);
		fChange= change;
		Assert.isNotNull(fChange);
	}
	
	/**
	 * Returns the <code>TextEditChange</code> managed by this node.
	 * 
	 * @return the <code>TextEditChange</code>
	 */
	public TextEditChangeGroup getTextEditChange() {
		return fChange;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.ChangeElement#getChangePreviewViewer()
	 */
	public ChangePreviewViewerDescriptor getChangePreviewViewer() throws CoreException {
		DefaultChangeElement element= getStandardChangeElement();
		if (element == null)
			return null;
		return element.getChangePreviewViewer();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.ChangeElement#feedInput(org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer)
	 */
	public void feedInput(IChangePreviewViewer viewer) throws CoreException {
		DefaultChangeElement element= getStandardChangeElement();
		if (element != null) {
			Change change= element.getChange();
			if (change instanceof TextChange) {
				IRegion range= getTextRange(this);
				Object input= null;
				if (range != null) {
					input= TextChangePreviewViewer.createInput(new TextEditChangeGroup[] {fChange}, range);
				} else {
					input= TextChangePreviewViewer.createInput(fChange, 2);
				}
				viewer.setInput(input);
			}
		} else {
			viewer.setInput(null);
		}
	}
	
	/* non Java-doc
	 * @see ChangeElement#setActive
	 */
	public void setActive(boolean active) {
		fChange.setEnabled(active);
	}
	
	/* non Java-doc
	 * @see ChangeElement.getActive
	 */
	public int getActive() {
		return fChange.isEnabled() ? ACTIVE : INACTIVE;
	}
	
	/* non Java-doc
	 * @see ChangeElement.getChildren
	 */
	public ChangeElement[] getChildren() {
		return fgChildren;
	}
	
	private DefaultChangeElement getStandardChangeElement() {
		ChangeElement element= getParent();
		while(!(element instanceof DefaultChangeElement) && element != null) {
			element= element.getParent();
		}
		return (DefaultChangeElement)element;
	}
	
	private static IRegion getTextRange(ChangeElement element) throws CoreException {
		if (element == null)
			return null;
		if (element instanceof PseudoJavaChangeElement) {
			return ((PseudoJavaChangeElement)element).getTextRange();
		} else if (element instanceof DefaultChangeElement) {
			return null;
		}
		return getTextRange(element.getParent());
	}
}

