package org.eclipse.ant.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import java.util.Vector;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.PatternSet;

public class RefreshLocal extends Task {
	protected IResource resource;
	protected int depth= IResource.DEPTH_INFINITE;
	protected PatternSet patternSet= new PatternSet();
	protected boolean useDefaultExcludes= true;
	private IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
	public RefreshLocal() {
		super();
	}
	/**
	 * add a name entry on the exclude list
	 */
	public PatternSet.NameEntry createExclude() {
		return patternSet.createExclude();
	}
	/**
	 * add a name entry on the include list
	 */
	public PatternSet.NameEntry createInclude() {
		return patternSet.createInclude();
	}
	/**
	 * Performs the copy operation.
	 */
	public void execute() throws BuildException {
		// make sure we don't have an illegal set of options
		validateAttributes();

		// deal with the single resource
		if (resource != null) {
			if (!resource.exists())
				throw new BuildException("Resource " + resource + " not found");
			try {
				resource.refreshLocal(depth, null);
				return;
			} catch (CoreException e) {
				throw new BuildException(e);
			}
		}

		// handle the patterns
		DirectoryScanner ds= getWorkspaceScanner();
		refreshResources(ds.getIncludedDirectories());
		refreshResources(ds.getIncludedFiles());
	}
	/**
	 * Returns the directory scanner needed to access the files to process.
	 */
	protected WorkspaceScanner getWorkspaceScanner() {
		WorkspaceScanner scanner= new WorkspaceScanner();
		scanner.setIncludes(patternSet.getIncludePatterns(getProject()));
		scanner.setExcludes(patternSet.getExcludePatterns(getProject()));
		if (useDefaultExcludes)
			scanner.addDefaultExcludes();
		scanner.scan();
		return scanner;
	}
	protected void refreshResources(String[] resources) throws BuildException {
		for (int i= 0; i < resources.length; i++) {
			IResource target= root.findMember(resources[i]);
			if (target == null)
				throw new BuildException("Resource " + resources[i] + " not found");
			try {
				target.refreshLocal(depth, null);
			} catch (CoreException e) {
				throw new BuildException(e);
			}
		}
	}
	/**
	 * Sets whether default exclusions should be used or not.
	 *
	 * @param useDefaultExcludes "true"|"on"|"yes" when default exclusions 
	 *                           should be used, "false"|"off"|"no" when they
	 *                           shouldn't be used.
	 */
	public void setDefaultexcludes(boolean value) {
		useDefaultExcludes= value;
	}
	public void setDepth(String value) {
		if ("zero".equalsIgnoreCase(value))
			depth= IResource.DEPTH_ZERO;
		else if ("one".equalsIgnoreCase(value))
			depth= IResource.DEPTH_ONE;
		else if ("infinite".equalsIgnoreCase(value))
			depth= IResource.DEPTH_INFINITE;
	}
	public void setResource(String value) {
		resource= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(value));
	}
	protected void validateAttributes() throws BuildException {
	}
}
