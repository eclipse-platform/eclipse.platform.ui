package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpPlugin;
/**
 * Table of plugins. Records all plugins and their version.
 */
public class PluginVersionInfo extends HelpProperties {
	Plugin basePlugin = HelpPlugin.getDefault();
	boolean doComparison = true;
	boolean hasChanged = false;
	Collection added = new ArrayList();
	Collection removed = new ArrayList();
	/**
	 * Creates table of current contributing plugins and their version.
	 * @param it iterator of current contributions (IConfigurationElement type)
	 */
	public PluginVersionInfo(String name, Iterator it) {
		this(name, it, HelpPlugin.getDefault());
	}
	/**
	 * Creates table of current contributing plugins and their version.
	 * @param name the name of the file to serialize the data to
	 * @param it iterator of current contributions (IConfigurationElement type)
	 * @param basePlugin use this plugin's state location to store the data
	 */
	public PluginVersionInfo(String name, Iterator it, Plugin basePlugin) {
		super(name, basePlugin);
		this.basePlugin = basePlugin;
		if (it == null)
			return;
		// create table of current contributions
		for (; it.hasNext();) {
			IPluginDescriptor plugin = (IPluginDescriptor) it.next();
			this.put(
				plugin.getUniqueIdentifier(),
				plugin.getVersionIdentifier().toString());
		}
	}
	/**
	 * Detects changes in contributions or their version
	 * since last time the contribution table was saved.
	 * @return true if contributions have changed
	 */
	public boolean detectChange() {
		if (!doComparison)
			return hasChanged;
		// Create table of contributions present before last save()
		HelpProperties oldContrs = new HelpProperties(this.name, basePlugin);
		oldContrs.restore();
		// check if contributions changed
		hasChanged = false;
		//
		for (Enumeration keysEnum = this.keys(); keysEnum.hasMoreElements();) {
			String oneContr = (String) keysEnum.nextElement();
			if (!oldContrs.containsKey(oneContr)) {
				added.add(oneContr);
			} else if (!this.get(oneContr).equals(oldContrs.get(oneContr))) {
				added.add(oneContr);
			}
		}
		for (Enumeration keysEnum = oldContrs.keys(); keysEnum.hasMoreElements();) {
			String oneContr = (String) keysEnum.nextElement();
			if (!this.containsKey(oneContr)) {
				removed.add(oneContr);
			} else if (!oldContrs.get(oneContr).equals(this.get(oneContr))) {
				removed.add(oneContr);
			}
		}
		hasChanged = added.size() > 0 || removed.size() > 0;
		doComparison = false;
		return hasChanged;
	}
	/**
	 * @return String - Collection of IDs of contributions that were added
	 *  or upgraded
	 */
	public Collection getAdded() {
		if (doComparison)
			detectChange();
		return added;
	}
	/**
	 * @return String - Collection of IDs of contributions that were removed
	 *  or upgraded
	 */
	public Collection getRemoved() {
		if (doComparison)
			detectChange();
		return removed;
	}
	/**
	 * Saves contributions to a file.
	 * After this method is called, calls to detectChange() will return false.
	 * @return true if operation was successful
	 */
	public boolean save() {
		if (super.save()) {
			doComparison = false;
			hasChanged = false;
			added = new ArrayList();
			removed = new ArrayList();
			return true;
		}
		return false;
	}
}