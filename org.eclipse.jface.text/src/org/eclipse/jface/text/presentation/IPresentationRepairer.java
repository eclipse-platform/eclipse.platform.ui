/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.presentation;



import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;


/**
 * A presentation repairer is a strategy used by a presentation reconciler to
 * rebuild a damaged region in a document's presentation. A presentation
 * repairer is assumed to be specific for a particular document content type.
 * The presentation repairer gets the region which it should repair and
 * constructs a "repair description". The presentation repairer merges the steps
 * contained within this description into the text presentation passed into
 * <code>createPresentation</code>.
 * <p>
 * This interface may be implemented by clients. Alternatively, clients may use
 * the rule-based default implementation
 * {@link org.eclipse.jface.text.rules.DefaultDamagerRepairer}. Implementers
 * should be registered with a presentation reconciler in order get involved in
 * the reconciling process.
 * </p>
 *
 * @see IPresentationReconciler
 * @see IDocument
 * @see org.eclipse.swt.custom.StyleRange
 * @see TextPresentation
 */
public interface IPresentationRepairer {


	/**
	 * Tells the presentation repairer on which document it will work.
	 *
	 * @param document the damager's working document
	 */
	void setDocument(IDocument document);

	/**
	 * Fills the given presentation with the style ranges which when applied to the
	 * presentation reconciler's text viewer repair the  presentation damage described by
	 * the given region.
	 *
	 * @param presentation the text presentation to be filled by this repairer
	 * @param damage the damage to be repaired
	 */
	void createPresentation(TextPresentation presentation, ITypedRegion damage);
}
