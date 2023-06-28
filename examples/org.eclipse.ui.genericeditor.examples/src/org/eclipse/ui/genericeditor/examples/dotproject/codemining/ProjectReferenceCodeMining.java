/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide extension point for CodeMining - Bug 528419
 */
package org.eclipse.ui.genericeditor.examples.dotproject.codemining;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

/**
 * Project reference mining.
 */
public class ProjectReferenceCodeMining extends LineHeaderCodeMining {

	private final String projectName;

	public ProjectReferenceCodeMining(String projectName, int beforeLineNumber, IDocument document,
			ICodeMiningProvider provider) throws BadLocationException {
		super(beforeLineNumber, document, provider);
		this.projectName = projectName;
	}

	@Override
	protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
		return CompletableFuture.runAsync(() -> {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			int refCount = project != null ? project.getReferencingProjects().length : 0;
			super.setLabel(refCount + (refCount > 1 ? " references" : " reference"));
		});
	}

}
