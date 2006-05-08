/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This class is a simple parser implementing the IReadmeFileParser
 * interface. It parses a Readme file into sections based on the
 * existence of numbered section tags in the input. A line beginning
 * with a number followed by a dot will be taken as a section indicator
 * (for example, 1., 2., or 12.). 
 * As well, a line beginning with a subsection-style series of numbers
 * will also be taken as a section indicator, and can be used to 
 * indicate subsections (for example, 1.1, or 1.1.12).
 */
public class DefaultSectionsParser implements IReadmeFileParser {
    /**
     * Returns the mark element that is the logical parent
     * of the given mark number.  Each dot in a mark number
     * represents a parent-child separation.  For example,
     * the parent of 1.2 is 1, the parent of 1.4.1 is 1.4.
     * Returns null if there is no appropriate parent.
     */
    protected IAdaptable getParent(Hashtable toc, String number) {
        int lastDot = number.lastIndexOf('.');
        if (lastDot < 0)
            return null;
        String parentNumber = number.substring(0, lastDot);
        return (IAdaptable) toc.get(parentNumber);
    }

    /**
     * Returns a string containing the contents of the given
     * file.  Returns an empty string if there were any errors
     * reading the file.
     */
    protected String getText(IFile file) {
        try {
            InputStream in = file.getContents();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read = in.read(buf);
            while (read > 0) {
                out.write(buf, 0, read);
                read = in.read(buf);
            }
            return out.toString();
        } catch (CoreException e) {
            // do nothing
        } catch (IOException e) {
            // do nothing
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Parses the input given by the argument.
     *
     * @param file  the element containing the input text
     * @return an element collection representing the parsed input
     */
    public MarkElement[] parse(IFile file) {
        Hashtable markTable = new Hashtable(40);
        Vector topLevel = new Vector();
        String s = getText(file);
        int start = 0;
        int end = -1;
        int lineno = 0;
        int lastlineno = 0;
        MarkElement lastme = null;
        int ix;

        // parse content for headings
        ix = s.indexOf('\n', start);
        while (ix != -1) {
            start = end + 1;
            end = ix = s.indexOf('\n', start);
            lineno++;
            if (ix != -1) {
                // skip blanks
                while (s.charAt(start) == ' ' || s.charAt(start) == '\t') {
                    start++;
                }
                if (Character.isDigit(s.charAt(start))) {
                    if (lastme != null) {
                        lastme.setNumberOfLines(lineno - lastlineno - 1);
                    }
                    lastlineno = lineno;
                    String markName = parseHeading(s, start, end);

                    //get the parent mark, if any.
                    String markNumber = parseNumber(markName);
                    IAdaptable parent = getParent(markTable, markNumber);
                    if (parent == null)
                        parent = file;

                    MarkElement me = new MarkElement(parent, markName, start,
                            end - start);
                    lastme = me;

                    markTable.put(markNumber, me);
                    if (parent == file) {
                        topLevel.add(me);
                    }
                }
            }
        }
        if (lastme != null) {
            // set the number of lines for the last section
            lastme.setNumberOfLines(lineno - lastlineno - 1);
        }
        MarkElement[] results = new MarkElement[topLevel.size()];
        topLevel.copyInto(results);
        return results;
    }

    /**
     * Creates a section name from the buffer and trims trailing
     * space characters.
     *
     * @param buffer  the string from which to create the section name
     * @param start  the start index
     * @param end  the end index
     * @return a section name
     */
    private String parseHeading(String buffer, int start, int end) {
        while (Character.isWhitespace(buffer.charAt(end - 1)) && end > start) {
            end--;
        }
        return buffer.substring(start, end);
    }

    /**
     * Returns the number for this heading.  A heading consists
     * of a number (an arbitrary string of numbers and dots), followed by
     * arbitrary text.
     */
    protected String parseNumber(String heading) {
        int start = 0;
        int end = heading.length();
        char c;
        do {
            c = heading.charAt(start++);
        } while ((c == '.' || Character.isDigit(c)) && start < end);

        //disregard trailing dots
        while (heading.charAt(start - 1) == '.' && start > 0) {
            start--;
        }
        return heading.substring(0, start);
    }
}
