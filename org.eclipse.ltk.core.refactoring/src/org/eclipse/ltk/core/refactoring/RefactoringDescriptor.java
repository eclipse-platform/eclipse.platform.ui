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

import org.eclipse.core.runtime.Assert;

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
 * constructor. In particular, if a refactoring descriptor represents a
 * refactoring which renames a project resource, the descriptor should have the
 * flag {@link #PROJECT_CHANGE} set and a valid argument {@link #NAME}. The
 * arguments {@link #INPUT} or {@link #ELEMENT} are reserved to specify the
 * input arguments of a particular refactoring. The format of the values of
 * these arguments is langugage-dependent, but should be standardized for a
 * particular programming language. These arguments may be preprocessed by
 * language-specific tools such as wizards to import refactoring information
 * into the local workspace.
 * </p>
 * <p>
 * All time stamps are measured in UTC milliseconds from the epoch (see
 * {@link java.util#Calendar}).
 * </p>
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
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
public class RefactoringDescriptor implements Comparable {

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
	 * Predefined argument called <code>element&lt;Number&gt;</code>.
	 * <p>
	 * This argument should be used to describe the elements being refactored.
	 * The value of this argument does not necessarily have to uniquely identify
	 * the elements. However, it must be possible to uniquely identify the
	 * elements using the value of this argument in conjunction with the values
	 * of the other user-defined attributes.
	 * </p>
	 * <p>
	 * The element arguments are simply distinguished by appending a number to
	 * the argument name, eg. element1. The indices of this argument are non
	 * zero-based.
	 * </p>
	 */
	public static final String ELEMENT= "element"; //$NON-NLS-1$

	/**
	 * Predefined argument called <code>input</code>.
	 * <p>
	 * This argument should be used to describe the element being refactored.
	 * The value of this argument does not necessarily have to uniquely identify
	 * the input element. However, it must be possible to uniquely identify the
	 * input element using the value of this argument in conjunction with the
	 * values of the other user-defined attributes.
	 * </p>
	 */
	public static final String INPUT= "input"; //$NON-NLS-1$

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

	/**
	 * Predefined argument called <code>name</code>.
	 * <p>
	 * This argument should be used to describe the name of the element being
	 * refactored. The value of this argument may be displayed in the user
	 * interface.
	 * </p>
	 */
	public static final String NAME= "name"; //$NON-NLS-1$

	/** Constant describing the absence of any flags (value: 0) */
	public static final int NONE= 0;

	/**
	 * Constant describing the project rename change flag (value: 8)
	 * <p>
	 * Clients should set this flag to indicate that the represented refactoring
	 * renames a project resource in the workspace. If this flag is set for a
	 * particular descriptor, the refactoring history service assumes that the
	 * argument {@link #NAME} of the refactoring descriptor denotes the new name
	 * of the project being renamed.
	 * </p>
	 */
	public static final int PROJECT_CHANGE= 1 << 3;

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
	 * Constant describing the user flag (value: 256)
	 * <p>
	 * This constant is not intended to be used in refactoring descriptors.
	 * Clients should use the value of this constant to define user-defined
	 * flags with values greater than this constant.
	 * </p>
	 */
	public static final int USER_CHANGE= 1 << 8;

	/** The map of arguments (element type: &lt;String, String&gt;) */
	private final Map fArguments;

	/** The comment associated with this refactoring, or <code>null</code> */
	private final String fComment;

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
	 * @param flags
	 *            the flags of the refactoring descriptor
	 */
	public RefactoringDescriptor(final String id, final String project, final String description, final String comment, final Map arguments, final int flags) {
		Assert.isTrue(id != null && !"".equals(id)); //$NON-NLS-1$
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		Assert.isTrue(project == null || !"".equals(project)); //$NON-NLS-1$
		Assert.isNotNull(arguments);
		Assert.isTrue(flags >= NONE);
		fID= id;
		fProject= project;
		fDescription= description;
		fComment= comment;
		fArguments= Collections.unmodifiableMap(new HashMap(arguments));
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
	 * Returns the flags of the refactoring descriptor.
	 * 
	 * @return the flags of the refactoring descriptor
	 */
	public final int getFlags() {
		return fFlags;
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
		// Assert.isTrue(fTimeStamp == -1);
		Assert.isTrue(stamp >= 0);
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
			buffer.append(",flags="); //$NON-NLS-1$
			buffer.append(fFlags);
			buffer.append("]"); //$NON-NLS-1$
		}

		return buffer.toString();
	}
}