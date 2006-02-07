/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search2.internal.ui.SearchMessages;

public class SelectedResourcesScopeDescription implements IScopeDescription {
	private static final String KEY_RESOURCES= "resources"; //$NON-NLS-1$
	private IResource[] fSelectedResources;

	public SelectedResourcesScopeDescription() {
	}
	
	public SelectedResourcesScopeDescription(IResource[] res, boolean copy) {
		fSelectedResources= copy ? (IResource[]) res.clone() : res;
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		return fSelectedResources == null ? ResourcesPlugin.getWorkspace().getRoot().getProjects() : fSelectedResources;
	}

	public void store(IDialogSettings section) {
		String[] paths= getPaths();
		section.put(KEY_RESOURCES, paths);
	}

	private String[] getPaths() {
		ArrayList paths= new ArrayList();
		IResource[] roots= getRoots(null);
		for (int i= 0; i < roots.length; i++) {
			IResource res= roots[i];
			paths.add(res.getFullPath().toPortableString());
		}
		return (String[]) paths.toArray(new String[paths.size()]);
	}
	
	public void restore(IDialogSettings section) {
		String[] paths= section.getArray(KEY_RESOURCES);
		restoreFromPaths(paths);
	}

	private void restoreFromPaths(String[] paths) {
		if (paths != null) {
			IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
			ArrayList resources= new ArrayList();
			for (int i= 0; i < paths.length; i++) {
				IPath path= Path.fromPortableString(paths[i]);
				IResource res= root.findMember(path);
				if (res != null) {
					resources.add(res);
				}
			}
			fSelectedResources= (IResource[]) resources.toArray(new IResource[resources.size()]);
		}
	}

	public void store(Properties props, String prefix) {
		String[] paths= getPaths();
		props.put(prefix+KEY_RESOURCES, paths);
	}
	
	public void restore(Properties props, String prefix) {
		Object paths= props.get(prefix+KEY_RESOURCES);
		if (paths instanceof String[]) {
			restoreFromPaths((String[]) paths);
		}
	}

	public String getLabelDetail() {
		switch (fSelectedResources.length) {
			case 1:
				return fSelectedResources[0].getName();
			default:
				return fSelectedResources[0].getName() + ", ..."; //$NON-NLS-1$
		}
	}
	
	public String getLabelForCombo() {
		return getLabel(SearchMessages.SelectedResourcesScopeDescription_label, SearchMessages.SelectedResourcesScopeDescription_projectLabel, SearchMessages.SelectedResourcesScopeDescription_multiProjectLabel);
	}

	public String getNameForDescription() {
		return getLabel(SearchMessages.SelectedResourcesScopeDescription_name, SearchMessages.SelectedResourcesScopeDescription_projectName, SearchMessages.SelectedResourcesScopeDescription_multiProjectName);
	}
	
	private String getLabel(String emptyLabel, String singleProject, String multipleProjects) {
		if (fSelectedResources == null || fSelectedResources.length == 0) {
			return emptyLabel;
		}
		
		String base;
		if (consistsOfProjects()) {
			if (fSelectedResources.length == 1) {
				base= singleProject;
			}
			else {
				base= multipleProjects;
			}
		}
		else {
			if (fSelectedResources.length == 1) {
				return RetrieverLabelProvider.getFileLableWithContainer(fSelectedResources[0].getFullPath());
			}
			base= emptyLabel;
		}
		return MessageFormat.format("{0} {1}", new Object[] {base, getLabelDetail()}); //$NON-NLS-1$
	}
			
	private boolean consistsOfProjects() {
		if (fSelectedResources == null || fSelectedResources.length == 0) {
			return false;
		}
		for (int i= 0; i < fSelectedResources.length; i++) {
			IResource res= fSelectedResources[i];
			if (res.getType() != IResource.PROJECT) {
				return false;
			}
		}
		return true;
	}

	public IFile[] getFiles(IWorkbenchPage page) {
		return null;
	}
}
