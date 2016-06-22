/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
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
        return "";
    }

    @Override
    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        return new Region(offset, 0);
    }
}