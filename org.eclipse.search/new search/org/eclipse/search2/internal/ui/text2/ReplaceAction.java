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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.PlatformUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.SearchAgainConfirmationDialog;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.Messages;
import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.search2.internal.ui.text.PositionTracker;

/**
 * Class to carry out replacement opertions. An instance may only be used once.
 */
public class ReplaceAction extends Action {
	private RetrieverPage fView;
	private Shell fShell;
	private RetrieverResult fSearchResult;
	private String fDialogTitle;
	private boolean fSuccess;
	private Pattern fPattern;
	private String fReplacement;
	private boolean fReplace;
	private RetrieverMatch fMatch;
	private HashSet fFileSet;
	private Map fLineMap;

	/**
	 * Constructor to replace all matches
	 */
	public ReplaceAction(RetrieverPage retriever, String action, boolean replace) {
		init(retriever, action, replace);
		setupReplaceAll();
	}

	/**
	 * Constructor to replace a selection of matches
	 */
	public ReplaceAction(RetrieverPage retriever, String action, boolean replace, IStructuredSelection sel) {
		init(retriever, action, replace);
		setupReplaceSelection(sel);
	}

	/**
	 * Constructor to replace a single match
	 */
	public ReplaceAction(RetrieverPage retriever, String action, boolean replace, RetrieverMatch match) {
		init(retriever, action, replace);
		fMatch= match;
	}

	private void init(RetrieverPage retriever, String action, boolean replace) {
		fView= retriever;
		fSearchResult= (RetrieverResult) fView.getInput();
		fShell= fView.getSite().getShell();
		fDialogTitle= action;
		fReplace= replace;
		setText(action);
	}

	private void setupReplaceAll() {
		fFileSet= new HashSet();
		fLineMap= Collections.EMPTY_MAP;
		Object[] lines= fSearchResult.getElements();
		for (int i= 0; i < lines.length; i++) {
			RetrieverLine line= (RetrieverLine) lines[i];
			if (needsReplacement(line)) {
				fFileSet.add(line.getParent());
			}
		}
		setEnabled(!fFileSet.isEmpty());
	}

	private boolean needsReplacement(RetrieverLine line) {
		if (!line.isFiltered()) {
			RetrieverMatch[] matches= line.getMatches(false);
			for (int j= 0; j < matches.length; j++) {
				RetrieverMatch match= matches[j];
				if (!match.isFiltered() && match.isReplaced() != fReplace) {
					return true;
				}
			}
		}
		return false;
	}

	private void setupReplaceSelection(IStructuredSelection sel) {
		// collect resources and lines of selection
		HashMap selectedResources= new HashMap();
		HashSet selectedLines= new HashSet();
		fFileSet= new HashSet();
		fLineMap= new HashMap();
		for (Iterator iter= sel.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IResource) {
				selectedResources.put(element, Boolean.TRUE);
			} else
				if (element instanceof RetrieverLine) {
					selectedLines.add(element);
				}
		}

		Object[] allLines= fSearchResult.getElements();
		for (int i= 0; i < allLines.length; i++) {
			RetrieverLine line= (RetrieverLine) allLines[i];
			if (needsReplacement(line)) {
				IFile file= line.getParent();
				if (containsFile(selectedResources, file)) {
					fFileSet.add(file);
				} else
					if (selectedLines.contains(line)) {
						fFileSet.add(file);
						addToLineMap(file, line);
					}
			}
		}
		setEnabled(!fFileSet.isEmpty());
	}

	private void addToLineMap(IFile file, RetrieverLine line) {
		ArrayList lines= (ArrayList) fLineMap.get(file);
		if (lines == null) {
			lines= new ArrayList();
			fLineMap.put(file, lines);
		}
		lines.add(line);
	}

	private boolean containsFile(HashMap resources, IFile file) {
		return containsResource(resources, file).booleanValue();
	}

	private Boolean containsResource(HashMap resources, IResource res) {
		Boolean result= (Boolean) resources.get(res);
		if (result == null) {
			IResource parent= res.getParent();
			result= parent == null ? Boolean.FALSE : containsResource(resources, parent);
			resources.put(res, result);
		}
		return result;
	}

	public void setReplacement(Pattern pattern, String replacement) {
		fPattern= pattern;
		fReplacement= replacement;
	}

	public void run() {
		fSuccess= false;
		if (fSearchResult == null) {
			return;
		}
		if (fReplace && (fPattern == null || fReplacement == null)) {
			return;
		}

		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		ISchedulingRule rule= workspace.getRuleFactory().modifyRule(workspace.getRoot());

		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				fSuccess= doPerformReplace(monitor);
			}
		};

		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(fView.getSite().getWorkbenchWindow(), runnable, rule);
		} catch (InvocationTargetException e) {
			SearchPlugin.log(e);
		} catch (InterruptedException e) {
		}
	}

	protected boolean doPerformReplace(IProgressMonitor monitor) {
		if (fMatch != null) {
			return doPerformSingleReplace(monitor);
		} else
			if (fFileSet != null && fLineMap != null) {
				return doPerformMultiReplace(monitor);
			}
		return false;
	}

	private boolean doPerformSingleReplace(IProgressMonitor monitor) {
		RetrieverMatch match= fMatch;
		RetrieverLine line= match.getLine();
		IFile file= line.getParent();
		HashSet searchedAgain= new HashSet();
		if (!makeCommitable(Collections.singleton(file), searchedAgain)) {
			return false;
		}
		if (searchedAgain.contains(file)) {
			match= mapMatch(match);
		}
		if (match == null) {
			MessageDialog.openError(fShell, fDialogTitle, SearchMessages.ReplaceOperation_error_cannotLocateMatch);
			return false;
		}

		ArrayList failures= new ArrayList();
		line= match.getLine();
		replaceInFile(monitor, line.getParent(), new RetrieverMatch[] {match}, failures);
		if (!failures.isEmpty()) {
			ErrorDialog.openError(fShell, fDialogTitle, SearchMessages.ReplaceOperation_error_operationFailed, (IStatus) failures.get(0));
			return false;
		}
		fView.getTreeViewer().refresh(line);
		return true;
	}

	private boolean doPerformMultiReplace(IProgressMonitor monitor) {
		HashSet searchedAgain= new HashSet();
		if (!makeCommitable(fFileSet, searchedAgain)) {
			return false;
		}

		for (Iterator iter= searchedAgain.iterator(); iter.hasNext();) {
			IFile file= (IFile) iter.next();
			ArrayList lines= (ArrayList) fLineMap.get(file);
			if (lines != null) {
				mapLines(file, lines);
			}
		}

		ArrayList failures= new ArrayList();
		monitor.beginTask(SearchMessages.ReplaceOperation_task_performChanges, fFileSet.size());
		try {
			for (Iterator iter= fFileSet.iterator(); iter.hasNext();) {
				IFile file= (IFile) iter.next();
				List lines= (List) fLineMap.get(file);
				if (lines == null) {
					lines= Arrays.asList(fSearchResult.getLinesForFile(file, false));
				}
				ArrayList matches= new ArrayList(lines.size());
				for (Iterator iterator= lines.iterator(); iterator.hasNext();) {
					RetrieverLine line= (RetrieverLine) iterator.next();
					if (line != null) {
						matches.addAll(Arrays.asList(line.getDisplayedMatches()));
					}
				}

				replaceInFile(new SubProgressMonitor(monitor, 1), file, (RetrieverMatch[]) matches.toArray(new RetrieverMatch[matches.size()]), failures);
				if (monitor.isCanceled()) {
					return false;
				}
			}
		} finally {
			monitor.done();
		}
		if (!failures.isEmpty()) {
			handleFailures((IStatus[]) failures.toArray(new IStatus[failures.size()]));
			return false;
		}

		fView.getTreeViewer().refresh();
		return true;
	}

	private void mapLines(IFile file, ArrayList lines) {
		RetrieverLine[] newLines= fSearchResult.getLinesForFile(file, false);
		for (int i= 0; i < lines.size(); i++) {
			RetrieverLine line= (RetrieverLine) lines.get(i);
			lines.set(i, findBestLine(line, newLines));
		}
	}

	private RetrieverMatch mapMatch(RetrieverMatch match) {
		RetrieverLine line= match.getLine();
		RetrieverLine[] newLines= fSearchResult.getLinesForFile(line.getParent(), false);

		RetrieverMatch bestMatch= null;
		RetrieverLine bestLine= findBestLine(line, newLines);
		if (bestLine != null) {
			int col= match.getLineOffset();
			int nearest= Integer.MAX_VALUE;
			RetrieverMatch[] candidates= bestLine.getDisplayedMatches();
			for (int i= 0; i < candidates.length; i++) {
				RetrieverMatch candidate= candidates[i];
				if (!candidate.isFiltered() && candidate.getOriginal().equals(match.getOriginal())) {
					int dist= Math.abs(candidate.getLineOffset() - col);
					if (dist < nearest) {
						dist= nearest;
						bestMatch= candidate;
					}
				}
			}
		}
		return bestMatch;
	}

	private RetrieverLine findBestLine(RetrieverLine line, RetrieverLine[] newLines) {
		// find the best line
		String lineData= line.getLine().trim();
		int lineNumber= line.getLineNumber();
		int nearest= Integer.MAX_VALUE;
		RetrieverLine best= null;
		for (int i= 0; i < newLines.length; i++) {
			RetrieverLine test= newLines[i];
			if (lineData.equals(test.getLine().trim())) {
				int dist= Math.abs(test.getLineNumber() - lineNumber);
				if (dist < nearest) {
					dist= nearest;
					best= test;
				}
			}
		}
		return best;
	}

	private boolean handleFailures(IStatus[] stati) {
		if (stati.length == 0) {
			return true;
		}

		String message= Messages.format(SearchMessages.ReplaceOperation_error_didNotSucceedForAllMatches, fDialogTitle);
		if (stati.length == 1) {
			ErrorDialog.openError(fShell, fDialogTitle, message, stati[0]);
		} else {
			MultiStatus mstatus= new MultiStatus(SearchPlugin.getID(), SearchPlugin.INTERNAL_ERROR, stati, SearchMessages.ReplaceOperation_error_multipleErrors, null);
			ErrorDialog.openError(fShell, fDialogTitle, message, mstatus);
		}
		return false;
	}

	private boolean makeCommitable(Collection files, HashSet searchedAgain) {
		if (files.isEmpty()) {
			return true;
		}

		HashSet readOnly= new HashSet();
		for (Iterator iter= files.iterator(); iter.hasNext();) {
			IFile file= (IFile) iter.next();
			if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
				if (!refreshFile(file)) {
					return false;
				}
				searchedAgain.add(file);
			}
			if (file.isReadOnly()) {
				readOnly.add(file);
			}
		}

		// try to make files writable
		if (!validateEdit(readOnly)) {
			return false;
		}

		// check files if they are still read-only
		int readOnlyCount= 0;
		for (Iterator iter= readOnly.iterator(); iter.hasNext();) {
			IFile rf= (IFile) iter.next();
			if (rf.isReadOnly()) {
				searchedAgain.remove(rf);
				readOnlyCount++;
			} else {
				searchedAgain.add(rf);
			}
		}

		// continue with all writable files?
		if (!confirmParialReplacement(files.size(), readOnlyCount)) {
			return false;
		}

		if (!searchAgain(searchedAgain)) {
			return false;
		}

		return true;
	}

	private boolean searchAgain(HashSet outdated) {
		// don't attempt to research on a restore operation!
		if (outdated.isEmpty() || !fReplace) {
			return true;
		}
		SearchAgainConfirmationDialog dialog= new SearchAgainConfirmationDialog(fShell, (ILabelProvider) fView.getTreeViewer().getLabelProvider(), Collections.EMPTY_LIST, new ArrayList(outdated));
		if (dialog.open() != IDialogConstants.OK_ID) {
			return false;
		}

		fSearchResult.searchAgain(outdated);
		return true;
	}

	private boolean refreshFile(IFile file) {
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
		} catch (CoreException e) {
			ErrorDialog.openError(fShell, fDialogTitle, SearchMessages.ReplaceOperation_error_whileRefreshing, e.getStatus());
			return false;
		}
		return true;
	}

	private boolean validateEdit(HashSet readOnly) {
		if (!readOnly.isEmpty()) {
			IStatus s= ResourcesPlugin.getWorkspace().validateEdit((IFile[]) readOnly.toArray(new IFile[readOnly.size()]), fShell);
			if (s.getSeverity() != IStatus.OK) {
				if (s.getSeverity() != IStatus.CANCEL && s.getCode() != IResourceStatus.READ_ONLY_LOCAL) {
					ErrorDialog.openError(fShell, fDialogTitle, SearchMessages.ReplaceOperation_error_whileValidateEdit, s);
				}
				return false;
			}
		}
		return true;
	}

	private boolean confirmParialReplacement(int writableFiles, int readOnlyFiles) {
		if (readOnlyFiles == 0) {
			return true;
		}
		if (writableFiles == 0) {
			MessageDialog.openError(fShell, fDialogTitle, SearchMessages.ReplaceOperation_error_allFilesReadOnly);
			return false;
		}

		String key= readOnlyFiles == 1 ? SearchMessages.ReplaceOperation_question_continueWithReadOnly_singular : SearchMessages.ReplaceOperation_question_continueWithReadOnly_plural;

		return MessageDialog.openQuestion(fShell, fDialogTitle, MessageFormat.format(key, new Object[] {new Integer(readOnlyFiles)}));
	}

	private void replaceInFile(IProgressMonitor monitor, IFile file, RetrieverMatch[] matches, Collection failures) {
		try {
			if (file.isReadOnly()) {
				return;
			}
			int ticks= 3;
			monitor.beginTask(SearchMessages.ReplaceOperation_task_performChanges, ticks);
			ITextFileBufferManager bm= FileBuffers.getTextFileBufferManager();
			try {
				ticks--;
				bm.connect(file.getFullPath(), new SubProgressMonitor(monitor, 1));
				if (monitor.isCanceled()) {
					return;
				}
				ITextFileBuffer fb= bm.getTextFileBuffer(file.getFullPath());
				boolean wasDirty= fb.isDirty();
				IDocument doc= fb.getDocument();
				PositionTracker tracker= InternalSearchUI.getInstance().getPositionTracker();
				for (int i= 0; i < matches.length; i++) {
					RetrieverMatch match= matches[i];
					if (!match.isFiltered() && (match.isReplaced() != fReplace)) {
						int offset= match.getOffset();
						int length= match.getLength();
						Position currentPosition= tracker.getCurrentPosition(match);
						if (currentPosition != null) {
							offset= currentPosition.offset;
							length= currentPosition.length;
						}
						String currentText= null;
						try {
							currentText= doc.get(offset, length);
						} catch (BadLocationException e) {
						}
						if (currentText == null || !currentText.equals(match.getCurrentText())) {
							failures.add(createErrorStatus(Messages.format(SearchMessages.ReplaceOperation_error_cannotLocateMatchAt, new Object[] {file.getFullPath(), new Integer(match.getLine().getLineNumber())})));
						} else {
							String newText= fReplace ? match.computeReplacement(fPattern, fReplacement) : match.getOriginal();
							if (newText == null) {
								failures.add(createErrorStatus(Messages.format(SearchMessages.ReplaceOperation_error_cannotComputeReplacement, new Object[] {file.getFullPath(), new Integer(match.getLine().getLineNumber())})));
							} else {
								try {
									doc.replace(offset, length, newText);
									match.setReplacement(fReplace ? newText : null);
								} catch (BadLocationException e) {
									failures.add(createErrorStatus(Messages.format(SearchMessages.ReplaceOperation_error_cannotLocateMatchAt, new Object[] {file, new Integer(match.getLine().getLineNumber())})));
								}
							}
						}
					}
				}
				if (!wasDirty) {
					ticks--;
					fb.commit(new SubProgressMonitor(monitor, 1), true);
				}
			} finally {
				bm.disconnect(file.getFullPath(), new SubProgressMonitor(monitor, ticks));
			}
		} catch (CoreException e) {
			failures.add(e.getStatus());
		} finally {
			monitor.done();
		}
	}

	private Status createErrorStatus(String message) {
		return new Status(IStatus.ERROR, SearchPlugin.getID(), SearchPlugin.INTERNAL_ERROR, message, null);
	}

	public boolean wasSuccessful() {
		return fSuccess;
	}
}
