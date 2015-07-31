/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Terry Parker <tparker@google.com> (Google Inc.) - Bug 441016 - Speed up text search by parallelizing it using JobGroups
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.io.CharConversionException;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
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

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.text.FileCharSequenceProvider.FileCharSequenceException;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;

/**
 * The visitor that does the actual work.
 */
public class TextSearchVisitor {

	public static final boolean TRACING= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.search/perf")); //$NON-NLS-1$ //$NON-NLS-2$
	private static final int NUMBER_OF_LOGICAL_THREADS= Runtime.getRuntime().availableProcessors();
	private static final int FILES_PER_JOB= 50;

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

		public IFile getFile() {
			return fFile;
		}

		public int getMatchOffset() {
			return fOffset;
		}

		public int getMatchLength() {
			return fLength;
		}

		public int getFileContentLength() {
			return fContent.length();
		}

		public char getFileContentChar(int offset) {
			return fContent.charAt(offset);
		}

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
		protected boolean shouldCancel(IStatus lastCompletedJobResult, int numberOfFailedJobs, int numberOfCancelledJobs) {
			return false;
		}
	}

	/**
	 * A job to find matches in a set of files.
	 */
	private class TextSearchJob extends Job {
		private final IFile[] fFiles;
		private final int fBegin;
		private final int fEnd;
		private final Map fDocumentsInEditors;
		private ReusableMatchAccess fReusableMatchAccess;
		private IProgressMonitor fMonitor;

		/**
		 * Searches for matches in a set of files.
		 *
		 * @param files an array of IFiles, a portion of which is to be processed
		 * @param begin the first element in the file array to process
		 * @param end one past the last element in the array to process
		 * @param documentsInEditors a map from IFile to IDocument for all open, dirty editors
		 */
		public TextSearchJob(IFile[] files, int begin, int end, Map documentsInEditors) {
			super(files[begin].getName());
			setSystem(true);
			fFiles = files;
			fBegin = begin;
			fEnd = end;
			fDocumentsInEditors = documentsInEditors;
		}

		protected IStatus run(IProgressMonitor inner) {
			fMonitor= inner;
			MultiStatus multiStatus=
					new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);
			for (int i = fBegin; i < fEnd; i++) {
				IStatus status= processFile(fFiles[i], this);
				// Only accumulate interesting status
				if (!status.isOK())
					multiStatus.add(status);
				// Group cancellation is propagated to this job's monitor.
				// Stop processing and return the status for the completed jobs.
				if (inner.isCanceled())
					break;
			}
			return multiStatus;
		}

		public IProgressMonitor getMonitor() {
			return fMonitor;
		}

		public Map getDocumentsInEditors() {
			return fDocumentsInEditors;
		}

		public ReusableMatchAccess getReusableMatchAccess() {
			if (fReusableMatchAccess == null)
				fReusableMatchAccess = new ReusableMatchAccess();
			return fReusableMatchAccess;
		}
	}


	private final TextSearchRequestor fCollector;
	private final Pattern fSearchPattern;

	private IProgressMonitor fProgressMonitor;

	private int fNumberOfScannedFiles;
	private int fNumberOfFilesToScan;
	private IFile fCurrentFile;
	private Object fLock= new Object();

	private final MultiStatus fStatus;

	private boolean fIsLightweightAutoRefresh;

	public TextSearchVisitor(TextSearchRequestor collector, Pattern searchPattern) {
		fCollector= collector;
		fStatus= new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);

		fSearchPattern= searchPattern;

		fIsLightweightAutoRefresh= Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false, null);
	}

	public IStatus search(IFile[] files, IProgressMonitor monitor) {
		if (files.length == 0) {
			return fStatus;
		}
		fProgressMonitor= monitor == null ? new NullProgressMonitor() : monitor;
		fNumberOfScannedFiles= 0;
		fNumberOfFilesToScan= files.length;
		fCurrentFile= null;
		int maxThreads= fCollector.canRunInParallel() ? NUMBER_OF_LOGICAL_THREADS : 1;
		int jobCount= 1;
		if (maxThreads > 1) {
			jobCount= (files.length + FILES_PER_JOB - 1) / FILES_PER_JOB;
		}
		final JobGroup jobGroup= new TextSearchJobGroup("Text Search", maxThreads, jobCount); //$NON-NLS-1$
		long startTime= TRACING ? System.currentTimeMillis() : 0;

		Job monitorUpdateJob= new Job(SearchMessages.TextSearchVisitor_progress_updating_job) {
			private int fLastNumberOfScannedFiles= 0;

			public IStatus run(IProgressMonitor inner) {
				while (!inner.isCanceled()) {
					// Propagate user cancellation to the JobGroup.
					if (fProgressMonitor.isCanceled()) {
						jobGroup.cancel();
						break;
					}

					IFile file;
					int numberOfScannedFiles;
					synchronized (fLock) {
						file= fCurrentFile;
						numberOfScannedFiles= fNumberOfScannedFiles;
					}
					if (file != null) {
						String fileName= file.getName();
						Object[] args= { fileName, new Integer(numberOfScannedFiles), new Integer(fNumberOfFilesToScan)};
						fProgressMonitor.subTask(Messages.format(SearchMessages.TextSearchVisitor_scanning, args));
						int steps= numberOfScannedFiles - fLastNumberOfScannedFiles;
						fProgressMonitor.worked(steps);
						fLastNumberOfScannedFiles += steps;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return Status.OK_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		};

		try {
			String taskName= fSearchPattern.pattern().length() == 0
					? SearchMessages.TextSearchVisitor_filesearch_task_label
					: Messages.format(SearchMessages.TextSearchVisitor_textsearch_task_label, fSearchPattern.pattern());
			fProgressMonitor.beginTask(taskName, fNumberOfFilesToScan);
			monitorUpdateJob.setSystem(true);
			monitorUpdateJob.schedule();
			try {
				fCollector.beginReporting();
				Map documentsInEditors= PlatformUI.isWorkbenchRunning() ? evalNonFileBufferDocuments() : Collections.EMPTY_MAP;
				int filesPerJob = (files.length + jobCount - 1) / jobCount;
				for (int first= 0; first < files.length; first += filesPerJob) {
					int end= Math.min(files.length, first + filesPerJob);
					Job job= new TextSearchJob(files, first, end, documentsInEditors);
					job.setJobGroup(jobGroup);
					job.schedule();
				}

				// The monitorUpdateJob is managing progress and cancellation,
				// so it is ok to pass a null monitor into the job group.
				jobGroup.join(0, null);
				if (fProgressMonitor.isCanceled())
					throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);

				fStatus.addAll(jobGroup.getResult());
				return fStatus;
			} catch (InterruptedException e) {
				throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
			} finally {
				monitorUpdateJob.cancel();
			}
		} finally {
			fProgressMonitor.done();
			fCollector.endReporting();
			if (TRACING) {
				Object[] args= { new Integer(fNumberOfScannedFiles), new Integer(jobCount), new Integer(NUMBER_OF_LOGICAL_THREADS), new Long(System.currentTimeMillis() - startTime) };
				System.out.println(Messages.format(
						"[TextSearch] Search duration for {0} files in {1} jobs using {2} threads: {3}ms", args)); //$NON-NLS-1$
			}
	   }
	}

	public IStatus search(TextSearchScope scope, IProgressMonitor monitor) {
		return search(scope.evaluateFilesInScope(fStatus), monitor);
	}

	/**
	 * Returns a map from IFile to IDocument for all open, dirty editors. After creation this map
	 * is not modified, so returning a non-synchronized map is ok.
	 *
	 * @return a map from IFile to IDocument for all open, dirty editors
	 */
	private Map evalNonFileBufferDocuments() {
		Map result= new HashMap();
		IWorkbench workbench= SearchPlugin.getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorReference[] editorRefs= pages[x].getEditorReferences();
				for (int z= 0; z < editorRefs.length; z++) {
					IEditorPart ep= editorRefs[z].getEditor(false);
					if (ep instanceof ITextEditor && ep.isDirty()) { // only dirty editors
						evaluateTextEditor(result, ep);
					}
				}
			}
		}
		return result;
	}

	private void evaluateTextEditor(Map result, IEditorPart ep) {
		IEditorInput input= ep.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) input).getFile();
			if (!result.containsKey(file)) { // take the first editor found
				ITextFileBufferManager bufferManager= FileBuffers.getTextFileBufferManager();
				ITextFileBuffer textFileBuffer= bufferManager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
				if (textFileBuffer != null) {
					// file buffer has precedence
					result.put(file, textFileBuffer.getDocument());
				} else {
					// use document provider
					IDocument document= ((ITextEditor) ep).getDocumentProvider().getDocument(input);
					if (document != null) {
						result.put(file, document);
					}
				}
			}
		}
	}

	public IStatus processFile(IFile file, TextSearchJob job) {
		// A natural cleanup after the change to use JobGroups is accepted would be to move these
		// methods to the TextSearchJob class.
		IProgressMonitor monitor= job.getMonitor();
		ReusableMatchAccess matchAccess= job.getReusableMatchAccess();
		Matcher matcher= fSearchPattern.pattern().length() == 0 ? null : fSearchPattern.matcher(new String());
		FileCharSequenceProvider fileCharSequenceProvider= new FileCharSequenceProvider();

		try {
			if (!fCollector.acceptFile(file) || matcher == null) {
				return Status.OK_STATUS;
			}

			IDocument document= getOpenDocument(file, job.getDocumentsInEditors());

			if (document != null) {
				DocumentCharSequence documentCharSequence= new DocumentCharSequence(document);
				// assume all documents are non-binary
				locateMatches(file, documentCharSequence, matcher, matchAccess, monitor);
			} else {
				CharSequence seq= null;
				try {
					seq= fileCharSequenceProvider.newCharSequence(file);
					if (hasBinaryContent(seq, file) && !fCollector.reportBinaryFile(file)) {
						return Status.OK_STATUS;
					}
					locateMatches(file, seq, matcher, matchAccess, monitor);
				} catch (FileCharSequenceProvider.FileCharSequenceException e) {
					e.throwWrappedException();
				} finally {
					if (seq != null) {
						try {
							fileCharSequenceProvider.releaseCharSequence(seq);
						} catch (IOException e) {
							SearchPlugin.log(e);
						}
					}
				}
			}
		} catch (UnsupportedCharsetException e) {
			String[] args= { getCharSetName(file), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_unsupportedcharset, args);
			return new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, e);
		} catch (IllegalCharsetNameException e) {
			String[] args= { getCharSetName(file), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_illegalcharset, args);
			return new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, e);
		} catch (IOException e) {
			String[] args= { getExceptionMessage(e), file.getFullPath().makeRelative().toString()};
			String message= Messages.format(SearchMessages.TextSearchVisitor_error, args);
			return new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, e);
		} catch (CoreException e) {
			if (fIsLightweightAutoRefresh && IResourceStatus.RESOURCE_NOT_FOUND == e.getStatus().getCode()) {
				return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
			}
			String[] args= { getExceptionMessage(e), file.getFullPath().makeRelative().toString() };
			String message= Messages.format(SearchMessages.TextSearchVisitor_error, args);
			return new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, e);
		} catch (StackOverflowError e) {
			// Trigger cancellation of remaining jobs in the group.
			// An alternative is to move this method into TextSearchJob and call getJobGroup().cancel() directly.
			fProgressMonitor.setCanceled(true);
			String message= SearchMessages.TextSearchVisitor_patterntoocomplex0;
			return new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, e);
		} finally {
			synchronized (fLock) {
				fCurrentFile= file;
				fNumberOfScannedFiles++;
			}
		}
		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	private boolean hasBinaryContent(CharSequence seq, IFile file) throws CoreException {
		IContentDescription desc= file.getContentDescription();
		if (desc != null) {
			IContentType contentType= desc.getContentType();
			if (contentType != null && contentType.isKindOf(Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT))) {
				return false;
			}
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
		} catch (FileCharSequenceException ex) {
			if (ex.getCause() instanceof CharConversionException)
				return true;
			throw ex;
		}
		return false;
	}

	private void locateMatches(IFile file, CharSequence searchInput, Matcher matcher, ReusableMatchAccess matchAccess, IProgressMonitor monitor) throws CoreException {
		try {
			matcher.reset(searchInput);
			int k= 0;
			while (matcher.find()) {
				int start= matcher.start();
				int end= matcher.end();
				if (end != start) { // don't report 0-length matches
					matchAccess.initialize(file, start, end - start, searchInput);
					boolean res= fCollector.acceptPatternMatch(matchAccess);
					if (!res) {
						return; // no further reporting requested
					}
				}
				// Periodically check for cancellation and quit working on the current file if the job has been cancelled.
				if (++k % 20 == 0 && monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			matchAccess.initialize(null, 0, 0, new String()); // clear references
		}
	}


	private String getExceptionMessage(Exception e) {
		String message= e.getLocalizedMessage();
		if (message == null) {
			return e.getClass().getName();
		}
		return message;
	}

	private IDocument getOpenDocument(IFile file, Map documentsInEditors) {
		IDocument document= (IDocument)documentsInEditors.get(file);
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

