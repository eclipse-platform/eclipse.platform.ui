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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.ISourceViewer;

public class XmlElementFormattingStrategy extends
        ContextBasedFormattingStrategy {

    /** Indentations to use by this strategy */
    private final LinkedList fIndentations = new LinkedList();

    /** Partitions to be formatted by this strategy */
    private final LinkedList fPartitions = new LinkedList();

    /** The position sets to keep track of during formatting */
    private final LinkedList fPositions = new LinkedList();

    /** access to the preferences store **/
    private final FormattingPreferences prefs = new FormattingPreferences();

    public XmlElementFormattingStrategy(ISourceViewer viewer) {
        super(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#format()
     */
    public void format() {

        super.format();

        Assert.isLegal(fPartitions.size() > 0);
        Assert.isLegal(fIndentations.size() > 0);

        TypedPosition partition = (TypedPosition) fPartitions.removeFirst();
        String lineIndent = fIndentations.removeFirst().toString();
        IDocument document = getViewer().getDocument();

        try {

            String formatted = formatElement(document, partition, lineIndent);

            String partitionText = document.get(partition.getOffset(),
                    partition.getLength());

            if (formatted != null && !formatted.equals(partitionText)) {
                document.replace(partition.getOffset(), partition.getLength(),
                        formatted);
            }

        } catch (BadLocationException e) {
        }
    }

private String formatElement(IDocument document, TypedPosition partition,
            String indentation) throws BadLocationException {

        String partitionText = document.get(partition.getOffset(), partition
                .getLength());

        IRegion line = document.getLineInformationOfOffset(partition
                .getOffset());

        FormattingPreferences prefs = new FormattingPreferences();

        int indentLength = partition.getOffset() - line.getOffset();

        return new XmlTagFormatter().format(partitionText, prefs, document.get(line.getOffset(),
                indentLength));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
     */
    public void formatterStarts(IFormattingContext context) {
        super.formatterStarts(context);

        FormattingContext current = (FormattingContext) context;

        fIndentations.addLast(current
                .getProperty(FormattingContextProperties.CONTEXT_INDENTATION));
        fPartitions.addLast(current
                .getProperty(FormattingContextProperties.CONTEXT_PARTITION));
        fPositions.addLast(current
                .getProperty(FormattingContextProperties.CONTEXT_POSITIONS));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStops()
     */
    public void formatterStops() {
        super.formatterStops();

        fIndentations.clear();
        fPartitions.clear();
        fPositions.clear();
    }

}