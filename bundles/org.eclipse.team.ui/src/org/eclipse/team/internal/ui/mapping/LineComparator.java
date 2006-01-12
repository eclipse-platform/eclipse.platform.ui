/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.io.*;
import java.util.ArrayList;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

/**
 * This implementation of IRangeComparator breaks an input stream into lines.
 * Copied from org.eclipse.compare.internal.merge.LineComparator 1.4 and
 * modified for {@link IStorage}.
 */
class LineComparator implements IRangeComparator {

    private String[] fLines;

    public LineComparator(InputStream is, String encoding) throws UnsupportedEncodingException {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line;
        ArrayList ar = new ArrayList();
        try {
            while ((line = br.readLine()) != null)
                ar.add(line);
        } catch (IOException e) {
        		// silently ignored
        }
        try {
            is.close();
        } catch (IOException e1) {
        }
        fLines = (String[]) ar.toArray(new String[ar.size()]);
    }

    public LineComparator(IStorage storage, String outputEncoding) throws UnsupportedEncodingException, CoreException {
		this(new BufferedInputStream(storage.getContents()), getEncoding(storage, outputEncoding));
	}

	private static String getEncoding(IStorage storage, String outputEncoding) throws CoreException {
		if (storage instanceof IEncodedStorage) {
			IEncodedStorage es = (IEncodedStorage) storage;
			String charset = es.getCharset();
			if (charset != null)
				return charset;
		}
		return outputEncoding;
	}

	String getLine(int ix) {
        return fLines[ix];
    }

    /* (non-Javadoc)
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
     */
    public int getRangeCount() {
        return fLines.length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int, org.eclipse.compare.rangedifferencer.IRangeComparator, int)
     */
    public boolean rangesEqual(int thisIndex, IRangeComparator other,
            int otherIndex) {
        String s1 = fLines[thisIndex];
        String s2 = ((LineComparator) other).fLines[otherIndex];
        return s1.equals(s2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int, int, org.eclipse.compare.rangedifferencer.IRangeComparator)
     */
    public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
        return false;
    }
}
