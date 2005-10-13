package org.eclipse.jface.text.source;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Describes a revision to an annotated document. A revision consists of one ore more
 * {@link ChangeRegion}s.
 * <p>
 * Clients may subclass.
 * </p>
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
public abstract class AnnotateRevision {
	final List fChangeRegions= new ArrayList();
	
	/**
	 * Creates a new revision.
	 */
	protected AnnotateRevision() {
	}
	
	/**
	 * Adds a line range to this revision.
	 * 
	 * @param range the line range that was changed with this revision
	 */
	public void addRange(ILineRange range) {
		fChangeRegions.add(new ChangeRegion(this, range));
	}

	/**
	 * Returns the hover information that will be shown when the user hovers over the a change
	 * region of this revision.
	 * 
	 * @return the hover information for this revision or <code>null</code> for no hover
	 */
	public abstract Object getHoverInfo();

	/**
	 * Returns the id of the committer of this revision. The id is used to show
	 * revisions by the same committer in the same color.
	 * 
	 * @return the id of the committer of this revision
	 */
	public abstract String getCommitterId();

	/**
	 * Returns the unique (within the document id of this revision.
	 * 
	 * @return the id of this revision
	 */
	public abstract String getId();
	
	/**
	 * Returns the modification date of this revision.
	 * 
	 * @return the modification date of this revision
	 */
	public abstract Date getDate();
	
	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Revision " + getId(); //$NON-NLS-1$
	}
}
