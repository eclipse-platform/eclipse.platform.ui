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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

public class XmlElementFormattingStrategy extends ContextBasedFormattingStrategy {

    /** Indentations to use by this strategy */
    private final LinkedList fIndentations = new LinkedList();

    /** Partitions to be formatted by this strategy */
    private final LinkedList fPartitions = new LinkedList();

    /** The position sets to keep track of during formatting */
    private final LinkedList fPositions = new LinkedList();

    public XmlElementFormattingStrategy(ISourceViewer viewer) {
        super(viewer);
    }

    /* (non-Javadoc)
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
            FormattingPreferences prefs = new FormattingPreferences();
            String formatted = formatElement(document, partition, lineIndent, prefs);

            String partitionText = document.get(partition.getOffset(), partition.getLength());

            if (formatted != null && !formatted.equals(partitionText)) {
            	document.replace(partition.getOffset(), partition.getLength(), formatted);
            }

        } catch (BadLocationException e) {
        }
    }

    private String formatElement(IDocument document, TypedPosition partition, String indentation, FormattingPreferences prefs) throws BadLocationException {

        String partitionText = document.get(partition.getOffset(), partition.getLength());

        StringBuffer formattedElement = null;

        // do we even need to think about wrapping?
        if (prefs.useElementWrapping() && !partitionText.startsWith("</")) { //$NON-NLS-1$

            IRegion line = document.getLineInformationOfOffset(partition.getOffset());

            int partitionLineOffset = partition.getOffset() - line.getOffset();

            // do we have a good candidate for a wrap?
            if (line.getLength() > prefs.getMaximumLineWidth()) {

                List attributes = getAttributes(partitionText);
                if (attributes.size() > 1) {
                    formattedElement = new StringBuffer();
                    String startTag = elementStart(partitionText);
                    formattedElement.append(startTag);
                    formattedElement.append(' ');
                    formattedElement.append(attributes.get(0));
                    formattedElement.append("\n"); //$NON-NLS-1$
                    
                    for (int i = 1; i < attributes.size(); i++) {
                        formattedElement.append(indentation);
                        for (int j = 0; j < (partitionLineOffset - indentation
                                .length())
                                + startTag.length() + 1; j++) {
                            formattedElement.append(' ');
                        }
                        formattedElement.append(attributes.get(i));
                        formattedElement.append("\n"); //$NON-NLS-1$
                    }
                    formattedElement.append(indentation);
                    for (int j = 0; j < (partitionLineOffset - indentation
                            .length()) + 1; j++) {
                        formattedElement.append(' ');
                    }
                    if (partitionText.endsWith("/>")) { //$NON-NLS-1$
                        formattedElement.append("/>"); //$NON-NLS-1$
                    } else if (partitionText.endsWith(">")) { //$NON-NLS-1$
                        formattedElement.append(">"); //$NON-NLS-1$
                    } else {
                        Assert.isTrue(false, "Bad Partitioner."); //$NON-NLS-1$
                    }
                }
            }
        }
        return formattedElement != null ? formattedElement.toString() : null;
    }

    private List getAttributes(String text) {

        List attributes = new ArrayList();

        int start = firstWhitespaceIn(text);
        boolean insideQuotes = false;

        boolean haveEquals = false;
        int quotes = 0;
        StringBuffer attributePair = new StringBuffer();

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
            case '"':
                insideQuotes = !insideQuotes;
                quotes++;
                attributePair.append(c);
                if (!insideQuotes && haveEquals && quotes == 2) {
                    // we're done with this attribute
                    attributes.add(attributePair.toString());
                    // reset
                    attributePair = new StringBuffer();
                    quotes = 0;
                    haveEquals = false;
                }
                break;
            case '=':
                attributePair.append(c);
                haveEquals = true;
                break;
            default:
                if (Character.isWhitespace(c) && !insideQuotes) {
                    if (!Character.isWhitespace(text.charAt(i - 1))
                            && attributePair.length() != 0) {
                        attributePair.append(' ');
                    }
                } else {
                    attributePair.append(c);
                }
                break;
            }
        }
        return attributes;
    }

    private String elementStart(String text) {
        return text.substring(0, firstWhitespaceIn(text));
    }

    private int firstWhitespaceIn(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) { 
            	return i; 
            }
        }
        return -1;
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStops()
     */
    public void formatterStops() {
        super.formatterStops();

        fIndentations.clear();
        fPartitions.clear();
        fPositions.clear();
    }
}
