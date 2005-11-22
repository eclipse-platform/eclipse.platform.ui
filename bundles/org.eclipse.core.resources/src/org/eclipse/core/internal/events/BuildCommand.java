/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.resources.ModelObject;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * The concrete implementation of <tt>ICommand</tt>.  This object
 * stores information about a particular builder, including a reference
 * to the builder instance itself if it has been instantiated.
 */
public class BuildCommand extends ModelObject implements ICommand {
	/**
	 * Internal flag masks for different build triggers.
	 */
	private static final int MASK_AUTO = 0x01;
	private static final int MASK_INCREMENTAL = 0x02;
	private static final int MASK_FULL = 0x04;
	private static final int MASK_CLEAN = 0x08;

	/**
	 * Flag bit indicating if this build command is configurable
	 */
	private static final int MASK_CONFIGURABLE = 0x10;
	
	/**
	 * Flag bit indicating if the configurable bit has been loaded from
	 * the builder extension declaration in XML yet.
	 */
	private static final int MASK_CONFIG_COMPUTED = 0x20;

	private static final int ALL_TRIGGERS = MASK_AUTO | MASK_CLEAN | MASK_FULL | MASK_INCREMENTAL;

	protected HashMap arguments;
	
	/**
	 * The builder instance for this command. Null if the builder has
	 * not yet been instantiated.
	 */
	protected IncrementalProjectBuilder builder;

	/**
	 * The triggers that this builder will respond to.  Since build triggers are not 
	 * bit-maskable, we use internal bit masks to represent each 
	 * trigger (MASK_* constants). By default, a command responds to all
	 * build triggers.
	 */
	private int triggers = ALL_TRIGGERS;

	/**
	 * Returns the trigger bit mask for the given trigger constant.
	 */
	private static int maskForTrigger(int trigger) {
		switch (trigger) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				return MASK_AUTO;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				return MASK_INCREMENTAL;
			case IncrementalProjectBuilder.FULL_BUILD :
				return MASK_FULL;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return MASK_CLEAN;
		}
		return 0;
	}

	public BuildCommand() {
		super(""); //$NON-NLS-1$
		this.arguments = new HashMap(0);
	}

	public Object clone() {
		BuildCommand result = null;
		result = (BuildCommand) super.clone();
		if (result == null)
			return null;
		result.setArguments(getArguments());
		//don't let references to builder instances leak out because they reference trees
		result.setBuilder(null);
		return result;
	}

	/**
	 * Computes whether this build command allows configuration of its
	 * triggers, based on information in the builder extension declaration.
	 */
	private void computeIsConfigurable() {
		triggers |= MASK_CONFIG_COMPUTED;
		IExtension extension = Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, name);
		if (extension != null) {
			IConfigurationElement[] configs = extension.getConfigurationElements();
			if (configs.length != 0) {
				String value = configs[0].getAttribute("isConfigurable"); //$NON-NLS-1$
				setConfigurable(value != null && value.equalsIgnoreCase(Boolean.TRUE.toString()));
			}
		}
	}

	/* (non-Javadoc)
	 * Method declared on Object
	 */
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (!(object instanceof BuildCommand))
			return false;
		BuildCommand command = (BuildCommand) object;
		// equal if same builder name and equal argument tables
		return getBuilderName().equals(command.getBuilderName()) && getArguments(false).equals(command.getArguments(false)) && triggers == command.triggers;
	}

	/**
	 * @see ICommand#getArguments()
	 */
	public Map getArguments() {
		return getArguments(true);
	}

	public Map getArguments(boolean makeCopy) {
		return arguments == null ? null : (makeCopy ? (Map) arguments.clone() : arguments);
	}

	public IncrementalProjectBuilder getBuilder() {
		return builder;
	}

	/**
	 * @see ICommand#getBuilderName()
	 */
	public String getBuilderName() {
		return getName();
	}

	/* (non-Javadoc)
	 * Method declared on Object
	 */
	public int hashCode() {
		// hash on name alone
		return 37 * getName().hashCode() + triggers;
	}

	/**
	 * @see ICommand#isBuilding(int)
	 */
	public boolean isBuilding(int trigger) {
		return (triggers & maskForTrigger(trigger)) != 0;
	}

	public boolean isConfigurable() {
		if ((triggers & MASK_CONFIG_COMPUTED) == 0)
			computeIsConfigurable();
		return (triggers & MASK_CONFIGURABLE) != 0;
	}

	/**
	 * @see ICommand#setArguments(Map)
	 */
	public void setArguments(Map value) {
		// copy parameter for safety's sake
		arguments = value == null ? null : new HashMap(value);
	}

	public void setBuilder(IncrementalProjectBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @see ICommand#setBuilderName(String)
	 */
	public void setBuilderName(String value) {
		//don't allow builder name to be null
		setName(value == null ? "" : value); //$NON-NLS-1$
	}

	/**
	 * @see ICommand#setBuilding(int, boolean)
	 */
	public void setBuilding(int trigger, boolean value) {
		if (!isConfigurable())
			return;
		if (value)
			triggers |= maskForTrigger(trigger);
		else
			triggers &= ~maskForTrigger(trigger);
	}

	/**
	 * Sets whether this build command allows its build triggers to be configured.
	 * This value should only be set when the builder extension declaration is
	 * read from the registry, or when a build command is read from the project
	 * description file on disk.  The value is not otherwise mutable.
	 */
	public void setConfigurable(boolean value) {
		triggers |= MASK_CONFIG_COMPUTED;
		if (value)
			triggers |= MASK_CONFIGURABLE;
		else
			triggers = ALL_TRIGGERS;
	}
	
	/**
	 * For debugging purposes only
	 */
	public String toString() {
		return "BuildCommand(" + getName() + ")";//$NON-NLS-1$ //$NON-NLS-2$
	}
}
