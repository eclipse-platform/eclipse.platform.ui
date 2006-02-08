/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;

/**
 * Descriptor object of a refactoring.
 * <p>
 * A refactoring descriptor contains refactoring-specific data which allows the
 * framework to completely reconstruct a particular refactoring instance and
 * execute it on an arbitrary workspace. Refactoring descriptors are identified
 * by their refactoring id {@link #getID()} and their time stamps
 * {@link #getTimeStamp()}.
 * </p>
 * <p>
 * Refactoring descriptors are potentially heavyweight objects which should not
 * be held on to. Use refactoring descriptor handles
 * {@link RefactoringDescriptorProxy} to store refactoring information.
 * </p>
 * <p>
 * Clients which create specific refactoring descriptors during change
 * generation should choose an informative description of the particular
 * refactoring instance and pass appropriate descriptor flags to the
 * constructor.
 * </p>
 * <p>
 * All time stamps are measured in UTC milliseconds from the epoch (see
 * {@link java.util#Calendar}).
 * </p>
 * <p>
 * Note: this class is indented to be subclassed to provide specialized
 * descriptor for particular refactorings.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see RefactoringDescriptorProxy
 * @see IRefactoringHistoryService
 * 
 * @since 3.2
 */
public abstract class RefactoringDescriptor implements Comparable {

	/**
	 * Constant describing the API change flag (value: 1)
	 * <p>
	 * Clients should set this flag to indicate that the represented refactoring
	 * may cause breaking API changes. If clients set the
	 * {@link #BREAKING_CHANGE} flag, they should set {@link #STRUCTURAL_CHANGE}
	 * as well.
	 * </p>
	 */
	public static final int BREAKING_CHANGE= 1 << 0;

	/**
	 * Constant describing the multi change flag (value: 4)
	 * <p>
	 * Clients should set this flag to indicate that the change created by the
	 * represented refactoring might causes changes in other files than the
	 * files of the input elements according to the semantics of the associated
	 * programming language.
	 * </p>
	 */
	public static final int MULTI_CHANGE= 1 << 2;

	/** Constant describing the absence of any flags (value: 0) */
	public static final int NONE= 0;

	/**
	 * Constant describing the structural change flag (value: 2)
	 * <p>
	 * Clients should set this flag to indicate that the change created by the
	 * represented refactoring might be a structural change according to the
	 * semantics of the associated programming language.
	 * </p>
	 */
	public static final int STRUCTURAL_CHANGE= 1 << 1;

	/**
	 * The unknown refactoring id (value:
	 * org.eclipse.ltk.core.refactoring.unknown)
	 * <p>
	 * This id is reserved by the refactoring framework to signal that a
	 * refactoring has been performed which did not deliver a refactoring
	 * descriptor via its {@link Change#getRefactoringDescriptor()} method. The
	 * refactoring history service never returns unknown refactorings. For
	 * consistency reasons, they are reported for
	 * {@link IRefactoringExecutionListener} or
	 * {@link IRefactoringHistoryListener} in order to keep the refactoring
	 * operation history intact.
	 * </p>
	 */
	public static final String ID_UNKNOWN= "org.eclipse.ltk.core.refactoring.unknown"; //$NON-NLS-1$

	/**
	 * Constant describing the user flag (value: 256)
	 * <p>
	 * This constant is not intended to be used in refactoring descriptors.
	 * Clients should use the value of this constant to define user-defined
	 * flags with values greater than this constant.
	 * </p>
	 */
	public static final int USER_CHANGE= 1 << 8;

	/**
	 * The comment associated with this refactoring, or <code>null</code> for
	 * no comment
	 */
	private String fComment;

	/** A human-readable description of the particular refactoring instance */
	private final String fDescription;

	/** The flags of the refactoring descriptor */
	private final int fFlags;

	/** The globally unique id of the refactoring */
	private final String fID;

	/**
	 * The name of the project this refactoring is associated with, or
	 * <code>null</code>
	 */
	private final String fProject;

	/**
	 * The time stamp, or <code>-1</code> if no time is associated with the
	 * refactoring
	 */
	private long fTimeStamp= -1;

	/**
	 * Creates a new refactoring descriptor.
	 * 
	 * @param id
	 *            the unique id of the refactoring
	 * @param project
	 *            the non-empty name of the project associated with this
	 *            refactoring, or <code>null</code> for a workspace
	 *            refactoring
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code> for no commment
	 * @param flags
	 *            the flags of the refactoring descriptor
	 */
	protected RefactoringDescriptor(final String id, final String project, final String description, final String comment, final int flags) {
		Assert.isTrue(id != null && !"".equals(id)); //$NON-NLS-1$
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		Assert.isTrue(project == null || !"".equals(project)); //$NON-NLS-1$
		Assert.isTrue(flags >= NONE);
		fID= id;
		fProject= project;
		fDescription= description;
		fComment= comment;
		fFlags= flags;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int compareTo(final Object object) {
		if (object instanceof RefactoringDescriptor) {
			final RefactoringDescriptor descriptor= (RefactoringDescriptor) object;
			return (int) (fTimeStamp - descriptor.fTimeStamp);
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean equals(final Object object) {
		if (object instanceof RefactoringDescriptor) {
			final RefactoringDescriptor descriptor= (RefactoringDescriptor) object;
			return fTimeStamp == descriptor.fTimeStamp && fDescription.equals(descriptor.fDescription);
		}
		return false;
	}

	/**
	 * Returns the comment associated with this refactoring.
	 * 
	 * @return the associated comment, or the empty string
	 */
	public final String getComment() {
		return (fComment != null) ? fComment : ""; //$NON-NLS-1$
	}

	/**
	 * Returns a human-readable description of this refactoring.
	 * 
	 * @return a description of this refactoring
	 */
	public final String getDescription() {
		return fDescription;
	}

	/**
	 * Returns the flags of this refactoring.
	 * 
	 * @return the flags of this refactoring
	 */
	public final int getFlags() {
		return fFlags;
	}

	/**
	 * Returns the unique id of this refactoring.
	 * 
	 * @return the unique id
	 */
	public final String getID() {
		return fID;
	}

	/**
	 * Returns the name of the associated project.
	 * 
	 * @return the non-empty name of the project, or <code>null</code>
	 */
	public final String getProject() {
		return fProject;
	}

	/**
	 * Returns the time stamp of this refactoring.
	 * 
	 * @return the time stamp, or <code>-1</code> if no time information is
	 *         available
	 */
	public final long getTimeStamp() {
		return fTimeStamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int hashCode() {
		int code= fDescription.hashCode();
		if (fTimeStamp >= 0)
			code+= (17 * fTimeStamp);
		return code;
	}

	/**
	 * Returns whether this descriptor describes an unknown refactoring.
	 * <p>
	 * The default implementation returns <code>false</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if the descriptor describes an unknown
	 *         refactoring, <code>false</code> otherwise
	 */
	public boolean isUnknown() {
		return false;
	}

	/**
	 * Sets the comment of this refactoring.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param comment
	 *            the non-empty comment to set, or <code>null</code>
	 */
	public final void setComment(final String comment) {
		fComment= comment;
	}

	/**
	 * Sets the time stamp of this refactoring. This method can be called only
	 * once.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param stamp
	 *            the time stamp to set
	 */
	public final void setTimeStamp(final long stamp) {
		Assert.isTrue(stamp >= 0);
		fTimeStamp= stamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {

		final StringBuffer buffer= new StringBuffer(128);

		buffer.append(getClass().getName());
		if (isUnknown())
			buffer.append("[unknown refactoring]"); //$NON-NLS-1$
		else {
			buffer.append("[timeStamp="); //$NON-NLS-1$
			buffer.append(fTimeStamp);
			buffer.append(",id="); //$NON-NLS-1$
			buffer.append(fID);
			buffer.append(",description="); //$NON-NLS-1$
			buffer.append(fDescription);
			buffer.append(",project="); //$NON-NLS-1$
			buffer.append(fProject);
			buffer.append(",comment="); //$NON-NLS-1$
			buffer.append(fComment);
			buffer.append(",flags="); //$NON-NLS-1$
			buffer.append(fFlags);
			buffer.append("]"); //$NON-NLS-1$
		}

		return buffer.toString();
	}
}