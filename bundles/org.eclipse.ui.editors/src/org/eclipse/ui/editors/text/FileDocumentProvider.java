/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.ui.editors.text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.UnsupportedCharsetException;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.manipulation.ContainerCreator;

import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.NLSUtility;
import org.eclipse.ui.internal.editors.text.WorkspaceOperationRunner;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ISchedulingRuleProvider;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;


/**
 * Shared document provider specialized for file resources (<code>IFile</code>).
 * <p>
 * This class may be instantiated or be subclassed.</p>
 */
public class FileDocumentProvider extends StorageDocumentProvider {

	/**
	 * The runnable context for that provider.
	 * @since 3.0
	 */
	private WorkspaceOperationRunner fOperationRunner;
	/**
	 * The scheduling rule factory.
	 * @since 3.0
	 */
	private IResourceRuleFactory fResourceRuleFactory;

	/**
	 * Runnable encapsulating an element state change. This runnable ensures
	 * that a element change failed message is sent out to the element state listeners
	 * in case an exception occurred.
	 *
	 * @since 2.0
	 */
	protected class SafeChange implements Runnable {

		/** The input that changes. */
		private IFileEditorInput fInput;

		/**
		 * Creates a new safe runnable for the given input.
		 *
		 * @param input the input
		 */
		public SafeChange(IFileEditorInput input) {
			fInput= input;
		}

		/**
		 * Execute the change.
		 * Subclass responsibility.
		 *
		 * @param input the input
		 * @throws Exception an exception in case of error
		 */
		protected void execute(IFileEditorInput input) throws Exception {
		}

		@Override
		public void run() {

			if (getElementInfo(fInput) == null) {
				fireElementStateChangeFailed(fInput);
				return;
			}

			try {
				execute(fInput);
			} catch (Exception e) {
				fireElementStateChangeFailed(fInput);
			}
		}
	}


	/**
	 * Synchronizes the document with external resource changes.
	 */
	protected class FileSynchronizer implements IResourceChangeListener, IResourceDeltaVisitor {

		/** The file editor input. */
		protected IFileEditorInput fFileEditorInput;
		/**
		 * A flag indicating whether this synchronizer is installed or not.
		 *
		 * @since 2.1
		 */
		protected boolean fIsInstalled= false;

		/**
		 * Creates a new file synchronizer. Is not yet installed on a resource.
		 *
		 * @param fileEditorInput the editor input to be synchronized
		 */
		public FileSynchronizer(IFileEditorInput fileEditorInput) {
			fFileEditorInput= fileEditorInput;
		}

		/**
		 * Creates a new file synchronizer which is not yet installed on a resource.
		 *
		 * @param fileEditorInput the editor input to be synchronized
		 * @deprecated use <code>FileSynchronizer(IFileEditorInput)</code>
		 */
		@Deprecated
		public FileSynchronizer(FileEditorInput fileEditorInput) {
			fFileEditorInput= fileEditorInput;
		}

		/**
		 * Returns the file wrapped by the file editor input.
		 *
		 * @return the file wrapped by the editor input associated with that synchronizer
		 */
		protected IFile getFile() {
			return fFileEditorInput.getFile();
		}

		/**
		 * Installs the synchronizer on the input's file.
		 */
		public void install() {
			getFile().getWorkspace().addResourceChangeListener(this);
			fIsInstalled= true;
		}

		/**
		 * Uninstalls the synchronizer from the input's file.
		 */
		public void uninstall() {
			getFile().getWorkspace().removeResourceChangeListener(this);
			fIsInstalled= false;
		}

		@Override
		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta= e.getDelta();
			try {
				if (delta != null && fIsInstalled)
					delta.accept(this);
			} catch (CoreException x) {
				handleCoreException(x, "FileDocumentProvider.resourceChanged"); //$NON-NLS-1$
			}
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (delta == null)
				return false;

			delta= delta.findMember(getFile().getFullPath());

			if (delta == null)
				return false;

			Runnable runnable= null;

			switch (delta.getKind()) {
				case IResourceDelta.CHANGED:
					FileInfo info= (FileInfo) getElementInfo(fFileEditorInput);
					if (info == null || info.fCanBeSaved)
						break;

					boolean isSynchronized= computeModificationStamp(getFile()) == info.fModificationStamp;
					if ((IResourceDelta.ENCODING & delta.getFlags()) != 0 && isSynchronized) {
						runnable= new SafeChange(fFileEditorInput) {
							@Override
							protected void execute(IFileEditorInput input) throws Exception {
								handleElementContentChanged(input);
							}
						};
					}

					if (runnable == null && (IResourceDelta.CONTENT & delta.getFlags()) != 0 && !isSynchronized) {
						runnable= new SafeChange(fFileEditorInput) {
							@Override
							protected void execute(IFileEditorInput input) throws Exception {
								handleElementContentChanged(input);
							}
						};
					}
					break;

				case IResourceDelta.REMOVED:
					if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
						final IPath path= delta.getMovedToPath();
						runnable= new SafeChange(fFileEditorInput) {
							@Override
							protected void execute(IFileEditorInput input) throws Exception {
								handleElementMoved(input, path);
							}
						};
					} else {
						info= (FileInfo) getElementInfo(fFileEditorInput);
						if (info != null && !info.fCanBeSaved) {
							runnable= new SafeChange(fFileEditorInput) {
								@Override
								protected void execute(IFileEditorInput input) throws Exception {
									handleElementDeleted(input);
								}
							};
						}
					}
					break;
				default:
					break;
			}

			if (runnable != null)
				update(runnable);

			return false;
		}

		/**
		 * Posts the update code "behind" the running operation.
		 *
		 * @param runnable the update code
		 */
		protected void update(Runnable runnable) {

			if (runnable instanceof SafeChange)
				fireElementStateChanging(fFileEditorInput);

			IWorkbench workbench= PlatformUI.getWorkbench();
			IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
			if (windows != null && windows.length > 0) {
				Display display= windows[0].getShell().getDisplay();
				display.asyncExec(runnable);
			} else {
				runnable.run();
			}
		}
	}



	/**
	 * Bundle of all required information to allow files as underlying document resources.
	 */
	protected class FileInfo extends StorageInfo {

		/** The file synchronizer. */
		public FileSynchronizer fFileSynchronizer;
		/** The time stamp at which this provider changed the file. */
		public long fModificationStamp= IResource.NULL_STAMP;
		/**
		 * The cached BOM of the the file on disk.
		 */
		private byte[] fBOM;

		/**
		 * Creates and returns a new file info.
		 *
		 * @param document the document
		 * @param model the annotation model
		 * @param fileSynchronizer the file synchronizer
		 */
		public FileInfo(IDocument document, IAnnotationModel model, FileSynchronizer fileSynchronizer) {
			super(document, model);
			fFileSynchronizer= fileSynchronizer;
		}
	}


	/**
	 * Creates and returns a new document provider.
	 */
	public FileDocumentProvider() {
		super();
		fResourceRuleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
	}

	/**
	 * Overrides <code>StorageDocumentProvider#setDocumentContent(IDocument, IEditorInput)</code>.
	 *
	 * @see StorageDocumentProvider#setDocumentContent(IDocument, IEditorInput)
	 * @deprecated use file encoding based version
	 * @since 2.0
	 */
	@Deprecated
	@Override
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput) throws CoreException {
		if (editorInput instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) editorInput).getFile();
			setDocumentContent(document, file.readString());
			return true;
		}
		return super.setDocumentContent(document, editorInput);
	}

	@Override
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding) throws CoreException {
		if (editorInput instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) editorInput).getFile();
			setDocumentContent(document, file.readString());
			return true;
		}
		return super.setDocumentContent(document, editorInput, encoding);
	}

	@Override
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return new ResourceMarkerAnnotationModel(input.getFile());
		}

		return super.createAnnotationModel(element);
	}

	/**
	 * Checks whether the given resource has been changed on the
	 * local file system by comparing the actual time stamp with the
	 * cached one. If the resource has been changed, a <code>CoreException</code>
	 * is thrown.
	 *
	 * @param cachedModificationStamp the cached modification stamp
	 * @param resource the resource to check
	 * @throws org.eclipse.core.runtime.CoreException if resource has been changed on the file system
	 */
	protected void checkSynchronizationState(long cachedModificationStamp, IResource resource) throws CoreException {
		if (cachedModificationStamp != computeModificationStamp(resource)) {
			Status status= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IResourceStatus.OUT_OF_SYNC_LOCAL, TextEditorMessages.FileDocumentProvider_error_out_of_sync, null);
			throw new CoreException(status);
		}
	}

	/**
	 * Computes the initial modification stamp for the given resource.
	 *
	 * @param resource the resource
	 * @return the modification stamp
	 */
	protected long computeModificationStamp(IResource resource) {
		long modificationStamp= resource.getModificationStamp();

		IPath path= resource.getLocation();
		if (path == null)
			return modificationStamp;

		modificationStamp= path.toFile().lastModified();
		return modificationStamp;
	}

	@Override
	public long getModificationStamp(Object element) {

		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return computeModificationStamp(input.getFile());
		}

		return super.getModificationStamp(element);
	}

	@Override
	public long getSynchronizationStamp(Object element) {

		if (element instanceof IFileEditorInput) {
			FileInfo info= (FileInfo) getElementInfo(element);
			if (info != null)
				return info.fModificationStamp;
		}

		return super.getSynchronizationStamp(element);
	}

	@Override
	protected void doSynchronize(Object element, IProgressMonitor monitor)  throws CoreException {
		if (element instanceof IFileEditorInput) {

			IFileEditorInput input= (IFileEditorInput) element;

			FileInfo info= (FileInfo) getElementInfo(element);
			if (info != null) {

				if (info.fFileSynchronizer != null) {
					info.fFileSynchronizer.uninstall();
					refreshFile(input.getFile(), monitor);
					info.fFileSynchronizer.install();
				} else {
					refreshFile(input.getFile(), monitor);
				}

				handleElementContentChanged((IFileEditorInput) element);
			}
			return;

		}
		super.doSynchronize(element, monitor);
	}

	@Override
	public boolean isDeleted(Object element) {

		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;

			IPath path= input.getFile().getLocation();
			if (path == null)
				return true;

			return !path.toFile().exists();
		}

		return super.isDeleted(element);
	}

	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof IFileEditorInput) {

			IFileEditorInput input= (IFileEditorInput) element;
			String encoding= null;

			FileInfo info= (FileInfo) getElementInfo(element);
			IFile file= input.getFile();
			encoding= getCharsetForNewFile(file, document, info);

			if (info != null && info.fBOM == IContentDescription.BOM_UTF_16LE && StandardCharsets.UTF_16.name().equals(encoding))
				encoding= StandardCharsets.UTF_16LE.name();

			Charset charset;
			try {
				charset= Charset.forName(encoding);
			} catch (UnsupportedCharsetException ex) {
				String message= NLSUtility.format(TextEditorMessages.DocumentProvider_error_unsupported_encoding_message_arg, encoding);
				IStatus s= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, ex);
				throw new CoreException(s);
			} catch (IllegalCharsetNameException ex) {
				String message= NLSUtility.format(TextEditorMessages.DocumentProvider_error_illegal_encoding_message_arg, encoding);
				IStatus s= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, ex);
				throw new CoreException(s);
			}

			CharsetEncoder encoder= charset.newEncoder();
			encoder.onMalformedInput(CodingErrorAction.REPLACE);
			encoder.onUnmappableCharacter(CodingErrorAction.REPORT);

			InputStream stream;

			try {
				byte[] bytes;
				ByteBuffer byteBuffer= encoder.encode(CharBuffer.wrap(document.get()));
				if (byteBuffer.hasArray())
					bytes= byteBuffer.array();
				else {
					bytes= new byte[byteBuffer.limit()];
					byteBuffer.get(bytes);
				}
				stream= new ByteArrayInputStream(bytes, 0, byteBuffer.limit());
			} catch (CharacterCodingException ex) {
				Assert.isTrue(ex instanceof UnmappableCharacterException);
				String message= NLSUtility.format(TextEditorMessages.DocumentProvider_error_charset_mapping_failed_message_arg, encoding);
				IStatus s= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, EditorsUI.CHARSET_MAPPING_FAILED, message, null);
				throw new CoreException(s);
			}

			/*
			 * XXX:
			 * This is a workaround for a corresponding bug in Java readers and writer,
			 * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
			 */
			if (info != null && info.fBOM == IContentDescription.BOM_UTF_8 && StandardCharsets.UTF_8.name().equals(encoding))
				stream= new SequenceInputStream(new ByteArrayInputStream(IContentDescription.BOM_UTF_8), stream);

			if (info != null && info.fBOM == IContentDescription.BOM_UTF_16LE && StandardCharsets.UTF_16LE.name().equals(encoding))
				stream= new SequenceInputStream(new ByteArrayInputStream(IContentDescription.BOM_UTF_16LE), stream);

			if (file.exists()) {

				if (info != null && !overwrite)
					checkSynchronizationState(info.fModificationStamp, file);

				// inform about the upcoming content change
				fireElementStateChanging(element);
				try {
					file.setContents(stream, overwrite, true, monitor);
				} catch (CoreException x) {
					// inform about failure
					fireElementStateChangeFailed(element);
					throw x;
				} catch (RuntimeException x) {
					// inform about failure
					fireElementStateChangeFailed(element);
					throw x;
				}

				// If here, the editor state will be flipped to "not dirty".
				// Thus, the state changing flag will be reset.

				if (info != null) {

					ResourceMarkerAnnotationModel model= (ResourceMarkerAnnotationModel) info.fModel;
					if (model != null)
						model.updateMarkers(info.fDocument);

					info.fModificationStamp= computeModificationStamp(file);
				}

			} else {
				SubMonitor subMonitor= SubMonitor.convert(monitor, TextEditorMessages.FileDocumentProvider_task_saving, 2);
				ContainerCreator creator= new ContainerCreator(file.getWorkspace(), file.getParent().getFullPath());
				creator.createContainer(subMonitor.split(1));
				file.create(stream, false, subMonitor.split(1));
			}

		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}

	/*
	 * @since 3.0
	 */
	private String getCharsetForNewFile(IFile targetFile, IDocument document, FileInfo info) {
		// User-defined encoding has first priority
		String encoding;
		try {
			encoding= targetFile.getCharset(false);
		} catch (CoreException ex) {
			encoding= null;
		}
		if (encoding != null)
			return encoding;

		// Probe content
		try (Reader reader= new DocumentReader(document)) {
			QualifiedName[] options= new QualifiedName[] { IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK };
			IContentDescription description= Platform.getContentTypeManager().getDescriptionFor(reader, targetFile.getName(), options);
			if (description != null) {
				encoding= description.getCharset();
				if (encoding != null)
					return encoding;
			}
		} catch (IOException ex) {
			// continue with next strategy
		}

		// Use file's encoding if the file has a BOM
		if (info != null && info.fBOM != null)
			return info.fEncoding;

		// Use parent chain
		try {
			return targetFile.getParent().getDefaultCharset();
		} catch (CoreException ex) {
			// Use global default
			return ResourcesPlugin.getEncoding();
		}
	}

	@Override
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {

			IFileEditorInput input= (IFileEditorInput) element;

			// Note that file.isSynchronized does not require a scheduling rule and thus helps to identify a no-op attempt
			// to refresh the file. The no-op will otherwise be blocked by a running build or cancel a running build
			IFile file= input.getFile();
			if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
				try {
					refreshFile(file);
				} catch (CoreException x) {
					handleCoreException(x, TextEditorMessages.FileDocumentProvider_createElementInfo);
				}
			}

			IDocument d= null;
			IStatus s= null;

			try {
				d= createDocument(element);
			} catch (CoreException x) {
				handleCoreException(x, TextEditorMessages.FileDocumentProvider_createElementInfo);
				s= x.getStatus();
				d= createEmptyDocument();
			}

			// Set the initial line delimiter
			if (d instanceof IDocumentExtension4) {
				String initalLineDelimiter= getLineDelimiterPreference(input.getFile());
				if (initalLineDelimiter != null)
					((IDocumentExtension4)d).setInitialLineDelimiter(initalLineDelimiter);
			}

			IAnnotationModel m= createAnnotationModel(element);
			FileSynchronizer f= new FileSynchronizer(input);
			f.install();

			FileInfo info= new FileInfo(d, m, f);
			info.fModificationStamp= computeModificationStamp(input.getFile());
			info.fStatus= s;
			info.fEncoding= getPersistedEncoding(element);
			info.fBOM= getBOM(element);

			/*
			 * The code below is a no-op in the implementation in this class
			 * because the info is not yet stored in the element map.
			 * Calling to not break clients who have overridden the method.
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=109255
			 */
			cacheEncodingState(element);

			return info;
		}

		return super.createElementInfo(element);
	}

	/**
	 * Returns the default line delimiter preference for the given file.
	 *
	 * @param file the file
	 * @return the default line delimiter
	 * @since 3.1
	 */
	private String getLineDelimiterPreference(IFile file) {
		IScopeContext[] scopeContext;
		if (file != null && file.getProject() != null) {
			// project preference
			scopeContext= new IScopeContext[] { new ProjectScope(file.getProject()) };
			String lineDelimiter= Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
			if (lineDelimiter != null)
				return lineDelimiter;
		}
		// workspace preference
		scopeContext= new IScopeContext[] { InstanceScope.INSTANCE };
		return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
	}

	@Override
	protected void disposeElementInfo(Object element, ElementInfo info) {
		if (info instanceof FileInfo) {
			FileInfo fileInfo= (FileInfo) info;
			if (fileInfo.fFileSynchronizer != null)
				fileInfo.fFileSynchronizer.uninstall();
		}

		super.disposeElementInfo(element, info);
	}

	/**
	 * Updates the element info to a change of the file content and sends out
	 * appropriate notifications.
	 *
	 * @param fileEditorInput the input of an text editor
	 */
	protected void handleElementContentChanged(IFileEditorInput fileEditorInput) {
		FileInfo info= (FileInfo) getElementInfo(fileEditorInput);
		if (info == null)
			return;

		IDocument document= createEmptyDocument();
		IStatus status= null;

		try {

			try {
				refreshFile(fileEditorInput.getFile());
			} catch (CoreException x) {
				handleCoreException(x, "FileDocumentProvider.handleElementContentChanged"); //$NON-NLS-1$
			}

			cacheEncodingState(fileEditorInput);
			setDocumentContent(document, fileEditorInput, info.fEncoding);

		} catch (CoreException x) {
			status= x.getStatus();
		}

		String newContent= document.get();

		if ( !newContent.equals(info.fDocument.get())) {

			// set the new content and fire content related events
			fireElementContentAboutToBeReplaced(fileEditorInput);

			removeUnchangedElementListeners(fileEditorInput, info);

			info.fDocument.removeDocumentListener(info);
			info.fDocument.set(newContent);
			info.fCanBeSaved= false;
			info.fModificationStamp= computeModificationStamp(fileEditorInput.getFile());
			info.fStatus= status;

			addUnchangedElementListeners(fileEditorInput, info);

			fireElementContentReplaced(fileEditorInput);

		} else {

			removeUnchangedElementListeners(fileEditorInput, info);

			// fires only the dirty state related event
			info.fCanBeSaved= false;
			info.fModificationStamp= computeModificationStamp(fileEditorInput.getFile());
			info.fStatus= status;

			addUnchangedElementListeners(fileEditorInput, info);

			fireElementDirtyStateChanged(fileEditorInput, false);
		}
	}

	/**
	 * Sends out the notification that the file serving as document input has been moved.
	 *
	 * @param fileEditorInput the input of an text editor
	 * @param path the path of the new location of the file
	 */
	protected void handleElementMoved(IFileEditorInput fileEditorInput, IPath path) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile newFile= workspace.getRoot().getFile(path);
		fireElementMoved(fileEditorInput, new FileEditorInput(newFile));
	}

	/**
	 * Sends out the notification that the file serving as document input has been deleted.
	 *
	 * @param fileEditorInput the input of an text editor
	 */
	protected void handleElementDeleted(IFileEditorInput fileEditorInput) {
		fireElementDeleted(fileEditorInput);
	}

	/*
	 * @see AbstractDocumentProvider#getElementInfo(Object)
	 * It's only here to circumvent visibility issues with certain compilers.
	 */
	@Override
	protected ElementInfo getElementInfo(Object element) {
		return super.getElementInfo(element);
	}

	@Override
	protected void doValidateState(Object element, Object computationContext) throws CoreException {

		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			FileInfo info= (FileInfo) getElementInfo(input);
			if (info != null) {
				IFile file= input.getFile();
				if (file.isReadOnly()) { // do not use cached state here
					IWorkspace workspace= file.getWorkspace();
					info.fStatus= workspace.validateEdit(new IFile[] { file }, computationContext);
				}
				if (isDerived(file)) {
					IStatus status= new Status(IStatus.WARNING, EditorsUI.PLUGIN_ID, EditorsUI.DERIVED_FILE, TextEditorMessages.FileDocumentProvider_warning_fileIsDerived, null);
					if (info.fStatus == null || info.fStatus.isOK())
						info.fStatus= status;
					else
						info.fStatus= new MultiStatus(EditorsUI.PLUGIN_ID, EditorsUI.STATE_VALIDATION_FAILED, new IStatus[] {info.fStatus, status}, TextEditorMessages.FileDocumentProvider_stateValidationFailed, null);
				}
			}
		}

		super.doValidateState(element, computationContext);
	}

	/*
	 *
	 * @see IResource#isDerived()
	 * @since 3.3
	 */
	private boolean isDerived(IResource resource) {
		while (resource != null) {
			if (resource.isDerived())
				return true;
			resource= resource.getParent();
		}
		return false;
	}

	@Override
	public boolean isModifiable(Object element) {
		if (!isStateValidated(element)) {
			if (element instanceof IFileEditorInput)
				return true;
		}
		return super.isModifiable(element);
	}

	@Override
	protected void doResetDocument(Object element, IProgressMonitor monitor) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			try {
				refreshFile(input.getFile(), monitor);
				cacheEncodingState(element);
			} catch (CoreException x) {
				handleCoreException(x,TextEditorMessages.FileDocumentProvider_resetDocument);
			}
		}

		super.doResetDocument(element, monitor);

		IAnnotationModel model= getAnnotationModel(element);
		if (model instanceof AbstractMarkerAnnotationModel) {
			AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) model;
			markerModel.resetMarkers();
		}
	}

	/**
	 * Refreshes the given file resource. This method will run the operation in the providers
	 * runnable context using the monitor supplied by {@link #getProgressMonitor()}.
	 *
	 * @param file the file
	 * @throws CoreException if the refresh fails
	 * @since 2.1
	 */
	protected void refreshFile(IFile file) throws CoreException {
		refreshFile(file, getProgressMonitor());
	}

	/**
	 * Refreshes the given file resource. This method will run the operation in the providers
	 * runnable context using given monitor.
	 *
	 * @param file the file to be refreshed
	 * @param monitor the progress monitor
	 * @throws org.eclipse.core.runtime.CoreException if the refresh fails
	 * @since 3.0
	 */
	protected void refreshFile(IFile file, IProgressMonitor monitor) throws CoreException {
		class RefreshFileOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {
			@Override
			protected void execute(IProgressMonitor m) throws CoreException {
				try {
					file.refreshLocal(IResource.DEPTH_INFINITE, m);
				} catch (OperationCanceledException x) {
					// ignore
				}
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return getRefreshRule(file);
			}
		}
		executeOperation(new RefreshFileOperation(), monitor);
	}

	@Override
	public boolean isSynchronized(Object element) {
		if (element instanceof IFileEditorInput) {
			if (getElementInfo(element) != null) {
				IFileEditorInput input= (IFileEditorInput) element;
				IResource resource= input.getFile();
				return resource.isSynchronized(IResource.DEPTH_ZERO);
			}
			return false;
		}
		return super.isSynchronized(element);
	}

	@Override
	public IContentType getContentType(Object element) throws CoreException {
		IContentType contentType= null;
		if (!canSaveDocument(element) && element instanceof IFileEditorInput)
			contentType= getContentType((IFileEditorInput) element);

		if (contentType == null)
			contentType= super.getContentType(element);

		if (contentType == null && element instanceof IFileEditorInput)
			contentType= getContentType((IFileEditorInput) element);

		return contentType;
	}

	/**
	 * Returns the content type of for the given file editor input or
	 * <code>null</code> if none could be determined.
	 *
	 * @param input the element
	 * @return the content type or <code>null</code>
	 * @throws CoreException if reading or accessing the underlying store
	 *                 fails
	 * @since 3.1
	 */
	private IContentType getContentType(IFileEditorInput input) throws CoreException {
		IContentDescription desc= input.getFile().getContentDescription();
		if (desc != null)
			return desc.getContentType();
		return null;
	}

	// --------------- Encoding support ---------------

	/**
	 * Returns the persisted encoding for the given element.
	 *
	 * @param element the element for which to get the persisted encoding
	 * @return the persisted encoding
	 * @since 2.1
	 */
	@Override
	protected String getPersistedEncoding(Object element) {
		return super.getPersistedEncoding(element);
	}



	/**
	 * Persists the given encoding for the given element.
	 *
	 * @param element the element for which to store the persisted encoding
	 * @param encoding the encoding
	 * @throws org.eclipse.core.runtime.CoreException if persisting the encoding fails
	 * @since 2.1
	 */
	@Override
	protected void persistEncoding(Object element, String encoding) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput editorInput= (IFileEditorInput)element;
			IFile file= editorInput.getFile();
			if (file != null) {
				file.setCharset(encoding, getProgressMonitor());
				StorageInfo info= (StorageInfo)getElementInfo(element);
				if (info != null) {
					if (encoding == null)
						info.fEncoding= file.getCharset();
					if (info instanceof FileInfo)
						((FileInfo)info).fBOM= getBOM(element);
				}
			}
		}
	}

	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		if (fOperationRunner == null)
			fOperationRunner = new WorkspaceOperationRunner();
		fOperationRunner.setProgressMonitor(monitor);
		return fOperationRunner;
	}

	@Override
	protected ISchedulingRule getResetRule(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return fResourceRuleFactory.refreshRule(input.getFile());
		}
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing <code>refresh</code> on the given element.
	 * This implementation uses default refresh rule provided by
	 * {@link IResourceRuleFactory#refreshRule(IResource)}.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>refresh</code>
	 * @since 3.11
	 */
	protected ISchedulingRule getRefreshRule(Object element) {
		if (element instanceof IResource) {
			return fResourceRuleFactory.refreshRule((IResource) element);
		}
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return fResourceRuleFactory.refreshRule(input.getFile());
		}
		return null;
	}

	@Override
	protected ISchedulingRule getSaveRule(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return computeSchedulingRule(input.getFile());
		}
		return null;
	}

	@Override
	protected ISchedulingRule getSynchronizeRule(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return fResourceRuleFactory.refreshRule(input.getFile());
		}
		return null;
	}

	@Override
	protected ISchedulingRule getValidateStateRule(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			IFile file= input.getFile();
			ISchedulingRule validateEditRule= fResourceRuleFactory.validateEditRule(new IResource[] { file });
			if (validateEditRule == null) {
				// Note that factory decides to provide a null rule for modifiable files (not read-only).
				// Null rule means, that org.eclipse.core.internal.resources.WorkManager.checkIn(ISchedulingRule, IProgressMonitor)
				// will run jobManager.beginRule(null, monitor); which will NOT show any progress dialog
				// and will *immediately* lock UI thread via lock.acquire(); while the workspace is locked
				// Providing here a file we enforce the progress dialog, where this operation can be cancelled by user,
				// so that an occasional "Modify" or "Save" of the editor will NOT block UI forever.
				return file;
			} else {
				return validateEditRule;
			}
		}
		return null;
	}

	/**
	 * Returns whether the underlying file has a BOM.
	 *
	 * @param element the element, or <code>null</code>
	 * @return <code>true</code> if the underlying file has BOM
	 */
	private byte[] getBOM(Object element) {
		if (element instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput)element).getFile();
			if (file != null) {
				try {
					IContentDescription description= file.getContentDescription();
					if (description != null)
						return (byte[])description.getProperty(IContentDescription.BYTE_ORDER_MARK);
				} catch (CoreException ex) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Reads the file's UTF-8 BOM if any and stores it.
	 * <p>
	 * XXX:
	 * This is a workaround for a corresponding bug in Java readers and writer,
	 * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
	 * </p>
	 * @param file the file
	 * @param encoding the encoding
	 * @param element the element, or <code>null</code>
	 * @throws org.eclipse.core.runtime.CoreException if reading the BOM fails
	 * @since 3.0
	 * @deprecated as of 3.0 this method is no longer in use and does nothing
	 */
	@Deprecated
	protected void readUTF8BOM(IFile file, String encoding, Object element) throws CoreException {
	}

	/**
	 * Internally caches the file's encoding data.
	 *
	 * @param element the element, or <code>null</code>
	 * @throws CoreException if the encoding cannot be retrieved
	 * @since 3.1
	 */
	protected void cacheEncodingState(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput editorInput= (IFileEditorInput)element;
			IFile file= editorInput.getFile();
			if (file != null) {
				ElementInfo info= getElementInfo(element);
				if (info instanceof StorageInfo)
					((StorageInfo)info).fEncoding= getPersistedEncoding(element);

				if (info instanceof FileInfo)
					((FileInfo)info).fBOM= getBOM(element);
			}
		}
	}

	/**
	 * Computes the scheduling rule needed to create or modify a resource. If
	 * the resource exists, its modify rule is returned. If it does not, the
	 * resource hierarchy is iterated towards the workspace root to find the
	 * first parent of <code>toCreateOrModify</code> that exists. Then the
	 * 'create' rule for the last non-existing resource is returned.
	 *
	 * @param toCreateOrModify the resource to create or modify
	 * @return the minimal scheduling rule needed to modify or create a resource
	 */
	private ISchedulingRule computeSchedulingRule(IResource toCreateOrModify) {
		if (toCreateOrModify.exists())
			return fResourceRuleFactory.modifyRule(toCreateOrModify);

		IResource parent= toCreateOrModify;
		do {
			toCreateOrModify= parent;
			parent= toCreateOrModify.getParent();
		} while (parent != null && !parent.exists());

		return fResourceRuleFactory.createRule(toCreateOrModify);
	}
}
