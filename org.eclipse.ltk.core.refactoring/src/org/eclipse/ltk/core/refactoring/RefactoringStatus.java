/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * A <code>RefactoringStatus</code> object represents the outcome of a
 * precondition checking operation. It manages a list of <code>
 * RefactoringStatusEntry</code> objects. Each <code>RefactoringStatusEntry
 * </code> object describes one particilar problem detected during
 * precondition checking.
 * <p>
 * Additionally a problem severityis managed. Severities are ordered as follows: 
 * <code>OK</code> &lt; <code>INFO</code> &lt; <code>WARNING</code> &lt; <code>
 * ERROR</code>. The status's problem severity is the maximum of the severities 
 * of all entries. If the status doesn't have any entry the status's severity 
 * is <code>OK</code>.
 * </p>
 * <p> 
 * Note: this class is not intented to be subclassed by clients.
 * </p>
 * 
 * @see RefactoringStatusEntry
 * @see Refactoring#checkAllConditions(IProgressMonitor)
 * 
 * @since 3.0
 */
public class RefactoringStatus {

	/** 
	 * Status severity constant (value 0) indicating this status represents the nominal case.
	 * @see #getSeverity
	 * @see #isOK
	 */
	public static final int OK= 0;

	/** 
	 * Status severity constant (value 1) indicating this status is informational only.
	 * @see #getSeverity
	 */
	public static final int INFO= 1;

	/** 
	 * Status severity constant (value 2) indicating this status represents a warning.
	 * This is used when the refactoring might cause confusion such as unintended overloading.
	 * @see #getSeverity
	 */
	public static final int WARNING= 2;

	/** 
	 * Status severity constant (value 3) indicating this status represents an error.
	 * This is used when the refactoring will introduce compilation errors if executed.
	 * @see #getSeverity
	 */
	public static final int ERROR= 3;

	/** 
	 * Status severity constant (value 4) indicating this status represents a fatal error.
	 * This is used when the refactoring can't be executed.
	 * @see #getSeverity
	 */
	public static final int FATAL= 4;

	/**
	 * List of refactoring status entries.
	 */
	private List fEntries;

	/**
	 * The status's severity. The following invariant holds for
	 * <code>fSeverity</code>: <code>OK</code> &le; fSeverity &le; 
	 * <code>FATAL</code>.
	 */
	private int fSeverity= OK;

	/**
	 * Creates a new refactoring status with an empty list of
	 * status entries and a severity of <code>OK</code>.
	 */
	public RefactoringStatus() {
		fEntries= new ArrayList(0);
	}
	
	/**
	 * Returns the severity.
	 * 
	 * @return the severity.
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/**
	 * Returns the list of refactoring status entries.
	 * 
	 * @return the list of refactoring status entries. Returns an empty array
	 *  if not entries are managed.
	 */
	public RefactoringStatusEntry[] getEntries() {
		return (RefactoringStatusEntry[])fEntries.toArray(new RefactoringStatusEntry[fEntries.size()]);
	}
	
	/**
	 * Returns whether the status has entries or not.
	 * 
	 * @return <code>true</code> if the status as any entries; otherwise
	 *  <code>false</code> is returned.
	 */
	public boolean hasEntries() {
		return !fEntries.isEmpty();
	}

	/**
	 * Returns the <code>RefactoringStatusEntry</code> at the specified index.
	 * 
	 * @param index the indes of the entry to return
	 * @return the enrty at the specified index
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public RefactoringStatusEntry getEntryAt(int index) {
		return (RefactoringStatusEntry)fEntries.get(index);
	}

	/**
	 * Returns the first entry managed by this refactoring status that
	 * matches the given plug-in identifier and code. If more than one
	 * entry exists that matches the criteria the first one in the list
	 * of entries is returned. Returns <code>null</code> if no entry
	 * matches.
	 * 
	 * @param pluginId the entry's plug-in identifier
	 * @param code the entry's code
	 * @return the entry that matches the given plug-in identifier and
	 *  code; <code>null</code> otherwise 
	 */
	public RefactoringStatusEntry getEntryMatchingCode(String pluginId, int code) {
		Assert.isTrue(pluginId != null);
		for (Iterator iter= fEntries.iterator(); iter.hasNext(); ) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)iter.next();
			if (pluginId.equals(entry.getPluginId()) && entry.getCode() == code)
				return entry;
		}
		return null;
	}

	/**
	 * Returns the first entry which severity is equal or greater than the
	 * given severity. If more than one entry exists that matches the 
	 * criteria the first one is returned. Returns <code>null</code> if no 
	 * entry matches.
	 * 
	 * @param severity the severity to search for. Must be one of <code>FATAL
	 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
	 * @return the entry that matches the search criteria
	 */
	public RefactoringStatusEntry getEntryMatchingSeverity(int severity) {
		Assert.isTrue(severity >= OK && severity <= FATAL);
		if (severity > fSeverity)
			return null;
		Iterator iter= fEntries.iterator();
		while (iter.hasNext()) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)iter.next();
			if (entry.getSeverity() >= severity)
				return entry;
		}
		return null;
	}

	/**
	 * Returns the first message which severity is equal or greater than the
	 * given severity. If more than one entry exists that matches the criteria
	 * the first one is returned. Returns <code>null</code> if no entry matches.
	 * 
	 * @param severity the severity to search for. Must be one of <code>FATAL
	 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
	 * @return the message of the entry that matches the search criteria
	 */
	public String getMessageMatchingSeverity(int severity) {
		RefactoringStatusEntry entry= getEntryMatchingSeverity(severity);
		if (entry == null)
			return null;
		return entry.getMessage();
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one entry filled with the given
	 * arguments.
	 * 
	 * @param severity the severity
	 * @param msg the message
	 * @param context the context. Can be <code>null</code>
	 * @param pluginId the plug-in identifier. Can be <code>null</code> if argument <code>
	 *  code</code> equals <code>NO_CODE</code>
	 * @param code the problem code. Must be either <code>NO_CODE</code> or equals or greater
	 *  than zero
	 * @param data application specific data
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createStatus(int severity, String msg, RefactoringStatusContext context, String pluginId, int code, Object data) {
		RefactoringStatus result= new RefactoringStatus();
		result.fEntries.add(new RefactoringStatusEntry(severity, msg, context, pluginId, code, data));
		result.fSeverity= severity;
		return result;
	}

	/**
	 * Creates a new <code>RefactorngStatus</code> with one <code>INFO</code> entry
	 * filled with the given message. 
	 * 
	 * @param msg the message of the info entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createInfoStatus(String msg) {
		return createStatus(INFO, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactorngStatus</code> with one <code>INFO</code> entry
	 * filled with the given message and context.
	 * 
	 * @param msg the message of the info entry
	 * @param context the context of the info entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createInfoStatus(String msg, RefactoringStatusContext context) {
		return createStatus(INFO, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactorngStatus</code> with one <code>WARNING</code> entry
	 * filled with the given message.
	 * 
	 * @param msg the message of the warning entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createWarningStatus(String msg) {
		return createStatus(WARNING, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a <code>RefactorngStatus</code> with one <code>WARNING</code> entry
	 * fill with the given message and context.
	 * 
	 * @param msg the message of the warning entry
	 * @param context the context of the warning entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createWarningStatus(String msg, RefactoringStatusContext context) {
		return createStatus(WARNING, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactorngStatus</code> with one <code>ERROR</code> entry
	 * filled with the given message.
	 * 
	 * @param msg the message of the error entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createErrorStatus(String msg) {
		return createStatus(ERROR, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a <code>RefactorngStatus</code> with one <code>ERROR</code> entry
	 * fill with the given message and context.
	 * 
	 * @param msg the message of the error entry
	 * @param context the context of the error entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createErrorStatus(String msg, RefactoringStatusContext context) {
		return createStatus(ERROR, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactorngStatus</code> with one <code>FATAL</code> entry
	 * filled with the given message.
	 * 
	 * @param msg the message of the fatal entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg) {
		return createStatus(FATAL, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a <code>RefactorngStatus</code> with one <code>FATAL</code> entry
	 * fill with the given message and context.
	 * 
	 * @param msg the message of the fatal entry
	 * @param context the context of the fatal entry
	 * @return the refactoring status
	 * 
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg, RefactoringStatusContext context) {
		return createStatus(FATAL, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactorngStatus</code> from the given <code>IStatus</code>. An
	 * OK status is mapped to an OK refactoring status, an information status is mapped 
	 * to a warning refactoring status, a warning status is mapped to an error refactoring 
	 * status and an error status is mapped to a fatal refactoring status. If the status
	 * is a <code>MultiStatus</code> the first level of children of the status will be added
	 * as refactoring status entries to the created refactoring status.
	 * 
	 * @param status the status to create a refactoring status from
	 * @return the refactoring status
	 * 
	 * @see IStatus
	 */
	public static RefactoringStatus create(IStatus status) {
		if (status.isOK())
			return new RefactoringStatus();

		if (!status.isMultiStatus()) {
			switch (status.getSeverity()) {
				case IStatus.INFO :
					return RefactoringStatus.createWarningStatus(status.getMessage());
				case IStatus.WARNING :
					return RefactoringStatus.createErrorStatus(status.getMessage());
				case IStatus.ERROR :
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
				default :
					return new RefactoringStatus();
			}
		} else {
			IStatus[] children= status.getChildren();
			RefactoringStatus result= new RefactoringStatus();
			for (int i= 0; i < children.length; i++) {
				result.merge(RefactoringStatus.create(children[i]));
			}
			return result;
		}
	}

	/**
	 * Merges the receiver and the parameter statuses. The resulting list of
	 * entries in the receiver will contain entries from both. The resuling
	 * severity in the reciver will be the more severe of its current severity
	 * and the parameter's severity. Merging with <code>null</code> is
	 * allowed - it has no effect.
	 * 
	 * @param other the refactoring status to merge with
	 * 
	 * @see #getSeverity
	 */
	public void merge(RefactoringStatus other) {
		if (other == null)
			return;
		fEntries.addAll(other.fEntries);
		fSeverity= Math.max(fSeverity, other.getSeverity());
	}

	/**
	 * Adds an <code>INFO</code> entry filled with the given message to this status. 
	 * If the current severity is <code>OK</code> it will be changed to <code>INFO
	 * </code>. It will remain unchanged otherwise.
	 * 
	 * @param msg the message of the info entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addInfo(String msg) {
		addInfo(msg, null);
	}

	/**
	 * Adds an <code>INFO</code> entry filled with the given message and context to 
	 * this status. If the current severity is <code>OK</code> it will be changed to 
	 * <code>INFO</code>. It will remain unchanged otherwise.
	 * 
	 * @param msg the message of the info entry
	 * @param context the context of the info entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addInfo(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.INFO, msg, context));
		fSeverity= Math.max(fSeverity, INFO);
	}

	/**
	 * Adds a <code>WARNING</code> entry filled with the given message to this status.
	 * If the current severity is <code>OK</code> or <code>INFO</code> it will be 
	 * changed to <code>WARNING</code>. It will remain unchanged otherwise.
	 * 
	 * @param msg the message of the warning entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addWarning(String msg) {
		addWarning(msg, null);
	}

	/**
	 * Adds a <code>WARNING</code> entry filled with the given message and context to 
	 * this status. If the current severity is <code>OK</code> or <code>INFO</code> it 
	 * will be changed to <code>WARNING</code>. It will remain unchanged otherwise.
	 * 
	 * @param msg the message of the warning entry
	 * @param context the context of the warning entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addWarning(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, msg, context));
		fSeverity= Math.max(fSeverity, WARNING);
	}

	/**
	 * Adds an <code>ERROR</code> entry filled with the given message to this status. 
	 * If the current severity is <code>OK</code>, <code>INFO</code> or <code>WARNING
	 * </code> it will be changed to <code>ERROR</code>. It will remain unchanged 
	 * otherwise.
	 * 
	 * @param msg the message of the error entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addError(String msg) {
		addError(msg, null);
	}

	/**
	 * Adds an <code>ERROR</code> entry filled with the given message and context to 
	 * this status. If the current severity is <code>OK</code>, <code>INFO</code> or 
	 * <code>WARNING</code> it will be changed to <code>ERROR</code>. It will remain 
	 * unchanged otherwise.
	 * 
	 * @param msg the message of the error entry
	 * @param context the context of the error entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addError(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.ERROR, msg, context));
		fSeverity= Math.max(fSeverity, ERROR);
	}

	/**
	 * Adds a <code>FATAL</code> entry filled with the given message to this status.
	 * The severity of this status will changed to <code>FATAL</code>. 
	 * 
	 * @param msg the message of the fatal entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addFatalError(String msg) {
		addFatalError(msg, null);
	}

	/**
	 * Adds a <code>FATAL</code> entry filled with the given message and status to
	 * this status. The severity of this status will changed to <code>FATAL</code>. 
	 * 
	 * @param msg the message of the fatal entry
	 * @param context the context of the fatal entry
	 * 
	 * @see RefactoringStatusEntry
	 */
	public void addFatalError(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.FATAL, msg, context));
		fSeverity= Math.max(fSeverity, FATAL);
	}

	/**
	 * Adds a new entry filled with the given arguments to this status. The severity
	 * of this status is set to the maximum of <code>fSeverity</code> and 
	 * <code>severity</code>. 
	 * 
	 * @param severity the severity of the entry
	 * @param msg the message of the entry
	 * @param context the context of the entry. Can be <code>null</code>
	 * @param pluginId the plug-in identifier of the entry. Can be <code>null</code> if 
	 *  argument <code>code</code> equals <code>NO_CODE</code>
	 * @param code the problem code of the entry. Must be either <code>NO_CODE</code> 
	 *  or equals or greater than zero
	 */
	public void addEntry(int severity, String msg, RefactoringStatusContext context, String pluginId, int code) {
		fEntries.add(new RefactoringStatusEntry(severity, msg, context, pluginId, code));
		fSeverity= Math.max(fSeverity, severity);
	}

	/**
	 * Adds a new entry filled with the given arguments to this status. The severity
	 * of this status is set to the maximum of <code>fSeverity</code> and 
	 * <code>severity</code>. 
	 * 
	 * @param severity the severity of the entry
	 * @param msg the message of the entry
	 * @param context the context of the entry. Can be <code>null</code>
	 * @param pluginId the plug-in identifier of the entry. Can be <code>null</code> if 
	 *  argument <code>code</code> equals <code>NO_CODE</code>
	 * @param code the problem code of the entry. Must be either <code>NO_CODE</code> 
	 *  or equals or greater than zero
	 * @param data application specific data of the entry
	 */
	public void addEntry(int severity, String msg, RefactoringStatusContext context, String pluginId, int code, Object data) {
		fEntries.add(new RefactoringStatusEntry(severity, msg, context, pluginId, code, data));
		fSeverity= Math.max(fSeverity, severity);
	}

	/**
	 * Adds the given <code>RefactoringStatusEntry</code>. The severity of this
	 * status is set to the maximum of <code>fSeverity</code> and the severity of
	 * the entry.
	 * 
	 * @param entry the <code>RefactoringStatusEntry</code> to be added
	 */
	public void addEntry(RefactoringStatusEntry entry) {
		Assert.isNotNull(entry);
		fEntries.add(entry);
		fSeverity= Math.max(fSeverity, entry.getSeverity());
	}

	/**
	 * Returns whether the status's severity is <code>OK</code> or not. 
	 * 
	 * @return <code>true</code> if the severity is <code>OK</code>; 
	 *  otherwise <code>false</code> is returned
	 */
	public boolean isOK() {
		return fSeverity == OK;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code>.
	 * 
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code>; otherwise <code>false</code> is returned
	 */
	public boolean hasFatalError() {
		return fSeverity == FATAL;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code> or <code>ERROR</code>.
	 * 
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code> or <code>ERROR</code>; otherwise <code>false
	 *  </code> is returned
	 */
	public boolean hasError() {
		return fSeverity == FATAL || fSeverity == ERROR;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code>, <code>ERROR</code> or <code>WARNING</code>.
	 * 
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code>, <code>ERROR</code> or <code>WARNING</code>; 
	 *  otherwise <code>false</code> is returned
	 */
	public boolean hasWarning() {
		return fSeverity == FATAL || fSeverity == ERROR || fSeverity == WARNING;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code>, <code>ERROR</code>, <code>WARNING</code> or 
	 * <code>INFO</code>.
	 * 
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code>, <code>ERROR</code>, <code>WARNING</code> or 
	 *  <code>INFO</code>; otherwise <code>false</code> is returned
	 */
	public boolean hasInfo() {
		return fSeverity == FATAL || fSeverity == ERROR || fSeverity == WARNING || fSeverity == INFO;
	}

	/*
	 * (non java-doc) 
	 * for debugging only
	 */
	public String toString() {
		StringBuffer buff= new StringBuffer();
		buff.append("<") //$NON-NLS-1$
			.append(getSeverityString(fSeverity)).append("\n"); //$NON-NLS-1$
		if (!isOK()) {
			for (Iterator iter= fEntries.iterator(); iter.hasNext(); ) {
				buff.append("\t") //$NON-NLS-1$
					.append(iter.next()).append("\n"); //$NON-NLS-1$
			}
		}
		buff.append(">"); //$NON-NLS-1$
		return buff.toString();
	}
	
	/*
	 * non java-doc 
	 * for debugging only not for nls
	 */
	/* package */static String getSeverityString(int severity) {
		Assert.isTrue(severity >= OK && severity <= FATAL);
		if (severity == RefactoringStatus.OK)
			return "OK"; //$NON-NLS-1$
		if (severity == RefactoringStatus.INFO)
			return "INFO"; //$NON-NLS-1$
		if (severity == RefactoringStatus.WARNING)
			return "WARNING"; //$NON-NLS-1$
		if (severity == RefactoringStatus.ERROR)
			return "ERROR"; //$NON-NLS-1$
		if (severity == RefactoringStatus.FATAL)
			return "FATALERROR"; //$NON-NLS-1$
		return null;
	}
}
