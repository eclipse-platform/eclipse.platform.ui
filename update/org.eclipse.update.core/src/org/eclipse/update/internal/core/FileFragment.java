/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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