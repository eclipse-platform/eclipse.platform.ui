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
package org.eclipse.team.tests.ccvs.ui.logformatter;


public class ArgumentParser {
	protected ArgumentParser() {
	}
	
	public boolean parse(String[] args) {
		int index = 0;
		String option = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg == null) continue;
			if (arg.charAt(0) == '-') {
				if (option != null && ! handleOption(option, null)) return false;
				option = arg;
			} else if (option != null) {
				if (! handleOption(option, arg)) return false;
				option = null;
			} else {
				if (! handleArgument(index++, arg)) return false;
			}
		}
		if (option != null && ! handleOption(option, null)) return false;
		return handleFinished();
	}
	
	protected boolean handleFinished() {
		return true;
	}
	
	protected boolean handleArgument(int index, String arg) {
		return false;
	}
	
	protected boolean handleOption(String option, String arg) {
		return false;
	}
}
