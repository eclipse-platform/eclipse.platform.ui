package org.eclipse.ui.externaltools.internal.ant.editor.text;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//

/*
 * This file originates from an internal package of Eclipse's 
 * Manifest Editor. It has been copied by GEBIT to here in order to
 * permanently use those features. It has been renamed and edited by GEBIT 
 * after copying.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 * @version 21.11.2002
 * @author Alf Schiefelbein
 */
public class PlantyDocumentProvider extends FileDocumentProvider {

    private IDocumentPartitioner createDocumentPartitioner() {
        DefaultPartitioner partitioner =
            new DefaultPartitioner(
                new PlantyPartitionScanner(),
                new String[] {
                    PlantyPartitionScanner.XML_TAG,
                    PlantyPartitionScanner.XML_COMMENT });
        return partitioner;
    }

    public IDocument createDocument(Object element) throws CoreException {
        IDocument document = super.createDocument(element);
        if (document != null) {
            IDocumentPartitioner partitioner = createDocumentPartitioner();
            if (partitioner != null) {
                partitioner.connect(document);
                document.setDocumentPartitioner(partitioner);
            }
        }
        return document;
    }
}
