package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class handles the updating of the .vcm_meta file in projects managed by the CVSTeamProvider.
 * 
 * It does so by listening to deltas on the project description and the .vcm_meta file itself.
 * 
 */
public class ProjectDescriptionManager implements IResourceChangeListener {

	public final static IPath PROJECT_DESCRIPTION_PATH = new Path(".vcm_meta"); //$NON-NLS-1$
	public final static IPath CORE_PROJECT_DESCRIPTION_PATH = new Path(".project"); //$NON-NLS-1$
	public final static boolean UPDATE_PROJECT_DESCRIPTION_ON_LOAD = true;

	public static final String VCMMETA_MARKER = "org.eclipse.team.cvs.core.vcmmeta";  //$NON-NLS-1$
	
	/*
	 * Read the project meta file into the provider description
	 */
	public static void readProjectDescription(IProjectDescription desc, InputStream stream) throws IOException, CVSException {
		SAXParser parser = new SAXParser();
		parser.setContentHandler(new ProjectDescriptionContentHandler(desc));
		try {
			parser.parse(new InputSource(stream));
		} catch (SAXException ex) {
			throw new CVSException(IStatus.ERROR, IStatus.ERROR, Policy.bind("ProjectDescriptionManager.unableToReadDescription"), ex); //$NON-NLS-1$
		}
	}

	public static void writeProjectDescription(IProject project, IProgressMonitor progress) throws CVSException {
			
		// generate the new contents of the project meta file into a string
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		String newContents = null;
		try {
			IProjectDescription desc = project.getDescription();
			ProjectDescriptionWriter.writeProjectDescription(desc, byteOutputStream);
			byteOutputStream.close();
			newContents = byteOutputStream.toString("UTF8"); //$NON-NLS-1$
		} catch (IOException ex) {
			throw CVSException.wrapException(project, Policy.bind("ProjectDescriptionManager.ioDescription"), ex); //$NON-NLS-1$
		} catch (CoreException ex) {
			throw CVSException.wrapException(project, Policy.bind("ProjectDescriptionManager.coreDescription"), ex); //$NON-NLS-1$
		}

		IFile descResource = project.getFile(PROJECT_DESCRIPTION_PATH);
		if (descResource.exists()) {
			// check if the existing contents are the same before rewriting
			String oldContents = null;
			try {
				StringBuffer stringBuffer = new StringBuffer();
				InputStream is = ((IFile) descResource).getContents();
				byte[] buf = new byte[512];
				int result = is.read(buf);
				while (result != -1) {
					stringBuffer.append(new String(buf, 0, result, "UTF8")); //$NON-NLS-1$
					result = is.read(buf);
				}
				oldContents = stringBuffer.toString();
				is.close();
			} catch (IOException ex) {
				throw CVSException.wrapException(project, Policy.bind("ProjectDescriptionManager.ioDescription"), ex); //$NON-NLS-1$
			} catch (CoreException ex) {
				throw CVSException.wrapException(project, Policy.bind("ProjectDescriptionManager.coreDescription"), ex); //$NON-NLS-1$
			}

			if (oldContents.equals(newContents)) {
				// the contents of the new file would be the same as the old
				return;
			}
			try {
				descResource.setContents(
					new ByteArrayInputStream(byteOutputStream.toByteArray()),
					false,
					false,
					progress);
			} catch (CoreException ex) {
				throw CVSException.wrapException(project, Policy.bind("ProjectDescriptionManager.coreDescription"), ex); //$NON-NLS-1$
			}
		} else {
			try {
				descResource.create(
					new ByteArrayInputStream(byteOutputStream.toByteArray()),
					false,
					progress);
			} catch (CoreException ex) {
				throw CVSException.wrapException(descResource, Policy.bind("ProjectDescriptionManager.coreDescription"), ex); //$NON-NLS-1$
			}

		}
	}

	/*
	 * To be called whenever the project description file is believed to have changed by
	 * a load/loadIfIncoming operation.
	 */
	public static void updateProjectIfNecessary(IProject project, IProgressMonitor progress) throws CoreException, CVSException {
		
		IFile descResource = project.getFile(PROJECT_DESCRIPTION_PATH);		
		if (descResource.exists() && UPDATE_PROJECT_DESCRIPTION_ON_LOAD) {
								
			// If a managed .project files exists, don't read the .vcm_meta
			ICVSFile coreDescResource = CVSWorkspaceRoot.getCVSFileFor(project.getFile(CORE_PROJECT_DESCRIPTION_PATH));
			if (coreDescResource.exists() && coreDescResource.isManaged()) {
				createVCMMetaMarker(descResource);
				Util.logError(Policy.bind("ProjectDescriptionManager.vcmmetaIgnored", project.getName()), null); //$NON-NLS-1$
				return;
			} else {
				ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
				if (! folder.isCVSFolder()) {
					createVCMMetaMarker(descResource);
					Util.logError(Policy.bind("ProjectDescriptionManager.vcmmetaIgnored", project.getName()), null); //$NON-NLS-1$
					return;
				}
			}
		
			// update project description file (assuming it has changed)
			IProjectDescription desc = project.getDescription();
			DataInputStream is = null;
			try {
				is = new DataInputStream(((IFile) descResource).getContents());
				try {
					readProjectDescription(desc, is);
				} finally {
					is.close();
				}
				try {
					project.setDescription(desc, IResource.FORCE | IResource.KEEP_HISTORY, progress);
				} catch (CoreException ex) {
					// Failing to set the description is probably due to a missing nature
					// Other natures are still set
					Util.logError(Policy.bind("ProjectDescriptionManager.unableToSetDescription"), ex); //$NON-NLS-1$
				}
				// Make sure we have the cvs nature (the above read may have removed it)
				if (!project.getDescription().hasNature(CVSProviderPlugin.getTypeId())) {
					try {
						// TeamPlugin.addNatureToProject(project, CVSProviderPlugin.getTypeId(), progress);
						
						// Set the nature manually in order to override any .project file
						IProjectDescription description = project.getDescription();
						String[] prevNatures= description.getNatureIds();
						String[] newNatures= new String[prevNatures.length + 1];
						System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
						newNatures[prevNatures.length]= CVSProviderPlugin.getTypeId();
						description.setNatureIds(newNatures);
						project.setDescription(description, IResource.FORCE | IResource.KEEP_HISTORY, progress);
					}  catch (CoreException ex) {
						// Failing to set the provider is probably due to a missing nature.
						// Other natures are still set
						Util.logError(Policy.bind("ProjectDescriptionManager.unableToSetDescription"), ex); //$NON-NLS-1$
					}
				}
				// Mark the .vcm_meta file with a problem marker
				if (project.getFile(CORE_PROJECT_DESCRIPTION_PATH).exists()) {
					createVCMMetaMarker(descResource);
				}
			} catch(TeamException ex) {
				Util.logError(Policy.bind("ProjectDescriptionManager.unableToReadDescription"), ex); //$NON-NLS-1$
				// something went wrong, delete the project description file
				descResource.delete(true, progress);
			} catch (IOException ex) {
				Util.logError(Policy.bind("ProjectDescriptionManager.unableToReadDescription"), ex); //$NON-NLS-1$
				// something went wrong, delete the project description file
				descResource.delete(true, progress);
			}
		}
	}

	/* 
	 * Write out the project description file.
	 * 
	 * For now just do it. It would be nice to detect the proper conditions
	 * 
	 */
	public static void writeProjectDescriptionIfNecessary(CVSTeamProvider provider, IResource resource, IProgressMonitor monitor) throws CVSException {
		if (resource.getType() == IResource.PROJECT || isProjectDescription(resource)) {
			IProject project = resource.getProject();
			if (project.getFile(PROJECT_DESCRIPTION_PATH).exists() /* || ! project.getFile(CORE_PROJECT_DESCRIPTION_PATH).exists() */) {
				writeProjectDescription(project, monitor);
			}
		}
	}

	public static boolean isProjectDescription(IResource resource) {
		return resource.getProjectRelativePath().equals(PROJECT_DESCRIPTION_PATH);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.ADDED);
			for (int i = 0; i < projectDeltas.length; i++) {
				IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				if (resource.getType() == IResource.PROJECT) {
					IProject project = (IProject)resource;
					RepositoryProvider provider = RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
					if (provider == null) continue;
					// First, check if the .vcm_meta file for the project is in the delta.
					IResourceDelta[] children = delta.getAffectedChildren(IResourceDelta.ADDED);
					boolean inSync = false;
					for (int j = 0; j < children.length; j++) {
						IResourceDelta childDelta = children[j];
						IResource childResource = childDelta.getResource();
						if (isProjectDescription(childResource))
							switch (childDelta.getKind()) {
								case IResourceDelta.REMOVED:
									writeProjectDescriptionIfNecessary((CVSTeamProvider)provider, project, Policy.monitorFor(null));
									inSync = true;
									break;
								case IResourceDelta.CHANGED:
								case IResourceDelta.ADDED:
									updateProjectIfNecessary(project, Policy.monitorFor(null));
									inSync = true;
									break;
							}
					}
					// Check if we didn't do anything above and the project description changed.
					if (! inSync && (delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
						writeProjectDescriptionIfNecessary((CVSTeamProvider)provider, project, Policy.monitorFor(null));
					}
				}
			}	
		} catch (CVSException ex) {
			Util.logError(Policy.bind("ProjectDescriptionManager.cannotUpdateDesc"), ex); //$NON-NLS-1$
		} catch (CoreException ex) {
			Util.logError(Policy.bind("ProjectDescriptionManager.cannotUpdateDesc"), ex); //$NON-NLS-1$
		} 
	}
	
	protected static IMarker createVCMMetaMarker(IResource resource) {
		try {
			IMarker[] markers = resource.findMarkers(VCMMETA_MARKER, false, IResource.DEPTH_ZERO);
   			if (markers.length == 1) {
   				return markers[0];
   			}
			IMarker marker = resource.createMarker(VCMMETA_MARKER);
			marker.setAttribute(IMarker.MESSAGE, Policy.bind("ProjectDescriptionManager.vcmmetaMarker" , resource.getName(), resource.getProject().getName()));  //$NON-NLS-1$
			return marker;
		} catch (CoreException e) {
			Util.logError(Policy.bind("ProjectDescriptionManager.markerError"), e); //$NON-NLS-1$
		}
		return null;
	}
}