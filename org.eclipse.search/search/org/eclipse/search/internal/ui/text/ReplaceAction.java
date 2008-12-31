/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;

import org.eclipse.search.internal.ui.SearchMessages;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

public class ReplaceAction extends Action {

	public static class ReplaceWizard extends RefactoringWizard {
		public ReplaceWizard(ReplaceRefactoring refactoring) {
			super(refactoring, RefactoringWizard.DIALOG_BASED_USER_INTERFACE);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ltk.ui.refactoring.RefactoringWizard#addUserInputPages()
		 */
		protected void addUserInputPages() {
			addPage(new ReplaceConfigurationPage((ReplaceRefactoring) getRefactoring()));
		}
	}

	private final FileSearchResult fResult;
	private final Object[] fSelection;
	private final boolean fSkipFiltered;
	private final Shell fShell;

	/**
	 * Creates the replace action to be
	 * @param shell the parent shell
	 * @param result the file search page to
	 * @param selection the selected entries or <code>null</code> to replace all
	 * @param skipFiltered if set to <code>true</code>, filtered matches will not be replaced
	 */
	public ReplaceAction(Shell shell, FileSearchResult result, Object[] selection, boolean skipFiltered) {
		fShell= shell;
		fResult= result;
		fSelection= selection;
		fSkipFiltered= skipFiltered;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			ReplaceRefactoring refactoring= new ReplaceRefactoring(fResult, fSelection, fSkipFiltered);
			ReplaceWizard refactoringWizard= new ReplaceWizard(refactoring);
			if (fSelection == null) {
				refactoringWizard.setDefaultPageTitle(SearchMessages.ReplaceAction_title_all);
			} else {
				refactoringWizard.setDefaultPageTitle(SearchMessages.ReplaceAction_title_selected);
			}
			RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
			op.run(fShell, SearchMessages.ReplaceAction_description_operation);
		} catch (InterruptedException e) {
			// refactoring got cancelled
		}
	}

}
