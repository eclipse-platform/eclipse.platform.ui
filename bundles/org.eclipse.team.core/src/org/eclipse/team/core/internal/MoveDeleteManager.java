package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.DefaultMoveDeleteHook;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;

public class MoveDeleteManager implements IMoveDeleteHook {

	private static final IMoveDeleteHook DEFAULT_HOOK = new DefaultMoveDeleteHook();
	
	private IMoveDeleteHook getHookFor(IResource resource) {
		IProject project = resource.getProject();
		RepositoryProvider provider = RepositoryProviderType.getProvider(project);
		if(provider==null) {
			return DEFAULT_HOOK; 
		}
		IMoveDeleteHook hook = provider.getMoveDeleteHook();
		if (hook == null) {
			return DEFAULT_HOOK;
		}
		return hook;
	}
	
	/*
	 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
	 */
	public boolean deleteFile(
		IResourceTree tree,
		IFile file,
		int updateFlags,
		IProgressMonitor monitor) {
		
		return getHookFor(file).deleteFile(tree, file, updateFlags, monitor);
	}

	/*
	 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
	 */
	public boolean deleteFolder(
		IResourceTree tree,
		IFolder folder,
		int updateFlags,
		IProgressMonitor monitor) {
		
		return getHookFor(folder).deleteFolder(tree, folder, updateFlags, monitor);
	}

	/*
	 * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
	 */
	public boolean deleteProject(
		IResourceTree tree,
		IProject project,
		int updateFlags,
		IProgressMonitor monitor) {
			
		return getHookFor(project).deleteProject(tree, project, updateFlags, monitor);
	}

	/*
	 * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
	 */
	public boolean moveFile(
		IResourceTree tree,
		IFile source,
		IFile destination,
		int updateFlags,
		IProgressMonitor monitor) {
		
		return getHookFor(source).moveFile(tree, source, destination, updateFlags, monitor);
	}

	/*
	 * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
	 */
	public boolean moveFolder(
		IResourceTree tree,
		IFolder source,
		IFolder destination,
		int updateFlags,
		IProgressMonitor monitor) {
			
		return getHookFor(source).moveFolder(tree, source, destination, updateFlags, monitor);
	}

	/*
	 * @see IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
	 */
	public boolean moveProject(
		IResourceTree tree,
		IProject source,
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor) {
			
		return getHookFor(source).moveProject(tree, source, description, updateFlags, monitor);
	}

}
