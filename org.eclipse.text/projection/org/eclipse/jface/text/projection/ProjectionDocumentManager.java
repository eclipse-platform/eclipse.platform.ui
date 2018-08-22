/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jface.text.projection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentInformationMapping;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.ISlaveDocumentManagerExtension;


/**
 * A <code>ProjectionDocumentManager</code> is one particular implementation
 * of {@link org.eclipse.jface.text.ISlaveDocumentManager}. This manager
 * creates so called projection documents (see
 * {@link org.eclipse.jface.text.projection.ProjectionDocument}as slave
 * documents for given master documents.
 * <p>
 * A projection document represents a particular projection of the master
 * document and is accordingly adapted to changes of the master document. Vice
 * versa, the master document is accordingly adapted to changes of its slave
 * documents. The manager does not maintain any particular management structure
 * but utilizes mechanisms given by {@link org.eclipse.jface.text.IDocument}
 * such as position categories and position updaters.
 * <p>
 * Clients can instantiate this class. This class is not intended to be
 * subclassed.</p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProjectionDocumentManager implements IDocumentListener, ISlaveDocumentManager, ISlaveDocumentManagerExtension {

	/** Registry for master documents and their projection documents. */
	private Map<IDocument, List<ProjectionDocument>> fProjectionRegistry= new HashMap<>();

	/**
	 * Registers the given projection document for the given master document.
	 *
	 * @param master the master document
	 * @param projection the projection document
	 */
	private void add(IDocument master, ProjectionDocument projection) {
		List<ProjectionDocument> list= fProjectionRegistry.get(master);
		if (list == null) {
			list= new ArrayList<>(1);
			fProjectionRegistry.put(master, list);
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
		List<ProjectionDocument> list= fProjectionRegistry.get(master);
		if (list != null) {
			list.remove(projection);
			if (list.size() == 0)
				fProjectionRegistry.remove(master);
		}
	}

	/**
	 * Returns whether the given document is a master document.
	 *
	 * @param master the document
	 * @return <code>true</code> if the given document is a master document known to this manager
	 */
	private boolean hasProjection(IDocument master) {
		return (fProjectionRegistry.get(master) != null);
	}

	/**
	 * Returns an iterator enumerating all projection documents registered for the given document or
	 * <code>null</code> if the document is not a known master document.
	 *
	 * @param master the document
	 * @return an iterator for all registered projection documents or <code>null</code>
	 */
	private Iterator<ProjectionDocument> getProjectionsIterator(IDocument master) {
		List<ProjectionDocument> list= fProjectionRegistry.get(master);
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
		Iterator<ProjectionDocument> e= getProjectionsIterator(master);
		if (e == null)
			return;

		while (e.hasNext()) {
			ProjectionDocument document= e.next();
			if (about)
				document.masterDocumentAboutToBeChanged(masterEvent);
			else
				document.masterDocumentChanged(masterEvent);
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		fireDocumentEvent(false, event);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		fireDocumentEvent(true, event);
	}

	@Override
	public IDocumentInformationMapping createMasterSlaveMapping(IDocument slave) {
		if (slave instanceof ProjectionDocument) {
			ProjectionDocument projectionDocument= (ProjectionDocument) slave;
			return projectionDocument.getDocumentInformationMapping();
		}
		return null;
	}

	@Override
	public IDocument createSlaveDocument(IDocument master) {
		if (!hasProjection(master))
			master.addDocumentListener(this);
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
		return new ProjectionDocument(master);
	}

	@Override
	public void freeSlaveDocument(IDocument slave) {
		if (slave instanceof ProjectionDocument) {
			ProjectionDocument projectionDocument= (ProjectionDocument) slave;
			IDocument master= projectionDocument.getMasterDocument();
			remove(master, projectionDocument);
			projectionDocument.dispose();
			if (!hasProjection(master))
				master.removeDocumentListener(this);
		}
	}

	@Override
	public IDocument getMasterDocument(IDocument slave) {
		if (slave instanceof ProjectionDocument)
			return ((ProjectionDocument) slave).getMasterDocument();
		return null;
	}

	@Override
	public boolean isSlaveDocument(IDocument document) {
		return (document instanceof ProjectionDocument);
	}

	@Override
	public void setAutoExpandMode(IDocument slave, boolean autoExpanding) {
		if (slave instanceof ProjectionDocument)
			((ProjectionDocument) slave).setAutoExpandMode(autoExpanding);
	}

	@Override
	public IDocument[] getSlaveDocuments(IDocument master) {
		List<ProjectionDocument> list= fProjectionRegistry.get(master);
		if (list != null) {
			IDocument[] result= new IDocument[list.size()];
			list.toArray(result);
			return result;
		}
		return null;
	}
}
