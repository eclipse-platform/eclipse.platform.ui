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
package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.model.IProcess;
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
	private static Map fgProcessTable;
	
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
		if (fgProcessTable == null) {
			fgProcessTable = new HashMap(10);
			
		}
		List queue = (List)fgProcessTable.get(process);
		if (queue == null) {
			queue = new ArrayList(10);
			fgProcessTable.put(process, queue);
		}
		synchronized (queue) {
			queue.add(new HyperlinkEntry(link, region, taskName));
		}
	}

	/**
	 * A new line has been added to the given console. Adds any task hyperlink
	 * associated with the line, to the console.
	 * 
	 * @param console
	 * @param newLine
	 */
	public static void processNewLine(IConsole console, IRegion newLine) {
		if (fgProcessTable == null) {
			return;
		}
		IProcess process = console.getProcess();
		List queue = (List)fgProcessTable.get(process);
		if (queue == null) {
			return;
		}
		synchronized (queue) {
			for (int i = 0; i < queue.size(); i++) {
				HyperlinkEntry entry = (HyperlinkEntry)queue.get(i);
				IRegion region = entry.getRegion();
				int offset = newLine.getOffset() + region.getOffset();
				int length = region.getLength();
				String text;
				try {
					text = console.getDocument().get(offset, length);
				} catch (BadLocationException e) {
					return;
				}
				if (text.equals(entry.getTaskName())) {
					console.addLink(entry.getLink(), offset, length);
					queue.remove(i);
					return;
				}
			}
		}
	}
	
	/**
	 * Disposes any information stored for the given process.
	 * 
	 * @param process
	 */
	public static void dispose(IProcess process) {
		if (fgProcessTable != null) {
			fgProcessTable.remove(process);
		}
	}
	
}
