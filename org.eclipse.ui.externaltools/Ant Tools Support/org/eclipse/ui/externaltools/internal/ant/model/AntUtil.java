package org.eclipse.ui.externaltools.internal.ant.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.StringTokenizer;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * General utility class dealing with Ant build files
 */
public final class AntUtil {
	public static final String RUN_TARGETS_ATTRIBUTE = IExternalToolConstants.TOOL_TYPE_ANT_BUILD + ".runTargets"; //$NON-NLS-1$;
	private static final String TARGET_SEPARATOR = ","; //$NON-NLS-1$;
	
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}
	
	/**
	 * Returns a single-string of target names for storage.
	 * 
	 * @param targets the array of target names
	 * @return a single-string representation of the target names,
	 *  or <code>null</code> if the target array is empty.
	 */
	public static String combineRunTargets(String[] targets) {
		if (targets.length == 0)
			return null;

		if (targets.length == 1)
			return targets[0];

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < targets.length - 1; i++) {
			buf.append(targets[i]);
			buf.append(TARGET_SEPARATOR);
		}
		buf.append(targets[targets.length - 1]);
		return buf.toString();
	}
	
	/**
	 * Returns the list of all targets for the Ant build file specified by
	 * the provided IPath, or <code>null</code> if no targets found.
	 * 
	 * @param path the location of the ant build file to get the targets from
	 * @return a list of <code>TargetInfo</code>
	 * 
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	public static TargetInfo[] getTargets(String path) throws CoreException {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(path);
	 	return runner.getAvailableTargets();
	}
	
	/**
	 * Returns whether the target described by the given
	 * <code>TargetInfo</code> is an internal target.
	 * 
	 * @param info the info of the target in question
	 * @return <code>true</code> if the target is an internal
	 * 		target, <code>false</code> otherwise
	 */
	public static boolean isInternalTarget(TargetInfo info) {
		return info.getName().charAt(0) == '-';	
	}

	/**
	 * Returns whether the target described by the given
	 * <code>TargetInfo</code> is a sub-target.
	 * 
	 * @param info the info of the target in question
	 * @return <code>true</code> if the target is a sub-target,
	 * 		<code>false</code> otherwise
	 */
	public static boolean isSubTarget(TargetInfo info) {
		return info.getDescription() == null;
	}
	
	/**
	 * Returns the list of target names to run
	 * 
	 * @param extraAttibuteValue the external tool's extra attribute value
	 * 		for the run targets key.
	 * @return a list of target names
	 */
	public static String[] parseRunTargets(String extraAttibuteValue) {
		if (extraAttibuteValue == null)
			return new String[0];
		
		// Need to handle case where separator character is
		// actually part of the target name!
		StringTokenizer tokenizer = new StringTokenizer(extraAttibuteValue, TARGET_SEPARATOR);
		String[] results = new String[tokenizer.countTokens()];
		for (int i = 0; i < results.length; i++) {
			results[i] = tokenizer.nextToken();
		}
		
		return results;
	}
}
