package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

/**
 * This class represents a list of targets that an
 * Ant file can run.
 */
public class AntTargetList {
	// The list of targets.
	private ArrayList targets = new ArrayList();
	
	// The target that will be run if no targets are specified
	// by the user.
	private String defaultTarget = null;
	
	/**
	 * Add a target to the list.
	 */
	/*package*/ void add(String target) {
		targets.add(target);	
	}
	
	/**
	 * Retrieve the default target. This method will return
	 * <code>null</code> if the default target has not yet
	 * been specified.
	 */
	public String getDefaultTarget() {
		return defaultTarget;	
	}
	
	/**
	 * Retrieve all the targets as an array.
	 */
	public String[] getTargets() {
		String[] array = new String[targets.size()];
		targets.toArray(array);
		return array;
	}
	
	/**
	 * Returns the number of targets in the list.
	 */
	public int getTargetCount() {
		return targets.size();
	}
	
	/**
	 * Set the default target.
	 */
	/*package*/ void setDefaultTarget(String target) {
		defaultTarget = target;
	}
	
	/**
	 * Validate the default target exists within the
	 * known target list and if not, remove it.
	 */
	/*package*/ boolean validateDefaultTarget() {
		if (defaultTarget == null)
			return true;
		if (targets.contains(defaultTarget))
			return true;
		defaultTarget = null;
		return false;
	}
}
