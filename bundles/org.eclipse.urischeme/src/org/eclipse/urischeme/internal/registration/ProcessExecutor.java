/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The productive implementation of {@link IProcessExecutor}
 */
public class ProcessExecutor implements IProcessExecutor {

	/**
	 * Starts a process
	 *
	 * @throws InterruptedException
	 *
	 * @see ProcessBuilder#start()
	 */
	@Override
	public String execute(String command, String... args) throws Exception {
		List<String> commands = new ArrayList<>();
		commands.add(command);
		commands.addAll(Arrays.asList(args));
		Process process = new ProcessBuilder(commands).start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			if (builder.length() > 0) {
				builder.append("\n");//$NON-NLS-1$
			}
			builder.append(line);
		}
		return process.waitFor() == 0 ? builder.toString() : ""; //$NON-NLS-1$
	}

}
