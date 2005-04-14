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
	private HelpIndexBuilder builder; 
	
	public BuildHelpIndex() {
	}

	public void execute() throws BuildException {
		File file = getManifestFile();
		if (file==null)
			return;
		builder = new HelpIndexBuilder(file);
		IProgressMonitor monitor = 
			(IProgressMonitor) getProject().getReferences().get(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
		try {
			builder.execute(monitor);
		}
		catch (CoreException e) {
			throw new BuildException(e.getMessage(), e.getCause());
		}
	}
	
	private File getManifestFile() {
		if (manifest==null)
			return null;
		File file =
			new Path(manifest).isAbsolute()
				? new File(manifest)
				: new File(getProject().getBaseDir(), manifest);
				return file;
	}

	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
}