/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v0.5 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;

import java.io.*;

class ByteCountOutputStream extends OutputStream {

	private long size = 0; 

	public void write(int b) throws IOException {
		size++;
	}
	public long getSize() {
		return size;
	}
}
