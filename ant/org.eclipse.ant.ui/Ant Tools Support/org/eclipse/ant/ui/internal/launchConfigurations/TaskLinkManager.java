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
package org.eclipse.ant.ui.internal.launchConfigurations;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ant.ui.internal.model.AntUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Manages task links per process. As messages are logged to the console from
 * build events, hyperlinks are created to link task names to the associated ant
 * script. The build logger registers a task hyperlink with this manager for
 * each build event associated with a task. When the associated line is later
 * appended to the console, the corresponding text region in the console
 * document is determined (as the length of a console document can not be
 * determined beforehand), and the hyperlink is added to the document.
 */
public class TaskLinkManager {
	
	/**
	 * A map of processes to lists of queued task hyperlink entries
	 */
	private static Map fgProcessToLinks;
	
	/**
	 * A map of processes to lists of queued new line regions
	 */
	private static Map fgProcessToNewLines;
	
	private static List fgRemoteAntBuilds;
	
	private static class HyperlinkEntry {
		private IConsoleHyperlink fLink;
		private IRegion fRegion;
		private String fTaskName;
		
		public HyperlinkEntry(IConsoleHyperlink link, IRegion region, String taskName) {
			fLink = link;
			fRegion = region;	
			fTaskName = taskName;
		}
		
		public IRegion getRegion() {
			return fRegion;
		}
		
		public IConsoleHyperlink getLink() {
			return fLink;
		}
		
		public String getTaskName() {
			return fTaskName;
		}
	}
	
	private static class LineEntry {
		private IConsole fConsole;
		private IRegion fRegion;
	
		public LineEntry(IConsole console, IRegion region) {
			fConsole = console;
			fRegion = region;	
		}
	
		public IRegion getRegion() {
			return fRegion;
		}
	
		public IConsole getConsole() {
			return fConsole;
		}
	}

	/**
	 * Not to be called.
	 */
	private TaskLinkManager() {
		super();
	}
	
	/**
	 * Registers a hyperlink for the given process and task name. The given
	 * region is relative to the beginning of the line (not the document).
	 * 
	 * @param process
	 * @param link
	 * @param region
	 * @param taskName
	 */
	public static void addTaskHyperlink(IProcess process, IConsoleHyperlink link, IRegion region, String taskName) {
		if (fgProcessToNewLines != null) {
			List queue = (List)fgProcessToNewLines.get(process);
			if (queue != null) {
				synchronized (queue) {
					for (Iterator iter = queue.iterator(); iter.hasNext();) {
						LineEntry newLine = (LineEntry) iter.next();
						if (addLink(newLine.getConsole(), link, newLine.getRegion().getOffset(), region, taskName)) {
							iter.remove();
							return;
						}
					}
				}
			}
		}
				
		if (fgProcessToLinks == null) {
			fgProcessToLinks = new HashMap(10);
			
		}
		List queue = (List)fgProcessToLinks.get(process);
		if (queue == null) {
			queue = new ArrayList(10);
			fgProcessToLinks.put(process, queue);
		}
		synchronized (queue) {
			queue.add(new HyperlinkEntry(link, region, taskName));
		}
	}
	
	private static boolean addLink(IConsole console, IConsoleHyperlink link, int lineOffset, IRegion region, String taskName) {
		int offset = lineOffset + region.getOffset();
		int length = region.getLength();
		
		String text;
		try {
			text = console.getDocument().get(offset, length);
		} catch (BadLocationException e) {
			return false;
		}
		if (text.equals(taskName)) {
			console.addLink(link, offset, length);
			return true;
		}
		return false;
	}

	/**
	 * A new line has been added to the given console. Adds any task hyperlink
	 * associated with the line, to the console.
	 * The new line may be stored to process future incoming tasks hyperlinks.
	 * 
	 * @param console
	 * @param newLine
	 */
	public static void processNewLine(IConsole console, IRegion newLine) {
		IProcess process = console.getProcess();
		if (fgRemoteAntBuilds != null && fgRemoteAntBuilds.contains(process)) {
			if (linkBuildFileMessage(console, newLine)) {
				fgRemoteAntBuilds.remove(process);
				return;
			}
		}
		if (fgProcessToLinks == null) {
			addNewLine(console, newLine, process);
			return;
		}
		
		List queue = (List)fgProcessToLinks.get(process);
		if (queue == null) {
			addNewLine(console, newLine, process);
			return;
		}
		synchronized (queue) {
			for (Iterator iter = queue.iterator(); iter.hasNext();) {
				HyperlinkEntry entry = (HyperlinkEntry) iter.next();
				if (addLink(console, entry.getLink(), newLine.getOffset(), entry.getRegion(), entry.getTaskName())) {
					iter.remove();
					return;
				}
			}
		}
	}
	
	private static void addNewLine(IConsole console, IRegion newLine, IProcess process) {
		if (fgProcessToNewLines == null) {
			fgProcessToNewLines = new HashMap();
		}
		List queue = (List)fgProcessToNewLines.get(process);
		if (queue == null) {
			queue= new ArrayList();
		}
		synchronized (queue) {
			queue.add(new LineEntry(console, newLine));
		}
		fgProcessToNewLines.put(process, queue);
	}

	/**
	 * Disposes any information stored for the given process.
	 * 
	 * @param process
	 */
	public static void dispose(IProcess process) {
		if (fgProcessToLinks != null) {
			fgProcessToLinks.remove(process);
		}
		if (fgProcessToNewLines != null) {
			fgProcessToNewLines.remove(process);
		}
		if (fgRemoteAntBuilds != null) {
			fgRemoteAntBuilds.remove(process);
		}
	}
	
	/**
	 * Registers the specified process as as remote Ant build process.
	 * Allows for the generation of the "Buildfile: somefile" link in the 
	 * Ant output.
	 * 
	 * @param process
	 */
	public static void registerRemoteAntBuild(IProcess process) {
		if (fgProcessToNewLines != null) {
			List queue = (List)fgProcessToNewLines.get(process);
			if (queue != null) {
				synchronized (queue) {
					for (Iterator iter = queue.iterator(); iter.hasNext();) {
						LineEntry newLine = (LineEntry) iter.next();
						if (linkBuildFileMessage(newLine.getConsole(), newLine.getRegion())){
							iter.remove();
							return;
						}
					}
				}
			}
		}
		if (fgRemoteAntBuilds == null) {
			fgRemoteAntBuilds= new ArrayList();
		}
		fgRemoteAntBuilds.add(process);
	}
	
	private static boolean linkBuildFileMessage(IConsole console, IRegion region) {
		
		String message= ""; //$NON-NLS-1$
		try {
			message = console.getDocument().get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
		}
		if (message.startsWith("Buildfile:")) { //$NON-NLS-1$
			String fileName = message.substring(10).trim();
			IFile file = AntUtil.getFileForLocation(fileName, null);
			if (file != null) {
				FileLink link = new FileLink(file, null,  -1, -1, -1);
				console.addLink(link, 11, fileName.length()); //$NON-NLS-1$
				//fBuildFileParent= file.getLocation().toFile().getParentFile();
				return true;
			}
		}
		return false;
	}
}
