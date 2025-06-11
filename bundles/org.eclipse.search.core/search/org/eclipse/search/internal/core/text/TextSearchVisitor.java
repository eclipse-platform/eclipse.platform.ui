/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Terry Parker <tparker@google.com> (Google Inc.) - Bug 441016 - Speed up text search by parallelizing it using JobGroups
 *     Sergey Prigogin (Google) - Bug 489551 - File Search silently drops results on StackOverflowError
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.io.CharConversionException;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.IDocument;

import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.SearchCoreMessages;
import org.eclipse.search.internal.core.SearchCorePlugin;
import org.eclipse.search.internal.core.text.FileCharSequenceProvider.FileCharSequenceException;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor {

	public static final boolean TRACING= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.search/perf")); //$NON-NLS-1$ //$NON-NLS-2$
	private static final int NUMBER_OF_LOGICAL_THREADS= Runtime.getRuntime().availableProcessors();

	/**
	 * Queue of files to be searched. IFile pointing to the same local file are
	 * grouped together
	 **/
	private final Queue<List<IFile>> fileBatches;

	public static class ReusableMatchAccess extends TextSearchMatchAccess {

		private int fOffset;
		private int fLength;
		private IFile fFile;
		private CharSequence fContent;

		public void initialize(IFile file, int offset, int length, CharSequence content) {
			fFile= file;
			fOffset= offset;
			fLength= length;
			fContent= content;
		}

		@Override
		public IFile getFile() {
			return fFile;
		}

		@Override
		public int getMatchOffset() {
			return fOffset;
		}

		@Override
		public int getMatchLength() {
			return fLength;
		}

		@Override
		public int getFileContentLength() {
			return fContent.length();
		}

		@Override
		public char getFileContentChar(int offset) {
			return fContent.charAt(offset);
		}

		@Override
		public String getFileContent(int offset, int length) {
			return fContent.subSequence(offset, offset + length).toString(); // must pass a copy!
		}
	}

	/**
	 * A JobGroup for text searches across multiple files.
	 */
	private static class TextSearchJobGroup extends JobGroup {
		public TextSearchJobGroup(String name, int maxThreads, int initialJobCount) {
			super(name, maxThreads, initialJobCount);
		}

		// Always continue processing all other files, even if errors are encountered in individual files.
		@Override
		protected boolean shouldCancel(IStatus lastCompletedJobResult, int numberOfFailedJobs, int numberOfCancelledJobs) {
			return false;
		}
	}

	/**
	 * A job to find matches in a set of files.
	 */
	private class TextSearchJob extends Job {
		private final Map<IFile, IDocument> fDocumentsInEditors;
		private FileCharSequenceProvider fileCharSequenceProvider;
		private final int jobCount;

		/**
		 * Searches for matches in the files.
		 *
		 * @param documentsInEditors
		 *            a map from IFile to IDocument for all open, dirty editors
		 * @param jobCount
		 *            number of Jobs
		 */
		public TextSearchJob(Map<IFile, IDocument> documentsInEditors, int jobCount) {
			super("File Search Worker"); //$NON-NLS-1$
			this.jobCount = jobCount;
			setSystem(true);
			fDocumentsInEditors= documentsInEditors;
		}

		@Override
		protected IStatus run(IProgressMonitor inner) {
			MultiStatus multiStatus=
					new MultiStatus(SearchCorePlugin.PLUGIN_ID, IStatus.OK,
							SearchCoreMessages.TextSearchEngine_statusMessage, null);
			SubMonitor subMonitor = SubMonitor.convert(inner, fileBatches.size() / jobCount); // approximate
			this.fileCharSequenceProvider= new FileCharSequenceProvider();
			List<IFile> sameFiles;
			while (((sameFiles = fileBatches.poll()) != null) && !fFatalError && !fProgressMonitor.isCanceled()) {
				IStatus status = processFile(sameFiles, subMonitor.split(1));
				// Only accumulate interesting status
				if (!status.isOK())
					multiStatus.add(status);
				// Group cancellation is propagated to this job's monitor.
				// Stop processing and return the status for the completed jobs.
			}
			fileCharSequenceProvider= null;
			synchronized (fLock) {
				fLock.notify();
			}
			return multiStatus;
		}

		public IStatus processFile(List<IFile> sameFiles, IProgressMonitor monitor) {
			// A natural cleanup after the change to use JobGroups is accepted would be to move these
			// methods to the TextSearchJob class.
			Matcher matcher= fSearchPattern.pattern().isEmpty() ? null : fSearchPattern.matcher(""); //$NON-NLS-1$
			IFile file = sameFiles.remove(0);
			monitor.setTaskName(file.getFullPath().toString());
			try {
				if (!fCollector.acceptFile(file) || matcher == null) {
					return Status.OK_STATUS;
				}

				List<TextSearchMatchAccess> occurences;
				CharSequence charsequence;

				IDocument document= getOpenDocument(file, getDocumentsInEditors());
				if (document != null) {
					charsequence = new DocumentCharSequence(document);
					// assume all documents are non-binary
					occurences = locateMatches(file, charsequence, matcher, monitor);
				} else {
					try {
						boolean reportTextOnly = !fCollector.reportBinaryFile(file);
						if (reportTextOnly && hasBinaryContentType(file)) {
							// fail fast for binary file types without opening the file
							return Status.OK_STATUS;
						}
						charsequence = fileCharSequenceProvider.newCharSequence(file);
						if (reportTextOnly && hasBinaryContent(charsequence)) {
							return Status.OK_STATUS;
						}
						occurences = locateMatches(file, charsequence, matcher, monitor);
					} catch (FileCharSequenceProvider.FileCharSequenceException e) {
						if (e.getCause() instanceof RuntimeException runtimeEx) {
							throw runtimeEx;
						}
						throw e;
					}
				}
				fCollector.flushMatches(file);

				for (IFile duplicateFiles : sameFiles) {
					// reuse previous result
					ReusableMatchAccess matchAccess= new ReusableMatchAccess();
					for (TextSearchMatchAccess occurence : occurences) {
						matchAccess.initialize(duplicateFiles, occurence.getMatchOffset(), occurence.getMatchLength(),
								charsequence);
						boolean goOn= fCollector.acceptPatternMatch(matchAccess);
						if (!goOn) {
							break;
						}
					}
					fCollector.flushMatches(duplicateFiles);
				}
				if (document == null) {
					try {
						fileCharSequenceProvider.releaseCharSequence(charsequence);
					} catch (IOException e) {
						SearchCorePlugin.log(e);
					}
				}
			} catch (UnsupportedCharsetException e) {
				Object[] args= { getCharSetName(file), file.getFullPath().makeRelative().toString()};
				String message = MessageFormat.format(SearchCoreMessages.TextSearchVisitor_unsupportedcharset, args);
				return new Status(IStatus.ERROR, SearchCorePlugin.PLUGIN_ID, IStatus.ERROR, message, e);
			} catch (IllegalCharsetNameException e) {
				Object[] args= { getCharSetName(file), file.getFullPath().makeRelative().toString()};
				String message = MessageFormat.format(SearchCoreMessages.TextSearchVisitor_illegalcharset, args);
				return new Status(IStatus.ERROR, SearchCorePlugin.PLUGIN_ID, IStatus.ERROR, message, e);
			} catch (IOException e) {
				Object[] args= { getExceptionMessage(e), file.getFullPath().makeRelative().toString()};
				String message = MessageFormat.format(SearchCoreMessages.TextSearchVisitor_error, args);
				return new Status(IStatus.ERROR, SearchCorePlugin.PLUGIN_ID, IStatus.ERROR, message, e);
			} catch (CoreException e) {
				if (fIsLightweightAutoRefresh && IResourceStatus.RESOURCE_NOT_FOUND == e.getStatus().getCode()) {
					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}
				Object[] args= { getExceptionMessage(e), file.getFullPath().makeRelative().toString() };
				String message = MessageFormat.format(SearchCoreMessages.TextSearchVisitor_error, args);
				return new Status(IStatus.ERROR, SearchCorePlugin.PLUGIN_ID, IStatus.ERROR, message, e);
			} catch (StackOverflowError e) {
				fFatalError= true;
				String message= SearchCoreMessages.TextSearchVisitor_patterntoocomplex0;
				return new Status(IStatus.ERROR, SearchCorePlugin.PLUGIN_ID, IStatus.ERROR, message, e);
			} finally {
				synchronized (fLock) {
					fCurrentFile= file;
					fNumberOfScannedFiles++;
				}
			}
			if (monitor.isCanceled()) {
				fFatalError = true;
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}

		public Map<IFile, IDocument> getDocumentsInEditors() {
			return fDocumentsInEditors;
		}

	}


	private final TextSearchRequestor fCollector;
	private final Pattern fSearchPattern;

	private volatile IProgressMonitor fProgressMonitor;

	private int fNumberOfScannedFiles;  // Protected by fLock
	private IFile fCurrentFile;  // Protected by fLock
	private final Object fLock = new Object();

	private final MultiStatus fStatus;
	private volatile boolean fFatalError; // If true, terminates the search.

	private volatile boolean fIsLightweightAutoRefresh;
	private DirtyFileProvider fDirtyDiscovery;

	public TextSearchVisitor(TextSearchRequestor collector, Pattern searchPattern, DirtyFileProvider dirtyDiscovery) {
		fCollector= collector;
		fDirtyDiscovery = dirtyDiscovery;
		fStatus = new MultiStatus(SearchCorePlugin.PLUGIN_ID, IStatus.OK,
				SearchCoreMessages.TextSearchEngine_statusMessage, null);

		fSearchPattern= searchPattern;

		fIsLightweightAutoRefresh= Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false, null);
		fileBatches = new ConcurrentLinkedQueue<>();
	}

	public IStatus search(IFile[] files, IProgressMonitor monitor) {
		if (files.length == 0) {
			return fStatus;
		}
		fProgressMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		synchronized (fLock) {
			fNumberOfScannedFiles = 0;
			fCurrentFile = null;
		}
		int threadsNeeded = Math.min(files.length, NUMBER_OF_LOGICAL_THREADS);
		// All but 1 threads should search. 1 thread does the UI updates:
		int jobCount = fCollector.canRunInParallel() && threadsNeeded > 1 ? threadsNeeded - 1 : 1;
		long startTime= TRACING ? System.currentTimeMillis() : 0;

		try {
			String taskName= fSearchPattern.pattern().isEmpty()
					? SearchCoreMessages.TextSearchVisitor_filesearch_task_label
					: ""; //$NON-NLS-1$
			try {
				fCollector.beginReporting();
				if (fProgressMonitor.isCanceled()) {
					throw new OperationCanceledException(SearchCoreMessages.TextSearchVisitor_canceled);
				}

				Map<IFile, IDocument> documentsInEditors = findDirtyFiles();

				// group files with same content together:
				Map<String, List<IFile>> localFilesByLocation = new LinkedHashMap<>();
				Map<String, List<IFile>> remoteFilesByLocation = new LinkedHashMap<>();

				for (IFile file : files) {
					IPath path = file.getLocation();
					String key = path == null ? file.getLocationURI().toString() : path.toString();
					Map<String, List<IFile>> filesByLocation = (path != null) ? localFilesByLocation
							: remoteFilesByLocation;
					filesByLocation.computeIfAbsent(key, k -> new ArrayList<>()).add(file);

				}
				localFilesByLocation.values().forEach(fileBatches::offer);
				remoteFilesByLocation.values().forEach(fileBatches::offer);
				int numberOfFilesToScan = fileBatches.size();
				fProgressMonitor.beginTask(taskName, numberOfFilesToScan);

				// Seed count over 1 can cause endless waits, see bug 543629
				// comment 2
				// TODO use seed = jobCount after the bug 543660 in JobGroup is
				// fixed

				final int seed = 1;
				final JobGroup jobGroup = new TextSearchJobGroup("Text Search", jobCount, seed); //$NON-NLS-1$
				for (int i = 0; i < jobCount; i++) {
					Job job = new TextSearchJob(documentsInEditors, jobCount);
					job.setJobGroup(jobGroup);
					job.schedule();
				}
				// update progress until finished or canceled:
				int numberOfScannedFiles = 0;
				int lastNumberOfScannedFiles = 0;
				while (!fProgressMonitor.isCanceled() && !jobGroup.getActiveJobs().isEmpty()
						&& numberOfScannedFiles != numberOfFilesToScan) {
					IFile file;
					synchronized (fLock) {
						try {
							// time only relevant on how often progress is
							// updated, but cancel is notified immediately:
							fLock.wait(100);
						} catch (InterruptedException e) {
							fProgressMonitor.setCanceled(true);
							break;
						}
						file = fCurrentFile;
						numberOfScannedFiles = fNumberOfScannedFiles;
					}
					if (file != null) {
						String fileName = file.getName();
						Object[] args = { fileName, Integer.valueOf(numberOfScannedFiles),
								Integer.valueOf(numberOfFilesToScan) };
						fProgressMonitor
								.subTask(MessageFormat.format(SearchCoreMessages.TextSearchVisitor_scanning, args));
						int steps = numberOfScannedFiles - lastNumberOfScannedFiles;
						fProgressMonitor.worked(steps);
						lastNumberOfScannedFiles += steps;
					}
				}
				if (fProgressMonitor.isCanceled()) {
					jobGroup.cancel();
				}
				// no need to pass progressMonitor (which would show wrong
				// progress) but null because jobGroup was already finished /
				// canceled anyway:
				jobGroup.join(0, null);
				if (fProgressMonitor.isCanceled()) {
					throw new OperationCanceledException(SearchCoreMessages.TextSearchVisitor_canceled);
				}

				fStatus.addAll(jobGroup.getResult());
				return fStatus;
			} catch (InterruptedException e) {
				throw new OperationCanceledException(SearchCoreMessages.TextSearchVisitor_canceled);
			} finally {
				fileBatches.clear();
			}
		} finally {
			fProgressMonitor.done();
			fCollector.endReporting();
			if (TRACING) {
				Object[] args= { Integer.valueOf(fNumberOfScannedFiles), Integer.valueOf(jobCount), Integer.valueOf(NUMBER_OF_LOGICAL_THREADS), Long.valueOf(System.currentTimeMillis() - startTime) };
				System.out.println(MessageFormat.format(
						"[TextSearch] Search duration for {0} files in {1} jobs using {2} threads: {3}ms", args)); //$NON-NLS-1$
			}
		}
	}

	private Map<IFile, IDocument> findDirtyFiles() {
		if (fDirtyDiscovery != null) {
			Map<IFile, IDocument> ret = fDirtyDiscovery.dirtyFiles();
			if (ret != null)
				return ret;
		}
		return Collections.emptyMap();
	}

	public IStatus search(TextSearchScope scope, IProgressMonitor monitor) {
		return search(scope.evaluateFilesInScope(fStatus), monitor);
	}

	private final IContentType TEXT_TYPE = Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);

	private boolean hasBinaryContentType(IFile file) {
		IContentType[] contentTypes;
		try (java.io.InputStream contents = file.getContents()) {
			contentTypes = Platform.getContentTypeManager().findContentTypesFor(contents, file.getName());
		} catch (IOException | CoreException e) {
			SearchCorePlugin.log(e);
			contentTypes = Platform.getContentTypeManager().findContentTypesFor(file.getName());
		}
		for (IContentType contentType : contentTypes) {
			if (contentType.isKindOf(TEXT_TYPE)) {
				return false; // is text
			}
		}
		if (contentTypes.length > 0) {
			return true; // has some not text type
		}
		return false; // unknown
	}

	private boolean hasBinaryContent(CharSequence seq) {
		if (seq instanceof String s) {
			return (s.contains("\0")); //$NON-NLS-1$
		}
		// avoid calling seq.length() at it runs through the complete file,
		// thus it would do so for all binary files.
		try {
			int limit= FileCharSequenceProvider.BUFFER_SIZE;
			for (int i= 0; i < limit; i++) {
				if (seq.charAt(i) == '\0') {
					return true;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			// ignored
		} catch (FileCharSequenceException ex) {
			if (ex.getCause() instanceof CharConversionException)
				return true;
			throw ex;
		}
		return false;
	}

	private List<TextSearchMatchAccess> locateMatches(IFile file, CharSequence searchInput, Matcher matcher, IProgressMonitor monitor) throws CoreException {
		List<TextSearchMatchAccess> occurences= null;
		matcher.reset(searchInput);
		// Check for cancellation before calling matcher.find() since that call
		// can be very expensive
		while (!monitor.isCanceled() && matcher.find()) {
			if (occurences == null) {
				occurences= new ArrayList<>();
			}
			int start= matcher.start();
			int end= matcher.end();
			if (end != start) { // don't report 0-length matches
				ReusableMatchAccess access= new ReusableMatchAccess();
				access.initialize(file, start, end - start, searchInput);
				occurences.add(access);
				boolean res= fCollector.acceptPatternMatch(access);
				if (!res) {
					return occurences; // no further reporting requested
				}
			}
		}
		if (occurences == null) {
			occurences= Collections.emptyList();
		}
		return occurences;
	}


	private String getExceptionMessage(Exception e) {
		String message= e.getLocalizedMessage();
		if (message == null) {
			return e.getClass().getName();
		}
		return message;
	}

	private IDocument getOpenDocument(IFile file, Map<IFile, IDocument> documentsInEditors) {
		IDocument document= documentsInEditors.get(file);
		if (document == null) {
			ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
			ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			if (textFileBuffer != null) {
				document= textFileBuffer.getDocument();
			}
		}
		return document;
	}

	private String getCharSetName(IFile file) {
		try {
			return file.getCharset();
		} catch (CoreException e) {
			return "unknown"; //$NON-NLS-1$
		}
	}

}
