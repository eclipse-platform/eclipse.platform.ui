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

package org.eclipse.jface.text;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * <code>ProjectionDocumentManager</code> is one particular implementation of 
 * <code>ISlaveDocumentManager</code>. This manager creates so called projection
 * documents as slave documents for given master documents.<p>
 * 
 * A projection document represents a particular projection of the parent 
 * document and is accordingly adapted to changes of the parent document. 
 * Vice versa, the parent document is accordingly adapted to changes of
 * its child documents. The manager does not maintain any particular management
 * structure but utilizes mechanisms given by <code>IDocument</code> such
 * as position categories and position updaters. <p>
 * This class if for internal use only.
 * 
 * @since 2.1
 */
public final class ProjectionDocumentManager implements IDocumentListener, ISlaveDocumentManager {
	
	
	/** 
	 * Name of the position category used to keep track of the parent document
	 * ranges that correspond to the fragments of the projection documents.
	 */
	public final static String PROJECTION_DOCUMENTS= "__projectiondocuments"; //$NON-NLS-1$
	
	
//	static class ProjectionDocumentPartitioner implements IDocumentPartitioner {
//		
//		protected ProjectionDocument fProjectionDocument;
//		protected IDocument fParentDocument;
//		
//		protected ProjectionDocumentPartitioner() {
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#getPartition(int)
//		 */
//		public ITypedRegion getPartition(int offset) {
//			try {
//				offset += fProjectionDocument.getParentDocumentRange().getOffset();
//				return fParentDocument.getPartition(offset);
//			} catch (BadLocationException x) {
//			}
//			
//			return null;
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#computePartitioning(int, int)
//		 */
//		public ITypedRegion[] computePartitioning(int offset, int length) {
//			try {
//				offset += fProjectionDocument.getParentDocumentRange().getOffset();
//				return fParentDocument.computePartitioning(offset, length);
//			} catch (BadLocationException x) {
//			}
//			
//			return null;
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#getContentType(int)
//		 */
//		public String getContentType(int offset) {
//			try {
//				offset += fProjectionDocument.getParentDocumentRange().getOffset();
//				return fParentDocument.getContentType(offset);
//			} catch (BadLocationException x) {
//			}
//			
//			return null;
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#getLegalContentTypes()
//		 */
//		public String[] getLegalContentTypes() {
//			return fParentDocument.getLegalContentTypes();
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#documentChanged(DocumentEvent)
//		 */
//		public boolean documentChanged(DocumentEvent event) {
//			// ignore as the parent does this for us
//			return false;
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#documentAboutToBeChanged(DocumentEvent)
//		 */
//		public void documentAboutToBeChanged(DocumentEvent event) {
//			// ignore as the parent does this for us
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#disconnect()
//		 */
//		public void disconnect() {
//			fProjectionDocument= null;
//			fParentDocument= null;
//		}
//		
//		/*
//		 * @see IDocumentPartitioner#connect(IDocument)
//		 */
//		public void connect(IDocument childDocument) {
//			Assert.isTrue(childDocument instanceof ProjectionDocument);
//			fProjectionDocument= (ProjectionDocument) childDocument;
//			fParentDocument= fProjectionDocument.getParentDocument();
//		}	
//	};
	
	
	
	/** The position updater shared by all master documents which have projection documents */
	private IPositionUpdater fProjectionPositionUpdater;
	/** Registry for master documents and their projection documents. */
	private Map fRegistar= new HashMap();
	
	
	/**
	 * Returns the projection position updater. If necessary, it is dynamically created.
	 *
	 * @return the child position updater
	 */
	protected IPositionUpdater getProjectionPositionUpdater() {
		if (fProjectionPositionUpdater == null)
			fProjectionPositionUpdater= new FragmentUpdater(PROJECTION_DOCUMENTS);
		return fProjectionPositionUpdater;
	}
	
	/**
	 * Registers the given projection document for the given master document.
	 * 
	 * @param parent the master document
	 * @param projection the projection document
	 */
	private void add(IDocument parent, ProjectionDocument projection) {
		List list= (List) fRegistar.get(parent);
		if (list == null) {
			list= new ArrayList(1);
			fRegistar.put(parent, list);
		}
		list.add(projection);
	}
	
	/**
	 * Unregisters the given projection document from its master.
	 * 
	 * @param parent the master document
	 * @param projection the projection document
	 */
	private void remove(IDocument parent, ProjectionDocument projection) {
		List list= (List) fRegistar.get(parent);
		if (list != null) {
			list.remove(projection);
			if (list.size() == 0)
				fRegistar.remove(parent);
		}
	}
	
	/**
	 * Returns whether the given document is a master document.
	 * 
	 * @param parent the document
	 * @return <code>true</code> if the given document is a master document known to this manager
	 */
	private boolean hasProjection(IDocument parent) {
		return (fRegistar.get(parent) instanceof List);
	}
	
	/**
	 * Returns an iterator enumerating all projection documents registered for the given document or
	 * <code>null</code> if the document is not a known master document.
	 * 
	 * @param parent the document
	 * @return an iterator for all registered projection documents or <code>null</code>
	 */
	private Iterator getProjectionsIterator(IDocument parent) {
		List list= (List) fRegistar.get(parent);
		if (list != null)
			return list.iterator();
		return null;
	}
		
	/**
	 * Informs all projection documents of the master document that issued the given document event.
	 *
	 * @param about indicates whether the change is about to happen or alread happend
	 * @param event the document event which will be processed to inform the projection documents
	 */
	protected void fireDocumentEvent(boolean about, DocumentEvent event) {
		IDocument parent= event.getDocument();
		Iterator e= getProjectionsIterator(parent);
		if (e == null)
			return;
		
		while (e.hasNext()) {
			ProjectionDocument document= (ProjectionDocument) e.next();
			if (about)
				document.parentDocumentAboutToBeChanged(event);
			else
				document.parentDocumentChanged(event);
		}
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		fireDocumentEvent(false, event);
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		fireDocumentEvent(true, event);
	}
		
	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#createMasterSlaveMapping(org.eclipse.jface.text.IDocument)
	 */
	public IDocumentInformationMapping createMasterSlaveMapping(IDocument slave) {
		if (slave instanceof ProjectionDocument) {
			ProjectionDocument projectionDocument= (ProjectionDocument) slave;
			return new CoordinatesTranslator(projectionDocument.getParentDocument(), PROJECTION_DOCUMENTS, projectionDocument, ProjectionDocument.FRAGMENT_CATEGORY);
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#createSlaveDocument(org.eclipse.jface.text.IDocument)
	 */
	public IDocument createSlaveDocument(IDocument master) {
		if (!master.containsPositionCategory(PROJECTION_DOCUMENTS)) {
			master.addPositionCategory(PROJECTION_DOCUMENTS);
			master.addPositionUpdater(getProjectionPositionUpdater());
			master.addDocumentListener(this);
		}

		ProjectionDocument slave= new ProjectionDocument(master, PROJECTION_DOCUMENTS);
//		IDocumentPartitioner partitioner= new ProjectionDocumentPartitioner();
//		slave.setDocumentPartitioner(partitioner);
//		partitioner.connect(master);

		add(master, slave);
		return slave;
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#freeSlaveDocument(org.eclipse.jface.text.IDocument)
	 */
	public void freeSlaveDocument(IDocument slave) {
		
		if ( !(slave instanceof ProjectionDocument))
			return;
			
		ProjectionDocument projectionDocument= (ProjectionDocument) slave;
			
//		projectionDocument.getDocumentPartitioner().disconnect();

		IDocument parent= projectionDocument.getParentDocument();
		remove(parent, projectionDocument);

		try {
			if (!hasProjection(parent))  {
				parent.removeDocumentListener(this);
				parent.removePositionUpdater(getProjectionPositionUpdater());
				parent.removePositionCategory(PROJECTION_DOCUMENTS);
			}
		} catch (BadPositionCategoryException x) {
		}
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#getMasterDocument(org.eclipse.jface.text.IDocument)
	 */
	public IDocument getMasterDocument(IDocument slave) {
		if (slave instanceof ProjectionDocument)
			return ((ProjectionDocument) slave).getParentDocument();
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#isSlaveDocument(org.eclipse.jface.text.IDocument)
	 */
	public boolean isSlaveDocument(IDocument document) {
		return (document instanceof ProjectionDocument);
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#setAutoExpandMode(org.eclipse.jface.text.IDocument, boolean)
	 */
	public void setAutoExpandMode(IDocument slave, boolean autoExpand) {
		// TODO
	}
}
