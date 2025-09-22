/*******************************************************************************
 * Copyright (c) 2007, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.PositionTracker;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;


public class ReplaceRefactoring extends Refactoring {

	private static class MatchGroup {
		public TextEditChangeGroup group;
		public FileMatch match;

		public MatchGroup(TextEditChangeGroup group, FileMatch match) {
			this.group= group;
			this.match= match;
		}
	}

	public static class SearchResultUpdateChange extends Change {

		private MatchGroup[] fMatchGroups;
		private Match[] fMatches;

		private final Map<URI, ArrayList<FileMatch>> fIgnoredMatches;
		private final FileSearchResult fResult;
		private final boolean fIsRemove;

		public SearchResultUpdateChange(FileSearchResult result, MatchGroup[] matchGroups, Map<URI, ArrayList<FileMatch>> ignoredMatches) {
			this(result, null, ignoredMatches, true);
			fMatchGroups= matchGroups;
		}

		private SearchResultUpdateChange(FileSearchResult result, Match[] matches, Map<URI, ArrayList<FileMatch>> ignoredMatches, boolean isRemove) {
			fResult= result;
			fMatches= matches;
			fIgnoredMatches= ignoredMatches;
			fIsRemove= isRemove;
		}

		@Override
		public Object getModifiedElement() {
			return null;
		}

		@Override
		public String getName() {
			return SearchMessages.ReplaceRefactoring_result_update_name;
		}

		@Override
		public void initializeValidationData(IProgressMonitor pm) {
		}

		@Override
		public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}

		private Match[] getMatches() {
			if (fMatches == null) {
				ArrayList<FileMatch> matches= new ArrayList<>();
				for (MatchGroup curr : fMatchGroups) {
					if (curr.group.isEnabled()) {
						FileMatch match= curr.match;
						matches.add(match);

						if (fIgnoredMatches == null) {
							continue;
						}

						// Add matches that we removed before starting the refactoring
						IFile file= match.getFile();
						URI uri= file.getLocationURI();
						if (uri != null) {
							ArrayList<FileMatch> ignoredMatches= fIgnoredMatches.get(uri);
							if (ignoredMatches != null) {
								matches.addAll(ignoredMatches);
							}
						}
					}
				}
				fMatches= matches.toArray(new Match[matches.size()]);
				fMatchGroups= null;
			}
			return fMatches;
		}

		@Override
		public Change perform(IProgressMonitor pm) throws CoreException {
			Match[] matches= getMatches();
			if (fIsRemove) {
				fResult.removeMatches(matches);
			} else {
				fResult.addMatches(matches);
			}
			return new SearchResultUpdateChange(fResult, matches, fIgnoredMatches, !fIsRemove);
		}

	}



	private final FileSearchResult fResult;
	private final Object[] fSelection;

	private final HashMap<IFile, Set<FileMatch>> fMatches;

	/**
	 * Map that keeps already collected locations. Contains both keys:
	 * IFileStore and URI, see URIUtil.equals(URI, URI)
	 */
	private final Map<Object, IFile> fAlreadyCollected;

	/** Map that keeps ignored matches (can be null). */
	private Map<URI, ArrayList<FileMatch>> fIgnoredMatches;

	private String fReplaceString;

	private Change fChange;

	public ReplaceRefactoring(FileSearchResult result, Object[] selection) {
		Assert.isNotNull(result);

		fResult= result;
		fSelection= selection;

		fMatches= new HashMap<>();
		fAlreadyCollected= new HashMap<>(selection != null ? selection.length : result.getElements().length);

		fReplaceString= null;
	}

	@Override
	public String getName() {
		return SearchMessages.ReplaceRefactoring_refactoring_name;
	}

	public void setReplaceString(String string) {
		fReplaceString= string;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		String searchString= getQuery().getSearchString();
		if (searchString.isEmpty()) {
			return RefactoringStatus.createFatalErrorStatus(SearchMessages.ReplaceRefactoring_error_illegal_search_string);
		}
		fMatches.clear();

		if (fSelection != null) {
			SubMonitor progress = SubMonitor.convert(pm);
			// "Unknown" progress, because selected elements can be containers
			progress.setWorkRemaining(100_000);
			for (Object element : fSelection) {
				collectMatches(element, progress);
			}
		} else {
			Object[] elements= fResult.getElements();
			SubMonitor progress = SubMonitor.convert(pm, elements.length);
			for (Object element : elements) {
				collectMatches(element, progress.split(1));
			}
		}
		if (!hasMatches()) {
			return RefactoringStatus.createFatalErrorStatus(SearchMessages.ReplaceRefactoring_error_no_matches);
		}
		return new RefactoringStatus();
	}

	private void collectMatches(Object object, SubMonitor progress) throws CoreException {
		progress.checkCanceled();
		if (object instanceof LineElement lineElement) {
			FileMatch[] matches= lineElement.getMatches(fResult);
			for (FileMatch fileMatch : matches) {
				if (isMatchToBeIncluded(fileMatch)) {
					getBucket(fileMatch.getFile()).add(fileMatch);
				}
			}
		} else if (object instanceof IContainer container) {
			IResource[] members= container.members();
			for (IResource member : members) {
				collectMatches(member, progress);
			}
		} else if (object instanceof IFile) {
			Match[] matches= fResult.getMatches(object);
			if (matches.length > 0) {
				Collection<FileMatch> bucket= null;
				for (Match match : matches) {
					FileMatch fileMatch= (FileMatch) match;
					if (isMatchToBeIncluded(fileMatch)) {
						if (bucket == null) {
							bucket= getBucket((IFile)object);
						}
						bucket.add(fileMatch);
					}
				}
			}
		}
		progress.worked(1);
	}

	public int getNumberOfFiles() {
		return fMatches.size();
	}

	public int getNumberOfMatches() {
		int count= 0;
		for (Set<FileMatch> bucket : fMatches.values()) {
			count += bucket.size();
		}
		return count;
	}

	public boolean hasMatches() {
		return !fMatches.isEmpty();
	}

	/**
	 * Checks whether the match should be included. Also collects ignored matches whose
	 * file is linked to an already collected match.
	 *
	 * @param match the match
	 * @return <code>true</code> iff the match should be included
	 * @since 3.7
	 */
	private boolean isMatchToBeIncluded(FileMatch match) {
		IFile file = match.getFile();
		URI uri = file.getLocationURI();
		if (uri == null) {
			return true;
		}
		if (file.equals(fAlreadyCollected.get(uri))) {
			return true; // another FileMatch for an IFile which already had
							// matches
		}
		Object key;
		try {
			key = EFS.getStore(uri);
		} catch (CoreException e) {
			// fall back to default equality test
			key = uri;
		}
		if (fAlreadyCollected.containsKey(key)) {
			if (fIgnoredMatches == null) {
				fIgnoredMatches = new HashMap<>();
			}
			ArrayList<FileMatch> matches = fIgnoredMatches.get(uri);
			if (matches == null) {
				matches = new ArrayList<>();
				fIgnoredMatches.put(uri, matches);
			}
			matches.add(match);
			return false;
		}

		fAlreadyCollected.put(key, file);
		fAlreadyCollected.put(uri, file);
		return true;
	}

	private Set<FileMatch> getBucket(IFile file) {
		Set<FileMatch> set= fMatches.get(file);
		if (set == null) {
			set= new HashSet<>();
			fMatches.put(file, set);
		}
		return set;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (fReplaceString == null) {
			return RefactoringStatus.createFatalErrorStatus(SearchMessages.ReplaceRefactoring_error_no_replace_string);
		}

		Pattern pattern= null;
		FileSearchQuery query= getQuery();
		if (query.isRegexSearch()) {
			pattern= createSearchPattern(query);
		}

		RefactoringStatus resultingStatus= new RefactoringStatus();

		Collection<IFile> allFilesSet= fMatches.keySet();
		IFile[] allFiles= allFilesSet.toArray(new IFile[allFilesSet.size()]);
		Arrays.sort(allFiles, new Comparator<IFile>() {
			private final Collator fCollator= Collator.getInstance();
			@Override
			public int compare(IFile o1, IFile o2) {
				String p1= o1.getFullPath().toString();
				String p2= o2.getFullPath().toString();
				return fCollator.compare(p1, p2);
			}
		});
		int workSize = allFiles.length;
		SubMonitor progress = SubMonitor.convert(pm, workSize * 2);
		checkFilesToBeChanged(allFiles, resultingStatus, progress.split(workSize));
		if (resultingStatus.hasFatalError()) {
			return resultingStatus;
		}
		progress.setWorkRemaining(workSize);
		CompositeChange compositeChange= new CompositeChange(SearchMessages.ReplaceRefactoring_composite_change_name);
		compositeChange.markAsSynthetic();

		ArrayList<MatchGroup> matchGroups= new ArrayList<>();
		boolean hasChanges= false;
		try {
			for (IFile file : allFiles) {
				progress.checkCanceled();
				Set<FileMatch> bucket= fMatches.get(file);
				if (!bucket.isEmpty()) {
					try {
						TextChange change = createFileChange(file, pattern, bucket, resultingStatus, matchGroups,
								progress);
						if (change != null) {
							compositeChange.add(change);
							hasChanges= true;
						}
					} catch (CoreException e) {
						String message= Messages.format(SearchMessages.ReplaceRefactoring_error_access_file, new Object[] { file.getName(), e.getLocalizedMessage() });
						return RefactoringStatus.createFatalErrorStatus(message);
					}
				}
				progress.worked(1);
			}
		} catch (PatternSyntaxException e) {
			String message= Messages.format(SearchMessages.ReplaceRefactoring_error_replacement_expression, e.getLocalizedMessage());
			return RefactoringStatus.createFatalErrorStatus(message);
		}
		if (!hasChanges && resultingStatus.isOK()) {
			return RefactoringStatus.createFatalErrorStatus(SearchMessages.ReplaceRefactoring_error_no_changes);
		}

		compositeChange.add(new SearchResultUpdateChange(fResult, matchGroups.toArray(new MatchGroup[matchGroups.size()]), fIgnoredMatches));

		fChange= compositeChange;
		return resultingStatus;
	}

	private void checkFilesToBeChanged(IFile[] filesToBeChanged, RefactoringStatus resultingStatus, SubMonitor pm)
			throws CoreException {
		ArrayList<IFile> readOnly= new ArrayList<>();
		for (IFile file : filesToBeChanged) {
			pm.checkCanceled();
			if (file.isReadOnly()) {
				readOnly.add(file);
			}
		}
		IFile[] readOnlyFiles= readOnly.toArray(new IFile[readOnly.size()]);

		IStatus status= ResourcesPlugin.getWorkspace().validateEdit(readOnlyFiles, getValidationContext());
		if (status.getSeverity() == IStatus.CANCEL) {
			throw new OperationCanceledException();
		}
		resultingStatus.merge(RefactoringStatus.create(status));
		if (resultingStatus.hasFatalError()) {
			return;
		}
		resultingStatus.merge(ResourceChangeChecker.checkFilesToBeChanged(filesToBeChanged, null));
	}

	private TextChange createFileChange(IFile file, Pattern pattern, Set<FileMatch> matches,
			RefactoringStatus resultingStatus, Collection<MatchGroup> matchGroups, SubMonitor pm)
			throws PatternSyntaxException, CoreException {
		PositionTracker tracker= InternalSearchUI.getInstance().getPositionTracker();

		TextFileChange change= new TextFileChange(Messages.format(SearchMessages.ReplaceRefactoring_group_label_change_for_file, file.getName()), file);
		change.setEdit(new MultiTextEdit());

		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		manager.connect(file.getFullPath(), LocationKind.IFILE, null);
		try {
			ITextFileBuffer textFileBuffer= manager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			if (textFileBuffer == null) {
				resultingStatus.addError(Messages.format(SearchMessages.ReplaceRefactoring_error_accessing_file_buffer, file.getName()));
				return null;
			}
			IDocument document= textFileBuffer.getDocument();
			String lineDelimiter= TextUtilities.getDefaultLineDelimiter(document);

			for (FileMatch match : matches) {
				pm.checkCanceled();
				int offset= match.getOffset();
				int length= match.getLength();
				Position currentPosition= tracker.getCurrentPosition(match);
				if (currentPosition != null) {
					offset= currentPosition.offset;
					if (length != currentPosition.length) {
						resultingStatus.addError(Messages.format(SearchMessages.ReplaceRefactoring_error_match_content_changed, file.getName()));
						continue;
					}
				}

				String originalText= getOriginalText(document, offset, length);
				if (originalText == null) {
					resultingStatus.addError(Messages.format(SearchMessages.ReplaceRefactoring_error_match_content_changed, file.getName()));
					continue;
				}

				String replacementString= computeReplacementString(pattern, originalText, fReplaceString, lineDelimiter);
				if (replacementString == null) {
					resultingStatus.addError(Messages.format(SearchMessages.ReplaceRefactoring_error_match_content_changed, file.getName()));
					continue;
				}

				ReplaceEdit replaceEdit= new ReplaceEdit(offset, length, replacementString);
				change.addEdit(replaceEdit);
				TextEditChangeGroup textEditChangeGroup= new TextEditChangeGroup(change, new TextEditGroup(SearchMessages.ReplaceRefactoring_group_label_match_replace, replaceEdit));
				change.addTextEditChangeGroup(textEditChangeGroup);
				matchGroups.add(new MatchGroup(textEditChangeGroup, match));
			}
		} finally {
			manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
		}
		return change;
	}

	private static String getOriginalText(IDocument doc, int offset, int length) {
		try {
			return doc.get(offset, length);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private Pattern createSearchPattern(FileSearchQuery query) {
		return PatternConstructor.createPattern(query.getSearchString(), true, true, query.isCaseSensitive(), false);
	}

	private String computeReplacementString(Pattern pattern, String originalText, String replacementText, String lineDelimiter) throws PatternSyntaxException {
		if (pattern != null) {
			try {
				replacementText= PatternConstructor.interpretReplaceEscapes(replacementText, originalText, lineDelimiter);

				Matcher matcher= pattern.matcher(originalText);
				StringBuilder sb = new StringBuilder();
				matcher.reset();
				if (matcher.find()) {
					matcher.appendReplacement(sb, replacementText);
				} else {
					return null;
				}
				matcher.appendTail(sb);
				return sb.toString();
			} catch (IndexOutOfBoundsException ex) {
				throw new PatternSyntaxException(ex.getLocalizedMessage(), replacementText, -1);
			}
		}
		return replacementText;
	}

	public FileSearchQuery getQuery() {
		return (FileSearchQuery) fResult.getQuery();
	}


	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fChange;
	}

}
