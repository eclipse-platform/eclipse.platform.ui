/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.dtree;

import org.eclipse.core.runtime.IPath;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
/** 
 * The <code>IElementInfoFlattener</code> interface supports
 * reading and writing element info objects.
 */
public interface IDataFlattener {
/** 
 * Reads a data object from the given input stream.
 * @param path the path of the element to be read
 * @param input the stream from which the element info should be read.
 * @return the object associated with the given path,
 *   which may be <code>null</code>.
 */	
public Object readData(IPath path, DataInput input) throws IOException;
/**
 * Writes the given data to the output stream.
 * <p> N.B. The bytes written must be sufficient for the
 * purposes of reading the object back in.
 * @param path the element's path in the tree
 * @param data the object associated with the given path,
 *   which may be <code>null</code>.
 */	
public void writeData(IPath path, Object data, DataOutput output) throws IOException;
}
