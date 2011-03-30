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
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ClassRenameParticipant extends
		org.eclipse.ltk.core.refactoring.participants.RenameParticipant {
	private IType type;
	
	@Override
	protected boolean initialize(Object element) {
		if( element instanceof IType ) {
			type = (IType) element;
		} else {
			type = null;
		}
		
		return type != null;
	}

	@Override
	public String getName() {
		return "Workbench Model Contribution Participant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		final Map<IFile, TextFileChange> changes= new HashMap<IFile, TextFileChange>();
		final String newName = type.getPackageFragment().getElementName().length() == 0 ? getArguments().getNewName() : type.getPackageFragment().getElementName() + "." + getArguments().getNewName();
		String oldName = type.getFullyQualifiedName().replace(".", "\\.");
		
		String[] filenames = { "*.e4xmi" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(filenames, false);
		Pattern pattern= Pattern.compile(oldName);
		
		TextSearchRequestor searchRequestor = new TextSearchRequestor() {
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
				IFile file = matchAccess.getFile();
				TextFileChange change= (TextFileChange) changes.get(file);
				
				if (change == null) {
					TextChange textChange = getTextChange(file);
					if (textChange != null) {
						return false;
					}
					change= new TextFileChange(file.getName(), file);
					change.setEdit(new MultiTextEdit());
					changes.put(file, change);
				}
				ReplaceEdit edit= new ReplaceEdit(matchAccess.getMatchOffset(), matchAccess.getMatchLength(), newName);
				change.addEdit(edit);
				change.addTextEditGroup(new TextEditGroup("Update contribution reference", edit)); //$NON-NLS-1$
				return true;
			}
		};
		TextSearchEngine.create().search(scope, searchRequestor, pattern, pm);
		
		if (changes.isEmpty())
			return null;
		
		CompositeChange result= new CompositeChange("Update contribution reference"); //$NON-NLS-1$
		for (TextFileChange c : changes.values()) {
			result.add(c);
		}
		return result;
	}

}
