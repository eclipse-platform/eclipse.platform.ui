package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

/**
 * General utility class dealing with Ant files
 */
public final class AntUtil {
	private static final String ATT_DEFAULT = "default"; //NON-NLS-1$
	private static final String ATT_NAME = "name"; //NON-NLS-1$
	private static final String TAG_TARGET = "target"; //NON-NLS-1$
	// Holds the current monitor that the Ant build logger can access
	private static IProgressMonitor monitor;
	
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}

	/**
	 * Returns the list of targets for the Ant file specified
	 * by the provided IPath, or <code>null</code> if no file is
	 * found or the file is not a valid Ant file.
	 */
	public static AntTargetList getTargetList(IPath path) {
		IMemento memento = getMemento(path);
		return getTargetList(memento);	
	}
	
	/**
	 * Returns an IMemento representing the Ant tool found in
	 * the supplied IPath, or <code>null</code> if a file is not found.
	 */
	private static IMemento getMemento(IPath path) {
		try {
			Reader reader = new FileReader(path.toFile());
			return XMLMemento.createReadRoot(reader);
		} catch (FileNotFoundException e) {
			ExternalToolsPlugin.getDefault().log("Error: Ant file not found.", e); // $NON-NLS-1$
			return null;
		}
	}

	/**
	 * Returns the list of targets of the Ant file represented by the
	 * supplied IMemento, or <code>null</code> if the memento is null or
	 * does not represent a valid Ant file.
	 */
	private static AntTargetList getTargetList(IMemento memento) {
		if (memento == null)
			return null;
		AntTargetList targets = new AntTargetList();
		
		String defaultTarget = memento.getString(ATT_DEFAULT);
		targets.setDefaultTarget(defaultTarget);
		
		IMemento[] targetMementos = memento.getChildren(TAG_TARGET);
		for (int i=0; i < targetMementos.length; i++) {
			IMemento targetMemento = targetMementos[i];
			String target = targetMemento.getString(ATT_NAME);
			targets.add(target);
		}
		
		// If the file has no targets, then it is not a
		// valid Ant file.
		if (targets.getTargets().length == 0)
			return null;
		else
			return targets;
	}
	
	/**
	 * Returns the last known progress monitor that the
	 * Ant build logger can use
	 */
	public static IProgressMonitor getCurrentProgressMonitor() {
		return AntUtil.monitor;
	}
	
	/**
	 * Sets the last known progress monitor that the
	 * Ant build logger can use
	 */
	public static void setCurrentProgressMonitor(IProgressMonitor monitor) {
		AntUtil.monitor = monitor;
	}
}
