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

package org.eclipse.update.internal.core;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class NioHelper {
	// This method must be in a seperate class file otherwise a NIO class not found error
	//  will be thrown loading the calling class.  We put it in it's own class so the
	//  caller can catch the error/exception.
	static void copyFile(File src, File dst) throws IOException {
		FileChannel in = null;
		FileChannel out = null; 
		
		try {
			in = new FileInputStream(src).getChannel();
			out = new FileOutputStream(dst).getChannel();
			in.transferTo( 0, in.size(), out);
		} finally {
			if (in != null)
				in.close();
			if (out != null) 
				out.close();
		}
	}


}
