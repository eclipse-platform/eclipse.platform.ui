/*******************************************************************************
 * Copyright (c) 2004 John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 * 	   IBM Corporation - bug 52076
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.formatter;

import java.util.LinkedList;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.ISourceViewer;

public class XmlDocumentFormattingStrategy extends ContextBasedFormattingStrategy {

    /** Indentations to use by this strategy */
    private final LinkedList fIndentations = new LinkedList();

    /** Partitions to be formatted by this strategy */
    private final LinkedList fPartitions = new LinkedList();

    /** The position sets to keep track of during formatting */
    private final LinkedList fPositions = new LinkedList();

    // TODO connect with preferences
    //private final boolean addNewlines = true;

    // TODO connect with preferences
   // private final String canonicalIndentStep = "\t"; //$NON-NLS-1$

    public XmlDocumentFormattingStrategy(ISourceViewer viewer) {
        super(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.formatter.IFormattingStrategy#format(java.lang.String,
     *      boolean, java.lang.String, int[])
     */
    public void format() {

        super.format();

        Assert.isLegal(fPartitions.size() > 0);
        Assert.isLegal(fIndentations.size() > 0);

//        final TypedPosition partition = (TypedPosition) fPartitions.removeFirst();
//        final String indent = fIndentations.removeFirst().toString();
       IDocument document = getViewer().getDocument();

        // TODO Since we are running short on time, we'll
        // format the whole document, not just a single partition.
        // We can correct this later--if we want to.
        
        String documentText = document.get();
        
        // setup formatter with preferences and format the text.
        FormattingPreferences prefs = new FormattingPreferences();
        
        XmlDocumentFormatter formatter = new XmlDocumentFormatter(documentText, prefs);     
        
        String formattedText = formatter.format();
        if(formattedText != null && ! formattedText.equals(documentText)) {
            document.set(formattedText);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
     */
    public void formatterStarts(IFormattingContext context) {
        super.formatterStarts(context);

        final FormattingContext current = (FormattingContext) context;

        fIndentations.addLast(current.getProperty(FormattingContextProperties.CONTEXT_INDENTATION));
        fPartitions.addLast(current.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
        fPositions.addLast(current.getProperty(FormattingContextProperties.CONTEXT_POSITIONS));
    }

    /*
     * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops()
     */
    public void formatterStops() {
        super.formatterStops();

        fIndentations.clear();
        fPartitions.clear();
        fPositions.clear();
    }
}