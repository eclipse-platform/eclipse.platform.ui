package org.eclipse.team.tests.ccvs.core.compatible;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;

/**
 * This class is responsible for executing cvs commands using
 * a reference CVS command line client.
 */
public class ReferenceClient {
	
	public static final String cvsLocation = System.getProperty("eclipse.cvs.command");
	/**
	 * Puts opetions into one String seperated by
	 * space.
	 * starts and ends with a space.
	 */
	private static String flatenOptions(String[] options) {
		
		StringBuffer result = new StringBuffer(" ");
		String quote;
		
		for (int i=0; i<options.length; i++) {
			
			if (options[i].indexOf(" ")==-1) {
				quote = "";
			} else {
				quote = "\"";
			}
			result.append(quote);
			result.append(options[i]);
			result.append(quote);
			result.append(' ');
		}
			
		return result.toString();
	}
	
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						File ioRoot,
						IProgressMonitor monitor, 
						PrintStream messageOut) 
						throws CVSException {

		Runtime runtime;
		Process process;
		BufferedReader stdIn;
		BufferedReader errIn;
		ICVSFolder mRoot;
		
		String global;
		String local;
		String arg;
		String commandLine;

		globalOptions = (String[]) globalOptions.clone();
		mRoot = Session.getManagedFolder(ioRoot);
		
		runtime = Runtime.getRuntime();
		global = flatenOptions(globalOptions);
		local = flatenOptions(localOptions);
		arg = flatenOptions(arguments);
		
		commandLine = cvsLocation + " ";
		commandLine = commandLine + global;
		commandLine = commandLine + request + " ";
		commandLine = commandLine + local;
		commandLine = commandLine + arg;
		
		// System.out.println(ioRoot.getPath() + "> " + commandLine);
		
		try {
			process = runtime.exec(commandLine, null, ioRoot);
		} catch (IOException e) {
			throw new CVSException("IOException while executing ReferenceClient",e);
		}
		
		stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
		new ContiniousPipe(stdIn, messageOut, "M ");

		errIn = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		new ContiniousPipe(errIn, messageOut, "E ");

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new CVSException("InterruptedException while executing ReferenceClient",e);
		}
		
		if (process.exitValue() != 0) {
			throw new ReferenceException("Return Code of CVS reference client: " + 
									process.exitValue() + "\nwhile executing: " + 
									commandLine);
		}	
		
		SyncFileUtil.mergeEntriesLogFiles(ioRoot);
		
	}
	
	/**
	 * 
	 * returns ":pserver:username@host:/cvs/root"
	 *         when you insert ":pserver:username:password@host:/cvs/root"
	 */
	public static String removePassword(String repoName) {
		
		int atPlace = -1;
		int colonPlace = -1;
		int colonCount = 0;
		String currentChar; 
		
		for (int i=0; i<repoName.length(); i++) {
			
			currentChar = repoName.substring(i,i+1);
			
			if (currentChar.equals(":")) {
				colonCount++;
				
				if (colonCount == 3) {
					colonPlace = i;
				}
			}
			
			if (currentChar.equals("@")) {
				if (colonPlace == -1) {
					
					// If the @ comes before the third colon, then 
					// we do not have a password and return with the
					// same string
					return repoName;
				} else {
					atPlace = i;
				}
				
			}
		}
		
		if (atPlace == -1) {
			return repoName;
		}
		
		return repoName.substring(0,colonPlace) + repoName.substring(atPlace);
	}
}

/**
 * This class does continiously pipe from a bufferdReader
 * to a printStream. It does stop as soon, as the bufferdReader is
 * closed an therefore an IOException is thrown or the pipe returns null.
 * 
 * It does close the BufferedReader on it's own (to be sure that it got
 * everything)
 */
class ContiniousPipe implements Runnable {
	
	BufferedReader in;
	PrintStream out;
	String prefix;
	
	ContiniousPipe(BufferedReader in, PrintStream out, String prefix) {
		this.in = in;
		this.out = out;
		this.prefix = prefix;
		(new Thread(this)).start();
	}
	
	public void run() {
		
		String line;
		
		try {
			while ((line=in.readLine()) != null) {
				out.println(prefix + line);
			}
		} catch (IOException e) {
			// Should not happen, as the PrintStream does not throw IOExceptions
			// at all an in is a stream from a process
		} finally {
			
			try {
				in.close();
			} catch (IOException e) {}
			
		}
	}
}