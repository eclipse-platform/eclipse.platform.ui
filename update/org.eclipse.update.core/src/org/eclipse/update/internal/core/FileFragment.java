/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.internal.core;

import java.io.*;


public class FileFragment{
	private File file;
	private int bytes;
	public FileFragment(File file, int size){
		this.file=file;
		this.bytes=size;
	}
	public File getFile(){
		return file;
	}
	public int getSize(){
		return bytes;
	}
}