package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
