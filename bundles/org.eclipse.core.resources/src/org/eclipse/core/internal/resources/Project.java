package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.properties.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;
import java.io.*;
import java.util.*;

public class Project extends Container implements IProject {
protected Project(IPath path, Workspace container) {
	super(path, container);
}
/**
 * Deletes everything but contents. Needed for restore where we do not find
 * the .prj file. Also used from #delete.
 */
public void basicDelete(MultiStatus status) throws CoreException {
	// get the location first because it will be null after we delete the
	// project from the tree.
	IPath path = getLocation();
	deleteResource(false, status);
	// Delete the contents directory for the project. Note that this is a garbage
	// collection action only, and the directory will not get deleted if it still
	// contains files.
	if (path != null)
		path.toFile().delete();
	workspace.getMetaArea().delete(this);
	clearHistory(null);
}
/*
 * If the creation boolean is true then this method is being called on project creation.
 * Otherwise it is being called via #setDescription. The difference is that we don't allow
 * some description fields to change value after project creation. (e.g. project location)
 */
protected MultiStatus basicSetDescription(ProjectDescription description, boolean creation) {
	MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, 13, "Problems encountered while setting project description", null);
	ProjectDescription current = (ProjectDescription) internalGetDescription();
	current.setComment(description.getComment());
	if (creation)
		current.setLocation(description.getLocation());
	// set the build order before setting the references or the natures
	current.setBuildSpec(description.getBuildSpec(true));

	// set the references before the natures 
	IProject[] oldReferences = current.getReferencedProjects();
	IProject[] newReferences = description.getReferencedProjects();
	if (!Arrays.equals(oldReferences, newReferences)) {
		current.setReferencedProjects(description.getReferencedProjects(true));
		workspace.flushBuildOrder();
	}

	// the natures last as this may cause recursive calls to setDescription.
	// Be careful not to rely on much state because (de)configuring a nature
	// may well result in recursive calls to this method.
	HashSet deletions = new HashSet(Arrays.asList(current.getNatureIds(false)));
	HashSet additions = new HashSet(Arrays.asList(description.getNatureIds(false)));
	additions.removeAll(Arrays.asList(current.getNatureIds(false)));
	deletions.removeAll(Arrays.asList(description.getNatureIds(false)));
	// set the list of nature ids BEFORE (de)configuration so recursive calls will
	// not try to do the same work.
	current.setNatureIds(description.getNatureIds(true));
	for (Iterator i = deletions.iterator(); i.hasNext();)
		deconfigureNature((String) i.next(), result);
	for (Iterator i = additions.iterator(); i.hasNext();)
		configureNature((String) i.next(), result);

	return result;
}
/** 
 * @see IProject#build
 */
public void build(int kind, String builderName, Map args, IProgressMonitor monitor) throws CoreException {
	try {
		workspace.prepareOperation();
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		if (!exists(flags, true) || !isOpen(flags))
			return;

		workspace.beginOperation(true);
		workspace.getBuildManager().build(this, kind, builderName, args, monitor);
		// FIXME: should we catch OperationCanceledExceptions?
	} finally {
		workspace.getWorkManager().avoidAutoBuild();
		workspace.endOperation(false, null);
	}
}
/** 
 * @see IProject
 */
public void build(int trigger, IProgressMonitor monitor) throws CoreException {
	try {
		workspace.prepareOperation();
		ResourceInfo info = getResourceInfo(false, false);
		int flags = getFlags(info);
		if (!exists(flags, true) || !isOpen(flags))
			return;

		workspace.beginOperation(true);
		workspace.getBuildManager().build(this, trigger, monitor);
		// FIXME: should we catch OperationCanceledExceptions?
	} finally {
		workspace.getWorkManager().avoidAutoBuild();
		workspace.endOperation(false, null);
	}
}
/**
 * Checks that this resource is accessible.  Typically this means that it
 * exists.  In the case of projects, they must also be open.
 * If phantom is true, phantom resources are considered.
 *
 * @exception CoreException if this resource is not accessible
 */
public void checkAccessible(int flags) throws CoreException {
	super.checkAccessible(flags);
	if (!isOpen(flags))
		throw new ResourceException(IResourceStatus.PROJECT_NOT_OPEN, getFullPath(), Policy.bind("mustBeOpen", new String[] { getFullPath().toString()}), null);
}
/**
 * Checks validity of the given project description.
 */
protected void checkDescription(IProjectDescription desc) throws CoreException {
	if (desc.getLocation() == null)
		return;
	MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INVALID_VALUE, "Invalid project description.", null);
	status.merge(workspace.validateName(desc.getName(), IResource.PROJECT));
	status.merge(workspace.validateProjectLocation(this, desc.getLocation()));
	if (!status.isOK())
		throw new ResourceException(status);
}
/**
 * @see IProject#close
 */
public void close(IProgressMonitor monitor) throws CoreException {
	close(true, monitor);
}
/**
 * @see #close(IProgressMonitor)
 */
public void close(boolean save, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String msg = Policy.bind("closing.2", new String[] { getFullPath().toString()});
		monitor.beginTask(msg, Policy.totalWork);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			int flags = getFlags(info);
			checkExists(flags, true);
			monitor.subTask(msg);
			if (!isOpen(flags))
				return;
			// Signal that this resource is about to be closed.  Do this at the very 
			// beginning so that people have a chance to do clean up while the 
			// resources still exist.
			// Do this before the begin to prevent lifecycle participants to change the tree.
			workspace.closing(this);

			workspace.beginOperation(true);
			if (save) {
				IProgressMonitor sub = Policy.subMonitorFor(monitor, Policy.opWork / 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
				workspace.getSaveManager().save(ISaveContext.PROJECT_SAVE, this, sub);
			}
			getMarkerManager().removeMarkers(this);
			// If the project has never been saved at this point, delete the 
			// project but leave its contents.
			if (!hasBeenSaved()) {
				delete(false, true, Policy.subMonitorFor(monitor, Policy.opWork / 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
				return;
			}
			// remove each member from the resource tree. 
			// DO NOT use resource.delete() as this will delete it from disk as well.
			IResource[] members = members();
			for (int i = 0; i < members.length; i++) {
				Resource member = (Resource) members[i];
				workspace.deleteResource(member);
			}
			// finally mark the project as closed.
			info = getResourceInfo(false, true);
			info.clear(M_OPEN);
			info.setModificationStamp(IResource.NULL_STAMP);
			info.clearSessionProperties();
			info.setSyncInfo(null);
			monitor.worked(Policy.opWork / 2);
			workspace.flushBuildOrder();
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
protected void configureNature(final String natureID, final MultiStatus status) {
	ISafeRunnable code = new ISafeRunnable() {
		public void run() throws Exception {
			IProjectNature nature = createNature(natureID);
			nature.configure();
			ProjectInfo info = (ProjectInfo) getResourceInfo(false, true);
			info.setNature(natureID, nature);
		}
		public void handleException(Throwable exception) {
			if (exception instanceof CoreException)
				status.add(((CoreException) exception).getStatus());
			else
				status.add(new ResourceStatus(13, getFullPath(), "Error configuring nature: " + natureID, exception));
		}
	};
	Platform.run(code);
}
/**
 * @see IProject#copy
 */
public void copy(IProjectDescription destination, boolean force, IProgressMonitor monitor) throws CoreException {
	Assert.isNotNull(destination);
	internalCopy(destination, force, monitor);
}
/**
 * @see IResource#copy
 */
public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	if (destination.segmentCount() == 1) {
		// copy project to project
		String projectName = destination.segment(0);
		IProjectDescription desc = getDescription();
		desc.setName(projectName);
		desc.setLocation(null);
		internalCopy(desc, force, monitor);
	} else {
		// copy project to folder
		Assert.isLegal(workspace.validatePath(destination.toString(), IResource.FOLDER).isOK());
		internalCopyToFolder(destination, force, monitor);
	}
}
protected void copyMetaArea(IProject source, IProject destination) throws CoreException {
	java.io.File oldMetaArea = workspace.getMetaArea().getLocationFor(source).toFile();
	java.io.File newMetaArea = workspace.getMetaArea().getLocationFor(destination).toFile();
	getLocalManager().getStore().copy(oldMetaArea, newMetaArea, IResource.DEPTH_INFINITE, null);
}
/**
 * @see IProject#create
 */
public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("create", Policy.totalWork);
		checkValidPath(path, PROJECT);
		try {
			workspace.prepareOperation();
			ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
			checkDoesNotExist(getFlags(info), true);
			if (description != null)
				checkDescription(description);

			workspace.beginOperation(true);
			workspace.createResource(this, false);
			info = (ProjectInfo) getResourceInfo(false, true);
			// setup a base description.  This will be updated if the given
			// description is not null.
			ProjectDescription desc = new ProjectDescription();
			desc.setName(getName());
			info.setDescription(desc);
			// setup the actual description.  if the given description is null,
			// go through the motions since setting the description may have
			// some side-effects.  
			MultiStatus status = basicSetDescription(description == null ? desc : (ProjectDescription) description, true);
			// inaccessible projects have a null modification stamp.
			// set this after setting the description as #setDescription
			// updates the stamp
			info.setModificationStamp(IResource.NULL_STAMP);
			try {
				getLocalManager().write(this, Policy.subMonitorFor(monitor, Policy.opWork));
			} catch (CoreException e) {
				workspace.deleteResource(this);
				throw e;
			}
			workspace.getSaveManager().requestSnapshot();
			// if there was a problem during the setting up of the description, report it here
			if (!status.isOK())
				throw new ResourceException(status);
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/**
 * @see IProject#create(IProgressMonitor)
 */
public void create(IProgressMonitor monitor) throws CoreException {
	create(null, monitor);
}
/**
 * Finds the nature extension, and initializes and returns an instance.
 */
protected IProjectNature createNature(String natureID) throws CoreException {
	IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_NATURES, natureID);
	if (extension == null) {
		String message = Policy.bind("natureExtension", new String[] {natureID});
		throw new ResourceException(Platform.PLUGIN_ERROR, getFullPath(), message, null);
	}
	IConfigurationElement[] configs = extension.getConfigurationElements();
	if (configs.length < 1) {
		String message = Policy.bind("natureClass", new String[] {natureID});
		throw new ResourceException(Platform.PLUGIN_ERROR, getFullPath(), message, null);
	}
	IConfigurationElement config = configs[0];
	try {
		IProjectNature nature = (IProjectNature) config.createExecutableExtension("run");
		nature.setProject(this);
		return nature;
	} catch (ClassCastException e) {
		String message = Policy.bind("natureImplement", new String[] {natureID});
		throw new ResourceException(Platform.PLUGIN_ERROR, getFullPath(), message, e);
	}
}
protected void deconfigureNature(final String natureID, final MultiStatus status) {
	final ProjectInfo info = (ProjectInfo) getResourceInfo(false, true);
	IProjectNature existingNature = info.getNature(natureID);
	if (existingNature == null) {
		// if there isn't a nature then create one so we can deconfig it.
		try {
			existingNature = createNature(natureID);
		} catch (CoreException e) {
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
				status.add(new ResourceStatus(13, getFullPath(), "Error deconfiguring nature: " + natureID, exception));
		}
	};
	Platform.run(code);
}
/**
 * @see IResource#delete(boolean, IProgressMonitor)
 */
public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
	delete(isOpen(), force, monitor);
}
/**
 * @see IProject#delete(boolean, boolean, IProgressMonitor)
 */
public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("deleting", new String[] { getFullPath().toString()}), Policy.totalWork);
		try {
			workspace.prepareOperation();
			/* if there is no such resource (including type check) then there is nothing
			   to delete and just return. */
			ResourceInfo info = getResourceInfo(false, false);
			if (!exists(getFlags(info), true))
				return;
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, Policy.bind("deleteProblem", null), null);
			/* Signal that this resource is about to be deleted. 
			   Do this before the begin to prevent lifecycle participants to change the tree. */
			workspace.deleting(this);

			workspace.beginOperation(true);
			if (deleteContent) {
				if (isOpen(getFlags(info)))
					info = null;
				else
					pseudoOpen();
				try {
					if (force)
						getLocalManager().getStore().delete(getLocation().toFile(), status);
					else {
						deleteWithoutForce(status, Policy.monitorFor(null));
						if (!status.isOK())
							throw new ResourceException(status);
					}
				} catch (CoreException e) {
					if (info != null) {
						getPropertyManager().closePropertyStore(this);
						workspace.deleteResource(this);
						workspace.getElementTree().createElement(getFullPath(), info);
					}
					throw e; // rethrow
				}
			}
			basicDelete(status);
			workspace.flushBuildOrder();
			if (!status.isOK())
				throw new ResourceException(status);
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
protected void deleteMetaArea(IProject project) throws CoreException {
	// close project's propertyStore
	getPropertyManager().closePropertyStore(project);
	java.io.File location = workspace.getMetaArea().getLocationFor(project).toFile();
	getLocalManager().getStore().delete(location);
}
private void deleteWithoutForce(MultiStatus status, IProgressMonitor monitor) throws CoreException {
	IResource[] members = members();
	for (int i = 0; i < members.length; i++) {
		IResource child = (IResource) members[i];
		try {
			getLocalManager().delete(child, false, true, false, Policy.monitorFor(null));
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}
}
/**
 * @see IProject
 */
public IProjectDescription getDescription() throws CoreException {
	ResourceInfo info = getResourceInfo(false, false);
	checkAccessible(getFlags(info));
	return (IProjectDescription) ((ProjectInfo) info).getDescription().clone();
}
/**
 * @see IProject#getNature
 */
public IProjectNature getNature(String natureID) throws CoreException {
	// Has it already been initialized?
	ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
	checkAccessible(getFlags(info));
	IProjectNature nature = info.getNature(natureID);
	if (nature == null) {
		// Not initialized yet. Does this project have the nature?
		if (!hasNature(natureID))
			return null;
		nature = createNature(natureID);
		info.setNature(natureID, nature);
	}
	return nature;
}
/**
 * @see IResource#getParent
 */
public IContainer getParent() {
	return workspace.getRoot();
}
/**
 * @see IProject
 */
public IPath getPluginWorkingLocation(IPluginDescriptor plugin) {
	if (!exists())
		return null;
	IPath result = workspace.getMetaArea().getWorkingLocation(this, plugin);
	result.toFile().mkdirs();
	return result;
}
/**
 * @see IResource#getProject
 */
public IProject getProject() {
	return this;
}
/**
 * @see IResource#getProjectRelativePath
 */
public IPath getProjectRelativePath() {
	return Path.EMPTY;
}
/*
 * @see IProject
 */
public IProject[] getReferencedProjects() throws CoreException {
	return ((ProjectDescription) internalGetDescription()).getReferencedProjects(true);
}
/**
 * @see IProject
 */
public IProject[] getReferencingProjects() {
	IProject[] projects = workspace.getRoot().getProjects();
	List result = new ArrayList(projects.length);
	for (int i = 0; i < projects.length; i++) {
		Project project = (Project) projects[i];
		if (!project.isAccessible())
			continue;
		IProject[] references = project.internalGetDescription().getReferencedProjects(false);
		for (int j = 0; j < references.length; j++)
			if (references[j].equals(this)) {
				result.add(projects[i]);
				break;
			}
	}
	return (IProject[]) result.toArray(new IProject[result.size()]);
}
/**
 * @see IResource#getType
 */
public int getType() {
	return PROJECT;
}
/**
 * Returns true if this project has ever been saved to disk before, and
 * false otherwise. 
 */
protected boolean hasBeenSaved() {
	IPath location = workspace.getMetaArea().getDescriptionLocationFor(this);
	return location.toFile().exists();
}
/**
 * @see IProject#hasNature
 */
public boolean hasNature(String natureID) throws CoreException {
	// use #internal method to avoid copy but still throw an
	// exception if the resource doesn't exist.
	IProjectDescription desc = internalGetDescription();
	if (desc == null)
		checkAccessible(NULL_FLAG);
	return desc.hasNature(natureID);
}
/**
 * Increments the content id for this resource.  Note that it is assumed
 * that this resource exists
 */
private void incrementContentId() {
	ResourceInfo info = getResourceInfo(false, true);
	info.incrementContentId();
}
protected void internalCopy(IProjectDescription destDesc, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("copying", new String[] { getFullPath().toString()}), Policy.totalWork);
		try {
			workspace.prepareOperation();
			String destName = destDesc.getName();
			IPath destPath = new Path(destName).makeAbsolute();
			// The following assert method throws CoreExceptions as stated in the IProject.copy API
			// and assert for programming errors. See checkCopyRequirements for more information.
			assertCopyRequirements(destPath, IResource.PROJECT);
			checkDescription(destDesc);
			Project destProject = (Project) workspace.getRoot().getProject(destName);
			IProjectDescription sourceDesc = internalGetDescription();
			workspace.changing(this);

			workspace.beginOperation(true);
			getLocalManager().refresh(this, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

			// close the property store so bogus info is not copied to the destination
			getPropertyManager().closePropertyStore(this);

			// copy the meta area for the project
			copyMetaArea(this, destProject);

			// copy just the project and not its children yet (tree node, properties)
			internalCopyProjectOnly(destProject, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

			// set the description
			destProject.internalSetDescription(destDesc, false);

			// Workaround for 1FW1IFY: ITPCORE:ALL - Builders not fixed up on project rename
			ProjectInfo info = (ProjectInfo) workspace.getResourceInfo(destProject.getFullPath(), false, false);
			info.setBuilders(null);

			// call super.copy for each child
			IResource[] children = members();
			for (int i = 0; i < children.length; i++)
				children[i].copy(destProject.getFullPath().append(children[i].getName()), force, Policy.subMonitorFor(monitor, Policy.opWork * 50 / 100 / children.length));

			// refresh local
			monitor.subTask(Policy.bind("syncTree", null));
			getLocalManager().refresh(destProject, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/*
 * Copies just the project and no children. Does NOT copy the meta area.
 */
protected void internalCopyProjectOnly(IResource destination, IProgressMonitor monitor) throws CoreException {
	// close the property store so bogus values aren't copied to the destination
	getPropertyManager().closePropertyStore(this);
	// copy the tree and properties
	workspace.copyTree(this, destination.getFullPath(), IResource.DEPTH_ZERO, false);
	getPropertyManager().copy(this, destination, IResource.DEPTH_ZERO);
}
protected void internalCopyToFolder(IPath destPath, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("renaming", new String[] { getFullPath().toString()}), Policy.totalWork);
		try {
			workspace.prepareOperation();
			// The following assert method throws CoreExceptions as stated in the IProject.move API
			// and assert for programming errors. See checkCopyRequirements for more information.
			assertCopyRequirements(destPath, IResource.FOLDER);
			IFolder destFolder = workspace.getRoot().getFolder(destPath);
			IProjectDescription description = internalGetDescription();
			workspace.changing(this);

			workspace.beginOperation(true);
			getLocalManager().refresh(this, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

			// copy just the project and not its children
			internalCopyProjectOnly(destFolder, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

			// call super.copy for each child
			IResource[] children = members();
			for (int i = 0; i < children.length; i++)
				children[i].copy(destFolder.getFullPath().append(children[i].getName()), force, Policy.subMonitorFor(monitor, Policy.opWork * 50 / 100 / children.length));

			monitor.subTask(Policy.bind("syncTree", null));
			workspace.flushBuildOrder();
			// refresh local
			getLocalManager().refresh(destFolder, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/**
 * This is an internal helper method. This implementation is different from the API
 * method getDescription(). This one does not check the project accessibility. It exists
 * in order to prevent "chicken and egg" problems in places like the project creation.
 * It may return null.
 */
public ProjectDescription internalGetDescription() {
	ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
	if (info == null)
		return null;
	return info.getDescription();
}
protected void internalMove(IProjectDescription destDesc, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("renaming", new String[] { getFullPath().toString()}), Policy.totalWork);
		try {
			workspace.prepareOperation();
			String destName = destDesc.getName();
			IPath destPath = new Path(destName).makeAbsolute();
			// The following assert method throws CoreExceptions as stated in the IProject.move API
			// and assert for programming errors. See checkMoveRequirements for more information.
			assertMoveRequirements(destPath, IResource.PROJECT);
			checkDescription(destDesc);
			Project destProject = (Project) workspace.getRoot().getProject(destName);
			IProjectDescription sourceDesc = internalGetDescription();
			workspace.changing(this);

			workspace.beginOperation(true);
			getLocalManager().refresh(this, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

			int rollbackLevel = 0;
			try {

				// close the property store
				getPropertyManager().closePropertyStore(this);

				// rename the meta area
				renameMetaArea(this, destProject, force, Policy.subMonitorFor(monitor, Policy.opWork * 5 / 100));
				rollbackLevel++; // 1

				// copy just the project and not its children (copies project tree node, properties)
				internalCopyProjectOnly(destProject, Policy.subMonitorFor(monitor, Policy.opWork * 5 / 100));
				rollbackLevel++; // 2

				// set the description
				destProject.internalSetDescription(destDesc, false);

				// Fix for 1FVU2FV: ITPCORE:WINNT - WALKBACK - Renaming project does not update project natures
				// Remove session property for active project natures, causing them to be reactivated.
				// Leave persistent property (list of nature IDs) alone.
				ProjectInfo info = (ProjectInfo) workspace.getResourceInfo(destProject.getFullPath(), false, true);
				// FIXME: should we be deconfiguring natures here? why do we clear it at all
				info.clearNatures();

				// Workaround for 1FW1IFY: ITPCORE:ALL - Builders not fixed up on project rename
				workspace.getBuildManager().renamedProject(destProject);
				rollbackLevel++; // 3

				// call super.move for each child
				IResource[] children = members();
				for (int i = 0; i < children.length; i++)
					children[i].move(destProject.getFullPath().append(children[i].getName()), force, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
				rollbackLevel++; // 4

				// delete the source
				delete(true, force, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
				rollbackLevel++; // 5

				// tell the marker manager that we moved so the marker deltas are ok
				monitor.subTask("Creating marker deltas.");
				getMarkerManager().moved(this, destProject, IResource.DEPTH_ZERO);
				monitor.worked(Policy.opWork * 10 / 100);

			} catch (CoreException e) {
				switch (rollbackLevel) {
					case 5 :
						// try and move everything back to the source
					case 4 :
						// project member deletion should be taken care of by super.move
					case 3:
						// ensure the builders reference the correct projects
						workspace.getBuildManager().renamedProject(this);
					case 2 :
						// rename the meta area back to the original
						try {
							renameMetaArea(destProject, this, force, null);
						} catch (CoreException e2) {
							// ignore this exception and rethrow the one that got us here
						}
					case 1 :
						// delete the destination
						try {
							destProject.delete(true, force, null);
						} catch (CoreException e2) {
							// ignore this exception and rethrow the one that got us here
						}
						break;
				}
				throw e;
			}

			monitor.subTask(Policy.bind("syncTree", null));
			workspace.flushBuildOrder();
			// refresh local
			getLocalManager().refresh(destProject, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
protected void internalMoveToFolder(IPath destPath, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("renaming", new String[] { getFullPath().toString()}), Policy.totalWork);
		try {
			workspace.prepareOperation();
			// The following assert method throws CoreExceptions as stated in the IProject.move API
			// and assert for programming errors. See checkMoveRequirements for more information.
			assertMoveRequirements(destPath, IResource.FOLDER);
			IFolder destFolder = workspace.getRoot().getFolder(destPath);
			IProjectDescription description = internalGetDescription();
			workspace.changing(this);

			workspace.beginOperation(true);
			getLocalManager().refresh(this, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

			int rollbackLevel = 0;
			try {
				// copy just the project and not its children
				internalCopyProjectOnly(destFolder, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
				rollbackLevel++; // 1

				// call super.move for each child
				IResource[] children = members();
				for (int i = 0; i < children.length; i++)
					children[i].move(destFolder.getFullPath().append(children[i].getName()), force, Policy.subMonitorFor(monitor, Policy.opWork * 30 / 100 / children.length));
				rollbackLevel++; // 2

				// delete the source
				delete(true, force, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
				rollbackLevel++; // 3

				// tell the marker manager that we moved so the marker deltas are ok
				monitor.subTask("Creating marker deltas.");
				getMarkerManager().moved(this, destFolder, IResource.DEPTH_ZERO);
				monitor.worked(Policy.opWork * 10 / 100);

			} catch (CoreException e) {
				switch (rollbackLevel) {
					case 3 :
						// re-create the source
					case 2 :
						// rollback for the children should be handled by super.move
					case 1 :
						// delete the destination
						try {
							destFolder.delete(force, null);
						} catch (CoreException e2) {
							// ignore the exception and re-throw the one that got us here
						}
						break;
				}
				throw e;
			}

			monitor.subTask(Policy.bind("syncTree", null));
			workspace.flushBuildOrder();
			// refresh local
			getLocalManager().refresh(destFolder, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/**
 * Sets this project's description to the given value.  This is the body of the
 * corresponding API method but is needed separately since it is used
 * during workspace restore (i.e., when you cannot do an operation)
 */
void internalSetDescription(IProjectDescription value, boolean incrementContentId) throws CoreException {
	ResourceInfo info = getResourceInfo(false, true);
	((ProjectInfo) info).setDescription((ProjectDescription) value);
	if (incrementContentId) {
		info.incrementContentId();
		workspace.updateModificationStamp(info);
		workspace.getSaveManager().requestSnapshot();
	}
}
public void internalSetLocal(boolean flag, int depth) throws CoreException {
	// do nothing for projects, but call for its children
	if (depth == IResource.DEPTH_ZERO)
		return;
	if (depth == IResource.DEPTH_ONE)
		depth = IResource.DEPTH_ZERO;
	// get the children via the workspace since we know that this
	// resource exists (it is local).
	IResource[] children = getChildren(this, false);
	for (int i = 0; i < children.length; i++)
		 ((Resource) children[i]).internalSetLocal(flag, depth);
}
/**
 * @see IResource#isAccessible
 */
public boolean isAccessible() {
	return isOpen();
}
/**
 * @see IResource#isLocal
 */
public boolean isLocal(int depth) {
	// the flags parm is ignored for projects so pass anything
	return isLocal(-1, depth);
}
/**
 * @see IResource#isLocal
 */
public boolean isLocal(int flags, int depth) {
	// don't check the flags....projects are always local
	if (depth == DEPTH_ZERO)
		return true;
	if (depth == DEPTH_ONE)
		depth = DEPTH_ZERO;
	// get the children via the workspace since we know that this
	// resource exists (it is local).
	IResource[] children = getChildren(this, false);
	for (int i = 0; i < children.length; i++)
		if (!children[i].isLocal(depth))
			return false;
	return true;
}
/**
 * @see IProject
 */
public boolean isOpen() {
	ResourceInfo info = getResourceInfo(false, false);
	return isOpen(getFlags(info));
}
/**
 * @see IProject
 */
public boolean isOpen(int flags) {
	return flags != NULL_FLAG && ResourceInfo.isSet(flags, M_OPEN);
}
/**
 * @see IProject#move
 */
public void move(IProjectDescription destination, boolean force, IProgressMonitor monitor) throws CoreException {
	Assert.isNotNull(destination);
	internalMove(destination, force, monitor);
}
/**
 * @see IResource#move
 */
public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	if (destination.segmentCount() == 1) {
		// move project to project
		String projectName = destination.segment(0);
		IProjectDescription desc = getDescription();
		desc.setName(projectName);
		desc.setLocation(null);
		internalMove(desc, force, monitor);
	} else {
		// move project to folder
		internalMoveToFolder(destination, force, monitor);
	}
}
/**
 * @see IProject
 */
public void open(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String msg = Policy.bind("opening.2", new String[] { getFullPath().toString()});
		monitor.beginTask(msg, Policy.totalWork);
		monitor.subTask(msg);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			int flags = getFlags(info);
			checkExists(flags, true);
			if (isOpen(flags))
				return;

			workspace.beginOperation(true);
			info = getResourceInfo(false, true); // get a mutable info
			info.set(M_OPEN);
			// the M_USED flag is used to indicate the difference between opening a project
			// for the first time and opening it from a previous close (restoring it from disk)
			if (info.isSet(M_USED))
				workspace.getSaveManager().restore(this, Policy.subMonitorFor(monitor, Policy.opWork * 30 / 100));
			else {
				info.set(M_USED);
				workspace.updateModificationStamp(info);
			}
			startup();
			monitor.worked(Policy.opWork * 20 / 100);
			refreshLocal(DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 50 / 100));
			workspace.flushBuildOrder();
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
// used in delete

// In this particular case, the project is closed but 
// we do want to delete its contents. The force flag
// is false what means that we should only delete
// resources that are in sync with the filesystem.
// So, we have to have the tree loaded.

private void pseudoOpen() throws CoreException {
	getResourceInfo(false, true).set(M_OPEN);
	workspace.getSaveManager().restore(this, null);
}

protected void renameMetaArea(IProject source, IProject destination, boolean force, IProgressMonitor monitor) throws CoreException {
	java.io.File oldMetaArea = workspace.getMetaArea().getLocationFor(source).toFile();
	java.io.File newMetaArea = workspace.getMetaArea().getLocationFor(destination).toFile();
	try {
		((Project) source).getLocalManager().getStore().move(oldMetaArea, newMetaArea, force, monitor);
	} catch (CoreException e) {
		String message = Policy.bind("renameMeta", new String[] { getFullPath().toString()});
		throw new ResourceException(IResourceStatus.ERROR, getFullPath(), message, e);
	}
}
/**
 * @see IProject
 */
public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("Setting description", Policy.totalWork);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));
			workspace.beginOperation(true);
			MultiStatus status = basicSetDescription((ProjectDescription) description, false);
			info = getResourceInfo(false, true);
			info.incrementContentId();
			workspace.updateModificationStamp(info);
			workspace.getSaveManager().requestSnapshot();
			if (!status.isOK())
				throw new CoreException(status);
		} finally {
			workspace.endOperation(true, monitor);
		}
	} finally {
		monitor.done();
	}
}
/**
 * Restore the non-persisted state for the project.  For example, read and set 
 * the description from the local meta area.  Also, open the property store etc.
 * This method is used when an open project is restored and so emulates
 * the behaviour of open().
 */
protected void startup() throws CoreException {
	if (!isOpen())
		return;
	workspace.opening(this);
}
/**
 * @see IResource
 */
public void touch(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("Touching", Policy.totalWork);
		try {
			workspace.prepareOperation();
			workspace.changing(this);

			workspace.beginOperation(true);
			super.touch(Policy.subMonitorFor(monitor, Policy.opWork));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
}
