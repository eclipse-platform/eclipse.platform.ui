/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;



/**
 * An abstract implementation of a sharable document provider.
 * <p>
 * Subclasses must implement <code>createDocument</code>,
 * <code>createAnnotationModel</code>, and <code>doSaveDocument</code>.
 * </p>
 */
public abstract class AbstractDocumentProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IDocumentProviderExtension4, IDocumentProviderExtension5 {

		/**
		 * Operation created by the document provider and to be executed by the providers runnable context.
		 *
		 * @since 3.0
		 */
		protected static abstract class DocumentProviderOperation implements IRunnableWithProgress {

		/**
		 * The actual functionality of this operation.
		 *
		 * @param monitor a progress monitor to track execution
		 * @throws CoreException if the execution fails
		 */
			protected abstract void execute(IProgressMonitor monitor) throws CoreException;

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					execute(monitor);
				} catch (CoreException x) {
					throw new InvocationTargetException(x);
				}
			}
		}

		/**
		 * Collection of all information managed for a connected element.
		 */
		protected class ElementInfo implements IDocumentListener {

			/** The element for which the info is stored */
			public Object fElement;
			/** How often the element has been connected */
			public int fCount;
			/** Can the element be saved */
			public boolean fCanBeSaved;
			/** The element's document */
			public IDocument fDocument;
			/** The element's annotation model */
			public IAnnotationModel fModel;
			/**
			 * Has element state been validated
			 * @since 2.0
			 */
			public boolean fIsStateValidated;
			/**
			 * The status of this element
			 * @since 2.0
			 */
			public IStatus fStatus;


			/**
			 * Creates a new element info, initialized with the given
			 * document and annotation model.
			 *
			 * @param document the document
			 * @param model the annotation model
			 */
			public ElementInfo(IDocument document, IAnnotationModel model) {
				fDocument= document;
				fModel= model;
				fCount= 0;
				fCanBeSaved= false;
				fIsStateValidated= false;
			}

			/**
			 * An element info equals another object if this object is an element info
			 * and if the documents of the two element infos are equal.
			 * @see Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(Object o) {
				if (o instanceof ElementInfo) {
					ElementInfo e= (ElementInfo) o;
					return fDocument.equals(e.fDocument);
				}
				return false;
			}

			@Override
			public int hashCode() {
				return fDocument.hashCode();
			}

			@Override
			public void documentChanged(DocumentEvent event) {
				fCanBeSaved= true;
				removeUnchangedElementListeners(fElement, this);
				fireElementDirtyStateChanged(fElement, fCanBeSaved);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
		}


	/**
	 * Enables a certain behavior.
	 * Indicates whether this provider should behave as described in
	 * use case 5 of http://bugs.eclipse.org/bugs/show_bug.cgi?id=10806.
	 * Current value: <code>true</code> since 3.0
	 * @since 2.0
	 */
	static final protected boolean PR10806_UC5_ENABLED= true;

	/**
	 * Enables a certain behavior.
	 * Indicates whether this provider should behave as described in
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=14469
	 * Notes: This contradicts <code>PR10806_UC5_ENABLED</code>.
	 * Current value: <code>false</code> since 3.0
	 * @since 2.0
	 */
	static final protected boolean PR14469_ENABLED= false;

	/**
	 * Constant for representing the OK status. This is considered a value object.
	 *
	 * @since 2.0
	 * @deprecated As of 3.6, replaced by {@link Status#OK_STATUS}
	 */
	@Deprecated
	static final protected IStatus STATUS_OK= new Status(IStatus.OK, TextEditorPlugin.PLUGIN_ID, IStatus.OK, EditorMessages.AbstractDocumentProvider_ok, null);

	/**
	 * Constant for representing the error status. This is considered a value object.
	 * @since 2.0
	 */
	static final protected IStatus STATUS_ERROR= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.INFO, EditorMessages.AbstractDocumentProvider_error, null);


	/** Element information of all connected elements */
	private Map<Object, ElementInfo> fElementInfoMap= new HashMap<>();
	/** The element state listeners */
	private List<IElementStateListener> fElementStateListeners= new ArrayList<>();
	/**
	 * The current progress monitor
	 * @since 2.1
	 */
	private IProgressMonitor fProgressMonitor;


	/**
	 * Creates a new document provider.
	 */
	protected AbstractDocumentProvider() {
	}

	/**
	 * Creates the document for the given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param element the element
	 * @return the document
	 * @exception CoreException if the document could not be created
	 */
	protected abstract IDocument createDocument(Object element) throws CoreException;

	/**
	 * Creates an annotation model for the given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param element the element
	 * @return the annotation model or <code>null</code> if none
	 * @exception CoreException if the annotation model could not be created
	 */
	protected abstract IAnnotationModel createAnnotationModel(Object element) throws CoreException;

	/**
	 * Performs the actual work of saving the given document provided for the
	 * given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param monitor a progress monitor to report progress and request cancelation
	 * @param element the element
	 * @param document the document
	 * @param overwrite indicates whether an overwrite should happen if necessary
	 * @exception CoreException if document could not be stored to the given element
	 */
	protected abstract void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException;

	/**
	 * Returns the runnable context for this document provider.
	 *
	 * @param monitor a progress monitor to track the operation
	 * @return the runnable context for this document provider
	 * @since 3.0
	 */
	protected abstract IRunnableContext getOperationRunner(IProgressMonitor monitor);

	/**
	 * Returns the scheduling rule required for executing
	 * <code>synchronize</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>synchronize</code>
	 * @since 3.0
	 */
	protected ISchedulingRule getSynchronizeRule(Object element) {
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing
	 * <code>validateState</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>validateState</code>
	 * @since 3.0
	 */
	protected ISchedulingRule getValidateStateRule(Object element) {
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing
	 * <code>save</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>save</code>
	 * @since 3.0
	 */
	protected ISchedulingRule getSaveRule(Object element) {
		return null;
	}

	/**
	 * Returns the scheduling rule required for executing
	 * <code>reset</code> on the given element. This default
	 * implementation returns <code>null</code>.
	 *
	 * @param element the element
	 * @return the scheduling rule for <code>reset</code>
	 * @since 3.0
	 */
	protected ISchedulingRule getResetRule(Object element) {
		return null;
	}

	/**
	 * Returns the element info object for the given element.
	 *
	 * @param element the element
	 * @return the element info object, or <code>null</code> if none
	 */
	protected ElementInfo getElementInfo(Object element) {
		return fElementInfoMap.get(element);
	}

	/**
	 * Creates a new element info object for the given element.
	 * <p>
	 * This method is called from <code>connect</code> when an element info needs
	 * to be created. The <code>AbstractDocumentProvider</code> implementation
	 * of this method returns a new element info object whose document and
	 * annotation model are the values of <code>createDocument(element)</code>
	 * and  <code>createAnnotationModel(element)</code>, respectively. Subclasses
	 * may override.</p>
	 *
	 * @param element the element
	 * @return a new element info object
	 * @exception CoreException if the document or annotation model could not be created
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		return new ElementInfo(createDocument(element), createAnnotationModel(element));
	}

	/**
	 * Disposes of the given element info object.
	 * <p>
	 * This method is called when an element info is disposed. The
	 * <code>AbstractDocumentProvider</code> implementation of this
	 * method does nothing. Subclasses may reimplement.</p>
	 *
	 * @param element the element
	 * @param info the element info object
	 */
	protected void disposeElementInfo(Object element, ElementInfo info) {
	}

	/**
	 * Called on initial creation and when the dirty state of the element
	 * changes to <code>false</code>. Adds all listeners which must be
	 * active as long as the element is not dirty. This method is called
	 * before <code>fireElementDirtyStateChanged</code> or <code>
	 * fireElementContentReplaced</code> is called.
	 * Subclasses may extend.
	 *
	 * @param element the element
	 * @param info the element info object
	 */
	protected void addUnchangedElementListeners(Object element, ElementInfo info) {
		if (info.fDocument != null)
			info.fDocument.addDocumentListener(info);
	}

	/**
	 * Called when the given element gets dirty. Removes all listeners
	 * which must be active only when the element is not dirty. This
	 * method is called before <code>fireElementDirtyStateChanged</code>
	 * or <code>fireElementContentReplaced</code> is called.
	 * Subclasses may extend.
	 *
	 * @param element the element
	 * @param info the element info object
	 */
	protected void removeUnchangedElementListeners(Object element, ElementInfo info) {
		if (info.fDocument != null)
			info.fDocument.removeDocumentListener(info);
	}

	/**
	 * Enumerates the elements connected via this document provider.
	 *
	 * @return the list of elements
	 */
	protected Iterator<Object> getConnectedElements() {
		Set<Object> s= new HashSet<>();
		Set<Object> keys= fElementInfoMap.keySet();
		if (keys != null)
			s.addAll(keys);
		return s.iterator();
	}

	@Override
	public final void connect(Object element) throws CoreException {
		ElementInfo info= fElementInfoMap.get(element);
		if (info == null) {

			info= createElementInfo(element);
			if (info == null)
				info= new ElementInfo(null, null);

			info.fElement= element;

			addUnchangedElementListeners(element, info);

			fElementInfoMap.put(element, info);
			if (fElementInfoMap.size() == 1)
				connected();
		}
		++ info.fCount;
	}

	/**
	 * This hook method is called when this provider starts managing documents for
	 * elements. I.e. it is called when the first element gets connected to this provider.
	 * Subclasses may extend.
	 * @since 2.0
	 */
	protected void connected() {
	}

	/*
	 * @see IDocumentProvider#disconnect
	 */
	@Override
	public final void disconnect(Object element) {
		ElementInfo info= fElementInfoMap.get(element);

		if (info == null)
			return;

		if (info.fCount == 1) {

			fElementInfoMap.remove(element);
			removeUnchangedElementListeners(element, info);
			disposeElementInfo(element, info);

			if (fElementInfoMap.size() == 0)
				disconnected();

		} else
		 	-- info.fCount;
	}

	/**
	 * This hook method is called when this provider stops managing documents for
	 * element. I.e. it is called when the last element gets disconnected from this provider.
	 * Subclasses may extend.
	 * @since 2.0
	 */
	protected void disconnected() {
	}

	@Override
	public IDocument getDocument(Object element) {

		if (element == null)
			return null;

		ElementInfo info= fElementInfoMap.get(element);
		return (info != null ? info.fDocument : null);
	}

	@Override
	public boolean mustSaveDocument(Object element) {

		if (element == null)
			return false;

		ElementInfo info= fElementInfoMap.get(element);
		return (info != null ? info.fCount == 1 && info.fCanBeSaved : false);
	}

	@Override
	public IAnnotationModel getAnnotationModel(Object element) {

		if (element == null)
			return null;

		ElementInfo info= fElementInfoMap.get(element);
		return (info != null ? info.fModel : null);
	}

	@Override
	public boolean canSaveDocument(Object element) {

		if (element == null)
			return false;

		ElementInfo info= fElementInfoMap.get(element);
		return (info != null ? info.fCanBeSaved : false);
	}

	/**
	 * Executes the actual work of reseting the given elements document.
	 *
	 * @param element the element
	 * @param monitor the progress monitor
	 * @throws CoreException if resetting fails
	 * @since 3.0
	 */
	protected void doResetDocument(Object element, IProgressMonitor monitor) throws CoreException {
		ElementInfo info= fElementInfoMap.get(element);
		if (info != null) {

			IDocument original= null;
			IStatus status= null;

			try {
				original= createDocument(element);
			} catch (CoreException x) {
				status= x.getStatus();
			}

			info.fStatus= status;

			if (original != null) {
				fireElementContentAboutToBeReplaced(element);
				info.fDocument.set(original.get());
				if (info.fCanBeSaved) {
					info.fCanBeSaved= false;
					addUnchangedElementListeners(element, info);
				}
				fireElementContentReplaced(element);
				fireElementDirtyStateChanged(element, false);
			}
		}
	}

	/**
	 * Executes the given operation in the providers runnable context.
	 *
	 * @param operation the operation to be executes
	 * @param monitor the progress monitor
	 * @exception CoreException the operation's core exception
	 * @since 3.0
	 */
	protected void executeOperation(DocumentProviderOperation operation, IProgressMonitor monitor) throws CoreException {
		try {
			IRunnableContext runner= getOperationRunner(monitor);
			if (runner != null)
				runner.run(false, false, operation);
			else
				operation.run(monitor);
		} catch (InvocationTargetException x) {
			Throwable e= x.getTargetException();
			if (e instanceof CoreException)
				throw (CoreException) e;
			String message= (e.getMessage() != null ? e.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.ERROR, message, e));
		} catch (InterruptedException x) {
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.CANCEL, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, x));
		}
	}

	@Override
	public final void resetDocument(final Object element) throws CoreException {

		if (element == null)
			return;

		class ResetOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException {
				doResetDocument(element, monitor);
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return getResetRule(element);
			}
		}

		executeOperation(new ResetOperation(), getProgressMonitor());
	}


	@Override
	public final void saveDocument(IProgressMonitor monitor, final Object element, final IDocument document, final boolean overwrite) throws CoreException {

		if (element == null)
			return;

		class SaveOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			@Override
			protected void execute(IProgressMonitor pm) throws CoreException {
				ElementInfo info= fElementInfoMap.get(element);
				if (info != null) {
					if (info.fDocument != document) {
						Status status= new Status(IStatus.WARNING, TextEditorPlugin.PLUGIN_ID, IStatus.ERROR, EditorMessages.AbstractDocumentProvider_error_save_inuse, null);
						throw new CoreException(status);
					}

					doSaveDocument(pm, element, document, overwrite);

					if (pm != null && pm.isCanceled())
						return;

					info.fCanBeSaved= false;
					addUnchangedElementListeners(element, info);
					fireElementDirtyStateChanged(element, false);

				} else {
					doSaveDocument(pm, element, document, overwrite);
				}
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return getSaveRule(element);
			}
		}

		executeOperation(new SaveOperation(), monitor);
	}

	/**
	 * The <code>AbstractDocumentProvider</code> implementation of this
	 * <code>IDocumentProvider</code> method does nothing. Subclasses may
	 * reimplement.
	 *
	 * @param element the element
	 */
	@Override
	public void aboutToChange(Object element) {
	}

	/**
	 * The <code>AbstractDocumentProvider</code> implementation of this
	 * <code>IDocumentProvider</code> method does nothing. Subclasses may
	 * reimplement.
	 *
	 * @param element the element
	 */
	@Override
	public void changed(Object element) {
	}

	@Override
	public void addElementStateListener(IElementStateListener listener) {
		Assert.isNotNull(listener);
		if (!fElementStateListeners.contains(listener))
			fElementStateListeners.add(listener);
	}

	@Override
	public void removeElementStateListener(IElementStateListener listener) {
		Assert.isNotNull(listener);
		fElementStateListeners.remove(listener);
	}

	/**
	 * Informs all registered element state listeners about a change in the
	 * dirty state of the given element.
	 *
	 * @param element the element
	 * @param isDirty the new dirty state
	 * @see IElementStateListener#elementDirtyStateChanged(Object, boolean)
	 */
	protected void fireElementDirtyStateChanged(Object element, boolean isDirty) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= e.next();
			l.elementDirtyStateChanged(element, isDirty);
		}
	}

	/**
	 * Informs all registered element state listeners about an impending
	 * replace of the given element's content.
	 *
	 * @param element the element
	 * @see IElementStateListener#elementContentAboutToBeReplaced(Object)
	 */
	protected void fireElementContentAboutToBeReplaced(Object element) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= e.next();
			l.elementContentAboutToBeReplaced(element);
		}
	}

	/**
	 * Informs all registered element state listeners about the just-completed
	 * replace of the given element's content.
	 *
	 * @param element the element
	 * @see IElementStateListener#elementContentReplaced(Object)
	 */
	protected void fireElementContentReplaced(Object element) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= e.next();
			l.elementContentReplaced(element);
		}
	}

	/**
	 * Informs all registered element state listeners about the deletion
	 * of the given element.
	 *
	 * @param element the element
	 * @see IElementStateListener#elementDeleted(Object)
	 */
	protected void fireElementDeleted(Object element) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= e.next();
			l.elementDeleted(element);
		}
	}

	/**
	 * Informs all registered element state listeners about a move.
	 *
	 * @param originalElement the element before the move
	 * @param movedElement the element after the move
	 * @see IElementStateListener#elementMoved(Object, Object)
	 */
	protected void fireElementMoved(Object originalElement, Object movedElement) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener l= e.next();
			l.elementMoved(originalElement, movedElement);
		}
	}

	@Override
	public long getModificationStamp(Object element) {
		return 0;
	}

	@Override
	public long getSynchronizationStamp(Object element) {
		return 0;
	}

	@Override
	public boolean isDeleted(Object element) {
		return false;
	}

	@Override
	public boolean isReadOnly(Object element) {
		return true;
	}

	@Override
	public boolean isModifiable(Object element) {
		return false;
	}

	/**
	 * Returns whether <code>validateState</code> has been called for the given element
	 * since the element's state has potentially been invalidated.
	 *
	 * @param element the element
	 * @return whether <code>validateState</code> has been called for the given element
	 * @since 2.0
	 */
	@Override
	public boolean isStateValidated(Object element) {
		ElementInfo info= fElementInfoMap.get(element);
		if (info != null)
			return info.fIsStateValidated;
		return false;
	}

	/**
	 * Hook method for validating the state of the given element. Must not take care of cache updating etc.
	 * Default implementation is empty.
	 *
	 * @param element the element
	 * @param computationContext the context in which validation happens
	 * @exception CoreException in case validation fails
	 * @since 2.0
	 */
	protected void doValidateState(Object  element, Object computationContext) throws CoreException {
	}

	@Override
	public void validateState(final Object element, final Object computationContext) throws CoreException {
		if (element == null)
			return;

		class ValidateStateOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException {
				ElementInfo info= fElementInfoMap.get(element);
				if (info == null)
					return;

				doValidateState(element, computationContext);

				doUpdateStateCache(element);
				info.fIsStateValidated= true;
				fireElementStateValidationChanged(element, true);
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return getValidateStateRule(element);
			}
		}

		executeOperation(new ValidateStateOperation(), getProgressMonitor());
	}

	/**
	 * Hook method for updating the state of the given element.
	 * Default implementation is empty.
	 *
	 * @param element the element
	 * @exception CoreException in case state cache updating fails
	 * @since 2.0
	 */
	protected void doUpdateStateCache(Object element) throws CoreException {
	}

	/**
	 * Returns whether the state of the element must be invalidated given its
	 * previous read-only state.
	 *
	 * @param element the element
	 * @param wasReadOnly the previous read-only state
	 * @return <code>true</code> if the state of the given element must be invalidated
	 * @since 2.0
	 */
	protected boolean invalidatesState(Object element, boolean wasReadOnly) {
		Assert.isTrue(PR10806_UC5_ENABLED != PR14469_ENABLED);
		boolean readOnlyChanged= (isReadOnly(element) != wasReadOnly && !wasReadOnly);
		if (PR14469_ENABLED)
			return readOnlyChanged && !canSaveDocument(element);
		return readOnlyChanged;
	}

	@Override
	public final void updateStateCache(Object element) throws CoreException {
		ElementInfo info= fElementInfoMap.get(element);
		if (info != null) {
			boolean wasReadOnly= isReadOnly(element);
			doUpdateStateCache(element);
			if (invalidatesState(element, wasReadOnly)) {
				info.fIsStateValidated= false;
				fireElementStateValidationChanged(element, false);
			}
		}
	}

	@Override
	public void setCanSaveDocument(Object element) {
		if (element != null) {
			ElementInfo info= fElementInfoMap.get(element);
			if (info != null) {
				info.fCanBeSaved= true;
				removeUnchangedElementListeners(element, info);
				fireElementDirtyStateChanged(element, info.fCanBeSaved);
			}
		}
	}

	/**
	 * Informs all registered element state listeners about a change in the state validation of the
	 * given element.
	 *
	 * @param element the element
	 * @param isStateValidated the flag indicating whether state validation is done
	 * @see IElementStateListenerExtension#elementStateValidationChanged(Object, boolean)
	 * @since 2.0
	 */
	protected void fireElementStateValidationChanged(Object element, boolean isStateValidated) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension l= (IElementStateListenerExtension) o;
				l.elementStateValidationChanged(element, isStateValidated);
			}
		}
	}

	/**
	 * Informs all registered element state listeners about the current state
	 * change of the element
	 *
	 * @param element the element
	 * @see IElementStateListenerExtension#elementStateChanging(Object)
	 * @since 2.0
	 */
	protected void fireElementStateChanging(Object element) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension l= (IElementStateListenerExtension) o;
				l.elementStateChanging(element);
			}
		}
	}

	/**
	 * Informs all registered element state listeners about the failed
	 * state change of the element
	 *
	 * @param element the element
	 * @see IElementStateListenerExtension#elementStateChangeFailed(Object)
	 * @since 2.0
	 */
	protected void fireElementStateChangeFailed(Object element) {
		Iterator<IElementStateListener> e= new ArrayList<>(fElementStateListeners).iterator();
		while (e.hasNext()) {
			Object o= e.next();
			if (o instanceof IElementStateListenerExtension) {
				IElementStateListenerExtension l= (IElementStateListenerExtension) o;
				l.elementStateChangeFailed(element);
			}
		}
	}

	@Override
	public IStatus getStatus(Object element) {
		ElementInfo info= fElementInfoMap.get(element);
		if (info != null) {
			if (info.fStatus != null)
				return info.fStatus;
			return (info.fDocument == null ? STATUS_ERROR : Status.OK_STATUS);
		}

		return STATUS_ERROR;
	}

	/**
	 * Performs the actual work of synchronizing the given element.
	 *
	 * @param element the element
	 * @param monitor the progress monitor
	 * @exception CoreException in the case that synchronization fails
	 * @since 3.0
	 */
	protected void doSynchronize(Object element, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public final void synchronize(final Object element) throws CoreException {

		if (element == null)
			return;

		class SynchronizeOperation extends DocumentProviderOperation implements ISchedulingRuleProvider {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException {
				doSynchronize(element, monitor);
			}

			@Override
			public ISchedulingRule getSchedulingRule() {
				return getSynchronizeRule(element);
			}
		}

		executeOperation(new SynchronizeOperation(), getProgressMonitor());
	}

	@Override
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor == null ? new NullProgressMonitor() : fProgressMonitor;
	}

	@Override
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
	}

	@Override
	public boolean isSynchronized(Object element) {
		return true;
	}

	@Override
	public boolean isNotSynchronizedException(Object element, CoreException ex) {
		return false;
	}

	@Override
	public IContentType getContentType(Object element) throws CoreException {
		return null;
	}
}
