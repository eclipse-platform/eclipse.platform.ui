/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor;

import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class AntElementHyperlinkDetector implements IHyperlinkDetector {

    private AntEditor fEditor;
    
    public AntElementHyperlinkDetector(AntEditor editor) {    
        fEditor= editor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region) {
        if (region == null) {
			return null;
        }
        region= XMLTextHover.getRegion(fEditor.getViewer(), region.getOffset());
        Object linkTarget= this.fEditor.findTarget(region);
		if (linkTarget == null) {
			return null;
		}
        return new IHyperlink[] {new AntElementHyperlink(fEditor, region, linkTarget)};
    }
}
