/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.help.internal.standalone.*;

/**
 * This program is used to start or stop Eclipse
 * Infocenter application.
 */
public class StandaloneInfocenter extends EclipseController{
	// ID of the application to run
	private static final String INFOCENTER_APPLICATION_ID =
		"org.eclipse.help.infocenterApplication";
	
	/**
	 * Constructs help system
	 * @param args array of String options and their values
	 * 	Option <code>-eclipseHome dir</code> specifies Eclipse
	 *  installation directory.
	 *  It must be provided, when current directory is not the same
	 *  as Eclipse installation directory.
	 *  Additionally, most options accepted by Eclipse execuable are supported.
	 * @param applicationID ID of Eclipse help application
	 */
	public StandaloneInfocenter(String[] args) {
		super(INFOCENTER_APPLICATION_ID, args);
	}
	
	/**
	 * @see org.eclipse.help.standalone.Infocenter#main(String[])
	 */
	public static void main(String[] args) {
		StandaloneInfocenter infocenter = new StandaloneInfocenter(args);
		
		List helpCommand = Options.getHelpCommand();

		if (infocenter.executeCommand(helpCommand)) {
			return;
		} else
			printMainUsage();
	}

	/**
	 * @return true if commands contained a known command
	 *  and it was executed
	 */
	private boolean executeCommand(List helpCommand) {
		if (helpCommand.size() <= 0) {
			return false;
		}
		String command = (String) helpCommand.get(0);
		if ("start".equalsIgnoreCase(command)) {
			start();
			return true;
		} else if ("shutdown".equalsIgnoreCase(command)) {
			shutdown();
			return true;
		}
		return false;
	}
	

	/**
	 * Prints usage of this class as a program.
	 */
	private static void printMainUsage() {
		System.out.println("Parameters syntax:");
		System.out.println();
		System.out.println(
			"-command start | shutdown [-eclipsehome eclipseInstallPath] [platform options] [-vmargs [Java VM arguments]]");
		System.out.println();
		System.out.println("where:");
		System.out.println(
			" dir specifies Eclipse installation directory; it must be provided, when current directory is not the same as Eclipse installation directory,");
		System.out.println(
			" platform options are other options that are supported by Eclipse Executable.");
	}

}
