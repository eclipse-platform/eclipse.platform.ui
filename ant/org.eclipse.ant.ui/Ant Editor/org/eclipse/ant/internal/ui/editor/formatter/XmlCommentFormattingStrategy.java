/*******************************************************************************
 * Copyright (c) 2004 John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 * 	   IBM Corporation - bug 40255
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.formatter;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.Assert;

public class XmlCommentFormattingStrategy extends ContextBasedFormattingStrategy {

    private static class CommentPartitionDecorator {

        public static CommentPartitionDecorator decorate(IDocument document,
                Position partition) {
            return new CommentPartitionDecorator(document, partition);
        }

        private IDocument document;

        private Position partition;

        public CommentPartitionDecorator(IDocument document, Position partition) {
        }

        /**
         * @param p1
         *            the partition with the low offset
         * @param p2
         *            the partition with the high offset
         * @return true of both partitions are to be formatted as a single unit
         */
        private boolean formatTogether(Position p1, Position p2)
                throws BadLocationException {

            Assert.isTrue(p1.offset < p2.offset);
            String interveningText = textBetween(p1, p2);

            if (interveningText.trim().length() == 0
                    && interveningText.indexOf('\n') == -1
                    && interveningText.indexOf('\r') == -1) {
                return true;
            } else {
                return false;
            }
        }

        public boolean formatWith(Position partitionToCompare)
                throws BadLocationException {

            Assert.isNotNull(this.document);
            Assert.isNotNull(this.partition);

            if (this.partition.offset < partitionToCompare.offset) {
                return formatTogether(this.partition, partitionToCompare);
            } else if (this.partition.offset > partitionToCompare.offset) {
                return formatTogether(partitionToCompare, this.partition);
            } else {
                // I wouldn't expect both partitions to
                // start at the same spot.
                return false;
            }
        }

        /**
         * @return {@link Position#getLength()}
         */
        public int getLength() {
            return partition.getLength();
        }

        /**
         * @return {@link Position#getOffset()}
         */
        public int getOffset() {
            return partition.getOffset();
        }

        public String getText() throws BadLocationException {
            return textOf(this.partition);
        }

        /**
         * @param string
         */
        public void replaceWith(String text) throws BadLocationException {

            if (text != null && !text.equals(this.getText()))
                    document.replace(this.getOffset(), this.getLength(), text);

        }

        /**
         * @param p1
         *            the partition with the low offset
         * @param p2
         *            the partition with the high offset
         * @return text between the end of the first positions and start of the
         *         second
         */
        private String textBetween(Position p1, Position p2)
                throws BadLocationException {
            return document.get(p1.offset + p1.length, p2.offset
                    - (p1.offset + p1.length));
        }

        private String textOf(Position textPartition) throws BadLocationException {
            return this.document.get(textPartition.offset, textPartition.length);
        }
    }

    private abstract static class Normalizer {

        abstract protected boolean isApplicableFor(String commentText);

        abstract protected String normalize(String commentText);

        public String safelyNormalize(String commentText) {
            if (isApplicableFor(commentText))
                return normalize(commentText);
            else
                return commentText;
        }
    }

    private static class SeparartorCommentNormalizer extends Normalizer {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ant.internal.ui.editor.format.XmlCommentFormattingStrategy.Normalizer#isApplicableFor(java.lang.String)
         */
        protected boolean isApplicableFor(String commentText) {
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ant.internal.ui.editor.format.XmlCommentFormattingStrategy.Normalizer#normalize(java.lang.String)
         */
        protected String normalize(String commentText) {
            return null;
        }
    }

    private class TextCommentNormalizer extends Normalizer {

        private String actualText(String comment) {
            return comment.substring(4, comment.length() - 3).trim();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ant.internal.ui.editor.format.XmlCommentFormattingStrategy.Normalizer#isApplicableFor(java.lang.String)
         */
        protected boolean isApplicableFor(String commentText) {

            return Character.isLetterOrDigit(actualText(commentText).charAt(0))
                    && commentText.indexOf('\n') == -1
                    && commentText.indexOf('\r') == -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ant.internal.ui.editor.format.XmlCommentFormattingStrategy.Normalizer#normalize(java.lang.String)
         */
        protected String normalize(String comment) {

            String text = comment.substring(4, comment.length() - 3).trim();
            
            // TODO assumes text length < fCommentWidth which isn't necessarily the case
            char[] whitespace = new char[fCommentWidth - (text.length() + 7)];
            Arrays.fill(whitespace,' ');

            return "<!-- " + actualText(comment) + String.valueOf(whitespace) //$NON-NLS-1$
                    + "-->"; //$NON-NLS-1$
        }
    }

	private final int fCommentWidth = 40; 

    /** Indentations to use by this strategy */
    private final LinkedList fIndentations;

    /** Normalizers for handling partition formatting */
    private final Normalizer[] fNormalizers;

    /** Partitions to be formatted by this strategy */
    private final LinkedList fPartitions;

    /**
     * @param viewer
     */
    public XmlCommentFormattingStrategy(ISourceViewer viewer) {
        super(viewer);
        fIndentations = new LinkedList();
        fPartitions = new LinkedList();
        fNormalizers = new Normalizer[] {};
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

       //final String indent = fIndentations.removeFirst().toString();
        final CommentPartitionDecorator partition = (CommentPartitionDecorator) fPartitions
                .removeFirst();

        // normalize length
        // normalize char pattern
        // format text
        // normalize

        try {

            for (int i = 0; i < fNormalizers.length; i++) {
                partition.replaceWith(fNormalizers[i].safelyNormalize(partition
                        .getText()));
            }

        } catch (BadLocationException e) {
        }

    }

    /*
     * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
     */
    public void formatterStarts(IFormattingContext context) {
        super.formatterStarts(context);

        final FormattingContext current = (FormattingContext) context;
        fIndentations.addLast(current.getProperty(FormattingContextProperties.CONTEXT_INDENTATION));

        fPartitions.addLast(CommentPartitionDecorator.decorate(getViewer().getDocument()
                , (Position) current.getProperty(FormattingContextProperties.CONTEXT_PARTITION)));
    }

    /*
     * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops()
     */
    public void formatterStops() {
        super.formatterStops();
        fIndentations.clear();
        fPartitions.clear();
    }
}