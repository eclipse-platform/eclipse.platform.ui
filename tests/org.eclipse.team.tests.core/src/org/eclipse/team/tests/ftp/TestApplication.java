/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.tests.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ftp.*;
import org.eclipse.team.internal.ftp.FTPException;
import org.eclipse.team.internal.ftp.FTPProxyLocation;
import org.eclipse.team.internal.ftp.FTPServerLocation;
import org.eclipse.team.internal.ftp.client.*;
import org.eclipse.team.internal.ftp.client.FTPClient;
import org.eclipse.team.internal.ftp.client.FTPCommunicationException;
import org.eclipse.team.internal.ftp.client.FTPDirectoryEntry;
import org.eclipse.team.internal.ftp.client.IFTPClientListener;

public class TestApplication implements IPlatformRunnable {
	protected PrintWriter out;
	protected BufferedReader in;
	protected FTPServerLocation location;
	protected FTPProxyLocation proxy;
	protected FTPClient client;
	protected IFTPClientListener listener;
	protected IProject project;
	
	public Object run(Object args) {
		/*** initialize ***/
		out = new PrintWriter(new OutputStreamWriter(System.out), true);
		in = new BufferedReader(new InputStreamReader(System.in));
		listener = new IFTPClientListener() {
			public void responseReceived(int responseCode, String responseText) {
				out.println(responseText);
			}
			public void requestSent(String command, String argument) {
				if (argument != null) {
					out.println(command + " " + argument);
				} else {
					out.println(command);
				}
			}
		};
		
		// we'll dump downloaded files into 
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("FTP-test");
		try {
			if (! project.exists()) {
				project.create(null);
			}
			if (! project.isOpen()) {
				project.open(null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		/*** run ***/
		printHelp();
		for (;;) {
			String line;
			try {
				line = in.readLine();
			} catch (IOException e) {
				break;
			}
			StringTokenizer tok = new StringTokenizer(line);
			if (! tok.hasMoreTokens()) continue;
			String command = tok.nextToken().toLowerCase();
			try {
				if (command.equals("proxy") && tok.countTokens() >= 2 && tok.countTokens() <= 4) {
					proxy = new FTPProxyLocation(
						tok.nextToken(),
						Integer.parseInt(tok.nextToken(), 10),
						tok.hasMoreTokens() ? tok.nextToken() : null,
						tok.hasMoreTokens() ? tok.nextToken() : null);
				} else if (command.equals("open") && tok.countTokens() == 5) {
					location = new FTPServerLocation(
						tok.nextToken(),
						Integer.parseInt(tok.nextToken(), 10),
						tok.nextToken(),
						tok.nextToken(),
						tok.nextToken().equals("true"));
					client = new FTPClient(location, proxy, listener);
					client.open(getProgressMonitor());
				} else if (command.equals("close")) {
					client.close(getProgressMonitor());
				} else if (command.equals("quit")) {
					break;
				} else if (command.equals("cd") && tok.countTokens() == 1) {
					client.changeDirectory(tok.nextToken(), getProgressMonitor());
				} else if (command.equals("ls") && tok.countTokens() == 0) {
					FTPDirectoryEntry[] infos = client.listFiles(null, getProgressMonitor());
					printFileList(infos);
				} else if (command.equals("ls") && tok.countTokens() == 1) {
					FTPDirectoryEntry[] infos = client.listFiles(tok.nextToken(), getProgressMonitor());
					printFileList(infos);
				} else if (command.equals("mkdir") && tok.countTokens() == 1) {
					client.createDirectory(tok.nextToken(), getProgressMonitor());
				} else if (command.equals("rmdir") && tok.countTokens() == 1) {
					client.deleteDirectory(tok.nextToken(), getProgressMonitor());
				} else if (command.equals("get") && tok.countTokens() == 3) {
					String remoteFilePath = tok.nextToken();
					boolean binary = tok.nextToken().equals("true");
					boolean resume = tok.nextToken().equals("true");
					IFile file = project.getFile(new Path(remoteFilePath).lastSegment());
					client.getFile(remoteFilePath, file, binary, resume, getProgressMonitor());
				} else if (command.equals("put") && tok.countTokens() == 2) {
					String remoteFilePath = tok.nextToken();
					boolean binary = tok.nextToken().equals("true");
					IFile file = project.getFile(new Path(remoteFilePath).lastSegment());
					client.putFile(remoteFilePath, file, binary, getProgressMonitor());
				} else if (command.equals("rm") && tok.countTokens() == 1) {
					client.deleteFile(tok.nextToken(), getProgressMonitor());
				} else {
					printHelp();
				}
			} catch (FTPException e) {
				out.println("error: " + e.toString());
			}
		}
		out.println("Terminated.");
		return null;
	}
	
	private IProgressMonitor getProgressMonitor() {
		return new NullProgressMonitor();
	}
	
	private void printHelp() {
		out.println("--- Interactive Test FTP Client ---");
		out.println("Commands:");
		out.println("  proxy <hostname> <port> <username> <password>");
		out.println("  open  <hostname> <port> <username> <password> <passive true/false>");
		out.println("  close");
		out.println("  help");
		out.println("  quit");
		out.println("  cd    <dir>");
		out.println("  ls    <dir>");
		out.println("  mkdir <dir>");
		out.println("  rmdir <dir>");
		out.println("  get   <file> <binary true/false> <resume true/false>");
		out.println("  put   <file> <binary true/false>");
		out.println("  rm    <file>");
		out.println();
	}
	
	private void printFileList(FTPDirectoryEntry[] infos) {
		for (int i = 0; i < infos.length; i++) {
			FTPDirectoryEntry info = infos[i];
			out.print(info.hasDirectorySemantics() ? "d" : "-");
			out.print(info.hasFileSemantics() ? "f" : "-");
			out.print(' ');
			out.print(pad("\"" + info.getName() + "\"", 40));
			out.print(' ');
			out.print(pad(info.getSize() != info.UNKNOWN_SIZE ? Long.toString(info.getSize()) : "?", 12));
			out.print(' ');
			if (info.getModTime() != null) out.print(info.getModTime().toGMTString());
			else out.println("?");
			out.println();
		}
	}
	
	private String pad(String string, int len) {
		int count = len - string.length();
		if (count <= 0) return string;
		StringBuffer buf = new StringBuffer(string);
		while (count-- > 0) buf.append(' ');
		return buf.toString();
	}
}
