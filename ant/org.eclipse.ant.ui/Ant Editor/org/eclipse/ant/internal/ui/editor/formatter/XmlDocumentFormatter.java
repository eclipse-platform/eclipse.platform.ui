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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.jface.text.Assert;

public class XmlDocumentFormatter {

    private static class CommentReader extends TagReader {

        private boolean complete = false;

        protected void clear() {
            this.complete = false;
        }

        public String getStartOfTag() {
            return "<!--"; //$NON-NLS-1$
        }

        protected String readTag() throws IOException {
            int intChar;
            char c;
            StringBuffer node = new StringBuffer();

            while (!complete && (intChar = reader.read()) != -1) {
                c = (char) intChar;

                node.append(c);

                if (c == '>' && node.toString().endsWith("-->")) { //$NON-NLS-1$
                    complete = true;
                }
            }
            return node.toString();
        }
    }

    private static class DoctypeDeclarationReader extends TagReader {

        private boolean complete = false;

        protected void clear() {
            this.complete = false;
        }

        public String getStartOfTag() {
            return "<!"; //$NON-NLS-1$
        }

        protected String readTag() throws IOException {
            int intChar;
            char c;
            StringBuffer node = new StringBuffer();

            while (!complete && (intChar = reader.read()) != -1) {
                c = (char) intChar;

                node.append(c);

                if (c == '>') {
                    complete = true;
                }
            }
            return node.toString();
        }

    }

    private static class ProcessingInstructionReader extends TagReader {

        private boolean complete = false;

        protected void clear() {
            this.complete = false;
        }

        public String getStartOfTag() {
            return "<?"; //$NON-NLS-1$
        }

        protected String readTag() throws IOException {
            int intChar;
            char c;
            StringBuffer node = new StringBuffer();

            while (!complete && (intChar = reader.read()) != -1) {
                c = (char) intChar;

                node.append(c);

                if (c == '>' && node.toString().endsWith("?>")) { //$NON-NLS-1$
                    complete = true;
                }
            }
            return node.toString();
        }
    }

    private static abstract class TagReader {

        protected Reader reader;

        private String tagText;

        protected abstract void clear();

        public int getPostTagDepthModifier() {
            return 0;
        }

        public int getPreTagDepthModifier() {
            return 0;
        }

        abstract public String getStartOfTag();

        public String getTagText() {
            return this.tagText;
        }

        public boolean isTextNode() {
            return false;
        }

        protected abstract String readTag() throws IOException;

        public boolean requiresInitialIndent() {
            return true;
        }

        public void setReader(Reader reader) throws IOException {
            this.reader = reader;
            this.clear();
            this.tagText = readTag();
        }

        public boolean startsOnNewline() {
            return true;
        }
    }

    private static class TagReaderFactory {

        // Warning: the order of the Array is important!
        private static TagReader[] tagReaders = new TagReader[]{new CommentReader(),
                                                                new DoctypeDeclarationReader(),
                                                                new ProcessingInstructionReader(),
                                                                new XmlElementReader()};

        private static TagReader textNodeReader = new TextReader();

        public static TagReader createTagReaderFor(Reader reader)
                                                                 throws IOException {

            char[] buf = new char[10];
            reader.mark(10);
            reader.read(buf, 0, 10);
            reader.reset();

            String startOfTag = String.valueOf(buf);

            for (int i = 0; i < tagReaders.length; i++) {
                if (startOfTag.startsWith(tagReaders[i].getStartOfTag())) {
                    tagReaders[i].setReader(reader);
                    return tagReaders[i];
                }
            }
            // else
            textNodeReader.setReader(reader);
            return textNodeReader;
        }
    }

    private static class TextReader extends TagReader {

        private boolean complete;

        private boolean isTextNode;

        protected void clear() {
            this.complete = false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader#getStartOfTag()
         */
        public String getStartOfTag() {
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader#isTextNode()
         */
        public boolean isTextNode() {
            return this.isTextNode;
        }

        protected String readTag() throws IOException {

            StringBuffer node = new StringBuffer();

            while (!complete) {

                reader.mark(1);
                int intChar = reader.read();
                if (intChar == -1) break;

                char c = (char) intChar;
                if (c == '<') {
                    reader.reset();
                    complete = true;
                } else {
                    node.append(c);
                }
            }

            // if this text node is just whitespace
            // remove it, except for the newlines.
            if (node.length() < 1) {
                this.isTextNode = false;

            } else if (node.toString().trim().length() == 0) {
                String whitespace = node.toString();
                node = new StringBuffer();
                for (int i = 0; i < whitespace.length(); i++) {
                    char whitespaceCharacter = whitespace.charAt(i);
                    if (whitespaceCharacter == '\n')
                            node.append(whitespaceCharacter);
                }
                this.isTextNode = false;

            } else {
                this.isTextNode = true;
            }
            return node.toString();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader#requiresInitialIndent()
         */
        public boolean requiresInitialIndent() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter.TagReader#startsOnNewline()
         */
        public boolean startsOnNewline() {
            return false;
        }
    }

    private static class XmlElementReader extends TagReader {

        private boolean complete = false;

        protected void clear() {
            this.complete = false;
        }

        public int getPostTagDepthModifier() {
            if (getTagText().endsWith("/>") || getTagText().endsWith("/ >")) { //$NON-NLS-1$ //$NON-NLS-2$
                return 0;
            } else if (getTagText().startsWith("</")) { //$NON-NLS-1$
                return 0;
            } else {
                return +1;
            }
        }

        public int getPreTagDepthModifier() {
            if (getTagText().startsWith("</")) { //$NON-NLS-1$
                return -1;
            } else {
                return 0;
            }
        }

        public String getStartOfTag() {
            return "<"; //$NON-NLS-1$
        }

        protected String readTag() throws IOException {

            StringBuffer node = new StringBuffer();

            boolean insideQuote = false;
            int intChar;

            while (!complete && (intChar = reader.read()) != -1) {
                char c = (char) intChar;

                node.append(c);
                // TODO logic incorrectly assumes that " is quote character
                // when it could also be '
                if (c == '"') {
                    insideQuote = !insideQuote;
                }
                if (c == '>' && !insideQuote) {
                    complete = true;
                }
            }
            return node.toString();
        }
    }

    private int depth;

    private String documentText;

    private StringBuffer formattedXml;

    private boolean lastNodeWasText;

    private FormattingPreferences prefs;

    private void copyNode(Reader reader, StringBuffer out) throws IOException {

        TagReader tag = TagReaderFactory.createTagReaderFor(reader);

        depth = depth + tag.getPreTagDepthModifier();

        if (!lastNodeWasText) {

            if (tag.startsOnNewline() && !hasNewlineAlready(out)) {
                out.append("\n"); //$NON-NLS-1$
            }

            if (tag.requiresInitialIndent()) {
                out.append(indent());
            }
        }

        out.append(tag.getTagText());

        depth = depth + tag.getPostTagDepthModifier();

        lastNodeWasText = tag.isTextNode();

    }

    /**
     * @return
     */
    public String format() {

        Assert.isNotNull(this.documentText);
        Assert.isNotNull(this.prefs);

        Reader reader = new StringReader(documentText);
        formattedXml = new StringBuffer();

        depth = 0;
        lastNodeWasText = false;
        try {
            while (true) {
                reader.mark(1);
                int intChar = reader.read();
                reader.reset();

                if (intChar != -1) {
                    copyNode(reader, formattedXml);
                } else {
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
           AntUIPlugin.log(e);
        }
        return formattedXml.toString();
    }

    private boolean hasNewlineAlready(StringBuffer out) {
        return out.lastIndexOf("\n") == formattedXml.length() - 1 //$NON-NLS-1$
               || out.lastIndexOf("\r") == formattedXml.length() - 1; //$NON-NLS-1$
    }

    private String indent() {
        StringBuffer indent = new StringBuffer(30);
        for (int i = 0; i < depth; i++) {
            indent.append(prefs.getCanonicalIndent());
        }
        return indent.toString();
    }

    public void setFormattingPreferences(FormattingPreferences prefs) {
        this.prefs = prefs;
    }

    public void setText(String documentText) {
        this.documentText = documentText;
    }
}
