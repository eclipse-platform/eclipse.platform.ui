package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import java.io.*;
/** 
 * The <code>IElementInfoFlattener</code> interface supports
 * reading and writing element info objects.
 */
public interface IElementInfoFlattener {
/** 
 * Reads an element info from the given input stream.
 * @param elementPath the path of the element to be read
 * @param input the stream from which the element info should be read.
 * @return the object associated with the given elementPath,
 *   which may be <code>null</code>.
 */	
public Object readElement(IPath elementPath, DataInput input) throws IOException;
/**
 * Writes the given element to the output stream.
 * <p> N.B. The bytes written must be sufficient for the
 * purposes of reading the object back in.
 * @param elementPath the element's path in the tree
 * @param element the object associated with the given path,
 *   which may be <code>null</code>.
 */	
public void writeElement(IPath elementPath, Object element, DataOutput output) throws IOException;
}
