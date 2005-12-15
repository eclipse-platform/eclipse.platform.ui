/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.internal.commands.util.Util;

/**
 * <p>
 * A command that has had one or more of its parameters specified. This class
 * serves as a utility class for developers that need to manipulate commands
 * with parameters. It handles the behaviour of generating a parameter map and a
 * human-readable name.
 * </p>
 * 
 * @since 3.1
 */
public final class ParameterizedCommand implements Comparable {

	/**
	 * The constant integer hash code value meaning the hash code has not yet
	 * been computed.
	 */
	private static final int HASH_CODE_NOT_COMPUTED = -1;

	/**
	 * A factor for computing the hash code for all parameterized commands.
	 */
	private static final int HASH_FACTOR = 89;

	/**
	 * The seed for the hash code for all parameterized commands.
	 */
	private static final int HASH_INITIAL = ParameterizedCommand.class
			.getName().hashCode();

	/**
	 * The index of the parameter id in the parameter values.
	 * 
	 * @deprecated no longer used
	 */
	public static final int INDEX_PARAMETER_ID = 0;

	/**
	 * The index of the human-readable name of the parameter itself, in the
	 * parameter values.
	 * 
	 * @deprecated no longer used
	 */
	public static final int INDEX_PARAMETER_NAME = 1;

	/**
	 * The index of the human-readable name of the value of the parameter for
	 * this command.
	 * 
	 * @deprecated no longer used
	 */
	public static final int INDEX_PARAMETER_VALUE_NAME = 2;

	/**
	 * The index of the value of the parameter that the command can understand.
	 * 
	 * @deprecated no longer used
	 */
	public static final int INDEX_PARAMETER_VALUE_VALUE = 3;

	/**
	 * Generates every possible combination of parameter values for the given
	 * parameters. Parameters values that cannot be initialized are just
	 * ignored. Optional parameters are considered.
	 * 
	 * @param startIndex
	 *            The index in the <code>parameters</code> that we should
	 *            process. This must be a valid index.
	 * @param parameters
	 *            The parameters in to process; must not be <code>null</code>.
	 * @return A collection (<code>Collection</code>) of combinations (<code>List</code>
	 *         of <code>Parameterization</code>).
	 * 
	 */
	private static final Collection expandParameters(final int startIndex,
			final IParameter[] parameters) {
		final boolean noMoreParameters = (startIndex + 1 >= parameters.length);

		final IParameter parameter = parameters[startIndex];
		IParameterValues values = null;
		try {
			values = parameter.getValues();
		} catch (final ParameterValuesException e) {
			if (noMoreParameters) {
				return Collections.EMPTY_LIST;
			}

			return expandParameters(startIndex, parameters);
		}
		final Map parameterValues = values.getParameterValues();
		final List parameterizations = new ArrayList(parameterValues.size());
		final Iterator parameterValueItr = parameterValues.entrySet()
				.iterator();
		while (parameterValueItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) parameterValueItr.next();
			final Parameterization parameterization = new Parameterization(
					parameter, (String) entry.getValue());
			parameterizations.add(parameterization);
		}
		if (parameter.isOptional()) {
			parameterizations.add(null);
		}

		// Check if another iteration will produce any more names.
		final int parameterizationCount = parameterizations.size();
		if (noMoreParameters) {
			// This is it, so just return the current parameterizations.
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = (Parameterization) parameterizations
						.get(i);
				final List combination = new ArrayList(1);
				combination.add(parameterization);
				parameterizations.set(i, combination);
			}
			return parameterizations;
		}

		// Make recursive call
		final Collection suffixes = expandParameters(startIndex + 1, parameters);
		if (suffixes.isEmpty()) {
			// This is it, so just return the current parameterizations.
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = (Parameterization) parameterizations
						.get(i);
				final List combination = new ArrayList(1);
				combination.add(parameterization);
				parameterizations.set(i, combination);
			}
			return parameterizations;
		}
		final Collection returnValue = new ArrayList();
		final Iterator suffixItr = suffixes.iterator();
		while (suffixItr.hasNext()) {
			final List combination = (List) suffixItr.next();
			final int combinationSize = combination.size();
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = (Parameterization) parameterizations
						.get(i);
				final List newCombination = new ArrayList(combinationSize + 1);
				newCombination.add(parameterization);
				newCombination.addAll(combination);
				returnValue.add(newCombination);
			}
		}

		return returnValue;
	}

	/**
	 * <p>
	 * Generates all the possible combinations of command parameterizations for
	 * the given command. If the command has no parameters, then this is simply
	 * a parameterized version of that command. If a parameter is optional, both
	 * the included and not included cases are considered.
	 * </p>
	 * <p>
	 * If one of the parameters cannot be loaded due to a
	 * <code>ParameterValuesException</code>, then it is simply ignored.
	 * </p>
	 * 
	 * @param command
	 *            The command for which the parameter combinations should be
	 *            generated; must not be <code>null</code>.
	 * @return A collection of <code>ParameterizedCommand</code> instances
	 *         representing all of the possible combinations. This value is
	 *         never empty and it is never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the command is not defined.
	 */
	public static final Collection generateCombinations(final Command command)
			throws NotDefinedException {
		final IParameter[] parameters = command.getParameters();
		if (parameters == null) {
			return Collections
					.singleton(new ParameterizedCommand(command, null));
		}

		final Collection expansion = expandParameters(0, parameters);
		final Collection combinations = new ArrayList(expansion.size());
		final Iterator expansionItr = expansion.iterator();
		while (expansionItr.hasNext()) {
			final List combination = (List) expansionItr.next();
			while (combination.remove(null)) {
				// Just keep removing while there are null entries left.
			}
			if (combination.isEmpty()) {
				combinations.add(new ParameterizedCommand(command, null));
			} else {
				final Parameterization[] parameterizations = (Parameterization[]) combination
						.toArray(new Parameterization[combination.size()]);
				combinations.add(new ParameterizedCommand(command,
						parameterizations));
			}
		}

		return combinations;
	}

	/**
	 * The base command which is being parameterized. This value is never
	 * <code>null</code>.
	 */
	private final Command command;

	/**
	 * The hash code for this object. This value is computed lazily, and marked
	 * as invalid when one of the values on which it is based changes.
	 */
	private transient int hashCode = HASH_CODE_NOT_COMPUTED;

	/**
	 * This is an array of parameterization defined for this command. This value
	 * may be <code>null</code> if the command has no parameters.
	 */
	private final Parameterization[] parameterizations;

	/**
	 * Constructs a new instance of <code>ParameterizedCommand</code> with
	 * specific values for zero or more of its parameters.
	 * 
	 * @param command
	 *            The command that is parameterized; must not be
	 *            <code>null</code>.
	 * @param parameterizations
	 *            An array of parameterizations binding parameters to values for
	 *            the command. This value may be <code>null</code>. This
	 *            argument is not copied; if you need to make changes to it
	 *            after constructing this parameterized command, then make a
	 *            copy yourself.
	 */
	public ParameterizedCommand(final Command command,
			final Parameterization[] parameterizations) {
		if (command == null) {
			throw new NullPointerException(
					"A parameterized command cannot have a null command"); //$NON-NLS-1$
		}

		this.command = command;
		this.parameterizations = (parameterizations == null || parameterizations.length == 0) ? null
				: parameterizations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public final int compareTo(final Object object) {
		final ParameterizedCommand command = (ParameterizedCommand) object;
		final boolean thisDefined = this.command.isDefined();
		final boolean otherDefined = command.command.isDefined();
		if (!thisDefined || !otherDefined) {
			return Util.compare(thisDefined, otherDefined);
		}

		try {
			final int compareTo = getName().compareTo(command.getName());
			if (compareTo == 0) {
				return getId().compareTo(command.getId());
			}
			return compareTo;
		} catch (final NotDefinedException e) {
			throw new Error(
					"Concurrent modification of a command's defined state"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ParameterizedCommand)) {
			return false;
		}

		final ParameterizedCommand command = (ParameterizedCommand) object;
		if (!Util.equals(this.command, command.command)) {
			return false;
		}

		return Util.equals(this.parameterizations, command.parameterizations);
	}

	/**
	 * Returns the base command. It is possible for more than one parameterized
	 * command to have the same identifier.
	 * 
	 * @return The command; never <code>null</code>, but may be undefined.
	 */
	public final Command getCommand() {
		return command;
	}

	/**
	 * Returns the command's base identifier. It is possible for more than one
	 * parameterized command to have the same identifier.
	 * 
	 * @return The command id; never <code>null</code>.
	 */
	public final String getId() {
		return command.getId();
	}

	/**
	 * Returns a human-readable representation of this command with all of its
	 * parameterizations.
	 * 
	 * @return The human-readable representation of this parameterized command;
	 *         never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the underlying command is not defined.
	 */
	public final String getName() throws NotDefinedException {
		final StringBuffer nameBuffer = new StringBuffer();
		nameBuffer.append(command.getName());
		if (parameterizations != null) {
			nameBuffer.append(" ("); //$NON-NLS-1$
			final int parameterizationCount = parameterizations.length;
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = parameterizations[i];
				nameBuffer.append(parameterization.getParameter().getName());
				nameBuffer.append(": "); //$NON-NLS-1$
				try {
					nameBuffer.append(parameterization.getValueName());
				} catch (final ParameterValuesException e) {
					/*
					 * Just let it go for now. If someone complains we can add
					 * more info later.
					 */
				}

				// If there is another item, append a separator.
				if (i + 1 < parameterizationCount) {
					nameBuffer.append(", "); //$NON-NLS-1$
				}
			}
			nameBuffer.append(')');
		}
		return nameBuffer.toString();
	}

	/**
	 * Returns the parameter map, as can be used to construct an
	 * <code>ExecutionEvent</code>.
	 * 
	 * @return The map of parameter ids (<code>String</code>) to parameter
	 *         values (<code>String</code>). This map is never
	 *         <code>null</code>, but may be empty.
	 */
	public final Map getParameterMap() {
		if ((parameterizations == null) || (parameterizations.length == 0)) {
			return Collections.EMPTY_MAP;
		}

		final Map parameterMap = new HashMap();
		for (int i = 0; i < parameterizations.length; i++) {
			final Parameterization parameterization = parameterizations[i];
			parameterMap.put(parameterization.getParameter().getId(),
					parameterization.getValue());
		}
		return parameterMap;
	}

	/**
	 * Executes this command with its parameters.
	 * 
	 * @param trigger
	 *            The object that triggered the execution; may be
	 *            <code>null</code>.
	 * @param applicationContext
	 *            The state of the application at the time the execution was
	 *            triggered; may be <code>null</code>.
	 * @return The result of the execution; may be <code>null</code>.
	 * @throws ExecutionException
	 *             If the handler has problems executing this command.
	 * @throws NotHandledException
	 *             If there is no handler.
	 */
	public final Object execute(final Object trigger,
			final Object applicationContext) throws ExecutionException,
			NotHandledException {
		return command.execute(new ExecutionEvent(getParameterMap(), trigger,
				applicationContext));
	}

	/**
	 * Executes this command with its parameters. This does extra checking to
	 * see if the command is enabled and defined. If it is not both enabled and
	 * defined, then the execution listeners will be notified and an exception
	 * thrown.
	 * 
	 * @param trigger
	 *            The object that triggered the execution; may be
	 *            <code>null</code>.
	 * @param applicationContext
	 *            The state of the application at the time the execution was
	 *            triggered; may be <code>null</code>.
	 * @return The result of the execution; may be <code>null</code>.
	 * @throws ExecutionException
	 *             If the handler has problems executing this command.
	 * @throws NotDefinedException
	 *             If the command you are trying to execute is not defined.
	 * @throws NotEnabledException
	 *             If the command you are trying to execute is not enabled.
	 * @throws NotHandledException
	 *             If there is no handler.
	 * @since 3.2
	 */
	public final Object executeWithChecks(final Object trigger,
			final Object applicationContext) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		return command.executeWithChecks(new ExecutionEvent(getParameterMap(),
				trigger, applicationContext));
	}

	/**
	 * Returns a <code>String</code> containing the command id and the
	 * parameter ids and parameter values for this
	 * <code>ParameterizedCommand</code>. The returned <code>String</code>
	 * can be stored by a client and later used to recontruct an equivalent
	 * <code>ParameterizedCommand</code> using the
	 * {@link CommandManager#deserialize(String)} method.
	 * <p>
	 * The syntax of the returned <code>String</code> is as follows:
	 * </p>
	 * <blockquote> <table cellpadding=0 cellspacing=0 summary="layout">
	 * <tr>
	 * <td><code>serialization = <u>commandId</u> [ '?' parameters ]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>parameters = parameter [ '&' parameters ]</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>parameter = <u>parameterId</u> [ '=' <u>parameterValue</code></u> ]</td>
	 * </tr>
	 * </table> </blockquote>
	 * <p>
	 * In the syntax above, sections inside square-brackets are optional. The
	 * characters in single quotes (<code>'?'</code>, <code>'&'</code> and
	 * <code>'='</code>) indicate literal characters.
	 * </p>
	 * <p>
	 * <code><u>commandId</u></code> represents the command id encoded with
	 * UTF-8 encoding. <code><u>parameterId</u></code> and
	 * <code><u>parameterValue</u></code> represent the parameter ids and
	 * values encoded with UTF-8 encoding. The order of the parameters is not
	 * defined (and not important). A missing <code><u>parameterValue</u></code>
	 * indicates that the value of the parameter is <code>null</code>.
	 * </p>
	 * <p>
	 * For example, the string shown below represents a serialized parameterized
	 * command that can be used to show the Resource perspective:
	 * </p>
	 * <p>
	 * <tt>org.eclipse.ui.perspectives.showPerspective?org.eclipse.ui.perspectives.showPerspective.perspectiveId=org.eclipse.ui.resourcePerspective</tt>
	 * </p>
	 * <p>
	 * This example shows the more general form with multiple parameters and
	 * null value parameters.
	 * </p>
	 * <p>
	 * <tt>command.id?param1.id=value1&amp;param2.id&amp;param3.id=value3</tt>
	 * </p>
	 * <p>
	 * The syntax used here was intentionally chosen to be "URI-friendly". The
	 * use of the <code>'?'</code>, <code>'&'</code> and <code>'='</code>
	 * characters and the UTF-8 encoding of the ids and values allows the String
	 * returned to form the suffix of a well-formed URI.
	 * </p>
	 * 
	 * @return A <code>String</code> containing the UTF-8 encoded command id,
	 *         parameter ids and parameter values.
	 * @throws SerializationException
	 *             if there is a problem creating the serialization
	 *             <code>String</code>
	 * @see CommandManager#deserialize(String)
	 * @since 3.2
	 */
	public final String serialize() throws SerializationException {

		final String encodedId = encodeUTF8(getId());

		if ((parameterizations == null) || (parameterizations.length == 0)) {
			return encodedId;
		}

		final StringBuffer buffer = new StringBuffer(encodedId);
		buffer.append('?');

		for (int i = 0; i < parameterizations.length; i++) {

			if (i > 0) {
				// insert separator between parameters
				buffer.append('&');
			}

			final Parameterization parameterization = parameterizations[i];
			final String parameterId = parameterization.getParameter().getId();
			final String encodedParameterId = encodeUTF8(parameterId);

			buffer.append(encodedParameterId);

			final String parameterValue = parameterization.getValue();
			if (parameterValue != null) {
				final String encodedParameterValue = encodeUTF8(parameterValue);
				buffer.append('=');
				buffer.append(encodedParameterValue);
			}
		}

		return buffer.toString();
	}

	/**
	 * Encodes a <code>String</code> with UTF-8 encoding.
	 * 
	 * @param rawText
	 *            a <code>String</code> to encode with UTF-8 encoding
	 * @return a <code>String</code> representing <code>rawText</code>
	 *         encoded with UTF-8 encoding
	 * @throws SerializationException
	 *             if UTF-8 encoding is not supported on this platform.
	 * @since 3.2
	 */
	private final String encodeUTF8(String rawText)
			throws SerializationException {
		try {
			return URLEncoder.encode(rawText, UTF_8_ENCODING);

		} catch (UnsupportedEncodingException ex) {
			throw new SerializationException("UTF-8 Encoding not supported", ex); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			hashCode = HASH_INITIAL * HASH_FACTOR + Util.hashCode(command);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(parameterizations);
			if (hashCode == HASH_CODE_NOT_COMPUTED) {
				hashCode++;
			}
		}
		return hashCode;
	}
    
    public final String toString() {
        final StringBuffer buffer = new StringBuffer();
		buffer.append("ParameterizedCommand("); //$NON-NLS-1$
		buffer.append(command);
		buffer.append(',');
		buffer.append(parameterizations);
		buffer.append(')');
		return buffer.toString();
	}
}
