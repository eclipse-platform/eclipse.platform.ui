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
import java.net.*;

//import org.eclipse.update.internal.core.net.http.IContext;
//import org.eclipse.update.internal.core.net.http.client.IStatusCodes;
//import org.eclipse.update.internal.core.net.http.client.Response;

/**
 * A File repsonse message
 */
public class FileResponse extends Response {

	/**
	 * 
	 */
	public FileResponse(URL url) {
		//super(IStatusCodes.HTTP_OK,"", context, in);
		super(url);
	}

	public InputStream getInputStream() throws IOException {
		return url.openStream();
	}
	
	public long getLastModified() {
		if (lastModified == 0) {
			File f = new File(url.getFile());
			if (f.isDirectory())
				f = new File(f, "site.xml");
			lastModified = f.lastModified();
		}
		return lastModified;
	}
}
