package org.eclipse.core.internal.resources;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.eclipse.core.internal.resources.ModelObjectReader;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.utils.Convert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
/**
 * 
 */
public class ProjectImporterExporter {
protected Workspace workspace;
	/**
	 * Singleton buffer created to prevent buffer creations in the
	 * transferStreams method.  Used an optimization, based on the
	 * assumption that multiple writes won't happen in a given
	 * instance of ProjectImporterExporter.
	 */
	private final byte[] buffer = new byte[8192];

	/** ids to identify files when importing */
	protected static final int PROJECT_DESCRIPTION	 	= 0;
	protected static final int SYNCINFO 				= 1;
	protected static final int MARKERS	 				= 2;
	protected static final int TREE		 				= 3;
	protected static final int RESOURCES				= 9;

	/** folder names */
	protected static final String RESOURCES_FOLDER = RESOURCES + "/";

public ProjectImporterExporter(Workspace workspace) {
	this.workspace = workspace;
}
/**
 * @see IWorkspaceRoot
 */
public void importProjects(IPath location, InputStream target, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		// FIXME: add message to catalog
		String title = Policy.bind("localstore.importingProject");
		monitor.beginTask(title, 15);
		try {
			java.io.File tempFile = createTempFile(target);
			ZipFile zipFile = new ZipFile(tempFile);
			try {
				IProjectDescription realDescription = extractDescription(zipFile);
				realDescription.setLocation(location);

				// We have to create and open the project first with a
				// basic description and the location. It is done because configuring a
				// nature of projects like a JavaProject tries to perform actions only
				// available for open projects.
				IProjectDescription basicDescription = workspace.newProjectDescription(realDescription.getName());
				basicDescription.setLocation(location);
				IProject project = workspace.getRoot().getProject(realDescription.getName());
				project.create(basicDescription, Policy.subMonitorFor(monitor, 1));
				project.open(Policy.subMonitorFor(monitor, 1));

				importTree(project, zipFile, null, Policy.subMonitorFor(monitor, 1));
				importMarkers(project, zipFile, null, Policy.subMonitorFor(monitor, 1));
				importSyncInfo(project, zipFile, null, Policy.subMonitorFor(monitor, 1));
				importResources(project, zipFile, Policy.subMonitorFor(monitor, 10));

				// set the real description
				((Project) project).internalSetDescription(realDescription, true);
			} finally {
				zipFile.close();
				tempFile.delete();
			}	
		} catch (IOException e) {
			// FIXME: add message to catalog
			String message = Policy.bind("localstore.problemImportingProject");
			throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, e);
		}
	} finally {
		monitor.done();
	}
}
/**
 * @see IWorkspaceRoot
 */
public void exportProjects(IProject[] projects, OutputStream target, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		// FIXME: add message to catalog
		String title = Policy.bind("localstore.exportingProjects");
		monitor.beginTask(title, 4);
		workspace.refreshLocal(projects, IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 2));
		try {
			ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(target));
			try {
				for (int i = 0; i < projects.length; i++) {
					IProject project = projects[i];
					exportMetadata(project, output, Policy.subMonitorFor(monitor, 1));
					exportResources(project, output, Policy.subMonitorFor(monitor, 1));
				}
			} finally {
				output.close();
			}
		} catch (IOException e) {
			// FIXME: add message to catalog
			String message = Policy.bind("localstore.problemExportingProjects");
			throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, e);
		}
	} finally {
		monitor.done();
	}
}
/**
 * 
 */
protected void exportMetadata(IProject project, ZipOutputStream output, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 4);
		IProjectDescription description = project.getDescription();
		exportDescription(description, output, Policy.subMonitorFor(monitor, 1));
		exportSyncInfo(project, output, Policy.subMonitorFor(monitor, 1));
		exportMarkers(project, output, Policy.subMonitorFor(monitor, 1));
		exportTree(project, output, Policy.subMonitorFor(monitor, 1));
	} finally {
		monitor.done();
	}
}
/**
 * 
 */
protected void exportResources(final IProject project, final ZipOutputStream output, IProgressMonitor monitor) throws CoreException, IOException {
	final IProgressMonitor subMonitor = Policy.monitorFor(monitor);
	try {
		int totalWork = ((Resource) project).countResources(IResource.DEPTH_INFINITE, false);
		subMonitor.beginTask("", totalWork + 1);
		
		final StringBuffer name = new StringBuffer(RESOURCES_FOLDER);
		IResourceVisitor exporter = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				try {
					name.setLength(RESOURCES_FOLDER.length());
					name.append(resource.getProjectRelativePath());
					if (resource.getType() == IResource.FILE) {
						ZipEntry target = new ZipEntry(name.toString());
						ResourceInfo info = ((Resource) resource).getResourceInfo(false, false);
						byte[] lastModified = Convert.longToBytes(info.getLocalSyncInfo());
						writeEntry(target, ((IFile) resource).getContents(), lastModified, output);
						return false;
					} else { // FOLDER
						name.append('/');
						ZipEntry target = new ZipEntry(name.toString());
						writeEntry(target, null, null, output);
						return true;
					}
				} catch (IOException e) {
					// FIXME: add message to catalog
					String message = Policy.bind("localstore.problemExportingProject", project.getFullPath().toString());
					throw new ResourceException(IResourceStatus.OPERATION_FAILED, project.getFullPath(), message, e);
				} finally {
					subMonitor.worked(1);
				}
			}
		};
		
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++)
			members[i].accept(exporter);
	} finally {
		monitor.done();
	}
}
/**
 * 
 */
protected void writeEntry(ZipEntry entry, InputStream contents, byte[] extraData, ZipOutputStream output) throws IOException {
	if (extraData != null)
		entry.setExtra(extraData);
	output.putNextEntry(entry);
	if (contents != null)
		transferStreams(contents, output);
	output.closeEntry();
}
/**
 * The source stream IS closed but the destination stream is NOT. It allows us to have more
 * flexibility when reusing this method.
 */
protected void transferStreams(InputStream source, OutputStream destination) throws IOException {
	try {
		/*
		 * Note: although synchronizing on the buffer is thread-safe,
		 * it may result in slower performance in the future if we want 
		 * to allow concurrent writes.
		 */
		synchronized (buffer) {
			int bytesRead;
			while ((bytesRead = source.read(buffer)) != -1)
				destination.write(buffer, 0, bytesRead);
		}
	} finally {
		try {
			source.close();
		} catch (IOException e) {
		}
	}
}
/**
 *  
 */
protected java.io.File createTempFile(InputStream contents) throws IOException {
	java.io.File target = getTempFile();
	FileOutputStream output = new FileOutputStream(target);
	try {
		transferStreams(contents, output);
	} finally {
		output.close();
	}
	return target;
}
protected java.io.File getTempFile() { 
	String name = "_temp_" + System.currentTimeMillis();
	java.io.File temp = ResourcesPlugin.getPlugin().getStateLocation().append(name).toFile();
	if (temp.exists())
		return getTempFile();
	return temp;
}
protected IProjectDescription extractDescription(ZipFile zipFile) throws IOException {
	ZipEntry entry = zipFile.getEntry(String.valueOf(PROJECT_DESCRIPTION));
	if (entry == null) {
		// FIXME: log the problem
		return workspace.newProjectDescription(getTempProjectName());
	}
	return (IProjectDescription) new ModelObjectReader().read(zipFile.getInputStream(entry));
}
protected String getTempProjectName() {
	String name = "_temp_" + System.currentTimeMillis();
	IProject project = workspace.getRoot().getProject(name);
	if (project.exists())
		return getTempProjectName();
	return name;
}
protected void importResource(IProject project, ZipFile zipFile, ZipEntry entry, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		String name = entry.getName().substring(RESOURCES_FOLDER.length());
		if (entry.isDirectory()) {
			IFolder folder = project.getFolder(name);
			folder.create(false, true, Policy.subMonitorFor(monitor, 1));
		} else { // FILE
			IFile file = project.getFile(name);
			file.create(zipFile.getInputStream(entry), false, Policy.subMonitorFor(monitor, 1));
		}
	} finally {
		monitor.done();
	}
}
/**
 * 
 */
protected void exportDescription(IProjectDescription description, ZipOutputStream output, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		ZipEntry entry = new ZipEntry(String.valueOf(PROJECT_DESCRIPTION));
		output.putNextEntry(entry);
		// the location should not be hardcoded
		description.setLocation(null);
		new ModelObjectWriter().write(description, output);
		output.closeEntry();
		monitor.worked(1);
	} finally {
		monitor.done();
	}
}
/**
 * Copied from SaveManager. Should refactor both to get a better reusability.
 */
protected void exportSyncInfo(final IProject project, ZipOutputStream output, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		ZipEntry entry = new ZipEntry(String.valueOf(SYNCINFO));
		output.putNextEntry(entry);

		final Synchronizer synchronizer = (Synchronizer) workspace.getSynchronizer();
		final List writtenPartners = new ArrayList(synchronizer.registry.size());
		final DataOutputStream syncInfoOutput = new DataOutputStream(output);
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				try {
					synchronizer.saveSyncInfo(resource, syncInfoOutput, writtenPartners);
					return true;
				} catch (IOException e) {
					// FIXME: add message to catalog
					String message = Policy.bind("localstore.problemExportingProject", project.getFullPath().toString());
					throw new ResourceException(IResourceStatus.OPERATION_FAILED, project.getFullPath(), message, e);
				}
			}
		};
		project.accept(visitor, IResource.DEPTH_INFINITE, true);

		output.closeEntry();
		monitor.worked(1);
	} finally {
		monitor.done();
	}
}
/**
 * Copied from SaveManager. Should refactor both to get a better reusability.
 */
protected void exportMarkers(final IProject project, ZipOutputStream output, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		ZipEntry entry = new ZipEntry(String.valueOf(MARKERS));
		output.putNextEntry(entry);

		final MarkerManager markerManager = workspace.getMarkerManager();
		final List writtenTypes = new ArrayList(5);
		final DataOutputStream markersOutput = new DataOutputStream(output);
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				try {
					markerManager.save(resource, markersOutput, writtenTypes);
					return true;
				} catch (IOException e) {
					// FIXME: add message to catalog
					String message = Policy.bind("localstore.problemExportingProject", project.getFullPath().toString());
					throw new ResourceException(IResourceStatus.OPERATION_FAILED, project.getFullPath(), message, e);
				}
			}
		};
		project.accept(visitor); // phantoms are not included

		output.closeEntry();
		monitor.worked(1);
	} finally {
		monitor.done();
	}
}
/**
 * Extract every entry of the zip file, delegating it to the appropriate methods (metadata or
 * resources).
 */
protected void extractEntries(IProject project, ZipFile zipFile, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", zipFile.size());
		
		// FIXME: should be more fault tolerant and try to import as much as we can
		
		for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			switch (Integer.parseInt(entry.getName().substring(0, 1))) {
				case PROJECT_DESCRIPTION :
					// already extracted
					continue;
				case RESOURCES :
					importResource(project, zipFile, entry, Policy.subMonitorFor(monitor, 1));
					continue;
				case SYNCINFO :
					importSyncInfo(project, zipFile, entry, Policy.subMonitorFor(monitor, 1));
					continue;
				case MARKERS :
					importMarkers(project, zipFile, entry, Policy.subMonitorFor(monitor, 1));
					continue;
				case TREE :
					importTree(project, zipFile, entry, Policy.subMonitorFor(monitor, 1));
					continue;
			}
		}
	} finally {
		monitor.done();
	}
}
/**
 * Copied from Synchronizer. Both should be refactored to get a better usability.
 */
protected void importSyncInfo(IProject project, ZipFile zipFile, ZipEntry entry, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		
		// FIXME:
		entry = zipFile.getEntry(String.valueOf(SYNCINFO));
		if (entry == null) {
			// FIXME: log the problem
			return;
		}
		
		DataInputStream input = new DataInputStream(zipFile.getInputStream(entry));
		Synchronizer synchronizer = (Synchronizer) workspace.getSynchronizer();
		SyncInfoReader reader = new SyncInfoReader(workspace, synchronizer);
		reader.readSyncInfo(input);
	} finally {
		monitor.done();
	}
}
/**
 * Copied from Synchronizer. Both should be refactored to get a better usability.
 */
protected void importMarkers(IProject project, ZipFile zipFile, ZipEntry entry, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		
		// FIXME:
		entry = zipFile.getEntry(String.valueOf(MARKERS));
		if (entry == null) {
			// FIXME: log the problem
			return;
		}
		
		DataInputStream input = new DataInputStream(zipFile.getInputStream(entry));
		if (input.available() > 0) { // FIXME: temporary hack
			MarkerReader reader = new MarkerReader(workspace);
			reader.read(input, true);
		}
	} finally {
		monitor.done();
	}
}
/**
 * 
 */
protected void exportTree(IProject project, ZipOutputStream output, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		ZipEntry entry = new ZipEntry(String.valueOf(TREE));
		output.putNextEntry(entry);
		DataOutputStream dos = new DataOutputStream(output);
		workspace.getSaveManager().writeTree((Project) project, dos, null);
		output.closeEntry();
		monitor.worked(1);
	} finally {
		monitor.done();
	}
}
/**
 * Copied from SaveManager. Both methods should be refactored to get a better reusability.
 */
protected void importTree(IProject project, ZipFile zipFile, ZipEntry entry, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 1);
		
		// FIXME:
		entry = zipFile.getEntry(String.valueOf(TREE));
		if (entry == null) {
			// FIXME: log the problem
			return;
		}
		
		DataInputStream input = new DataInputStream(zipFile.getInputStream(entry));
		try {
			WorkspaceTreeReader reader = WorkspaceTreeReader.getReader(workspace, input.readInt());
			// FIXME: In the future, this code should be removed.
			// See comments in WorkspaceTreeReader_0.
			if (reader instanceof WorkspaceTreeReader_0) {
				// reset the stream
				input = new DataInputStream(zipFile.getInputStream(entry));
			}
			reader.readTree(project, input, Policy.subMonitorFor(monitor, Policy.totalWork));
		} finally {
			input.close();
		}
	} finally {
		monitor.done();
	}
}
protected void importResources(IProject project, ZipFile zipFile, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", zipFile.size());
		IPath location = project.getLocation();
		for(Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (!entry.getName().startsWith(RESOURCES_FOLDER))
				continue;
			String name = entry.getName().substring(RESOURCES_FOLDER.length());
			if (entry.isDirectory()) {
				location.append(name).toFile().mkdirs();
			} else { // FILE
				java.io.File file = location.append(name).toFile();
				FileOutputStream target = new FileOutputStream(file);
				try {
					transferStreams(zipFile.getInputStream(entry), target);
				} finally {
					target.close();
				}
				byte[] lastModified = entry.getExtra();
				file.setLastModified(Convert.bytesToLong(lastModified));
			}
		}
	} finally {
		monitor.done();
	}
}
}