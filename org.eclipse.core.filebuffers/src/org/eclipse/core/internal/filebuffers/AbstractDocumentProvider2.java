/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.filebuffers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;



/**
 * An abstract implementation of a shareable document provider.
 * <p>
 * Subclasses must implement <code>createDocument</code>,
 * <code>createAnnotationModel</code>, and <code>doSaveDocument</code>.
 * </p>
 */
public abstract class AbstractDocumentProvider2 implements IDocumentProvider2 {
		
		/**
		 * Collection of all information managed for a connected element.
		 */
		protected class ElementInfo implements IDocumentListener {
			
			/** The element for which the info is stored */
			public Object fElement;
			/** How often the element has been connected */
			public int fReferenceCount;
			/** Can the element be saved */
			public boolean fCanBeSaved;
			/** The element's document */
			public IDocument fDocument;
			/** Has element state been validated */
			public boolean fIsStateValidated;
			/** The status of this element */
			public IStatus fStatus;
			
			
			/**
			 * Creates a new element info, initialized with the given
			 * document and annotation model.
			 *
			 * @param document the document
			 */
			public ElementInfo(IDocument document) {
				fDocument= document;
				fReferenceCount= 0;
				fCanBeSaved= false;
				fIsStateValidated= false;
			}
			
			/**
			 * An element info equals another object if this object is an element info
			 * and if the elements of the two element infos are equal.
			 */
			public boolean equals(Object o) {
				if (o instanceof ElementInfo) {
					ElementInfo e= (ElementInfo) o;
					return fElement.equals(e.fElement);
				}
				return false;
			}
			
			/*
			 * @see Object#hashCode()
			 */
			public int hashCode() {
				return fDocument.hashCode();
			}
			
			/*
			 * @see IDocumentListener#documentChanged(DocumentEvent)
			 */
			public void documentChanged(DocumentEvent event) {
				fCanBeSaved= true;
				removeUnchangedElementListeners(fElement, this);
				fireElementDirtyStateChanged(fElement, fCanBeSaved);
			}
			
			/*
			 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
			 */
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
		};
			
	/**
	 * Constant for representing the ok status. This is considered a value object.
	 */
	static final protected IStatus STATUS_OK= new Status(IStatus.OK, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, "AbstractDocumentProvider.ok", null);
	/**
	 * Constant for representing the error status. This is considered a value object.
	 */
	static final protected IStatus STATUS_ERROR= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.INFO, "AbstractDocumentProvider.error", null);
	
	
	/** Element information of all connected elements */
	private Map fElementInfoMap= new HashMap();
	/** The element state listeners */
	private List fElementStateListeners= new ArrayList();
	/** The current progress monitor */
	private IProgressMonitor fProgressMonitor;

	
	/**
	 * Creates a new document provider.
	 */
	protected AbstractDocumentProvider2() {
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
	 * Performs the actual work of saving the given document provided for the given element.
	 * <p>
	 * Subclasses must implement this method.</p>
	 *
	 * @param element the element
	 * @param document the document
	 * @param overwrite indicates whether an overwrite should happen if necessary
	 * @exception CoreException if document could not be stored to the given element
	 */
	protected abstract void doSaveDocument(Object element, IDocument document, boolean overwrite) throws CoreException;
	
	
	/**
	 * Returns the element info object for the given element.
	 *
	 * @param element the element
	 * @return the element info object, or <code>null</code> if none
	 */
	protected ElementInfo getElementInfo(Object element) {
		return (ElementInfo) fElementInfoMap.get(element);
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
		return new ElementInfo(createDocument(element));
	}
	
	/**
	 * Disposes the given element info object.
	 * <p>
	 * This method is called when an element info is disposed. The 
	 * <code>AbstractDocumentProvider</code> implementation of this
	 * method does nothing. Subclasses may reimplement.</p>
	 *
	 * @param info the element info object
	 */
	protected void disposeElementInfo(ElementInfo info) {
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
	 * @return the list of elements (element type: <code>Object</code>)
	 */
	protected Iterator getConnectedElementsIterator() {
		Set s= new HashSet();
		Set keys= fElementInfoMap.keySet();
		if (keys != null)
			s.addAll(keys);
		return s.iterator();
	}
	
	/*
	 * @see IDocumentProvider#connect(Object)
	 */
	public final void connect(Object element) throws CoreException {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info == null) {
			
			info= createElementInfo(element);
			if (info == null) 
				info= new ElementInfo(null);
								
			info.fElement= element;
			
			addUnchangedElementListeners(element, info);
			
			fElementInfoMap.put(element, info);
			if (fElementInfoMap.size() == 1)
				connected();
		}	
		++ info.fReferenceCount;		
	}
	
	/**
	 * This hook method is called when this provider starts managing documents for 
	 * elements. I.e. it is called when the first element gets connected to this provider.
	 * Subclasses may extend.
	 */
	protected void connected() {
	}
	
	/*
	 * @see IDocumentProvider#disconnect
	 */
	public final void disconnect(Object element) {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		
		if (info == null)
			return;
		
		if (info.fReferenceCount == 1) {
			
			fElementInfoMap.remove(element);
			removeUnchangedElementListeners(element, info);
			disposeElementInfo(info);
			
			if (fElementInfoMap.size() == 0)
				disconnected();
			
		} else
		 	-- info.fReferenceCount;
	}
	
	/**
	 * This hook method is called when this provider stops managing documents for
	 * element. I.e. it is called when the last element gets disconnected from this provider.
	 * Subcalles may extend.
	 */
	protected void disconnected() {
	}
	
	/*
	 * @see IDocumentProvider#getDocument(Object)
	 */
	public IDocument getDocument(Object element) {
		
		if (element == null)
			return null;
			
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		return (info != null ? info.fDocument : null);
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IDocumentProvider#getElement(org.eclipse.jface.text.IDocument)
	 */
	public Object getElement(IDocument document) {
		Iterator e= fElementInfoMap.keySet().iterator();
		while (e.hasNext())  {
			Object key= e.next();
			ElementInfo info= (ElementInfo) fElementInfoMap.get(key);
			if (info != null && document == info.fDocument)
				return info.fElement;
		}
		return null;
	}
	
	/*
	 * @see IDocumentProvider#mustSaveDocument(Object)
	 */
	public boolean mustSaveDocument(Object element) {
		
		if (element == null)
			return false;
			
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		return (info != null ? info.fReferenceCount == 1 && info.fCanBeSaved : false);
	}
	
	/*
	 * @see IDocumentProvider#canSaveDocument(Object)
	 */
	public boolean canSaveDocument(Object element) {
		
		if (element == null)
			return false;
			
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		return (info != null ? info.fCanBeSaved : false);
	}
	
	/*
	 * @see IDocumentProvider#resetDocument(Object)
	 */
	public void restoreDocument(Object element) throws CoreException {
		if (element == null)
			return;
			
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
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
				
				String originalContents= original.get();
				boolean replaceContents= !originalContents.equals(info.fDocument.get());
				
				if (replaceContents)  {
					fireElementContentAboutToBeReplaced(element);
					info.fDocument.set(original.get());
				}
				
				if (info.fCanBeSaved) {
					info.fCanBeSaved= false;
					addUnchangedElementListeners(element, info);
				}
				
				if (replaceContents)
					fireElementContentReplaced(element);
					
				fireElementDirtyStateChanged(element, false);
			}
		}
	}
	
	/*
	 * @see IDocumentProvider#saveDocument(Object, boolean)
	 */
	public void saveDocument(Object element, boolean overwrite) throws CoreException {
		
		if (element == null)
			return;
			
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null && info.fCanBeSaved) {
			doSaveDocument(element, info.fDocument, overwrite);
			info.fCanBeSaved= false;
			addUnchangedElementListeners(element, info);
			fireElementDirtyStateChanged(element, false);
		}
	}
	
	/*
	 * @see IDocumentProvider#addElementStateListener(IElementStateListener)
	 */
	public void addElementStateListener(IElementStateListener2 listener) {
		Assert.isNotNull(listener);
		if (!fElementStateListeners.contains(listener))
			fElementStateListeners.add(listener);
	}
	
	/*
	 * @see IDocumentProvider#removeElementStateListener(IElementStateListener)
	 */
	public void removeElementStateListener(IElementStateListener2 listener) {
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
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
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
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.documentContentAboutToBeReplaced(element);
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
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.documentContentReplaced(element);
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
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.elementMoved(originalElement, movedElement);
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
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.elementDeleted(element);
		}
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IDocumentProvider#isStateValidated(java.lang.Object)
	 */
	public boolean isStateValidated(Object element) {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
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
	 */
	protected void doValidateState(Object  element, Object computationContext) throws CoreException {
	}

	/*
	 * @see org.eclipse.core.buffer.text.IDocumentProvider#validateState(java.lang.Object, java.lang.Object)
	 */
	public void validateState(Object element, Object computationContext) throws CoreException {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null && !info.fIsStateValidated)  {
			
			doValidateState(element, computationContext);
			
			doUpdateStateCache(element);
			info.fIsStateValidated= true;
			fireElementStateValidationChanged(element, true);
		}
	}
	
	/**
	 * Hook method for updating the state of the given element.
	 * Default implementation is empty.
	 * 
	 * @param element the element
	 * @exception CoreException in case state cache updating fails
	 */
	protected void doUpdateStateCache(Object element) throws CoreException {
	}
	
//	/**
//	 * Returns whether the state of the element must be invalidated given its
//	 * previous read-only state.
//	 * 
//	 * @param element the element
//	 * @param wasReadOnly the previous read-only state
//	 * @return <code>true</code> if the state of the given element must be invalidated
//	 * @since 2.0
//	 */
//	protected boolean invalidatesState(Object element, boolean wasReadOnly) {
//		boolean readOnlyChanged= (isReadOnly(element) != wasReadOnly);
//		return readOnlyChanged && !canSaveDocument(element);
//	}
	
//	final public void updateStateCache(Object element) throws CoreException {
//		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
//		if (info != null) {
//			boolean wasReadOnly= isReadOnly(element);
//			doUpdateStateCache(element);
//			if (invalidatesState(element, wasReadOnly)) {
//				info.fIsStateValidated= false;
//				fireElementStateValidationChanged(element, false);
//			}
//		}
//	}
	

//	public void setCanSaveDocument(Object element) {
//		if (element != null) {
//			ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
//			if (info != null) {
//				info.fCanBeSaved= true;
//				removeUnchangedElementListeners(element, info);
//				fireElementDirtyStateChanged(element, info.fCanBeSaved);
//			}
//		}
//	}
	
	/**
	 * Informs all registered element state listeners about a change in the
	 * state validation of the given element.
	 *
	 * @param element the element
	 * @param isStateValidated
	 */
	protected void fireElementStateValidationChanged(Object element, boolean isStateValidated) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.elementStateValidationChanged(element, isStateValidated);
		}
	}
	
	/**
	 * Informs all registered element state listeners about the current state
	 * change of the element
	 *
	 * @param element the element
	 */
	protected void fireElementStateChanging(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.elementStateChanging(element);
		}
	}
	
	/**
	 * Informs all registered element state listeners about the failed
	 * state change of the element
	 *
	 * @param element the element
	 */
	protected void fireElementStateChangeFailed(Object element) {
		Iterator e= new ArrayList(fElementStateListeners).iterator();
		while (e.hasNext()) {
			IElementStateListener2 l= (IElementStateListener2) e.next();
			l.elementStateChangeFailed(element);
		}
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IDocumentProvider#getStatus(java.lang.Object)
	 */
	public IStatus getStatus(Object element) {
		ElementInfo info= (ElementInfo) fElementInfoMap.get(element);
		if (info != null) {
			if (info.fStatus != null)
				return info.fStatus;
			return (info.fDocument == null ? STATUS_ERROR : STATUS_OK);
		}
		
		return STATUS_ERROR;
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IDocumentProvider#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor == null ? new NullProgressMonitor() : fProgressMonitor;
	}

	/*
	 * @see org.eclipse.core.buffer.text.IDocumentProvider#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
	}
}
