/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;

/**
 * @author markus.schorn@windriver.com
 */
public class EditorDescriptorLabelProvider extends LabelProvider {
	public final static EditorDescriptorLabelProvider INSTANCE= new EditorDescriptorLabelProvider();

	private List imagesToDispose= new ArrayList();

	private EditorDescriptorLabelProvider() {
		super();
	}

	public void dispose() {
		super.dispose();
		for (Iterator e= imagesToDispose.iterator(); e.hasNext();) {
			((Image) e.next()).dispose();
		}
		imagesToDispose.clear();
	}

	public Image getColumnImage(Object element, int row) {
		return getImage(element);
	}

	public String getColumnText(Object element, int row) {
		return getText(element);
	}

	public Image getImage(Object element) {
		if (element instanceof Map.Entry) {
			Entry e= (Entry) element;
			if (e.getValue() instanceof IEditorDescriptor) {
				Image image= ((IEditorDescriptor) e.getValue()).getImageDescriptor().createImage();
				imagesToDispose.add(image);
				return image;
			}
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof Map.Entry) {
			Entry e= (Entry) element;
			return e.getKey().toString();
		}
		return null;
	}
}
