/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.model;

import java.io.File;

import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.editor.AntEditorCompletionProcessor;
import org.xml.sax.Attributes;

public class AntAntNode extends AntTaskNode {

    String fFile;

    public AntAntNode(Task task, Attributes attributes) {
        super(task);
        StringBuffer label= new StringBuffer("ant "); //$NON-NLS-1$
        String more = attributes.getValue(IAntModelConstants.ATTR_DIR);
        if (more != null) {
            label.append(more);
            label.append(File.separatorChar);
        }
        fFile = attributes.getValue(IAntModelConstants.ATTR_ANT_FILE);
        if (fFile == null) {
            fFile= "build.xml"; //$NON-NLS-1$
        }
        label.append(fFile);
        
        more = attributes.getValue(IAntModelConstants.ATTR_TARGET);
        if(more != null) {
            label.append(' ');
            label.append(more);
        }
        setBaseLabel(label.toString());
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#getReferencedElement(int)
     */
    public String getReferencedElement(int offset) {
        if (fFile != null) {
            String textToSearch= getAntModel().getText(getOffset(), offset - getOffset());
            String attributeString = AntEditorCompletionProcessor.getAttributeStringFromDocumentStringToPrefix(textToSearch);
            if ("antfile".equals(attributeString)) {  //$NON-NLS-1$
                return fFile;
            }
        }
        return null;
    }
}