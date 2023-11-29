/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Wim Jongman <wim.jongman@remainsoftware.com> Bug 432892: Eclipse 4 Application does not work after renaming the
 * project name
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x.refactorparticipants;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.RefactorModel;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.pde.internal.ui.refactoring.MovedTextFileChange;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

@SuppressWarnings("restriction")
class RefactorParticipantDelegate {

	private static final String E4_MODEL_CHANGES = "Eclipse 4 Application Model Changes"; //$NON-NLS-1$

	/**
	 * Creates a set of changes and returns a new {@link CompositeChange} or
	 * adds the changes to the passed {@link CompositeChange}.
	 *
	 * @param pModel
	 *
	 * @return a set of changes in a {@link CompositeChange}
	 */
	public static CompositeChange createChange(IProgressMonitor pProgressMonitor, final RefactorModel pModel)
			throws CoreException, OperationCanceledException {

		final String[] filenames = { "*.e4xmi" }; //$NON-NLS-1$
		final FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(filenames, false);

		final Map<IFile, TextFileChange> changes = new HashMap<>();
		final TextSearchRequestor searchRequestor = new TextSearchRequestor() {

			@Override
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
				final IFile file = matchAccess.getFile();
				TextFileChange change = changes.get(file);

				if (change == null) {
					final TextChange textChange = pModel.getTextChange(file);
					if (textChange != null) {
						return false;
					}

					if (pModel.isProjectRename()
							&& file.getProject().equals(pModel.getOldProject())) {
						// The project/resources get refactored before the
						// TextChange is applied, therefore we need their
						// future locations
						final IProject newProject = pModel.getNewProject();

						// If the model is in a non-standard location the
						// new project will keep that location, only the project
						// will be changed
						final IPath oldFile = file.getFullPath().removeFirstSegments(
								1);
						final IFile newFile = newProject.getFile(oldFile);

						change = new MovedTextFileChange(file.getName(),
								newFile, file);
						change.setEdit(new MultiTextEdit());
						changes.put(file, change);

					} else {
						change = new TextFileChange(file.getName(), file);
						change.setEdit(new MultiTextEdit());
						changes.put(file, change);
					}
				}

				final ReplaceEdit edit = new ReplaceEdit(
						matchAccess.getMatchOffset(),
						matchAccess.getMatchLength(),
						pModel.getNewTextCurrentIndex());
				change.addEdit(edit);
				change.addTextEditGroup(new TextEditGroup(E4_MODEL_CHANGES,
						edit));
				return true;
			}
		};

		CompositeChange result;
		final TextSearchEngine searchEngine = TextSearchEngine.create();
		int count = pModel.getRenameCount();
		while (count > 0) {
			pModel.setIndex(count - 1);
			searchEngine.search(
					scope,
					searchRequestor,
					TextSearchEngine.createPattern(
							pModel.getOldTextCurrentIndex(), true, false),
					pProgressMonitor);
			count--;
		}

		if (changes.isEmpty()) {
			return null;
		}

		result = new CompositeChange(E4_MODEL_CHANGES);
		for (final TextFileChange c : changes.values()) {
			result.add(c);
		}
		return result;
	}
}
