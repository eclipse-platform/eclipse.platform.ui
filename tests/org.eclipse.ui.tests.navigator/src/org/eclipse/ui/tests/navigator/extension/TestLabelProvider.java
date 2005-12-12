/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

public class TestLabelProvider extends LabelProvider implements
		ICommonLabelProvider, IDescriptionProvider {

	public void init(IExtensionStateModel aStateModel,
			ITreeContentProvider aContentProvider) {

	}

	public Image getImage(Object element) {
		if (element instanceof TestExtensionTreeData)
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		return null;
	}

	public String getText(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getName();
		}
		return null;
	}

	public String getDescription(Object anElement) {
		if (anElement instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) anElement;
			return "TestItem: " + data.getName();
		}
		return null;
	}

	public void restoreState(IMemento aMemento) {

	}

	public void saveState(IMemento aMemento) {

	}

}
