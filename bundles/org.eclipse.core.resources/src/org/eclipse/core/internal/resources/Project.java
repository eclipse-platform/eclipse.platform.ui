package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	/**
	 * Used to ensure we don't read the description immediately after writing it.
	 */
	private boolean isWritingDescription = false;
	
protected Project(IPath path, Workspace container) {
	super(path, container);
}

/*
 * If the creation boolean is true then this method is being called on project creation.
 * Otherwise it is being called via #setDescription. The difference is that we don't allow
 * some description fields to change value after project creation. (e.g. project location)
 */
protected MultiStatus basicSetDescription(ProjectDescription description) {
	String message = Policy.bind("resources.projectDesc");
	MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_WRITE_METADATA, message, null);
	ProjectDescription current = (ProjectDescription) internalGetDescription();
	current.setComment(description.getComment());
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
	workspace.getNatureManager().configureNatures(this, current, description, result);
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
	if (!isOpen(flags)) {
		String message = Policy.bind("resources.mustBeOpen", getFullPath().toString());
		throw new ResourceException(IResourceStatus.PROJECT_NOT_OPEN, getFullPath(), message, null);
	}
}
/**
 * Checks validity of the given project description.
 */
protected void checkDescription(IProject project, IProjectDescription desc, boolean moving) throws CoreException {
	IPath location = desc.getLocation();
	if (location == null)
		return;
	String message = Policy.bind("resources.invalidProjDesc");
	MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INVALID_VALUE, message, null);
	status.merge(workspace.validateName(desc.getName(), IResource.PROJECT));
	if (moving) {
		// if we got here from a move call then we should check the location in the description since
		// its possible that we want to do a rename without moving the contents. (and we shouldn't
		// throw an Overlapping mapping exception in this case) So if the source description's location
		// is null (we are using the default) or if the locations aren't equal, then validate the location
		// of the new description. Otherwise both locations aren't null and they are equal so ignore validation.
		IProjectDescription sourceDesc = internalGetDescription();
		if (sourceDesc.getLocation() == null || !locationsEqual(sourceDesc, desc))
			status.merge(workspace.validateProjectLocation(project, location));
	} else
		// otherwise continue on like before
		status.merge(workspace.validateProjectLocation(project, location));
	if (!status.isOK())
		throw new ResourceException(status);
}
/**
 * @see IProject#close
 */
public void close(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String msg = Policy.bind("resources.closing.1", getFullPath().toString());
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
			// beginning so that infrastructure pieces have a chance to do clean up 
			// while the resources still exist.
			// Do this before the begin to prevent lifecycle participants to change the tree.
			workspace.closing(this);
			workspace.beginOperation(true);
			// flush the build order early in case there is a problem
			workspace.flushBuildOrder();
			IProgressMonitor sub = Policy.subMonitorFor(monitor, Policy.opWork / 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
			IStatus saveStatus = workspace.getSaveManager().save(ISaveContext.PROJECT_SAVE, this, sub);
			internalClose();
			monitor.worked(Policy.opWork / 2);
			if (saveStatus != null && !saveStatus.isOK())
				throw new ResourceException(saveStatus);
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
 * @see IProject#copy
 */
public void copy(IProjectDescription destination, boolean force, IProgressMonitor monitor) throws CoreException {
	// FIXME - the logic here for copying projects needs to be moved to Resource.copy
	//   so that IResource.copy(IProjectDescription,int,IProgressMonitor) works properly for
	//   projects and honours all update flags
	Assert.isNotNull(destination);
	internalCopy(destination, force, monitor);
}
public void copy(IProjectDescription destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	boolean force = (updateFlags | IResource.FORCE) != 0;
	copy(destination, force, monitor);
}
/**
 * @see IResource#copy
 */
public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	// FIXME - the logic here for copying projects needs to be moved to Resource.copy
	//   so that IResource.copy(IPath,int,IProgressMonitor) works properly for
	//   projects and honours all update flags
	monitor = Policy.monitorFor(monitor);
	if (destination.segmentCount() == 1) {
		// copy project to project
		String projectName = destination.segment(0);
		IProjectDescription desc = getDescription();
		desc.setName(projectName);
		desc.setLocation(null);
		internalCopy(desc, force, monitor);
	} else {
		// will fail since we're trying to copy a project to a non-project
		checkCopyRequirements(destination, IResource.PROJECT);
	}
}
public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	boolean force = (updateFlags | IResource.FORCE) != 0;
	copy(destination, force, monitor);
}
protected void copyMetaArea(IProject source, IProject destination, IProgressMonitor monitor) throws CoreException {
	java.io.File oldMetaArea = workspace.getMetaArea().locationFor(source).toFile();
	java.io.File newMetaArea = workspace.getMetaArea().locationFor(destination).toFile();
	getLocalManager().getStore().copy(oldMetaArea, newMetaArea, IResource.DEPTH_INFINITE, monitor);
}
/**
 * @see IProject#create
 */
public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("resources.create"), Policy.totalWork);
		checkValidPath(path, PROJECT);
		try {
			workspace.prepareOperation();
			checkDoesNotExist();
			if (description != null)
				checkDescription(this, description, false);
			workspace.beginOperation(true);
			workspace.createResource(this, false);
			workspace.getMetaArea().create(this);
			ProjectInfo info = (ProjectInfo) getResourceInfo(false, true);
	
			// setup description to obtain project location
			ProjectDescription desc;
			if (description == null) {
				desc = new ProjectDescription();
			} else {
				desc = (ProjectDescription)((ProjectDescription)description).clone();
			}
			desc.setName(getName());
			info.setDescription(desc);
			//look for a description on disk
			try {
				if (getLocalManager().hasSavedProject(this)) {
					updateDescription();
					//make sure the .location file is written
					workspace.getMetaArea().writeLocation(this);
				} else {
					//write out the project
					writeDescription(IResource.FORCE);
				}
			} catch (CoreException e) {
				workspace.deleteResource(this);
				throw e;
			}
			// inaccessible projects have a null modification stamp.
			// set this after setting the description as #setDescription
			// updates the stamp
			info.setModificationStamp(IResource.NULL_STAMP);
			workspace.getSaveManager().requestSnapshot();
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
 * @see IResource#delete(boolean, IProgressMonitor)
 */
public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	delete(updateFlags, monitor);
}

/**
 * @see IProject#delete(boolean, boolean, IProgressMonitor)
 */
public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= deleteContent ? IResource.ALWAYS_DELETE_PROJECT_CONTENT : IResource.NEVER_DELETE_PROJECT_CONTENT;
	delete(updateFlags, monitor);
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
		nature = workspace.getNatureManager().createNature(this, natureID);
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
 * @see IProject#hasNature
 */
public boolean hasNature(String natureID) throws CoreException {
	checkAccessible(getFlags(getResourceInfo(false, false)));
	// use #internal method to avoid copy but still throw an
	// exception if the resource doesn't exist.
	IProjectDescription desc = internalGetDescription();
	if (desc == null)
		checkAccessible(NULL_FLAG);
	return desc.hasNature(natureID);
}
protected void internalCopy(IProjectDescription destDesc, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.copying", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
		try {
			workspace.prepareOperation();
			String destName = destDesc.getName();
			IPath destPath = new Path(destName).makeAbsolute();
			// The following assert method throws CoreExceptions as stated in the IProject.copy API
			// and assert for programming errors. See checkCopyRequirements for more information.
			assertCopyRequirements(destPath, IResource.PROJECT);
			Project destProject = (Project) workspace.getRoot().getProject(destName);
			checkDescription(destProject, destDesc, false);
			workspace.changing(this);

			workspace.beginOperation(true);
			getLocalManager().refresh(this, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

			// close the property store so incorrect info is not copied to the destination
			getPropertyManager().closePropertyStore(this);

			// copy the meta area for the project
			copyMetaArea(this, destProject, Policy.subMonitorFor(monitor, Policy.opWork * 5 / 100));

			// copy just the project and not its children yet (tree node, properties)
			internalCopyProjectOnly(destProject, Policy.subMonitorFor(monitor, Policy.opWork * 5 / 100));

			// set the description
			destProject.internalSetDescription(destDesc, false);
			// call super.copy for each child
			IResource[] children = members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
			for (int i = 0; i < children.length; i++)
				children[i].copy(destProject.getFullPath().append(children[i].getName()), force, Policy.subMonitorFor(monitor, Policy.opWork * 50 / 100 / children.length));

			// write out the new project description to the meta area. This will ovewrite 
			//the .project file that was copied during the recursive copy in the previous step
			try {
				destProject.writeDescription(IResource.FORCE);
			} catch (CoreException e) {
				try {
					destProject.delete(force, null);
				} catch (CoreException e2) {
					// ignore and rethrow the exception that got us here
				}
				throw e;
			}
			monitor.worked(Policy.opWork * 10 / 100);

			// refresh local
			monitor.subTask(Policy.bind("resources.updating"));
			getLocalManager().refresh(destProject, DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
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
	workspace.copyTree(this, destination.getFullPath(), IResource.DEPTH_ZERO, false, false);
	getPropertyManager().copy(this, destination, IResource.DEPTH_ZERO);
	
	//clear instantiated builders and natures because they reference the project handle
	ProjectInfo info = (ProjectInfo) ((Resource)destination).getResourceInfo(false, true);
	info.setBuilders(null);
	info.clearNatures();
	
	//clear session properties and markers for the new project, because they shouldn't be copied.
	info.setMarkers(null);
	info.clearSessionProperties();
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
		//if the project is not accessible, stamp will be null and should remain null
		if (info.getModificationStamp() != NULL_STAMP)
			workspace.updateModificationStamp(info);
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
 * @see IProject#isNatureEnabled(String)
 */
public boolean isNatureEnabled(String natureId) throws CoreException {
	checkAccessible(getFlags(getResourceInfo(false, false)));
	return workspace.getNatureManager().isNatureEnabled(this, natureId);
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
	move(destination, force ? IResource.FORCE : IResource.NONE, monitor);
}
protected boolean locationsEqual(IProjectDescription desc1, IProjectDescription desc2) {
	IPath loc1 = desc1.getLocation();
	IPath loc2 = desc2.getLocation();
	if (loc1 == null && loc2 == null)
		return true;
	if (loc1 == null || loc2 == null)
		return false;
	return loc1.equals(loc2);
}
/**
 * @see IResource#move
 */
public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	move(destination, force ? IResource.FORCE : IResource.NONE, monitor);
}
/**
 * @see IProject
 */
public void open(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String msg = Policy.bind("resources.opening.1", getFullPath().toString());
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
			// flush the build order early in case there is a problem
			workspace.flushBuildOrder();
			info = getResourceInfo(false, true);
			info.set(M_OPEN);
			// the M_USED flag is used to indicate the difference between opening a project
			// for the first time and opening it from a previous close (restoring it from disk)
			if (info.isSet(M_USED)) {
				workspace.getSaveManager().restore(this, Policy.subMonitorFor(monitor, Policy.opWork * 30 / 100));
			} else {
				info.set(M_USED);
				workspace.updateModificationStamp(info);
			}
			startup();
			monitor.worked(Policy.opWork * 20 / 100);
			refreshLocal(DEPTH_INFINITE, Policy.subMonitorFor(monitor, Policy.opWork * 50 / 100));
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
 * @see IProject
 */
public void setDescription(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
	// FIXME - update flags should be honoured:
	//    KEEP_HISTORY means capture .project file in local history
	//    FORCE means overwrite any existing .project file 
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("resources.setDesc"), Policy.totalWork);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));
			//If we're out of sync and !FORCE, then fail.
			//If the file is missing, we want to write the new description then throw an exception.
			boolean hadSavedDescription = true;
			if (((updateFlags & IResource.FORCE) == 0)) {
				hadSavedDescription = getLocalManager().hasSavedProject(this);
				if (hadSavedDescription && !getLocalManager().isDescriptionSynchronized(this)) {
					String message = Policy.bind("resources.projectDescSync", getName());
					throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, getFullPath(), message, null);
				}
			}
			workspace.beginOperation(true);
			workspace.changing(this);
			writeDescription(description, updateFlags);
			MultiStatus status = basicSetDescription((ProjectDescription) description);
			info = getResourceInfo(false, true);
			info.incrementContentId();
			workspace.updateModificationStamp(info);
			if (!hadSavedDescription) {
				String msg = Policy.bind("resources.missingProjectMetaRepaired", getName());
				status.merge(new ResourceStatus(IResourceStatus.MISSING_DESCRIPTION_REPAIRED, getFullPath(), msg));
			}
			if (!status.isOK())
				throw new CoreException(status);
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/**
 * @see IProject
 */
public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
	// funnel all operations to central method
	setDescription(description, IResource.KEEP_HISTORY, monitor);
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
		String message = Policy.bind("resource.touch", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
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
/**
 * The project description file on disk is better than the description in memory.  
 * Make sure the project description in memory is synchronized with the 
 * description file contents.
 */
protected void updateDescription() throws CoreException {
	if (isWritingDescription)
		return;
	workspace.changing(this);
	ProjectDescription description = getLocalManager().read(this, false);
	internalSetDescription(description, true);
}
/**
 * Writes the project description file to disk.  This is the only method
 * that should ever be writing the description, because it ensures that
 * the description isn't then immediately discovered as an incoming
 * change and read back from disk.
 */
public void writeDescription(int updateFlags) throws CoreException {
	writeDescription(internalGetDescription(), updateFlags);
}
/**
 * Writes the project description file to disk.  This is the only method
 * that should ever be writing the description, because it ensures that
 * the description isn't then immediately discovered as an incoming
 * change and read back from disk.
 */
public void writeDescription(IProjectDescription description, int updateFlags) throws CoreException {
	isWritingDescription = true;
	try {
		getLocalManager().internalWrite(this, description, updateFlags);
	} finally {
		isWritingDescription = false;
	}
}
protected void renameMetaArea(IProject source, IProject destination, IProgressMonitor monitor) throws CoreException {
	java.io.File oldMetaArea = workspace.getMetaArea().locationFor(source).toFile();
	java.io.File newMetaArea = workspace.getMetaArea().locationFor(destination).toFile();
	getLocalManager().getStore().move(oldMetaArea, newMetaArea, false, monitor);
}
/**
 * Closes the project.  This is called during restore when there is a failure
 * to read the project description.  Since it is called during workspace restore,
 * it cannot start any operations.
 */
protected void internalClose() throws CoreException {
	workspace.flushBuildOrder();
	getMarkerManager().removeMarkers(this, IResource.DEPTH_INFINITE);
	// remove each member from the resource tree. 
	// DO NOT use resource.delete() as this will delete it from disk as well.
	IResource[] members = members(IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	for (int i = 0; i < members.length; i++) {
		Resource member = (Resource) members[i];
		workspace.deleteResource(member);
	}
	// finally mark the project as closed.
	ResourceInfo info = getResourceInfo(false, true);
	info.clear(M_OPEN);
	info.clearSessionProperties();
	info.setModificationStamp(IResource.NULL_STAMP);
	info.setSyncInfo(null);
}
}