/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.base.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.search.HelpIndexBuilder;

public class BuildHelpIndex extends Task {
	private String manifest;
	private String targetDir;
	private HelpIndexBuilder builder; 
	
	public BuildHelpIndex() {
	}

	public void execute() throws BuildException {
		File file = getFile(manifest);
		if (file==null)
			throw new BuildException("Manifest not set.");
		File target = getFile(targetDir);
		if (target==null)
			throw new BuildException("Target directory not set.");
		builder = new HelpIndexBuilder();
		builder.setManifest(file);
		builder.setTarget(target);
		IProgressMonitor monitor = 
			(IProgressMonitor) getProject().getReferences().get(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
		try {
			builder.execute(monitor);
		}
		catch (CoreException e) {
			throw new BuildException(e.getMessage(), e.getCause());
		}
	}
	
	private File getFile(String fileName) {
		if (fileName==null)
			return null;
		File file =
			new Path(fileName).isAbsolute()
				? new File(fileName)
				: new File(getProject().getBaseDir(), fileName);
				return file;
	}

	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
	
	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}
}