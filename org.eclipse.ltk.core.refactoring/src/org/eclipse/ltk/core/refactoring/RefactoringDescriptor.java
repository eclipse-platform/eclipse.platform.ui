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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Descriptor object of a refactoring.
 * <p>
 * A refactoring descriptor contains refactoring-specific data which allows to
 * framework to completely reconstruct a particular refactoring instance and
 * execute it on the workspace. Refactoring descriptors are identified by their
 * refactoring id {@link #getID()} and their time stamps {@link #getTimeStamp()}.
 * </p>
 * <p>
 * Refactoring descriptors are potentially heavyweight objects which should not
 * be held on to. Use refactoring descriptor handles
 * {@link RefactoringDescriptorProxy} to store refactoring information.
 * </p>
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class RefactoringDescriptor implements Comparable {

	/** The map of arguments (element type: &lt;String, String&gt;) */
	private final Map fArguments;

	/** The comment associated with this refactoring, or <code>null</code> */
	private final String fComment;

	/** A human-readable description of the particular refactoring instance */
	private final String fDescription;

	/** The globally unique id of the refactoring */
	private final String fID;

	/**
	 * The name of the project this refactoring is associated with, or
	 * <code>null</code>
	 */
	private final String fProject;

	/** The time stamp, or <code>-1</code> */
	private long fTimeStamp= -1;

	/**
	 * Creates a new refactoring descriptor.
	 * 
	 * @param id
	 *            the unique id of the refactoring
	 * @param project
	 *            the non-empty name of the project associated with this
	 *            refactoring, or <code>null</code>
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code> for no commment
	 * @param arguments
	 *            the argument map (element type: &lt;String, String&gt;). The
	 *            keys of the arguments are required to be non-empty strings
	 *            which must not contain spaces. The values must be non-empty
	 *            strings
	 */
	public RefactoringDescriptor(final String id, final String project, final String description, final String comment, final Map arguments) {
		Assert.isTrue(id != null && !"".equals(id)); //$NON-NLS-1$
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		Assert.isTrue(project == null || !"".equals(project)); //$NON-NLS-1$
		Assert.isNotNull(arguments);
		fID= id;
		fProject= project;
		fDescription= description;
		fComment= comment;
		fArguments= Collections.unmodifiableMap(new HashMap(arguments));
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
			return fTimeStamp == descriptor.fTimeStamp;
		}
		return false;
	}

	/**
	 * Returns the arguments describing the refactoring, in no particular order.
	 * 
	 * @return the argument map (element type: &lt;String, String&gt;). The
	 *         resulting map cannot be modified.
	 */
	public final Map getArguments() {
		return fArguments;
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
	 * Returns a human-readable description of the particular refactoring
	 * instance.
	 * 
	 * @return a description of the refactoring
	 */
	public final String getDescription() {
		return fDescription;
	}

	/**
	 * Returns the unique id of the refactoring.
	 * 
	 * @return the unique id
	 */
	public final String getID() {
		return fID;
	}

	/**
	 * Returns the name of the project this refactoring is associated with.
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
		return (int) fTimeStamp;
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
	 * Sets the time stamp of this refactoring.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param stamp
	 *            the time stamp to set
	 */
	public final void setTimeStamp(final long stamp) {
		fTimeStamp= stamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String toString() {

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
			buffer.append(",arguments="); //$NON-NLS-1$
			buffer.append(fArguments);
			buffer.append(",comment="); //$NON-NLS-1$
			buffer.append(fComment);
			buffer.append("]"); //$NON-NLS-1$
		}

		return buffer.toString();
	}
}