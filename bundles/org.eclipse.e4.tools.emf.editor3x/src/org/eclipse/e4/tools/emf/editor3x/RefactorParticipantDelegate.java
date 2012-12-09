/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

class RefactorParticipantDelegate {
	public static Change createChange(IProgressMonitor pm,
			final RefactoringParticipant p, String oldUrl, final String newUrl)
			throws CoreException, OperationCanceledException {
		String[] filenames = { "*.e4xmi" };
		FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(
				filenames, false);
		Pattern pattern = Pattern.compile(oldUrl);

		final Map<IFile, TextFileChange> changes = new HashMap<IFile, TextFileChange>();
		TextSearchRequestor searchRequestor = new TextSearchRequestor() {
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess)
					throws CoreException {
				IFile file = matchAccess.getFile();
				TextFileChange change = (TextFileChange) changes.get(file);

				if (change == null) {
					TextChange textChange = p.getTextChange(file);
					if (textChange != null) {
						return false;
					}
					change = new TextFileChange(file.getName(), file);
					change.setEdit(new MultiTextEdit());
					changes.put(file, change);
				}
				ReplaceEdit edit = new ReplaceEdit(
						matchAccess.getMatchOffset(),
						matchAccess.getMatchLength(), newUrl);
				change.addEdit(edit);
				change.addTextEditGroup(new TextEditGroup(
						"Eclipse 4 Application Model Changes", edit)); //$NON-NLS-1$
				return true;
			}
		};
		TextSearchEngine.create().search(scope, searchRequestor, pattern, pm);

		if (changes.isEmpty())
			return null;

		CompositeChange result = new CompositeChange(
				"Eclipse 4 Application Model Changes"); //$NON-NLS-1$
		for (TextFileChange c : changes.values()) {
			result.add(c);
		}
		return result;
	}
}
