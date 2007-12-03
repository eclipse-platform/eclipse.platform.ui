/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * Maintains collection of known nature descriptors, and implements
 * nature-related algorithms provided by the workspace.
 */
public class NatureManager implements ILifecycleListener, IManager {
	//maps String (nature ID) -> descriptor objects
	protected Map descriptors;

	//maps IProject -> String[] of enabled natures for that project
	protected Map natureEnablements;

	//maps String (builder ID) -> String (nature ID)
	protected Map buildersToNatures = null;
	//colour constants used in cycle detection algorithm
	private static final byte WHITE = 0;
	private static final byte GREY = 1;
	private static final byte BLACK = 2;

	protected NatureManager() {
		super();
	}

	/**
	 * Computes the list of natures that are enabled for the given project.
	 * Enablement computation is subtlely different from nature set
	 * validation, because it must find and remove all inconsistencies.
	 */
	protected String[] computeNatureEnablements(Project project) {
		final ProjectDescription description = project.internalGetDescription();
		if (description == null)
			return new String[0];//project deleted concurrently
		String[] natureIds = description.getNatureIds();
		int count = natureIds.length;
		if (count == 0)
			return natureIds;

		//set of the nature ids being validated (String (id))
		HashSet candidates = new HashSet(count * 2);
		//table of String(set ID) -> ArrayList (nature IDs that belong to that set)
		HashMap setsToNatures = new HashMap(count);
		for (int i = 0; i < count; i++) {
			String id = natureIds[i];
			ProjectNatureDescriptor desc = (ProjectNatureDescriptor) getNatureDescriptor(id);
			if (desc == null)
				continue;
			if (!desc.hasCycle)
				candidates.add(id);
			//build set to nature map
			String[] setIds = desc.getNatureSetIds();
			for (int j = 0; j < setIds.length; j++) {
				String set = setIds[j];
				ArrayList current = (ArrayList) setsToNatures.get(set);
				if (current == null) {
					current = new ArrayList(5);
					setsToNatures.put(set, current);
				}
				current.add(id);
			}
		}
		//now remove all natures that belong to sets with more than one member
		for (Iterator it = setsToNatures.values().iterator(); it.hasNext();) {
			ArrayList setMembers = (ArrayList) it.next();
			if (setMembers.size() > 1) {
				candidates.removeAll(setMembers);
			}
		}
		//now walk over the set and ensure all pre-requisite natures are present
		//need to walk in prereq order because if A requires B and B requires C, and C is
		//disabled for some other reason, we must ensure both A and B are disabled
		String[] orderedCandidates = (String[]) candidates.toArray(new String[candidates.size()]);
		orderedCandidates = sortNatureSet(orderedCandidates);
		for (int i = 0; i < orderedCandidates.length; i++) {
			String id = orderedCandidates[i];
			IProjectNatureDescriptor desc = getNatureDescriptor(id);
			String[] required = desc.getRequiredNatureIds();
			for (int j = 0; j < required.length; j++) {
				if (!candidates.contains(required[j])) {
					candidates.remove(id);
					break;
				}
			}
		}
		//remaining candidates are enabled
		return (String[]) candidates.toArray(new String[candidates.size()]);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#getNatureDescriptor(String)
	 */
	public IProjectNatureDescriptor getNatureDescriptor(String natureId) {
		lazyInitialize();
		return (IProjectNatureDescriptor) descriptors.get(natureId);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#getNatureDescriptors()
	 */
	public IProjectNatureDescriptor[] getNatureDescriptors() {
		lazyInitialize();
		Collection values = descriptors.values();
		return (IProjectNatureDescriptor[]) values.toArray(new IProjectNatureDescriptor[values.size()]);
	}

	public void handleEvent(LifecycleEvent event) {
		switch (event.kind) {
			case LifecycleEvent.PRE_PROJECT_CHANGE :
			case LifecycleEvent.PRE_PROJECT_CLOSE :
			case LifecycleEvent.PRE_PROJECT_DELETE :
			case LifecycleEvent.PRE_PROJECT_MOVE :
			case LifecycleEvent.PRE_PROJECT_OPEN :
				flushEnablements((IProject) event.resource);
		}
	}

	/**
	 * Configures the nature with the given ID for the given project.
	 */
	protected void configureNature(final Project project, final String natureID, final MultiStatus errors) {
		ISafeRunnable code = new ISafeRunnable() {
			public void run() throws Exception {
				IProjectNature nature = createNature(project, natureID);
				nature.configure();
				ProjectInfo info = (ProjectInfo) project.getResourceInfo(false, true);
				info.setNature(natureID, nature);
			}

			public void handleException(Throwable exception) {
				if (exception instanceof CoreException)
					errors.add(((CoreException) exception).getStatus());
				else
					errors.add(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, project.getFullPath(), NLS.bind(Messages.resources_errorNature, natureID), exception));
			}
		};
		if (Policy.DEBUG_NATURES) {
			System.out.println("Configuring nature: " + natureID + " on project: " + project.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		SafeRunner.run(code);
	}

	/**
	 * Configures the natures for the given project.  Natures found in the new description
	 * that weren't present in the old description are added, and natures missing from the
	 * new description are removed.  Updates the old description so that it reflects
	 * the new set of the natures.  Errors are added to the given multi-status.
	 */
	public void configureNatures(Project project, ProjectDescription oldDescription, ProjectDescription newDescription, MultiStatus status) {
		// Be careful not to rely on much state because (de)configuring a nature
		// may well result in recursive calls to this method.
		HashSet oldNatures = new HashSet(Arrays.asList(oldDescription.getNatureIds(false)));
		HashSet newNatures = new HashSet(Arrays.asList(newDescription.getNatureIds(false)));
		if (oldNatures.equals(newNatures))
			return;
		HashSet deletions = (HashSet) oldNatures.clone();
		HashSet additions = (HashSet) newNatures.clone();
		additions.removeAll(oldNatures);
		deletions.removeAll(newNatures);
		//do validation of the changes.  If any single change is invalid, fail the whole operation
		IStatus result = validateAdditions(newNatures, additions, project);
		if (!result.isOK()) {
			status.merge(result);
			return;
		}
		result = validateRemovals(newNatures, deletions);
		if (!result.isOK()) {
			status.merge(result);
			return;
		}
		// set the list of nature ids BEFORE (de)configuration so recursive calls will
		// not try to do the same work.
		oldDescription.setNatureIds(newDescription.getNatureIds(true));
		flushEnablements(project);
		//(de)configure in topological order to maintain consistency of configured set
		String[] ordered = null;
		if (deletions.size() > 0) {
			ordered = sortNatureSet((String[]) deletions.toArray(new String[deletions.size()]));
			for (int i = ordered.length; --i >= 0;)
				deconfigureNature(project, ordered[i], status);
		}
		if (additions.size() > 0) {
			ordered = sortNatureSet((String[]) additions.toArray(new String[additions.size()]));
			for (int i = 0; i < ordered.length; i++)
				configureNature(project, ordered[i], status);
		}
	}

	/**
	 * Finds the nature extension, and initializes and returns an instance.
	 */
	protected IProjectNature createNature(Project project, String natureID) throws CoreException {
		IExtension extension = Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_NATURES, natureID);
		if (extension == null) {
			String message = NLS.bind(Messages.resources_natureExtension, natureID);
			throw new ResourceException(Platform.PLUGIN_ERROR, project.getFullPath(), message, null);
		}
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length < 1) {
			String message = NLS.bind(Messages.resources_natureClass, natureID);
			throw new ResourceException(Platform.PLUGIN_ERROR, project.getFullPath(), message, null);
		}
		//find the runtime configuration element
		IConfigurationElement config = null;
		for (int i = 0; config == null && i < configs.length; i++)
			if ("runtime".equalsIgnoreCase(configs[i].getName())) //$NON-NLS-1$
				config = configs[i];
		if (config == null) {
			String message = NLS.bind(Messages.resources_natureFormat, natureID);
			throw new ResourceException(Platform.PLUGIN_ERROR, project.getFullPath(), message, null);
		}
		try {
			IProjectNature nature = (IProjectNature) config.createExecutableExtension("run"); //$NON-NLS-1$
			nature.setProject(project);
			return nature;
		} catch (ClassCastException e) {
			String message = NLS.bind(Messages.resources_natureImplement, natureID);
			throw new ResourceException(Platform.PLUGIN_ERROR, project.getFullPath(), message, e);
		}
	}

	/**
	 * Deconfigures the nature with the given ID for the given project.
	 */
	protected void deconfigureNature(final Project project, final String natureID, final MultiStatus status) {
		final ProjectInfo info = (ProjectInfo) project.getResourceInfo(false, true);
		IProjectNature existingNature = info.getNature(natureID);
		if (existingNature == null) {
			// if there isn't a nature then create one so we can deconfig it.
			try {
				existingNature = createNature(project, natureID);
			} catch (CoreException e) {
				// Ignore - we are removing a nature that no longer exists in the install
				return;
			}
		}
		final IProjectNature nature = existingNature;
		ISafeRunnable code = new ISafeRunnable() {
			public void run() throws Exception {
				nature.deconfigure();
				info.setNature(natureID, null);
			}

			public void handleException(Throwable exception) {
				if (exception instanceof CoreException)
					status.add(((CoreException) exception).getStatus());
				else
					status.add(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, project.getFullPath(), NLS.bind(Messages.resources_natureDeconfig, natureID), exception));
			}
		};
		if (Policy.DEBUG_NATURES) {
			System.out.println("Deconfiguring nature: " + natureID + " on project: " + project.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		SafeRunner.run(code);
	}

	/**
	 * Marks all nature descriptors that are involved in cycles
	 */
	protected void detectCycles() {
		Collection values = descriptors.values();
		ProjectNatureDescriptor[] natures = (ProjectNatureDescriptor[]) values.toArray(new ProjectNatureDescriptor[values.size()]);
		for (int i = 0; i < natures.length; i++)
			if (natures[i].colour == WHITE)
				hasCycles(natures[i]);
	}

	/**
	 * Returns a status indicating failure to configure natures.
	 */
	protected IStatus failure(String reason) {
		return new ResourceStatus(IResourceStatus.INVALID_NATURE_SET, reason);
	}

	/**
	 * Returns the ID of the project nature that claims ownership of the
	 * builder with the given ID.  Returns null if no nature owns that builder.
	 */
	public String findNatureForBuilder(String builderID) {
		if (buildersToNatures == null) {
			buildersToNatures = new HashMap(10);
			IProjectNatureDescriptor[] descs = getNatureDescriptors();
			for (int i = 0; i < descs.length; i++) {
				String natureId = descs[i].getNatureId();
				String[] builders = ((ProjectNatureDescriptor) descs[i]).getBuilderIds();
				for (int j = 0; j < builders.length; j++) {
					//FIXME: how to handle multiple natures specifying same builder
					buildersToNatures.put(builders[j], natureId);
				}
			}
		}
		return (String) buildersToNatures.get(builderID);
	}

	protected void flushEnablements(IProject project) {
		if (natureEnablements != null) {
			natureEnablements.remove(project);
			if (natureEnablements.size() == 0) {
				natureEnablements = null;
			}
		}
	}

	/**
	 * Returns the cached array of enabled natures for this project,
	 * or null if there is nothing in the cache.
	 */
	protected String[] getEnabledNatures(Project project) {
		String[] enabled;
		if (natureEnablements != null) {
			enabled = (String[]) natureEnablements.get(project);
			if (enabled != null)
				return enabled;
		}
		enabled = computeNatureEnablements(project);
		setEnabledNatures(project, enabled);
		return enabled;
	}

	/**
	 * Returns true if there are cycles in the graph of nature 
	 * dependencies starting at root i.  Returns false otherwise.
	 * Marks all descriptors that are involved in the cycle as invalid.
	 */
	protected boolean hasCycles(ProjectNatureDescriptor desc) {
		if (desc.colour == BLACK) {
			//this subgraph has already been traversed, so we know the answer
			return desc.hasCycle;
		}
		//if we are already grey, then we have found a cycle
		if (desc.colour == GREY) {
			desc.hasCycle = true;
			desc.colour = BLACK;
			return true;
		}
		//colour current descriptor GREY to indicate it is being visited
		desc.colour = GREY;

		//visit all dependents of nature i
		String[] required = desc.getRequiredNatureIds();
		for (int i = 0; i < required.length; i++) {
			ProjectNatureDescriptor dependency = (ProjectNatureDescriptor) getNatureDescriptor(required[i]);
			//missing dependencies cannot create cycles
			if (dependency != null && hasCycles(dependency)) {
				desc.hasCycle = true;
				desc.colour = BLACK;
				return true;
			}
		}
		desc.hasCycle = false;
		desc.colour = BLACK;
		return false;
	}

	/**
	 * Returns true if the given project has linked resources, and false otherwise.
	 */
	protected boolean hasLinks(IProject project) {
		try {
			IResource[] children = project.members();
			for (int i = 0; i < children.length; i++)
				if (children[i].isLinked())
					return true;
		} catch (CoreException e) {
			//not possible for project to be inaccessible
			Policy.log(e.getStatus());
		}
		return false;
	}

	/**
	 * Checks if the two natures have overlapping "one-of-nature" set 
	 * memberships.  Returns the name of one such overlap, or null if
	 * there is no set overlap.
	 */
	protected String hasSetOverlap(IProjectNatureDescriptor one, IProjectNatureDescriptor two) {
		if (one == null || two == null) {
			return null;
		}
		//efficiency not so important because these sets are very small
		String[] setsOne = one.getNatureSetIds();
		String[] setsTwo = two.getNatureSetIds();
		for (int iOne = 0; iOne < setsOne.length; iOne++) {
			for (int iTwo = 0; iTwo < setsTwo.length; iTwo++) {
				if (setsOne[iOne].equals(setsTwo[iTwo])) {
					return setsOne[iOne];
				}
			}
		}
		return null;
	}

	/**
	 * Perform depth-first insertion of the given nature ID into the result list.
	 */
	protected void insert(ArrayList list, Set seen, String id) {
		if (seen.contains(id))
			return;
		seen.add(id);
		//insert prerequisite natures
		IProjectNatureDescriptor desc = getNatureDescriptor(id);
		if (desc != null) {
			String[] prereqs = desc.getRequiredNatureIds();
			for (int i = 0; i < prereqs.length; i++)
				insert(list, seen, prereqs[i]);
		}
		list.add(id);
	}

	/* (non-Javadoc)
	 * Returns true if the given nature is enabled for the given project.
	 * 
	 * @see IProject#isNatureEnabled(String)
	 */
	public boolean isNatureEnabled(Project project, String id) {
		String[] enabled = getEnabledNatures(project);
		for (int i = 0; i < enabled.length; i++) {
			if (enabled[i].equals(id))
				return true;
		}
		return false;
	}

	/**
	 * Only initialize the descriptor cache when we know it is actually needed.
	 * Running programs may never need to refer to this cache.
	 */
	protected void lazyInitialize() {
		if (descriptors != null)
			return;
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_NATURES);
		IExtension[] extensions = point.getExtensions();
		descriptors = new HashMap(extensions.length * 2 + 1);
		for (int i = 0, imax = extensions.length; i < imax; i++) {
			IProjectNatureDescriptor desc = null;
			try {
				desc = new ProjectNatureDescriptor(extensions[i]);
			} catch (CoreException e) {
				Policy.log(e.getStatus());
			}
			if (desc != null)
				descriptors.put(desc.getNatureId(), desc);
		}
		//do cycle detection now so it only has to be done once
		//cycle detection on a graph subset is a pain
		detectCycles();
	}

	/**
	 * Sets the cached array of enabled natures for this project.
	 */
	protected void setEnabledNatures(IProject project, String[] enablements) {
		if (natureEnablements == null)
			natureEnablements = Collections.synchronizedMap(new HashMap(20));
		natureEnablements.put(project, enablements);
	}

	public void shutdown(IProgressMonitor monitor) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see IWorkspace#sortNatureSet(String[])
	 */
	public String[] sortNatureSet(String[] natureIds) {
		int count = natureIds.length;
		if (count == 0)
			return natureIds;
		ArrayList result = new ArrayList(count);
		HashSet seen = new HashSet(count);//for cycle and duplicate detection
		for (int i = 0; i < count; i++)
			insert(result, seen, natureIds[i]);
		//remove added prerequisites that didn't exist in original list
		seen.clear();
		seen.addAll(Arrays.asList(natureIds));
		for (Iterator it = result.iterator(); it.hasNext();) {
			Object id = it.next();
			if (!seen.contains(id))
				it.remove();
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	public void startup(IProgressMonitor monitor) {
		((Workspace) ResourcesPlugin.getWorkspace()).addLifecycleListener(this);
	}

	/**
	 * Validates the given nature additions in the nature set for this
	 * project.  Tolerates existing inconsistencies in the nature set.
	 * @param newNatures the complete new set of nature IDs for the project, 
	 * 	including additions
	 * @param additions the subset of newNatures that represents natures
	 * 	being added
	 * @return An OK status if all additions are valid, and an error status 
	 * 	if any of the additions introduce new inconsistencies.
	 */
	protected IStatus validateAdditions(HashSet newNatures, HashSet additions, IProject project) {
		Boolean hasLinks = null;//three states: true, false, null (not yet computed)
		//perform checks in order from least expensive to most expensive
		for (Iterator added = additions.iterator(); added.hasNext();) {
			String id = (String) added.next();
			// check for adding a nature that is not available. 
			IProjectNatureDescriptor desc = getNatureDescriptor(id);
			if (desc == null) {
				return failure(NLS.bind(Messages.natures_missingNature, id));
			}
			// check for adding a nature that creates a circular dependency 
			if (((ProjectNatureDescriptor) desc).hasCycle) {
				return failure(NLS.bind(Messages.natures_hasCycle, id));
			}
			// check for adding a nature that has a missing prerequisite. 
			String[] required = desc.getRequiredNatureIds();
			for (int i = 0; i < required.length; i++) {
				if (!newNatures.contains(required[i])) {
					return failure(NLS.bind(Messages.natures_missingPrerequisite, id, required[i]));
				}
			}
			// check for adding a nature that creates a duplicated set member.
			for (Iterator all = newNatures.iterator(); all.hasNext();) {
				String current = (String) all.next();
				if (!current.equals(id)) {
					String overlap = hasSetOverlap(desc, getNatureDescriptor(current));
					if (overlap != null) {
						return failure(NLS.bind(Messages.natures_multipleSetMembers, overlap));
					}
				}
			}
			//check for adding a nature that has linked resource veto
			if (!desc.isLinkingAllowed()) {
				if (hasLinks == null) {
					hasLinks = hasLinks(project) ? Boolean.TRUE : Boolean.FALSE;
				}
				if (hasLinks.booleanValue())
					return failure(NLS.bind(Messages.links_vetoNature, project.getName(), id));
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Validates whether a project with the given set of natures should allow
	 * linked resources.  Returns an OK status if linking is allowed, 
	 * otherwise a non-OK status indicating why linking is not allowed.
	 * Linking is allowed if there is no project nature that explicitly disallows it.
	 * No validation is done on the nature ids themselves (ids that don't have
	 * a corresponding nature definition will be ignored).
	 */
	public IStatus validateLinkCreation(String[] natureIds) {
		for (int i = 0; i < natureIds.length; i++) {
			IProjectNatureDescriptor desc = getNatureDescriptor(natureIds[i]);
			if (desc != null && !desc.isLinkingAllowed()) {
				String msg = NLS.bind(Messages.links_natureVeto, desc.getLabel());
				return new ResourceStatus(IResourceStatus.LINKING_NOT_ALLOWED, msg);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Validates the given nature removals in the nature set for this
	 * project.  Tolerates existing inconsistencies in the nature set.
	 * 
	 * @param newNatures the complete new set of nature IDs for the project, 
	 * 	excluding deletions
	 * @param deletions the nature IDs that are being removed from the set.
	 * @return An OK status if all removals are valid, and a not OK status 
	 * 	if any of the deletions introduce new inconsistencies.
	 */
	protected IStatus validateRemovals(HashSet newNatures, HashSet deletions) {
		//iterate over new nature set, and ensure that none of their prerequisites are being deleted
		for (Iterator it = newNatures.iterator(); it.hasNext();) {
			String currentID = (String) it.next();
			IProjectNatureDescriptor desc = getNatureDescriptor(currentID);
			if (desc != null) {
				String[] required = desc.getRequiredNatureIds();
				for (int i = 0; i < required.length; i++) {
					if (deletions.contains(required[i])) {
						return failure(NLS.bind(Messages.natures_invalidRemoval, required[i], currentID));
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateNatureSet(String[])
	 */
	public IStatus validateNatureSet(String[] natureIds) {
		int count = natureIds.length;
		if (count == 0)
			return Status.OK_STATUS;
		String msg = Messages.natures_invalidSet;
		MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INVALID_NATURE_SET, msg, null);

		//set of the nature ids being validated (String (id))
		HashSet natures = new HashSet(count * 2);
		//set of nature sets for which a member nature has been found (String (id))
		HashSet sets = new HashSet(count);
		for (int i = 0; i < count; i++) {
			String id = natureIds[i];
			ProjectNatureDescriptor desc = (ProjectNatureDescriptor) getNatureDescriptor(id);
			if (desc == null) {
				result.add(failure(NLS.bind(Messages.natures_missingNature, id)));
				continue;
			}
			if (desc.hasCycle)
				result.add(failure(NLS.bind(Messages.natures_hasCycle, id)));
			if (!natures.add(id))
				result.add(failure(NLS.bind(Messages.natures_duplicateNature, id)));
			//validate nature set one-of constraint
			String[] setIds = desc.getNatureSetIds();
			for (int j = 0; j < setIds.length; j++) {
				if (!sets.add(setIds[j]))
					result.add(failure(NLS.bind(Messages.natures_multipleSetMembers, setIds[j])));
			}
		}
		//now walk over the set and ensure all pre-requisite natures are present
		for (int i = 0; i < count; i++) {
			IProjectNatureDescriptor desc = getNatureDescriptor(natureIds[i]);
			if (desc == null)
				continue;
			String[] required = desc.getRequiredNatureIds();
			for (int j = 0; j < required.length; j++)
				if (!natures.contains(required[j]))
					result.add(failure(NLS.bind(Messages.natures_missingPrerequisite, natureIds[i], required[j])));
		}
		//if there are no problems we must return a status whose code is OK
		return result.isOK() ? Status.OK_STATUS : (IStatus) result;
	}
}
