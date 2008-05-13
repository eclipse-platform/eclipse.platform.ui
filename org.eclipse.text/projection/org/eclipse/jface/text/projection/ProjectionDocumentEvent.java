/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.SlaveDocumentEvent;


/**
 * This event is sent out by an
 * {@link org.eclipse.jface.text.projection.ProjectionDocument}when it is
 * manipulated. The manipulation is either a content manipulation or a change of
 * the projection between the master and the slave. Clients can determine the
 * type of change by asking the projection document event for its change type
 * (see {@link #getChangeType()}) and comparing it with the predefined types
 * {@link #PROJECTION_CHANGE}and {@link #CONTENT_CHANGE}.
 * <p>
 * Clients are not supposed to create instances of this class. Instances are
 * created by {@link org.eclipse.jface.text.projection.ProjectionDocument}
 * instances. This class is not intended to be subclassed.</p>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProjectionDocumentEvent extends SlaveDocumentEvent {

	/** The change type indicating a projection change */
	public final static Object PROJECTION_CHANGE= new Object();
	/** The change type indicating a content change */
	public final static Object CONTENT_CHANGE= new Object();

	/** The change type */
	private Object fChangeType;
	/** The offset of the change in the master document */
	private int fMasterOffset= -1;
	/** The length of the change in the master document */
	private int fMasterLength= -1;

	/**
	 * Creates a new content change event caused by the given master document
	 * change. Instances created using this constructor return <code>-1</code>
	 * when calling <code>getMasterOffset</code> or
	 * <code>getMasterLength</code>. This information can be obtained by
	 * accessing the master event.
	 *
	 * @param doc the changed projection document
	 * @param offset the offset in the projection document
	 * @param length the length in the projection document
	 * @param text the replacement text
	 * @param masterEvent the original master event
	 */
	public ProjectionDocumentEvent(IDocument doc, int offset, int length, String text, DocumentEvent masterEvent) {
		super(doc, offset, length, text, masterEvent);
		fChangeType= CONTENT_CHANGE;
	}

	/**
	 * Creates a new projection change event for the given properties. Instances
	 * created with this constructor return the given master document offset and
	 * length but do not have an associated master document event.
	 *
	 * @param doc the projection document
	 * @param offset the offset in the projection document
	 * @param length the length in the projection document
	 * @param text the replacement text
	 * @param masterOffset the offset in the master document
	 * @param masterLength the length in the master document
	 */
	public ProjectionDocumentEvent(IDocument doc, int offset, int length, String text, int masterOffset, int masterLength) {
		super(doc, offset, length, text, null);
		fChangeType= PROJECTION_CHANGE;
		fMasterOffset= masterOffset;
		fMasterLength= masterLength;
	}

	/**
	 * Creates a new projection document event for the given properties. The
	 * projection change is caused by a manipulation of the master document. In
	 * order to accommodate the master document change, the projection document
	 * had to change the projection. Instances created with this constructor
	 * return the given master document offset and length and also have an
	 * associated master document event.
	 *
	 * @param doc the projection document
	 * @param offset the offset in the projection document
	 * @param length the length in the projection document
	 * @param text the replacement text
	 * @param masterOffset the offset in the master document
	 * @param masterLength the length in the master document
	 * @param masterEvent the master document event
	 */
	public ProjectionDocumentEvent(IDocument doc, int offset, int length, String text, int masterOffset, int masterLength, DocumentEvent masterEvent) {
		super(doc, offset, length, text, masterEvent);
		fChangeType= PROJECTION_CHANGE;
		fMasterOffset= masterOffset;
		fMasterLength= masterLength;
	}

	/**
	 * Returns the change type of this event. This is either {@link #PROJECTION_CHANGE} or
	 * {@link #CONTENT_CHANGE}.
	 *
	 * @return the change type of this event
	 */
	public Object getChangeType() {
		return fChangeType;
	}

	/**
	 * Returns the offset of the master document range that has been added or removed in case this
	 * event describes a projection change, otherwise it returns <code>-1</code>.
	 *
	 * @return the master document offset of the projection change or <code>-1</code>
	 */
	public int getMasterOffset() {
		return fMasterOffset;
	}

	/**
	 * Returns the length of the master document range that has been added or removed in case this event
	 * describes a projection changed, otherwise <code>-1</code>.
	 *
	 * @return the master document length of the projection change or <code>-1</code>
	 */
	public int getMasterLength() {
		return fMasterLength;
	}
}
