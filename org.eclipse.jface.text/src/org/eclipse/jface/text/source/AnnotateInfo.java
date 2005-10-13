package org.eclipse.jface.text.source;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Assert;

/**
 * Encapsulates annotate info for one line-based document.
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
public final class AnnotateInfo {
	final List fRevisions= new ArrayList();
	/**
	 * Creates a new annotate information object.
	 */
	public AnnotateInfo() {
	}
	
	/**
	 * Adds a revision.
	 * 
	 * @param revision a revision
	 */
	public void addRevision(AnnotateRevision revision) {
		Assert.isLegal(revision != null);
		fRevisions.add(revision);
	}
	
}
