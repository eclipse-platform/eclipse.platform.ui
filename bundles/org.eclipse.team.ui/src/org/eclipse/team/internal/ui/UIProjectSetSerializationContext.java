/**********************************************************************
 Copyright (c) 2004 Dan Rubel and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:

 Dan Rubel - initial API and implementation

 **********************************************************************/

package org.eclipse.team.internal.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.dialogs.IPromptCondition;
import org.eclipse.team.internal.ui.dialogs.PromptingDialog;

/**
 * The UI based context in which project serialization occurs.
 * The class may be subclasses to represent different UI based serialization contexts.
 * It is recommended that all UI based serialization contexts
 * use this class directly or indirectly as their superclass. 
 * 
 * @since 3.0
 */
public class UIProjectSetSerializationContext extends ProjectSetSerializationContext {

	/**
	 * The parent shell for this UI context
	 */
	private final Shell shell;

	/**
	 * Construct a new instance
	 * 
	 * @param shell The parent shell for this UI context
	 */
	public UIProjectSetSerializationContext(Shell shell, String filename) {
		super(filename);
		Assert.isNotNull(shell);
		this.shell = shell;
	}

	/**
	 * Answer the shell associated with this UI context.
	 * 
	 * @return the shell (not <code>null</code>)
	 */
	public Object getShell() {
		return shell;
	}
	
	/**
	 * Given an array of projects that currently exist in the workspace
	 * prompt the user to determine which of those projects should be overwritten.
	 * <p>
	 * This default implementation prompts the user
	 * to determine which projects should be overwritten.
	 * Subclasses may override this as appropriate.
	 * 
	 * @param projects 
	 * 		an array of projects currently existing in the workspace
	 * 		that are desired to be overwritten.
	 * 		(not <code>null</code>, contains no <code>null</code>s)
	 * @return
	 * 		an array of zero or more projects that should be overwritten
	 * 		or <code>null</code> if the operation is to be canceled
	 * 
	 * @see org.eclipse.team.core.ProjectSetSerializationContext#confirmOverwrite(org.eclipse.core.resources.IProject[])
	 */
	public IProject[] confirmOverwrite(final IProject[] projects) throws TeamException {
		IPromptCondition prompt = new IPromptCondition() {
			List resources = Arrays.asList(projects);
			public boolean needsPrompt(IResource resource) {
				return resources.contains(resource);
			}
			public String promptMessage(IResource resource) {
				return Policy.bind("UIProjectSetSerializationContext.0", resource.getName()); //$NON-NLS-1$
			}
		};
		PromptingDialog dialog =
			new PromptingDialog(
				(Shell)getShell(),
				projects,
				prompt,
				Policy.bind("UIProjectSetSerializationContext.1")); //$NON-NLS-1$
		IResource[] resourcesToOverwrite;
		try {
			resourcesToOverwrite = dialog.promptForMultiple();
		} catch (InterruptedException e) {
			// Return null indicating that the user canceled the operation
			return null;
		}
		IProject[] projectsToOverwrite = new IProject[resourcesToOverwrite.length];
		System.arraycopy(resourcesToOverwrite, 0, projectsToOverwrite, 0, resourcesToOverwrite.length);
		return projectsToOverwrite;
	}

}
