package org.eclipse.help.internal.remote;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.help.internal.server.*;
import org.eclipse.help.internal.HelpSystem;
import java.io.*;

import org.eclipse.help.internal.util.*;

import org.eclipse.help.IHelp;

/**
 * Launcher for standalone help system
 */
public class StandaloneFacade implements IPlatformRunnable {
	private static StandaloneFacade instance = new StandaloneFacade();
	private IHelp help;
	/**
	 * StandaloneHelpSystem constructor comment.
	 */
	public StandaloneFacade() {
		super();
		help = new Help();
	}
	public void displayHelp(String infoSet) {
		help.displayHelp(infoSet);
	}
	public StandaloneFacade instance() {
		return instance;
	}
	public Object run(Object args) {
		return this;
	}
}
