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
package org.eclipse.jface.text.projection;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentInformationMapping;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.ISlaveDocumentManager;



/**
 * <code>ProjectionDocumentManager</code> is one particular implementation of
 * <code>ISlaveDocumentManager</code>. This manager creates so called
 * projection documents as slave documents for given master documents.
 * <p>
 * A projection document represents a particular projection of the master
 * document and is accordingly adapted to changes of the master document. Vice
 * versa, the master document is accordingly adapted to changes of its slave
 * documents. The manager does not maintain any particular management structure
 * but utilizes mechanisms given by <code>IDocument</code> such as position
 * categories and position updaters.
 * <p>
 * API in progress. Do not yet use.
 * 
 * @since 3.0
 */
public class ProjectionDocumentManager implements IDocumentListener, ISlaveDocumentManager {
	
	
	/**
	 * Name of the position category used to keep track of the master
	 * document's fragments that correspond to the segments of the projection
	 * documents.
	 */
	protected final static String FRAGMENTS_CATEGORY= "__fragmentsCategory"; //$NON-NLS-1$

	/**
	 * Name of the position category used to keep track of the project
	 * document's segments that correspond to the fragments of the master
	 * documents.
	 */
	protected final static String SEGMENTS_CATEGORY= "__segmentsCategory"; //$NON-NLS-1$
	
	
	/** The position updater shared by all master documents which have projection documents */
	private IPositionUpdater fFragmentsUpdater;
	/** Registry for master documents and their projection documents. */
	private Map fRegistry= new HashMap();
	
	
	/**
	 * Returns the fragments updater. If necessary, it is dynamically created.
	 * 
	 * @return the fragments updater
	 */
	private IPositionUpdater getFragmentsUpdater() {
		if (fFragmentsUpdater == null)
			fFragmentsUpdater= new FragmentUpdater(FRAGMENTS_CATEGORY);
		return fFragmentsUpdater;
	}
	
	/**
	 * Registers the given projection document for the given master document.
	 * 
	 * @param master the master document
	 * @param projection the projection document
	 */
	private void add(IDocument master, ProjectionDocument projection) {
		List list= (List) fRegistry.get(master);
		if (list == null) {
			list= new ArrayList(1);
			fRegistry.put(master, list);
		}
		list.add(projection);
	}
	
	/**
	 * Unregisters the given projection document from its master.
	 * 
	 * @param master the master document
	 * @param projection the projection document
	 */
	private void remove(IDocument master, ProjectionDocument projection) {
		List list= (List) fRegistry.get(master);
		if (list != null) {
			list.remove(projection);
			if (list.size() == 0)
				fRegistry.remove(master);
		}
	}
	
	/**
	 * Returns whether the given document is a master document.
	 * 
	 * @param master the document
	 * @return <code>true</code> if the given document is a master document known to this manager
	 */
	private boolean hasProjection(IDocument master) {
		return (fRegistry.get(master) instanceof List);
	}
	
	/**
	 * Returns an iterator enumerating all projection documents registered for the given document or
	 * <code>null</code> if the document is not a known master document.
	 * 
	 * @param master the document
	 * @return an iterator for all registered projection documents or <code>null</code>
	 */
	private Iterator getProjectionsIterator(IDocument master) {
		List list= (List) fRegistry.get(master);
		if (list != null)
			return list.iterator();
		return null;
	}
		
	/**
	 * Informs all projection documents of the master document that issued the given document event.
	 *
	 * @param about indicates whether the change is about to happen or happened already
	 * @param masterEvent the document event which will be processed to inform the projection documents
	 */
	protected void fireDocumentEvent(boolean about, DocumentEvent masterEvent) {
		IDocument master= masterEvent.getDocument();
		Iterator e= getProjectionsIterator(master);
		if (e == null)
			return;
		
		while (e.hasNext()) {
			ProjectionDocument document= (ProjectionDocument) e.next();
			if (about)
				document.masterDocumentAboutToBeChanged(masterEvent);
			else
				document.masterDocumentChanged(masterEvent);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		fireDocumentEvent(false, event);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
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
			return projectionDocument.getProjectionMapping();
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#createSlaveDocument(org.eclipse.jface.text.IDocument)
	 */
	public IDocument createSlaveDocument(IDocument master) {
		if (!master.containsPositionCategory(FRAGMENTS_CATEGORY)) {
			master.addPositionCategory(FRAGMENTS_CATEGORY);
			master.addPositionUpdater(getFragmentsUpdater());
			master.addDocumentListener(this);
		}
		ProjectionDocument slave= createProjectionDocument(master);
		add(master, slave);
		return slave;
	}
	
	/**
	 * Factory method for projection documents. 
	 * 
	 * @param master the master document
	 * @return the newly created projection document
	 */
	protected ProjectionDocument createProjectionDocument(IDocument master) {
		return new ProjectionDocument(master, FRAGMENTS_CATEGORY, SEGMENTS_CATEGORY);
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#freeSlaveDocument(org.eclipse.jface.text.IDocument)
	 */
	public void freeSlaveDocument(IDocument slave) {
		if (slave instanceof ProjectionDocument) {
			ProjectionDocument projectionDocument= (ProjectionDocument) slave;
			IDocument master= projectionDocument.getMasterDocument();
			remove(master, projectionDocument);
			
			try {
				if (!hasProjection(master))  {
					master.removeDocumentListener(this);
					master.removePositionUpdater(getFragmentsUpdater());
					master.removePositionCategory(FRAGMENTS_CATEGORY);
				}
			} catch (BadPositionCategoryException x) {
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.ISlaveDocumentManager#getMasterDocument(org.eclipse.jface.text.IDocument)
	 */
	public IDocument getMasterDocument(IDocument slave) {
		if (slave instanceof ProjectionDocument)
			return ((ProjectionDocument) slave).getMasterDocument();
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
	public void setAutoExpandMode(IDocument slave, boolean autoExpanding) {
		if (slave instanceof ProjectionDocument)
			((ProjectionDocument) slave).setAutoExpandMode(autoExpanding);
	}
}
