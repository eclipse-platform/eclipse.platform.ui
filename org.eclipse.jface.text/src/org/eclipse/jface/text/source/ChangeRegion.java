package org.eclipse.jface.text.source;

import org.eclipse.jface.text.Assert;

/**
 * A change region describes a contiguous range of lines that was changed in the same revision
 * of a document.
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
class ChangeRegion {
	final ILineRange fLines;
	final AnnotateRevision fRevision;
	/**
	 * Creates a new change region for the given revision and line range.
	 * 
	 * @param revision the revision of the new region
	 * @param lines the line range of the new region
	 */
	public ChangeRegion(AnnotateRevision revision, ILineRange lines) {
		Assert.isLegal(revision != null);
		Assert.isLegal(lines != null);
		fLines= lines;
		fRevision=revision;
	}
	
	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ChangeRegion [" + fRevision.toString() + ", [" + fLines.getStartLine() + "+" + fLines.getNumberOfLines() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
