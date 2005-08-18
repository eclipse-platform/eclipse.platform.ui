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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.core.refactoring.Change;

import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.Assert;

public class TextEditChangeElement extends ChangeElement {
	
	private static final ChangeElement[] fgChildren= new ChangeElement[0];
	
	private TextEditBasedChangeGroup fChangeGroup;
	
	public TextEditChangeElement(ChangeElement parent, TextEditBasedChangeGroup changeGroup) {
		super(parent);
		fChangeGroup= changeGroup;
		Assert.isNotNull(fChangeGroup);
	}
	
	/**
	 * Returns the <code>TextEditBasedChangeGroup</code> managed by this node.
	 * 
	 * @return the <code>TextEditBasedChangeGroup</code>
	 */
	public TextEditBasedChangeGroup getChangeGroup() {
		return fChangeGroup;
	}
	
	public Object getModifiedElement() {
		return fChangeGroup;
	}
	
	public Change getChange() {
		return null;
	}
	
	public ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException {
		DefaultChangeElement element= getDefaultChangeElement();
		if (element == null)
			return null;
		return element.getChangePreviewViewerDescriptor();
	}
	
	public void feedInput(IChangePreviewViewer viewer) throws CoreException {
		DefaultChangeElement element= getDefaultChangeElement();
		if (element != null) {
			Change change= element.getChange();
			if (change instanceof TextEditBasedChange) {
				IRegion range= getTextRange(this);
				ChangePreviewViewerInput input= null;
				if (range != null) {
					input= TextEditChangePreviewViewer.createInput(change, new TextEditBasedChangeGroup[] {fChangeGroup}, range);
				} else {
					input= TextEditChangePreviewViewer.createInput(change, fChangeGroup, 2);
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
	public void setEnabled(boolean enabled) {
		fChangeGroup.setEnabled(enabled);
	}
	
	public void setEnabledShallow(boolean enabled) {
		fChangeGroup.setEnabled(enabled);
	}
	
	/* non Java-doc
	 * @see ChangeElement.getActive
	 */
	public int getActive() {
		return fChangeGroup.isEnabled() ? ACTIVE : INACTIVE;
	}
	
	/* non Java-doc
	 * @see ChangeElement.getChildren
	 */
	public ChangeElement[] getChildren() {
		return fgChildren;
	}
	
	private DefaultChangeElement getDefaultChangeElement() {
		ChangeElement element= getParent();
		while(!(element instanceof DefaultChangeElement) && element != null) {
			element= element.getParent();
		}
		return (DefaultChangeElement)element;
	}
	
	private static IRegion getTextRange(ChangeElement element) throws CoreException {
		if (element == null)
			return null;
		if (element instanceof PseudoLanguageChangeElement) {
			return ((PseudoLanguageChangeElement)element).getTextRange();
		} else if (element instanceof DefaultChangeElement) {
			return null;
		}
		return getTextRange(element.getParent());
	}
}