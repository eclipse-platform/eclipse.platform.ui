/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.jarprocessor;

import java.io.IOException;
import java.util.Properties;

/**
 * @author aniefer
 *
 */
public abstract class CommandStep implements IProcessStep {
	protected String command = null;
	protected String extension = null;
	private  Properties options = null;
	
	public CommandStep(Properties options, String command, String extension) {
		this.command = command;
		this.extension = extension;
		this.options = options;
	}

	protected static int execute(String[] cmd) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(cmd);
		try {
			int result = proc.waitFor();
			return result;
		} catch (InterruptedException e) {
			//ignore
		}
		return -1;
	}
	
	public Properties getOptions() {
		if(options == null)
			options = new Properties();
		return options;
	}
}
