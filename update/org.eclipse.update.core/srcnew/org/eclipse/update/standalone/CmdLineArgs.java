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
package org.eclipse.update.standalone;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

public class CmdLineArgs {
	private HashMap options = new HashMap();
	public CmdLineArgs(String[] args) {
		// default command
		options.put("-command", "install");

		for (int i = 0; i < args.length - 1; i++) {
			if ("-command".equals(args[i])) {
				if (isValidCommand(args[i + 1])) {
					options.put("-command", args[i + 1]);
					i++;
				} else {
					System.out.println("Invalid command:" + args[i + 1]);
					return;
				}
			}
			if (isValidParam(args[i])) {
				options.put(args[i], args[i + 1]);
				i++;
			}
		}
	}

	private boolean isValidParam(String param) {
		return param.equals("-command")
			|| param.equals("-version")
			|| param.equals("-to")
			|| param.equals("-from")
			|| param.equals("-featureId");
	}

	private boolean isValidCommand(String cmd) {
		return cmd.equals("install")
			|| cmd.equals("enable")
			|| cmd.equals("disable");
	}

	public ScriptedCommand getCommand() {
		String cmd = (String)options.get("-command");
		if (cmd.equals("install"))
			return new InstallCommand(
				(String) options.get("-featureId"),
				(String) options.get("-version"),
				(String) options.get("-from"),
				(String) options.get("-to"));
		else if (cmd.equals("enable"))
		return new EnableCommand(
			(String) options.get("-featureId"),
			(String) options.get("-version"),
			(String) options.get("-from"),
			(String) options.get("-to"));
		else if (cmd.equals("disable")) 
		return new DisableCommand(
			(String) options.get("-featureId"),
			(String) options.get("-version"),
			(String) options.get("-from"),
			(String) options.get("-to"));
		else
			return null;
	}
	
}
