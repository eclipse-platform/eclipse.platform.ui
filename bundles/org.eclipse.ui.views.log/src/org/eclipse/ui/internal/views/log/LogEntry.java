/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 209474, 207344
 *     Eike Stepper <stepper@esc-net.de>              - bug 429372
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 485843
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.eclipse.core.runtime.IStatus;

/**
 * Represents a given entry in the Error view
 */
public class LogEntry extends AbstractEntry {

	public static final String SPACE = " "; //$NON-NLS-1$
	public static final String F_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"; //$NON-NLS-1$
	static final DateTimeFormatter GREGORIAN_SDF = DateTimeFormatter.ofPattern(F_DATE_FORMAT, Locale.ENGLISH)
			.withZone(ZoneId.systemDefault());
	static final DateTimeFormatter LOCAL_SDF = DateTimeFormatter.ofPattern(F_DATE_FORMAT)
			.withZone(ZoneId.systemDefault());

	private String pluginId;
	private int severity;
	private int code;
	private String fDateString;
	private Date fDate;
	private String message;
	private String stack;
	private LogSession session;

	/**
	 * Constructor
	 */
	public LogEntry() {
		//do nothing
	}

	/**
	 * Constructor - creates a new entry from the given status
	 * @param status an existing status to create a new entry from
	 */
	public LogEntry(IStatus status) {
		this(status, null);
	}

	/**
	 * Constructor - creates a new entry from the given status
	 * @param status an existing status to create a new entry from
	 */
	public LogEntry(IStatus status, LogSession session) {
		processStatus(status, session);
	}

	/**
	 * Returns the {@link LogSession} for this entry or the parent {@link LogSession}
	 * iff:
	 * <ul>
	 * <li>The session is <code>null</code> for this entry</li>
	 * <li>The parent of this entry is not <code>null</code> and is a {@link LogEntry}</li>
	 * </ul>
	 * @return the {@link LogSession} for this entry
	 */
	public LogSession getSession() {
		if ((session == null) && (parent != null) && (parent instanceof LogEntry)) {
			return ((LogEntry) parent).getSession();
		}
		return session;
	}

	/**
	 * Sets the {@link LogSession} for this entry. No validation is done on the new session.
	 * @param session the session to set.
	 */
	void setSession(LogSession session) {
		this.session = session;
	}

	/**
	 * Returns the severity of this entry.
	 * @return the severity
	 * @see IStatus#OK
	 * @see IStatus#WARNING
	 * @see IStatus#INFO
	 * @see IStatus#ERROR
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * Returns if the severity of this entry is {@link IStatus#OK}
	 * @return if the entry is OK or not
	 */
	public boolean isOK() {
		return severity == IStatus.OK;
	}

	/**
	 * Returns the code for this entry
	 * @return the code for this entry
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the id of the plugin that generated this entry
	 * @return the plugin id of this entry
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * Returns the message for this entry or <code>null</code> if there is no message
	 * @return the message or <code>null</code>
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the stack trace for this entry or <code>null</code> if there is no stack trace
	 * @return the stack trace or <code>null</code>
	 */
	public String getStack() {
		return stack;
	}

	/**
	 * Returns a pretty-print formatting for the date for this entry
	 * @return the formatted date for this entry
	 */
	public String getFormattedDate() {
		if (fDateString == null) {
			fDateString = LOCAL_SDF.format(getDate().toInstant());
		}
		return fDateString;
	}

	/**
	 * Returns the date for this entry or the epoch if the current date value is <code>null</code>
	 * @return the entry date or the epoch if there is no date entry
	 */
	public Date getDate() {
		if (fDate == null) {
			fDate = new Date(0); // unknown date - return epoch
		}
		return fDate;
	}

	/**
	 * Returns the human-readable text representation of the integer
	 * severity value or '<code>?</code>' if the severity is unknown.
	 * @return the text representation of the severity
	 */
	public String getSeverityText() {
		switch (severity) {
			case IStatus.ERROR : {
				return Messages.LogView_severity_error;
			}
			case IStatus.WARNING : {
				return Messages.LogView_severity_warning;
			}
			case IStatus.INFO : {
				return Messages.LogView_severity_info;
			}
			case IStatus.OK : {
				return Messages.LogView_severity_ok;
			}
		}
		return "?"; //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return getSeverityText();
	}

	@Override
	public String getLabel(Object obj) {
		return getSeverityText();
	}

	/**
	 * Processes a given line from the log file
	 */
	public void processEntry(String line) throws IllegalArgumentException {
		//!ENTRY <pluginID> <severity> <code> <date>
		//!ENTRY <pluginID> <date> if logged by the framework!!!
		StringTokenizer stok = new StringTokenizer(line, SPACE);
		severity = 0;
		code = 0;
		StringBuilder dateBuffer = new StringBuilder();
		int tokens = stok.countTokens();
		String token = null;
		for (int i = 0; i < tokens; i++) {
			token = stok.nextToken();
			switch (i) {
				case 0 : {
					break;
				}
				case 1 : {
					pluginId = token;
					break;
				}
				case 2 : {
					try {
						severity = Integer.parseInt(token);
					} catch (NumberFormatException nfe) {
						appendToken(dateBuffer, token);
					}
					break;
				}
				case 3 : {
					try {
						code = Integer.parseInt(token);
					} catch (NumberFormatException nfe) {
						appendToken(dateBuffer, token);
					}
					break;
				}
				default : {
					appendToken(dateBuffer, token);
				}
			}
		}
		String stringToParse = dateBuffer.toString();
		try {
			Date date = Date.from(Instant.from(GREGORIAN_SDF.parse(stringToParse)));
			if (date != null) {
				fDate = date;
				fDateString = LOCAL_SDF.format(fDate.toInstant());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse '" + dateBuffer + "'", e); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/**
	 * Adds the given token to the given buffer, adding a space as needed
	 * @param token
	 *
	 * @since 3.6
	 */
	void appendToken(StringBuilder buffer, String token) {
		if (buffer.length() > 0) {
			buffer.append(SPACE);
		}
		buffer.append(token);
	}

	/**
	 * Processes the given sub-entry from the log
	 * @return the depth of the sub-entry
	 */
	public int processSubEntry(String line) throws IllegalArgumentException {
		//!SUBENTRY <depth> <pluginID> <severity> <code> <date>
		//!SUBENTRY  <depth> <pluginID> <date>if logged by the framework!!!
		StringTokenizer stok = new StringTokenizer(line, SPACE);
		StringBuilder dateBuffer = new StringBuilder();
		int depth = 0;
		String token = null;
		int tokens = stok.countTokens();
		for (int i = 0; i < tokens; i++) {
			token = stok.nextToken();
			switch (i) {
				case 0 : {
					break;
				}
				case 1 : {
					try {
						depth = Integer.parseInt(token);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Failed to parse '" + token + "'", e); //$NON-NLS-1$//$NON-NLS-2$
					}
					break;
				}
				case 2 : {
					pluginId = token;
					break;
				}
				case 3 : {
					try {
						severity = Integer.parseInt(token);
					} catch (NumberFormatException nfe) {
						appendToken(dateBuffer, token);
					}
					break;
				}
				case 4 : {
					try {
						code = Integer.parseInt(token);
					} catch (NumberFormatException nfe) {
						appendToken(dateBuffer, token);
					}
					break;
				}
				default : {
					appendToken(dateBuffer, token);
				}
			}
		}
		try {
			Date date = Date.from(Instant.from(GREGORIAN_SDF.parse(dateBuffer.toString())));
			if (date != null) {
				fDate = date;
				fDateString = LOCAL_SDF.format(fDate.toInstant());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse '" + dateBuffer + "'", e); //$NON-NLS-1$//$NON-NLS-2$
		}
		return depth;
	}

	/**
	 * Sets the stack to the given stack value.
	 * No validation is performed on the new value.
	 */
	void setStack(String stack) {
		this.stack = stack;
	}

	/**
	 * Sets the message to the given message value.
	 * No validation is performed on the new value
	 */
	void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Process the given status and sub-statuses to fill this entry
	 */
	private void processStatus(IStatus status, LogSession session) {
		pluginId = status.getPlugin();
		severity = status.getSeverity();
		code = status.getCode();
		fDate = new Date();
		fDateString = LOCAL_SDF.format(fDate.toInstant());
		message = status.getMessage();
		this.session = session;
		Throwable throwable = status.getException();
		if (throwable != null) {
			StringWriter swriter = new StringWriter();
			try (PrintWriter pwriter = new PrintWriter(swriter)) {
				throwable.printStackTrace(pwriter);
				pwriter.flush();
			}
			stack = swriter.toString();
		}
		IStatus[] schildren = status.getChildren();
		if (schildren.length > 0) {
			for (IStatus element : schildren) {
				addChild(new LogEntry(element, session));
			}
		}
	}

	@Override
	public void write(PrintWriter writer) {
		if (session != null) {
			writer.println(session.getSessionData());
		}
		writer.println(pluginId);
		writer.println(getSeverityText());
		if (fDate != null) {
			writer.println(getDate());
		}
		if (message != null) {
			writer.println(getMessage());
		}
		if (stack != null) {
			writer.println();
			writer.println(stack);
		}
	}
}
