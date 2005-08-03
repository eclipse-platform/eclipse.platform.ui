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

package org.eclipse.core.internal.model;

import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.osgi.util.NLS;

public class RegistryResolver {

	private Map idmap;
	private PluginRegistryModel reg;
	private MultiStatus status;
	private boolean trimPlugins = true;
	private boolean crossLink = true;

	private boolean DEBUG_RESOLVE = false;
	private static final String OPTION_DEBUG_RESOLVE = "org.eclipse.core.runtime/registry/debug/resolve"; //$NON-NLS-1$

	// constraint entry
	// A constraint is made for each relationship where 'parent' requires 'prq'.
	// ver is the version number we must try to match.  It can be null if we just
	// want to match the latest.
	// cEntry points to the parent ConstraintsEntry element.
	private class Constraint {
		private PluginDescriptorModel parent;
		private PluginPrerequisiteModel prq;
		private PluginVersionIdentifier ver;
		private byte type = PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED;
		private ConstraintsEntry cEntry = null;

		private Constraint(PluginDescriptorModel parent, PluginPrerequisiteModel prq) {
			this.parent = parent;
			this.prq = prq;
			if (prq != null) {
				ver = RegistryResolver.this.getVersionIdentifier(prq);
				type = prq.getMatchByte();
				if ((ver != null) && (type == PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED))
					type = PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE;
			}
		}

		private int getMatchType() {
			return type;
		}

		private ConstraintsEntry getConstraintsEntry() {
			return cEntry;
		}

		private void setConstraintsEntry(ConstraintsEntry entry) {
			cEntry = entry;
		}

		private PluginDescriptorModel getParent() {
			return parent;
		}

		private PluginPrerequisiteModel getPrerequisite() {
			return prq;
		}

		private PluginVersionIdentifier getVersionIdentifier() {
			return ver;
		}

		public String toString() {
			if (prq == null)
				return "(null)"; //$NON-NLS-1$
			String s = parent.toString() + "->" + prq.getPlugin(); //$NON-NLS-1$
			switch (prq.getMatchByte()) {
				case PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED :
					s += "(any)"; //$NON-NLS-1$
					break;
				case PluginPrerequisiteModel.PREREQ_MATCH_PERFECT :
					s += IModel.PLUGIN_REQUIRES_MATCH_PERFECT;
					break;
				case PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT :
					s += IModel.PLUGIN_REQUIRES_MATCH_EQUIVALENT;
					break;
				case PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE :
					s += IModel.PLUGIN_REQUIRES_MATCH_COMPATIBLE;
					break;
				case PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL :
					s += IModel.PLUGIN_REQUIRES_MATCH_GREATER_OR_EQUAL;
					break;
			}
			return s;
		}
	}

	// constraint index structure
	// Each time an IndexEntry is created, a single ContraintsEntry
	// is created and put into the IndexEntry's concurrentList.
	// Note that the new ConstraintsEntry will always point
	// back to the IndexEntry it is associated with (parent).
	// A ConstraintsEntry holds a group of constraints that can be
	// resolved, without conflict for a particular plugin id.  The
	// constraints are all of the form where another plugin id 
	// requires some version of this plugin id as a prerequisite.
	private class ConstraintsEntry {
		private IndexEntry parent;
		private List constraintList = new LinkedList();
		// lastResolved doesn't seem to be used.  Is it designed to
		// eliminate the numerous calls to find a matching plugin 
		// descriptor?  Calls to find a matching plugin descriptor
		// iterate through each version of this plugin and each 
		// constraint in each ConstraintsEntry.
		private PluginDescriptorModel lastResolved = null;
		private boolean isResolved = false;
		private PluginDescriptorModel bestMatch = null;
		private boolean bestMatchEnabled = false;

		private ConstraintsEntry(IndexEntry parent) {
			// Create a new ConstraintsEntry and point 'parent'
			// back to the associated IndexEntry
			this.parent = parent;
		}

		private int constraintCount() {
			// Returns the number of Constraint entries in
			// constraintList.  Initially this will be 0.
			return constraintList.size();
		}

		private PluginDescriptorModel addConstraint(Constraint c) {
			// Add this Constraint to the list of constraints 
			// for this ConstraintsEntry.  Note that while a
			// given ConstraintsEntry can have many Constraints,
			// any Constraint can have only one ConstraintsEntry.
			// This method will return a single plugin descriptor which
			// is the most recent descriptor which satisfies this 
			// constraint.
			constraintList.add(c);
			c.setConstraintsEntry(this);
			// get all of the plugin descriptors which satisfy this 
			// constraint and all other constraints in this ConstraintsEntry
			List constrained = getMatchingDescriptors();
			if (constrained.size() <= 0) {
				// looks like we have a conflict
				constraintList.remove(c);
				c.setConstraintsEntry(null);
				return null;
			} else {
				// match will be only the latest version plugin which 
				// satisfies these constraints
				PluginDescriptorModel match = (PluginDescriptorModel) constrained.get(0);
				if (!match.equals(lastResolved)) {
					lastResolved = match;
					isResolved = false;
				}
				return match;
			}
		}

		private void removeConstraint(Constraint c) {
			if (DEBUG_RESOLVE)
				debug("removing constraint " + c.toString()); //$NON-NLS-1$
			constraintList.remove(c);
			c.setConstraintsEntry(null);
			lastResolved = null;
			isResolved = false;
		}

		private void removeConstraintFor(PluginPrerequisiteModel prereq) {
			List remove = new ArrayList();
			for (Iterator list = constraintList.iterator(); list.hasNext();) {
				Constraint c = (Constraint) list.next();
				if (c.getPrerequisite() == prereq)
					remove.add(c);
			}
			for (Iterator list = remove.iterator(); list.hasNext();)
				removeConstraint((Constraint) list.next());
		}

		private PluginDescriptorModel getMatchingDescriptor() {
			// We do this a lot. Can we use some mechanism to 
			// hold the last matching descriptor and discard
			// it if the constraints change?
			List constrained = getMatchingDescriptors();
			if (constrained.size() <= 0)
				return null;
			else
				return (PluginDescriptorModel) constrained.get(0);
		}

		private List getMatchingDescriptors() {
			// The object of the game here is to return a list of plugin
			// descriptors that match the list of Constraint elements
			// hanging off this ConstraintsEntry.

			// constrained will be a list of matching plugin descriptors
			List constrained = new LinkedList();

			for (Iterator list = parent.versions().iterator(); list.hasNext();) {
				// parent is an IndexEntry and versions is a list of all the 
				// plugin descriptors, in version order (biggest to smallest),
				// that have this plugin id.
				PluginDescriptorModel pd = (PluginDescriptorModel) list.next();
				if (pd.getEnabled())
					constrained.add(pd);
			}
			// constrained now contains all of the enabled plugin descriptors for
			// this IndexEntry.  The next step is to remove any that don't fit.

			for (Iterator list = constraintList.iterator(); list.hasNext();) {
				// For each Constraint, go through all of the versions of this plugin
				// and remove any from 'constrained' which don't match the criteria
				// for this Constraint.

				// constraintList is all the Constraint entries for this ConstraintsEntry.
				Constraint c = (Constraint) list.next();
				if (c.getMatchType() == PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED)
					continue;
				for (Iterator list2 = parent.versions().iterator(); list2.hasNext();) {
					PluginDescriptorModel pd = (PluginDescriptorModel) list2.next();
					if (!pd.getEnabled())
						// ignore disabled plugins
						continue;
					switch (c.getMatchType()) {
						case PluginPrerequisiteModel.PREREQ_MATCH_PERFECT :
							if (!getVersionIdentifier(pd).isPerfect(c.getVersionIdentifier()))
								constrained.remove(pd);
							break;
						case PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT :
							if (!getVersionIdentifier(pd).isEquivalentTo(c.getVersionIdentifier()))
								constrained.remove(pd);
							break;
						case PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE :
							if (!getVersionIdentifier(pd).isCompatibleWith(c.getVersionIdentifier()))
								constrained.remove(pd);
							break;
						case PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL :
							if (!getVersionIdentifier(pd).isGreaterOrEqualTo(c.getVersionIdentifier()))
								constrained.remove(pd);
							break;
					}
				}
			}

			// At this point, constrained will contain only those plugin descriptors which
			// satisfy ALL of the Constraint entries.

			return constrained;
		}

		private void preresolve(List roots) {
			// All of the constraints that need to be added, have been.  Now just
			// pick the plugin descriptor that is a best fit for all of these
			// constraints.  Root nodes will not have any constraints (since nothing
			// requires this plugin as a prerequisite, by definition).  For root
			// node, just pick up the latest version.

			if (constraintList.size() <= 0) {
				// This should be a root descriptor.  So, just pick up the latest
				// version of the root.
				if (roots.contains(parent.getId())) {
					bestMatch = (PluginDescriptorModel) parent.versions().get(0);
					if (bestMatch == null) {
						if (DEBUG_RESOLVE)
							debug("*ERROR* no resolved descriptor for " + parent.getId()); //$NON-NLS-1$
					} else
						bestMatchEnabled = bestMatch.getEnabled();
				}
			} else {
				// If this isn't a root descriptor, get the latest version of the
				// plugin descriptor which matches all the constraints we have.
				// Pick the plugin that best matches all the constraints.  Any
				// allowable conflicts will be in another ConstraintsEntry.
				bestMatch = getMatchingDescriptor();
				if (bestMatch == null) {
					if (DEBUG_RESOLVE)
						debug("*ERROR* no resolved descriptor for " + parent.getId()); //$NON-NLS-1$
				} else
					bestMatchEnabled = true;
			}
		}

		private void resolve() {
			// Assumptions:  All constraints that need to be added, have been.
			//		- preresolve (above) has been called and a bestMatch (if it 
			//		exists) has been identified
			//		- all versions of this plugin have been disabled (so it is
			//		up to this method to enable the plugin that is a bestMatch
			//		for all of the constraints in this ConstraintsEntry).
			if (bestMatch != null) {
				// All of the versions of this plugin will have been disabled.
				// Enable only the one which is the best match.
				// bestMatchEnabled will be set to false if this particular plugin
				// caused an unresolvable conflict.  Therefore, setEnabled(bestMatchEnabled)
				// will leave this delinquent plugin disabled.
				bestMatch.setEnabled(bestMatchEnabled);
				if (bestMatchEnabled) {
					if (DEBUG_RESOLVE)
						debug("configured " + bestMatch.toString()); //$NON-NLS-1$
					if (constraintList.size() > 0) {
						for (int i = 0; i < constraintList.size(); i++) {
							// Put which actual version this prerequisite resolved to in the
							// relevant prerequisite in the registry.
							PluginPrerequisiteModel prq = (PluginPrerequisiteModel) ((Constraint) constraintList.get(i)).getPrerequisite();
							prq.setResolvedVersion(getVersionIdentifier(bestMatch).toString());
						}
					}
				}
			}
		}

		private boolean isResolved() {
			return this.isResolved;
		}

		private void isResolved(boolean isResolved) {
			this.isResolved = isResolved;
		}
	}

	// plugin descriptor index structure
	// There is exactly one IndexEntry for each plugin id.
	// The actual plugin descriptor is an element of verList.
	// Multiple versions of this plugin id are found in verList
	// and are ordered from the highest version number (assumed
	// to be the most recent) to the lowest version number.
	// concurrentList contains a list of ConstraintsEntry's which
	// group constraints together into non-conflicting groups.
	private class IndexEntry {
		private String id;
		private List verList = new LinkedList();
		private List concurrentList = new ArrayList();

		private IndexEntry(String id) {
			this.id = id;
			// Create the first ConstraintsEntry with no constraints
			concurrentList.add(new ConstraintsEntry(this));
		}

		private String getId() {
			return id;
		}

		private ConstraintsEntry getConstraintsEntryFor(Constraint c) {
			// Each Constraint must have exactly one ConstraintsEntry but
			// a ConstraintsEntry may have many (non-conflicting) Constraints.
			ConstraintsEntry ce = c.getConstraintsEntry();
			if (ce != null)
				return ce;
			ce = (ConstraintsEntry) concurrentList.get(0);
			if (c.getPrerequisite() == null)
				c.setConstraintsEntry(ce);
			return ce;
		}

		private PluginDescriptorModel addConstraint(Constraint c) {
			int concurrentCount = concurrentList.size();

			// try to find constraits entry that can accommodate new constraint
			for (Iterator list = concurrentList.iterator(); list.hasNext();) {
				ConstraintsEntry cie = (ConstraintsEntry) list.next();
				PluginDescriptorModel pd = cie.addConstraint(c);
				// If pd comes back null, adding this constraint to the
				// ConstraintsEntry cie will cause a conflict (no plugin
				// descriptor can satisfy all the constraints).
				if (pd != null) {

					// constraint added OK and no concurrency
					if (concurrentCount <= 1)
						return pd;

					// constraint added OK but have concurrency
					if (allowConcurrencyFor(pd))
						return pd;
					else {
						cie.removeConstraint(c); // cannot be concurrent
						return null;
					}
				}
			}

			// If we get to this point, the constraint we are trying to add
			// gave us no matching plugins when used in conjunction with the
			// other constraints in a particular ConstraintsEntry.  Add a
			// new ConstraintsEntry and put this constraint in it (only if
			// concurrency is allowed).  Concurrency is allowed only if the
			// plugin we find which matches this constraint has no extensions
			// or extension points.

			// attempt to create new constraints entry
			ConstraintsEntry cie;
			PluginDescriptorModel pd;

			if (concurrentList.size() == 1) {
				// ensure base entry allows concurrency
				cie = (ConstraintsEntry) concurrentList.get(0);
				pd = cie.getMatchingDescriptor();
				if (!allowConcurrencyFor(pd))
					return null;
			}

			cie = new ConstraintsEntry(this);
			pd = cie.addConstraint(c);
			if (pd == null) {
				cie.removeConstraint(c); // no matching target
				return null;
			}
			if (!allowConcurrencyFor(pd)) {
				cie.removeConstraint(c); // cannot be concurrent
				return null;
			}
			if (DEBUG_RESOLVE)
				debug("creating new constraints list in " + id + " for " + c.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			concurrentList.add(cie);
			return pd;
		}

		private boolean allowConcurrencyFor(PluginDescriptorModel pd) {
			if (pd == null)
				return false;
			if (pd.getDeclaredExtensions() != null && pd.getDeclaredExtensions().length > 0)
				return false;
			if (pd.getDeclaredExtensionPoints() != null && pd.getDeclaredExtensionPoints().length > 0)
				return false;
			return true;
		}

		private void removeConstraintFor(PluginPrerequisiteModel prereq) {
			for (Iterator list = concurrentList.iterator(); list.hasNext();)
				((ConstraintsEntry) list.next()).removeConstraintFor(prereq);
		}

		private PluginDescriptorModel getMatchingDescriptorFor(Constraint c) {
			ConstraintsEntry cie = getConstraintsEntryFor(c);
			return cie.getMatchingDescriptor();
		}

		private void disableAllDescriptors() {
			for (Iterator list = verList.iterator(); list.hasNext();) {
				PluginDescriptorModel pd = (PluginDescriptorModel) list.next();
				pd.setEnabled(false);
			}
		}

		private void resolveDependencies(List roots) {
			// preresolved will pick out the plugin which has the highest version
			// number and satisfies all the constraints.  This is then put in
			// bestMatch field of the ConstraintsEntry.
			for (Iterator list = concurrentList.iterator(); list.hasNext();)
				((ConstraintsEntry) list.next()).preresolve(roots);
			// Now all versions of this plugin are disabled.
			disableAllDescriptors();
			// Now, find the best match (from preresolve above) and enable it.
			// Be sure to update any prerequisite entries with the version number
			// of the plugin we are actually using.
			for (Iterator list = concurrentList.iterator(); list.hasNext();)
				((ConstraintsEntry) list.next()).resolve();
		}

		/**
		 * A root plug-in does not have any constraints attached to it.
		 */
		private boolean isRoot() {
			if (concurrentList.size() != 1)
				return false;
			ConstraintsEntry constraintsEntry = (ConstraintsEntry) concurrentList.get(0);
			return constraintsEntry.constraintCount() == 0;
		}

		private List versions() {
			return verList;
		}

		private boolean isResolvedFor(Constraint c) {
			ConstraintsEntry cie = getConstraintsEntryFor(c);
			return cie.isResolved();
		}

		private void isResolvedFor(Constraint c, boolean value) {
			ConstraintsEntry cie = getConstraintsEntryFor(c);
			cie.isResolved(value);
		}

	}

	// subtree resolution "cookie" (composite change list)
	private class Cookie {
		private boolean ok = true;
		private List changes = new ArrayList(); // a list of Constraints

		private Cookie() {
		}

		private boolean addChange(Constraint c) {
			// Keep a list of all constraints so that
			//	- we can spot circular dependencies
			//	- we can clean up if there is an unresolvable conflict
			PluginPrerequisiteModel prereq = c.getPrerequisite();
			for (Iterator list = changes.iterator(); list.hasNext();)
				if (prereq == ((Constraint) list.next()).getPrerequisite())
					// We have a circular dependency
					return false;
			changes.add(c);
			return true;
		}

		private List getChanges() {
			return changes;
		}

		private boolean isOk() {
			return ok;
		}

		private void isOk(boolean value) {
			ok = value;
		}
	}

	public RegistryResolver() {
		String debug = Platform.getDebugOption(OPTION_DEBUG_RESOLVE);
		DEBUG_RESOLVE = debug == null ? false : (debug.equalsIgnoreCase("true") ? true : false); //$NON-NLS-1$
	}

	private void add(PluginDescriptorModel pd) {

		String key = pd.getId();
		List verList;
		IndexEntry ix = (IndexEntry) idmap.get(key);

		// create new index entry if one does not exist for plugin
		if (ix == null) {
			ix = new IndexEntry(key);
			idmap.put(key, ix);
		}

		// insert plugin into list maintaining version order
		verList = ix.versions();
		int i = 0;
		for (i = 0; i < verList.size(); i++) {
			PluginDescriptorModel element = (PluginDescriptorModel) verList.get(i);
			if (getVersionIdentifier(pd).equals(getVersionIdentifier(element)))
				return; // ignore duplicates
			if (getVersionIdentifier(pd).isGreaterThan(getVersionIdentifier(element)))
				break;
		}
		verList.add(i, pd);
	}

	private void addExtensions(ExtensionModel[] extensions, PluginDescriptorModel plugin) {
		// Add all the extensions (presumably from a fragment) to plugin
		int extLength = extensions.length;
		for (int i = 0; i < extLength; i++) {
			extensions[i].setParentPluginDescriptor(plugin);
		}
		ExtensionModel[] list = plugin.getDeclaredExtensions();
		int listLength = (list == null ? 0 : list.length);
		ExtensionModel[] result = null;
		if (list == null)
			result = new ExtensionModel[extLength];
		else {
			result = new ExtensionModel[list.length + extLength];
			System.arraycopy(list, 0, result, 0, list.length);
		}
		System.arraycopy(extensions, 0, result, listLength, extLength);
		plugin.setDeclaredExtensions(result);
	}

	private void addExtensionPoints(ExtensionPointModel[] extensionPoints, PluginDescriptorModel plugin) {
		// Add all the extension points (presumably from a fragment) to plugin
		int extPtLength = extensionPoints.length;
		for (int i = 0; i < extPtLength; i++) {
			extensionPoints[i].setParentPluginDescriptor(plugin);
		}
		ExtensionPointModel[] list = plugin.getDeclaredExtensionPoints();
		int listLength = (list == null ? 0 : list.length);
		ExtensionPointModel[] result = null;
		if (list == null)
			result = new ExtensionPointModel[extPtLength];
		else {
			result = new ExtensionPointModel[list.length + extPtLength];
			System.arraycopy(list, 0, result, 0, list.length);
		}
		System.arraycopy(extensionPoints, 0, result, listLength, extPtLength);
		plugin.setDeclaredExtensionPoints(result);
	}

	private void addLibraries(LibraryModel[] libraries, PluginDescriptorModel plugin) {
		// Add all the libraries (presumably from a fragment) to plugin
		int libLength = libraries.length;
		LibraryModel[] list = plugin.getRuntime();
		LibraryModel[] result = null;
		int listLength = (list == null ? 0 : list.length);
		if (list == null)
			result = new LibraryModel[libLength];
		else {
			result = new LibraryModel[list.length + libLength];
			System.arraycopy(list, 0, result, 0, list.length);
		}
		System.arraycopy(libraries, 0, result, listLength, libLength);
		plugin.setRuntime(result);
	}

	private void addPrerequisites(PluginPrerequisiteModel[] prerequisites, PluginDescriptorModel plugin) {
		// Add all the prerequisites (presumably from a fragment) to plugin
		int reqLength = prerequisites.length;
		PluginPrerequisiteModel[] list = plugin.getRequires();
		PluginPrerequisiteModel[] result = null;
		int listLength = (list == null ? 0 : list.length);
		if (list == null)
			result = new PluginPrerequisiteModel[reqLength];
		else {
			result = new PluginPrerequisiteModel[list.length + reqLength];
			System.arraycopy(list, 0, result, 0, list.length);
		}
		System.arraycopy(prerequisites, 0, result, listLength, reqLength);
		plugin.setRequires(result);
	}

	private void debug(String s) {
		System.out.println("Registry Resolve: " + s); //$NON-NLS-1$
	}

	private void error(String message) {
		Status error = new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, message, null);
		status.add(error);
		if (InternalPlatform.DEBUG && DEBUG_RESOLVE)
			System.out.println(error.toString());
	}

	private void information(String message) {
		if (InternalPlatform.DEBUG && DEBUG_RESOLVE)
			System.out.println(message);
	}

	public IExtensionPoint getExtensionPoint(PluginDescriptorModel plugin, String extensionPointId) {
		if (extensionPointId == null)
			return null;
		ExtensionPointModel[] list = plugin.getDeclaredExtensionPoints();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (extensionPointId.equals(list[i].getId()))
				return (IExtensionPoint) list[i];
		}
		return null;
	}

	private PluginVersionIdentifier getVersionIdentifier(PluginModel model) {
		try {
			return new PluginVersionIdentifier(model.getVersion());
		} catch (RuntimeException e) {
			// if the version is invalid, an AssertionFailedException (not visible here) will be thrown
			if (this.DEBUG_RESOLVE)
				// only show if we are debugging
				e.printStackTrace(System.out);
			// Hopefully, we will never get here.  The version number
			// has already been successfully converted from a string to
			// a PluginVersionIdentifier and back to a string.  But keep
			// this catch around in case something does go wrong.
			return new PluginVersionIdentifier(0, 0, 0);
		}
	}

	private PluginVersionIdentifier getVersionIdentifier(PluginPrerequisiteModel prereq) {
		String version = prereq.getVersion();
		return version == null ? null : new PluginVersionIdentifier(version);
	}

	private boolean fragmentHasPrerequisites(PluginFragmentModel fragment) {
		PluginPrerequisiteModel[] requires = fragment.getRequires();
		if (requires == null || requires.length == 0)
			return true;
		for (int i = 0; i < requires.length; i++) {
			// Use the idmap to determine if a plugin exists.  We know
			// that all plugins in this registry already have an entry
			// in the idmap.  If the right idmap entry doesn't exist,
			// this plugin is not in the registry.
			if (idmap.get(requires[i].getPlugin()) == null) {
				// We know this plugin doesn't exist
				error(NLS.bind(Messages.parse_badPrereqOnFrag, fragment.getName(), requires[i].getPlugin()));
				return false;
			}
		}
		return true;
	}

	private void linkFragments() {
		/* For each fragment, find out which plugin descriptor it belongs
		 * to and add it to the list of fragments in this plugin.
		 */
		PluginFragmentModel[] fragments = reg.getFragments();
		for (int i = 0; i < fragments.length; i++) {
			PluginFragmentModel fragment = fragments[i];
			if (!requiredFragment(fragment)) {
				// There is a required field missing on this fragment, so 
				// ignore it.
				String id, name;
				if ((id = fragment.getId()) != null)
					error(NLS.bind(Messages.parse_fragmentMissingAttr, id));
				else if ((name = fragment.getName()) != null)
					error(NLS.bind(Messages.parse_fragmentMissingAttr, name));
				else
					error(Messages.parse_fragmentMissingIdName);
				continue;
			}
			if (!fragmentHasPrerequisites(fragment)) {
				// This fragment requires a plugin that does not 
				// exist.  Ignore the fragment.
				continue;
			}

			// Now find a plugin that fits the matching criteria specified for this fragment and
			// its related plugin
			PluginDescriptorModel plugin = null;
			IndexEntry ix = (IndexEntry) idmap.get(fragment.getPluginId());
			byte matchType = fragment.getMatch();
			if (ix != null) {
				for (Iterator list = ix.versions().iterator(); list.hasNext() && plugin == null;) {
					PluginDescriptorModel pd = (PluginDescriptorModel) list.next();
					if (pd.getEnabled()) {
						// return the highest version that fits the matching criteria
						switch (matchType) {
							case PluginFragmentModel.FRAGMENT_MATCH_PERFECT :
								if (getVersionIdentifier(pd).isPerfect(new PluginVersionIdentifier(fragment.getPluginVersion())))
									plugin = pd;
								break;
							case PluginFragmentModel.FRAGMENT_MATCH_EQUIVALENT :
								if (getVersionIdentifier(pd).isEquivalentTo(new PluginVersionIdentifier(fragment.getPluginVersion())))
									plugin = pd;
								break;
							case PluginFragmentModel.FRAGMENT_MATCH_COMPATIBLE :
							case PluginFragmentModel.FRAGMENT_MATCH_UNSPECIFIED :
								if (getVersionIdentifier(pd).isCompatibleWith(new PluginVersionIdentifier(fragment.getPluginVersion())))
									plugin = pd;
								break;
							case PluginFragmentModel.FRAGMENT_MATCH_GREATER_OR_EQUAL :
								if (getVersionIdentifier(pd).isGreaterOrEqualTo(new PluginVersionIdentifier(fragment.getPluginVersion())))
									plugin = pd;
								break;
						}
					}
				}
			}

			if (plugin == null) {
				// We couldn't find this fragment's plugin
				error(NLS.bind(Messages.parse_missingFragmentPd, fragment.getPluginId(), fragment.getId()));
				continue;
			}

			// Add this fragment to the list of fragments for this plugin descriptor
			PluginFragmentModel[] list = plugin.getFragments();
			PluginFragmentModel[] newList;
			if (list == null) {
				newList = new PluginFragmentModel[1];
				newList[0] = fragment;
			} else {
				newList = new PluginFragmentModel[list.length + 1];
				System.arraycopy(list, 0, newList, 0, list.length);
				newList[list.length] = fragment;
			}
			plugin.setFragments(newList);
		}
	}

	private void removeConstraintFor(PluginPrerequisiteModel prereq) {

		String id = prereq.getPlugin();
		IndexEntry ix = (IndexEntry) idmap.get(id);
		if (ix == null) {
			if (DEBUG_RESOLVE)
				debug("unable to locate index entry for " + id); //$NON-NLS-1$
			return;
		}
		ix.removeConstraintFor(prereq);
	}

	private void resolve() {

		// Start by putting each plugin in the idmap.  We are
		// going to need this for the call to linkFragments.
		PluginDescriptorModel[] pluginList = reg.getPlugins();
		idmap = new HashMap();
		for (int i = 0; i < pluginList.length; i++) {
			// Check to see if all the required fields exist now.
			// For example, if we have a null plugin version identifier,
			// the add(pluginList[i]) will give a null pointer
			// exception.
			if (!requiredPluginDescriptor(pluginList[i])) {
				pluginList[i].setEnabled(false);
				String id, name;
				if ((id = pluginList[i].getId()) != null)
					error(NLS.bind(Messages.parse_pluginMissingAttr, id));
				else if ((name = pluginList[i].getName()) != null)
					error(NLS.bind(Messages.parse_pluginMissingAttr, name));
				else
					error(Messages.parse_pluginMissingIdName);
				continue;
			}
			add(pluginList[i]);
		}
		// Add all the fragments to their associated plugin.
		// Note that this will check for all the required fields in
		// the fragment.
		linkFragments();
		// Now we have to cycle through the plugin list again
		// to assimilate all the fragment information and 
		// check for 'required' fields.
		for (int i = 0; i < pluginList.length; i++) {
			if (pluginList[i].getFragments() != null) {
				// Take all the information in each fragment and
				// embed it in the plugin descriptor
				resolvePluginFragments(pluginList[i]);
			}
		}

		// resolve root descriptors
		List roots = resolveRootDescriptors();

		if (roots.size() == 0) {
			// No roots, likely due to a circular dependency
			// (or multiple circular dependencies).  Disable
			// all plugins before returning.  Remember to trim
			// the registry if needed.
			PluginDescriptorModel[] plugins = reg.getPlugins();
			for (int i = 0; i < plugins.length; i++) {
				plugins[i].setEnabled(false);
			}
			resolvePluginRegistry();
			idmap = null;
			reg = null;
			error(Messages.plugin_unableToResolve);
			return;
		}

		// roots is a list of those plugin ids that are not a
		// prerequisite for any other plugin.  Note that roots
		// contains ids only.

		// process all root nodes (first those previously on roots list, then those on the orphans set) 
		// The orphans of an iteration will become the roots of the next one.	
		for (Set orphans, rootsSet = new HashSet(roots); !rootsSet.isEmpty(); rootsSet = orphans) {
			orphans = new HashSet();
			// walk the dependencies and setup constraints	
			for (Iterator rootsIter = rootsSet.iterator(); rootsIter.hasNext();) {
				String rootID = (String) rootsIter.next();
				resolveNode(rootID, null, null, null, orphans);
				// At this point we have set up all the Constraint and
				// ConstraintsEntry components.  But we may not have found which
				// plugin is the best match for a given set of constraints.
			}
			// build the roots set for the next iteration
			for (Iterator orphansIter = orphans.iterator(); orphansIter.hasNext();) {
				IndexEntry orphan = (IndexEntry) idmap.get(orphansIter.next());
				// only after a complete iteration over the roots set we can decide if 
				// a potential orphan is a real orphan
				// Now we need to resolve for these new roots (they may
				// not have been resolved before, especially if the parent
				// was looking for an older version and not the latest
				// version which is what we pick up for the roots).					
				if (orphan.isRoot()) {
					if (DEBUG_RESOLVE)
						debug("orphan " + orphan.getId()); //$NON-NLS-1$				
					roots.add(orphan.getId());
				} else
					orphansIter.remove();
			}
		}

		// resolve dependencies
		Iterator plugins = idmap.entrySet().iterator();
		while (plugins.hasNext()) {
			IndexEntry ix = (IndexEntry) ((Map.Entry) plugins.next()).getValue();
			// Now go off and find the plugin that matches the
			// constraints.  Note that root plugins will always use the 
			// latest version.
			ix.resolveDependencies(roots);
		}

		// walk down the registry structure and resolve links
		// between extensions and extension points
		resolvePluginRegistry();

		// unhook registry and index
		idmap = null;
		reg = null;
	}

	public IStatus resolve(PluginRegistryModel registry) {
		// This is the entry point to the registry resolver.
		// Calling this method, with a valid registry will 
		// cause this registry to be 'resolved'.

		status = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, "", null); //$NON-NLS-1$

		if (registry.isResolved())
			// don't bother doing anything if it's already resolved
			return status;

		reg = registry;
		resolve();
		registry.markResolved();
		return status;
	}

	private void resolveExtension(ExtensionModel ext) {

		String target = ext.getExtensionPoint();
		int ix = target.lastIndexOf("."); //$NON-NLS-1$
		String pluginId = target.substring(0, ix);
		String extPtId = target.substring(ix + 1);
		String message;

		PluginDescriptorModel plugin = (PluginDescriptorModel) reg.getPlugin(pluginId);
		if (plugin == null) {
			message = NLS.bind(Messages.parse_extPointUnknown, target, ext.getParentPluginDescriptor().getId());
			error(message);
			return;
		}
		if (!plugin.getEnabled()) {
			message = NLS.bind(Messages.parse_extPointDisabled, target, ext.getParentPluginDescriptor().getId());
			error(message);
			return;
		}

		ExtensionPointModel extPt = (ExtensionPointModel) getExtensionPoint(plugin, extPtId);
		if (extPt == null) {
			message = NLS.bind(Messages.parse_extPointUnknown, target, ext.getParentPluginDescriptor().getId());
			error(message);
			return;
		}

		ExtensionModel[] oldValues = extPt.getDeclaredExtensions();
		ExtensionModel[] newValues = null;
		if (oldValues == null)
			newValues = new ExtensionModel[1];
		else {
			newValues = new ExtensionModel[oldValues.length + 1];
			System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
		}
		newValues[newValues.length - 1] = ext;
		extPt.setDeclaredExtensions(newValues);
	}

	private Cookie resolveNode(String child, PluginDescriptorModel parent, PluginPrerequisiteModel prq, Cookie cookie, Set orphans) {
		// This method is called recursively to setup dependency constraints.
		// Top invocation is passed null parent and null prerequisite.
		// We are trying to resolve for the plugin descriptor with id 'child'.

		if (DEBUG_RESOLVE)
			debug("PUSH> " + child); //$NON-NLS-1$

		if (cookie == null)
			cookie = new Cookie();

		// lookup child entry
		IndexEntry ix = (IndexEntry) idmap.get(child);
		// We should now have the IndexEntry for the plugin we 
		// wish to resolve
		if (ix == null) {
			// If this is an optional prerequisite and not a root
			// node, we can just ignore this prerequisite if there
			// is no IndexEntry (as there is no corresponding plugin)
			// and continue processing.
			if (prq.getOptional() && parent != null && child != null)
				return cookie;
			if (parent != null)
				error(NLS.bind(Messages.parse_prereqDisabled, parent.getId(), child));
			if (DEBUG_RESOLVE)
				debug("<POP  " + child + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
			cookie.isOk(false);
			return cookie;
		}

		// try to add new dependency constraint
		Constraint currentConstraint = new Constraint(parent, prq);
		// A constraint will be added for each parent which requires us.
		PluginDescriptorModel childPd = null;
		if (parent != null) {
			childPd = ix.addConstraint(currentConstraint);
			if (childPd == null) {
				if (prq.getOptional()) {
					// This is an optional prerequisite.  Ignore the conflict and this
					// prerequisite.
					orphans.add(ix.getId());
					information(NLS.bind(Messages.parse_unsatisfiedOptPrereq, parent.getId(), child));
					return cookie;
				} else {
					// This prerequisite is mandatory.  
					String message = NLS.bind(Messages.parse_unsatisfiedPrereq, parent.getId(), child);
					error(message);
					if (DEBUG_RESOLVE)
						debug("<POP  " + child + " unable to satisfy constraint"); //$NON-NLS-1$ //$NON-NLS-2$
					cookie.isOk(false);
					return cookie;
				}
			} else if (!cookie.addChange(currentConstraint)) {
				if (prq.getOptional()) {
					// This is an optional prerequisite.  Ignore the loop, and the
					// prerequisite
					information(NLS.bind(Messages.parse_prereqOptLoop, parent.getId(), child));
					return cookie;
				} else {
					String message = NLS.bind(Messages.parse_prereqLoop, parent.getId(), child);
					error(message);
					if (DEBUG_RESOLVE)
						debug("<POP  " + child + " prerequisite loop"); //$NON-NLS-1$ //$NON-NLS-2$
					cookie.isOk(false);
					return cookie;
				}
			}
		} else {
			// This is a root node.  There is no prerequisite so this IndexEntry must
			// exist
			childPd = ix.getMatchingDescriptorFor(currentConstraint);
			if (childPd == null) {
				if (DEBUG_RESOLVE)
					debug("<POP  " + child + " not found (missing descriptor entry)"); //$NON-NLS-1$ //$NON-NLS-2$
				cookie.isOk(false);
				return cookie;
			}
		}

		// check to see if subtree is already resolved
		if (ix.isResolvedFor(currentConstraint)) {
			if (DEBUG_RESOLVE)
				debug("<POP  " + child + " already resolved"); //$NON-NLS-1$ //$NON-NLS-2$
			return cookie;
		}

		// select the subtree to resolve
		PluginPrerequisiteModel[] prereqs = childPd.getRequires();
		PluginPrerequisiteModel prereq;
		prereqs = prereqs == null ? new PluginPrerequisiteModel[0] : prereqs;
		for (int i = 0; cookie.isOk() && i < prereqs.length; i++) {
			prereq = (PluginPrerequisiteModel) prereqs[i];
			cookie = resolveNode(prereq.getPlugin(), childPd, prereq, cookie, orphans);
		}

		// if we failed, remove any constraints we added
		if (!cookie.isOk()) {
			Constraint cookieConstraint;
			for (Iterator change = cookie.getChanges().iterator(); change.hasNext();) {
				cookieConstraint = (Constraint) change.next();
				prereq = cookieConstraint.getPrerequisite();
				if (childPd == cookieConstraint.getParent()) {
					// keep track of orphaned subtrees
					if (!orphans.contains(prereq.getPlugin()) && (idmap.get(prereq.getPlugin()) != null))
						orphans.add(prereq.getPlugin());
				}
				removeConstraintFor(prereq);
			}
			// Make sure you picked up all the orphaned subtrees
			// for this childPd
			for (int i = 0; i < prereqs.length; i++) {
				if (!orphans.contains(prereqs[i].getPlugin()) && (idmap.get(prereqs[i].getPlugin()) != null))
					orphans.add(prereqs[i].getPlugin());
			}
			if (parent != null)
				error(NLS.bind(Messages.parse_prereqDisabled, parent.getId(), child));
			childPd.setEnabled(false);
			if (DEBUG_RESOLVE)
				debug("<POP  " + child + " failed to resolve subtree"); //$NON-NLS-1$ //$NON-NLS-2$
			return cookie;
		} else {
			// we're done
			ix.isResolvedFor(currentConstraint, true);
			if (DEBUG_RESOLVE)
				debug("<POP  " + child + " " + getVersionIdentifier(childPd)); //$NON-NLS-1$ //$NON-NLS-2$
			return cookie;
		}
	}

	private void resolvePluginDescriptor(PluginDescriptorModel pd) {
		ExtensionModel[] list = pd.getDeclaredExtensions();
		if (list == null || list.length == 0 || !pd.getEnabled())
			// Can be disabled if all required attributes not present
			return;
		for (int i = 0; i < list.length; i++)
			resolveExtension((ExtensionModel) list[i]);
	}

	private void resolvePluginFragment(PluginFragmentModel fragment, PluginDescriptorModel plugin) {
		ExtensionModel[] extensions = fragment.getDeclaredExtensions();
		if (extensions != null)
			// Add all the fragment extensions to the plugin
			addExtensions(extensions, plugin);

		ExtensionPointModel[] points = fragment.getDeclaredExtensionPoints();
		if (points != null)
			// Add all the fragment extension points to the plugin
			addExtensionPoints(points, plugin);

		LibraryModel[] libraries = fragment.getRuntime();
		if (libraries != null)
			// Add all the fragment library entries to the plugin
			addLibraries(libraries, plugin);

		PluginPrerequisiteModel[] prerequisites = fragment.getRequires();
		if (prerequisites != null)
			// Add all the fragment prerequisites to the plugin
			addPrerequisites(prerequisites, plugin);
	}

	private void resolvePluginFragments(PluginDescriptorModel plugin) {
		/* For each fragment contained in the fragment list of this plugin, 
		 * apply all the fragment bits to the plugin (e.g. all of the fragment's
		 * extensions are added to the list of extensions in the plugin).  Be
		 * sure to use only the latest version of any given fragment (in case
		 * there are multiple versions of a given fragment id).  So note that,
		 * if there are multiple versions of a given fragment id, all but the
		 * latest version will be discarded.
		 */

		// The boolean 'dirty' will remain false if there is only one
		// version of every fragment id associated with this plugin
		boolean dirty = false;

		PluginFragmentModel[] fragmentList = plugin.getFragments();
		HashMap latestFragments = new HashMap(30);
		for (int i = 0; i < fragmentList.length; i++) {
			String fragmentId = fragmentList[i].getId();
			PluginFragmentModel latestVersion = (PluginFragmentModel) latestFragments.get(fragmentId);
			if (latestVersion == null) {
				// We don't have any fragments with this id yet
				latestFragments.put(fragmentId, fragmentList[i]);
			} else {
				dirty = true;
				if (getVersionIdentifier(fragmentList[i]).equals(getVersionIdentifier(latestVersion)))
					// ignore duplicates
					error(NLS.bind(Messages.parse_duplicateFragment, fragmentId, fragmentList[i].getVersion()));
				if (getVersionIdentifier(fragmentList[i]).isGreaterThan(getVersionIdentifier(latestVersion))) {
					latestFragments.put(fragmentId, fragmentList[i]);
				}
			}
		}

		// latestFragments now contains the latest version of each fragment
		// id for this plugin

		// Now add the latest version of each fragment to the plugin
		Set latestOnly = new HashSet();
		for (Iterator list = latestFragments.values().iterator(); list.hasNext();) {
			PluginFragmentModel latestFragment = (PluginFragmentModel) list.next();
			if (dirty)
				latestOnly.add(latestFragment);
			int numLibraries = latestFragment.getRuntime() == null ? 0 : latestFragment.getRuntime().length;
			resolvePluginFragment(latestFragment, plugin);
			// If this fragment added library entries, check to see if it
			// added a duplicate library entry.
			if (numLibraries != 0) {
				// Something got added
				LibraryModel[] libraries = plugin.getRuntime();
				// Put all the library names into a set as we know the set will not
				// have any duplicates.
				Set libNames = new HashSet();
				int setSize = libNames.size();
				for (int i = 0; i < libraries.length; i++) {
					libNames.add(libraries[i].getName());
					if (libNames.size() == setSize) {
						// We know this library name didn't get added to the set.
						// Ignore the duplicate but indicate an error
						String[] bindings = {latestFragment.getId(), plugin.getId(), libraries[i].getName()};
						error(NLS.bind(Messages.parse_duplicateLib, bindings));
					} else {
						setSize = libNames.size();
					}
				}
			}
		}
		// Currently the fragments on the plugin include all fragment 
		// versions.  Now strip off all but the latest version of each
		// fragment id (only if necessary).
		if (dirty)
			plugin.setFragments((PluginFragmentModel[]) latestOnly.toArray(new PluginFragmentModel[latestOnly.size()]));

	}

	private void resolvePluginRegistry() {
		// filter out disabled plugins from "live" registry
		if (trimPlugins)
			trimRegistry();

		// resolve relationships
		if (crossLink) {
			// cross link all of the extensions and extension points.
			PluginDescriptorModel[] plugins = reg.getPlugins();
			for (int i = 0; i < plugins.length; i++)
				resolvePluginDescriptor(plugins[i]);
		}
	}

	private boolean requiredPluginDescriptor(PluginDescriptorModel plugin) {
		boolean retValue = true;
		retValue = plugin.getName() != null && plugin.getId() != null && plugin.getVersion() != null;
		if (!retValue)
			return retValue;

		PluginPrerequisiteModel[] requiresList = plugin.getRequires();
		ExtensionModel[] extensions = plugin.getDeclaredExtensions();
		ExtensionPointModel[] extensionPoints = plugin.getDeclaredExtensionPoints();
		LibraryModel[] libraryList = plugin.getRuntime();
		PluginFragmentModel[] fragments = plugin.getFragments();

		if (requiresList != null) {
			for (int i = 0; i < requiresList.length && retValue; i++) {
				retValue = retValue && requiredPrerequisite(requiresList[i]);
			}
		}
		if (extensions != null) {
			for (int i = 0; i < extensions.length && retValue; i++) {
				retValue = retValue && requiredExtension(extensions[i]);
			}
		}
		if (extensionPoints != null) {
			for (int i = 0; i < extensionPoints.length && retValue; i++) {
				retValue = retValue && requiredExtensionPoint(extensionPoints[i]);
			}
		}
		if (libraryList != null) {
			for (int i = 0; i < libraryList.length && retValue; i++) {
				retValue = retValue && requiredLibrary(libraryList[i]);
			}
		}
		if (fragments != null) {
			for (int i = 0; i < fragments.length && retValue; i++) {
				retValue = retValue && requiredFragment(fragments[i]);
			}
		}

		return retValue;
	}

	private boolean requiredPrerequisite(PluginPrerequisiteModel prerequisite) {
		return ((prerequisite.getPlugin() != null));
	}

	private boolean requiredExtension(ExtensionModel extension) {
		return (extension.getExtensionPoint() != null);
	}

	private boolean requiredExtensionPoint(ExtensionPointModel extensionPoint) {
		return ((extensionPoint.getName() != null) && (extensionPoint.getId() != null));
	}

	private boolean requiredLibrary(LibraryModel library) {
		return (library.getName() != null);
	}

	private boolean requiredFragment(PluginFragmentModel fragment) {
		return ((fragment.getName() != null) && (fragment.getId() != null) && (fragment.getPlugin() != null) && (fragment.getPluginVersion() != null) && (fragment.getVersion() != null));
	}

	private List resolveRootDescriptors() {

		// Determine the roots of the dependency tree. Disable all
		// but one versions of the root descriptors.

		// get list of all plugin identifiers in the registry
		List ids = new ArrayList();
		ids.addAll(idmap.keySet());

		// ids is just a list of all the plugin id's
		// The following while loop will remove all id's that
		// appear in any prerequisite list.

		// iterate over the list eliminating targets of <requires> entries
		Iterator p = idmap.entrySet().iterator();
		while (p.hasNext()) {
			IndexEntry ix = (IndexEntry) ((Map.Entry) p.next()).getValue();
			if (ix != null) {
				List list = ix.versions();
				int ixSize = list.size();
				if (ixSize > 0) {
					// Remove any prerequisite mentioned in any version of this plugin
					for (int i = 0; i < ixSize; i++) {
						PluginDescriptorModel pd = (PluginDescriptorModel) list.get(i);
						PluginPrerequisiteModel[] prereqs = pd.getRequires();
						for (int j = 0; prereqs != null && j < prereqs.length; j++) {
							ids.remove(prereqs[j].getPlugin());
						}
					}
				}
			}
		}

		if (ids.size() > 0) {
			// disable all but the most recent version of root descriptors
			String id;
			p = ids.iterator();
			while (p.hasNext()) {
				id = (String) p.next();
				IndexEntry ix = (IndexEntry) idmap.get(id);
				if (ix != null) {
					List list = ix.versions();
					for (int i = 0; i < list.size(); i++) {
						PluginDescriptorModel pd = (PluginDescriptorModel) list.get(i);
						if (i == 0) {
							// Don't disable this one.  It is the
							// one with the highest version number.
							if (DEBUG_RESOLVE)
								debug("root " + pd); //$NON-NLS-1$
						} else {
							// Disable all versions except the one with the
							// highest version number.
							if (DEBUG_RESOLVE)
								debug("     " + pd + " disabled"); //$NON-NLS-1$ //$NON-NLS-2$
							pd.setEnabled(false);
						}
					}
				}
			}
		} else {
			if (DEBUG_RESOLVE)
				debug("NO ROOTS"); //$NON-NLS-1$
		}

		return ids;
	}

	/**
	 * Specifies whether extensions and extension points should be cross 
	 * linked during the resolve process.
	 */
	public void setCrossLink(boolean value) {
		crossLink = value;
	}

	/**
	 * Specified whether disabled plugins should to be removed when the resolve
	 * is completed.
	 */
	public void setTrimPlugins(boolean value) {
		trimPlugins = value;
	}

	private void trimRegistry() {
		PluginDescriptorModel[] list = reg.getPlugins();
		for (int i = 0; i < list.length; i++) {
			PluginDescriptorModel pd = (PluginDescriptorModel) list[i];
			if (!pd.getEnabled()) {
				if (DEBUG_RESOLVE)
					debug("removing " + pd.toString()); //$NON-NLS-1$
				reg.removePlugin(pd.getId(), pd.getVersion());
			}
		}
	}
}
