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
package org.eclipse.help.internal.browser;
import java.io.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

/**
 * Used to receive output from processes
 */
public class StreamConsumer extends Thread {
	BufferedReader bReader;
	private String lastLine;
	public StreamConsumer(InputStream inputStream) {
		super();
		setDaemon(true);
		bReader = new BufferedReader(new InputStreamReader(inputStream));
	}
	public void run() {
		try {
			String line;
			while (null != (line = bReader.readLine())) {
				lastLine = line;
				BrowserLog.log(line);
			}
			bReader.close();
		} catch (IOException ioe) {
			HelpPlugin.logError(Resources.getString("WE001"), ioe);
		}
	}
	/**
	 * @return last line obtained or null
	 */
	public String getLastLine() {
		return lastLine;
	}

}