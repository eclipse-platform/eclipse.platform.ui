package org.eclipse.jface.text.presentation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;


/**
 * Presentation repairers are used by a presentation reconciler
 * to rebuild a damaged region in a document's presentation. A presentation repairer
 * is assumed to be specific for a particular document content type. The presentation
 * repairer gets the region which it should repair and constructs a "repair description" 
 * The presentation repairer adds the individual steps of this sequence into the 
 * text presentation it gets passed in.<p>
 * This interface must either be implemented by clients or clients use the rule-based
 * default implementation <code>RuleBasedDamagerRepairer</code>. Implementers should be
 * registered with a presentation reconciler in order get involved in the reconciling 
 * process. 
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
	 * presentation reconciler's text viewer repair the  presentational damage described by
	 * the given region.
	 *
	 * @param presentation the text presentation to be filled by this repairer
	 * @param damage the damage to be repaired
	 */
	void createPresentation(TextPresentation presentation, ITypedRegion damage);
}
