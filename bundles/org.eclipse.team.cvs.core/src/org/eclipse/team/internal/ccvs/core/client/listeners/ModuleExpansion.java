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
package org.eclipse.team.internal.ccvs.core.client.listeners;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;

/**
 * 
 */
public class ModuleExpansion {
	
	/** Name of the CVS module as found in the CVSROOT/modules file */
	private String moduleName;
	
	/** Expansion of the module name returned by the CVS server */
	private String[] expansions;
	
	private LocalOption[] options;
	
	ModuleExpansion(String moduleName, String[] expansions, LocalOption[] options) {
		this.moduleName = moduleName;
		this.expansions = expansions;
		this.options = options;
	}
	
	/**
	 * @see IModuleExpansion#getModuleName()
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @see IModuleExpansion#getModuleExpansionString()
	 */
	public String getModuleExpansionString() {
		String result = expansions[0];
		for (int i=1;i<expansions.length; i++)
			result = result + ", " + expansions[i]; //$NON-NLS-1$
		return result;
	}
	
	/**
	 * @see IModuleExpansion#getExpansions()
	 */
	public String[] getExpansions() {
		return expansions;
	}
	
	public LocalOption[] getOptions() {
		return options;
	}
	
	/**
	 * Translate an array of module names to their expansions.
	 * The resulting List of Strings may be bigger than the original
	 */
	private List getExpansionsFor(String[] possibleModules, Map moduleMappings, int depth) throws CVSException {
		List result = new ArrayList();
		for (int i=0;i<possibleModules.length;i++) {
			// Is it a module?
			if (possibleModules[i].charAt(0) == '&')
				result.addAll(getExpansionsFor(possibleModules[i].substring(1), moduleMappings, depth));
			else
				result.add(possibleModules[i]);
		}
		return result;
	}

	/**
	 * Translate a module name to its expansion.
	 * The resulting List may contain one or more Strings
	 */
	private List getExpansionsFor(String module, Map moduleMappings, int depth) throws CVSException {
		if (depth > moduleMappings.size()) {
			// Indicate that a circular reference exists
			throw new CVSException(Policy.bind("ModuleExpansion.circular", module));//$NON-NLS-1$
		}
		Object mappings = moduleMappings.get(module);
		if (mappings == null) {
			// If there's no mapping assume it is a project name
			List result = new ArrayList();
			result.add(module);
			return result;
		} else {
			// Follow any expansion chains
			return getExpansionsFor(((ModuleExpansion)mappings).expansions, moduleMappings, depth + 1);
		}
	}
	
	/**
	 * Resolve the module mappings using moduleMappings which maps
	 * module names to their ModuleExpansion
	 */
	public void resolveModuleReferencesUsing(Map moduleMappings) {
		try {
			List result = getExpansionsFor(expansions, moduleMappings, 0);
			expansions = (String[])result.toArray(new String[result.size()]);
		} catch (CVSException e) {
			// Is this the best way to show the circular reference problem?
			//    Or should we just leave the expansions untouched?
			List result = new ArrayList();
			result.add(e.getStatus().getMessage());
			result.addAll(Arrays.asList(expansions));
			expansions = (String[])result.toArray(new String[result.size()]);
		}
	}
}


