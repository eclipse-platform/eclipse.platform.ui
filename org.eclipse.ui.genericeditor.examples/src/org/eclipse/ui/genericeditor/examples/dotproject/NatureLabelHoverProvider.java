/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *     Lucas Bullen (Red Hat Inc.) - [Bug 527071] Hover breaks with missing nature
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
public class NatureLabelHoverProvider implements ITextHover {

	public NatureLabelHoverProvider() {
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		String contents= textViewer.getDocument().get();
		int offset= hoverRegion.getOffset();
		int endIndex= contents.indexOf("</nature>", offset);
		if (endIndex==-1) return "";
		int startIndex= contents.substring(0, offset).lastIndexOf("<nature>");
		if (startIndex==-1) return "";
		String selection = contents.substring(startIndex+"<nature>".length(), endIndex);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectNatureDescriptor[] natureDescriptors= workspace.getNatureDescriptors();
		for (int i= 0; i < natureDescriptors.length; i++) {
			if (natureDescriptors[i].getNatureId().equals(selection))
				return natureDescriptors[i].getLabel();
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}
}