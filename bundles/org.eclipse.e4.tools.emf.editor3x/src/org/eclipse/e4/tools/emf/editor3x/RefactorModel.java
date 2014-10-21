/*******************************************************************************
 * Copyright (c) 2014 Remain Software, Industrial-TSI and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wim Jongman <wim.jongman@remainsoftware.com> - Bug 432892: Eclipse 4 Application does not work after renaming the
 * project name
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;

/**
 * A temporary data storage for Ecipse 4 model refactoring. Use the static
 * methods to get hold of an instance.
 *
 * @author Remain Software - Wim Jongman
 *
 */
public class RefactorModel {

	int fIndex = 0;
	IProject fNewProject;
	List<String> fNewTexts = new ArrayList<String>();

	IProject fOldProject;

	List<String> fOldTexts = new ArrayList<String>();

	private RefactoringParticipant fParticipant;

	/**
	 * Factory for an empty model. Use the {@link #addTextRename(String, String)} method to add one or more text
	 * renames.
	 *
	 * @param pParticipant
	 * @return the model
	 */
	public static RefactorModel getModel(RefactoringParticipant pParticipant) {
		return new RefactorModel().setRefactorParticipant(pParticipant);
	}

	/**
	 * Factory for a model with one rename. Use the {@link #addTextRename(String, String)} method to add one or more
	 * text
	 * renames.
	 *
	 * @param pParticipant
	 * @param pOldText
	 * @param pNewText
	 * @return the model.
	 */
	public static RefactorModel getModel(RefactoringParticipant pParticipant,
		String pOldText, String pNewText) {
		return new RefactorModel().addTextRename(pOldText, pNewText);
	}

	/**
	 * Factory for a model with one rename and a project rename. The project
	 * rename uses the old and new project because the refactoring framework
	 * expects the already renamed filenames. Use the {@link #addTextRename(String, String)} method to add one or more
	 * text
	 * renames and the old and the new project.
	 *
	 * @param pParticipant
	 * @param pOldText
	 * @param pNewText
	 * @param pOldProject
	 * @param pNewProject
	 * @return the model.
	 */
	public static RefactorModel getModel(RefactoringParticipant pParticipant,
		String pOldText, String pNewText, IProject pOldProject,
		IProject pNewProject) {
		return new RefactorModel().addTextRename(pOldText, pNewText)
			.setProjectRename(pOldProject, pNewProject)
			.setRefactorParticipant(pParticipant);
	}

	/**
	 * Adds a text rename to be processed later. For example, if the project
	 * name changes there can be <code>bundlclass://</code> and <code>platform:/plugin</code> changes.
	 *
	 * @param oldText
	 * @param newText
	 * @return the model
	 */
	public RefactorModel addTextRename(String oldText, String newText) {
		Assert.isNotNull(oldText);
		Assert.isNotNull(newText);
		fOldTexts.add(oldText);
		fNewTexts.add(newText);
		return this;
	}

	/**
	 * When project renaming this returns the new project. This project does not
	 * necessarily exist yet.
	 *
	 * @return the new project or null if it was not set
	 * @see RefactorModel#setProjectRename(IProject, IProject)
	 */
	public IProject getNewProject() {
		return fNewProject;
	}

	/**
	 * @return the current index set by {@link #setIndex(int)}
	 */
	public String getNewTextCurrentIndex() {
		return fNewTexts.get(fIndex);
	}

	/**
	 * When project renaming this returns the old project.
	 *
	 * @return the new project or null if it was not set
	 * @see RefactorModel#setProjectRename(IProject, IProject)
	 */
	public IProject getOldProject() {
		return fOldProject;
	}

	/**
	 *
	 * @return the old text in the current index.
	 */
	public String getOldTextCurrentIndex() {
		return fOldTexts.get(fIndex);
	}

	/**
	 * @return the refactoring participant
	 */
	public RefactoringParticipant getRefactoringParticipant() {
		return fParticipant;
	}

	/**
	 * @return the number of text renames in this model
	 */
	public int getRenameCount() {
		return fOldTexts.size();
	}

	/**
	 * Delegates to the same method of the embedded RefactoringParticipant.
	 *
	 * @param file
	 * @return a TextChange object
	 * @see RefactoringParticipant#getTextChange(Object)
	 */
	public TextChange getTextChange(IFile file) {
		return fParticipant.getTextChange(file);
	}

	/**
	 * @return true if this is a projec rename (old and new project are set)
	 */
	public boolean isProjectRename() {
		return fOldProject != null;
	}

	/**
	 * Sets the current 0-based index. May not be out of bounds.
	 *
	 * @param index
	 * @return the model
	 */
	public RefactorModel setIndex(int index) {
		Assert.isTrue(index >= 0);
		Assert.isTrue(index < fOldTexts.size());
		fIndex = index;
		return this;
	}

	/**
	 * Sets the old and the new project in case of project renaming.
	 *
	 * @param oldProject
	 * @param newProject
	 * @return the model
	 */
	public RefactorModel setProjectRename(IProject oldProject,
		IProject newProject) {
		Assert.isNotNull(oldProject);
		Assert.isNotNull(newProject);
		fOldProject = oldProject;
		fNewProject = newProject;
		return this;
	}

	/**
	 * Sets the RefactoringParticipant.
	 *
	 * @param pParticipant
	 * @return the model
	 * @see RefactoringParticipant
	 */
	public RefactorModel setRefactorParticipant(
		RefactoringParticipant pParticipant) {
		fParticipant = pParticipant;
		return this;
	}
}
