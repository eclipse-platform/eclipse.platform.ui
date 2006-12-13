/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.incubator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @since 3.3
 * 
 */
public class EditorProvider extends AbstractProvider {

	private Map idToElement = new HashMap();

	public AbstractElement getElementForId(String id) {
		getElements();
		return (EditorElement) idToElement.get(id);
	}

	public AbstractElement[] getElements() {
		idToElement.clear();
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editors = activePage.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			EditorElement editorElement = new EditorElement(editors[i], this);
			idToElement.put(editorElement.getId(), editorElement);
		}
		return (AbstractElement[]) idToElement.values().toArray(
				new AbstractElement[idToElement.values().size()]);
	}

	public String getId() {
		return "org.eclipse.ui.editors"; //$NON-NLS-1$
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	public String getName() {
		return IncubatorMessages.CtrlEAction_Editors;
	}
}
