/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.tools.services.impl.AbstractTranslationProvider;

public abstract class ProjectOSGiTranslationProvider extends AbstractTranslationProvider {
	private IProject project;
	private List<String> observedFiles = new ArrayList<String>();
	private IResourceChangeListener listener;
	private String basename;

	public ProjectOSGiTranslationProvider(IProject project) {
		super();
		this.project = project;
		this.listener = new IResourceChangeListener() {

			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
					try {
						event.getDelta().accept(new IResourceDeltaVisitor() {

							public boolean visit(IResourceDelta delta) throws CoreException {
								return ProjectOSGiTranslationProvider.this.visit(delta);
							}
						});
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		this.project.getWorkspace().addResourceChangeListener(listener);
		IFile f = this.project.getFile("META-INF/MANIFEST.MF"); //$NON-NLS-1$
		if (f.exists()) {
			handleManifestChange(f);
		} else {
			basename = "OSGI-INF/l10n/bundle"; //$NON-NLS-1$
		}
	}

	boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getResource() instanceof IWorkspaceRoot) {
			return true;
		} else if (delta.getResource().equals(project)) {
			return true;
		} else if (delta.getResource().getProjectRelativePath().toString().equals("META-INF")) { //$NON-NLS-1$
			return true;
		} else if (delta.getResource().getProjectRelativePath().toString().equals("META-INF/MANIFEST.MF")) { //$NON-NLS-1$
			handleManifestChange((IFile) delta.getResource());
			return false;
		}

		for (String o : observedFiles) {
			if (delta.getResource().getProjectRelativePath().toString().equals(o)) {
				clearCache();
				return false;
			}

			String[] p = o.split("/"); //$NON-NLS-1$
			int i = 0;
			String path = ""; //$NON-NLS-1$
			do {
				path += p[i];
				if (delta.getResource().getProjectRelativePath().toString().equals(path)) {
					return true;
				}
				path += "/"; //$NON-NLS-1$
			} while (++i < p.length);
		}

		return false;
	}

	private void handleManifestChange(IFile file) {
		try {
			String newValue = extractBasenameFromManifest(file);

			if (!newValue.equals(basename)) {
				if (basename != null) {
					basename = newValue;
					clearCache();
				} else {
					basename = newValue;
				}
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String extractBasenameFromManifest(IFile file) throws CoreException, IOException {
		InputStream in = file.getContents();
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;
		String newValue = "OSGI-INF/l10n/bundle"; //$NON-NLS-1$
		while ((line = r.readLine()) != null) {
			if (line.startsWith("Bundle-Localization:")) { //$NON-NLS-1$
				newValue = line.substring("Bundle-Localization:".length()).trim(); //$NON-NLS-1$
				break;
			}
		}

		r.close();
		return newValue;
	}

	@Override
	protected InputStream getResourceAsStream(String name) {
		IFile f = project.getFile(name);
		observedFiles.add(name);
		try {
			if (f.exists()) {
				return f.getContents();
			} else {
				return null;
			}
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	protected void clearCache() {
		super.clearCache();
		observedFiles.clear();
	}

	@Override
	protected String getBasename() {
		return basename;
	}

	public void dispose() {

	}
}