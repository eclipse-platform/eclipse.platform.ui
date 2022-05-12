/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Benjamin Muskalla - bug 222861 [Commands] ParameterizedCommand#equals broken
 *******************************************************************************/

package org.eclipse.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

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
@SuppressWarnings("rawtypes")
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
	private static final int HASH_INITIAL = ParameterizedCommand.class.getName().hashCode();

	/**
	 * The index of the parameter id in the parameter values.
	 *
	 * @deprecated no longer used
	 */
	@Deprecated
	public static final int INDEX_PARAMETER_ID = 0;

	/**
	 * The index of the human-readable name of the parameter itself, in the
	 * parameter values.
	 *
	 * @deprecated no longer used
	 */
	@Deprecated
	public static final int INDEX_PARAMETER_NAME = 1;

	/**
	 * The index of the human-readable name of the value of the parameter for
	 * this command.
	 *
	 * @deprecated no longer used
	 */
	@Deprecated
	public static final int INDEX_PARAMETER_VALUE_NAME = 2;

	/**
	 * The index of the value of the parameter that the command can understand.
	 *
	 * @deprecated no longer used
	 */
	@Deprecated
	public static final int INDEX_PARAMETER_VALUE_VALUE = 3;

	/**
	 * Escapes special characters in the command id, parameter ids and parameter
	 * values for {@link #serialize()}. The special characters
	 * {@link CommandManager#PARAMETER_START_CHAR},
	 * {@link CommandManager#PARAMETER_END_CHAR},
	 * {@link CommandManager#ID_VALUE_CHAR},
	 * {@link CommandManager#PARAMETER_SEPARATOR_CHAR} and
	 * {@link CommandManager#ESCAPE_CHAR} are escaped by prepending a
	 * {@link CommandManager#ESCAPE_CHAR} character.
	 *
	 * @param rawText
	 *            a <code>String</code> to escape special characters in for
	 *            serialization.
	 * @return a <code>String</code> representing <code>rawText</code> with
	 *         special serialization characters escaped
	 * @since 3.2
	 */
	private static String escape(final String rawText) {

		// defer initialization of a StringBuilder until we know we need one
		StringBuilder buffer = null;

		for (int i = 0; i < rawText.length(); i++) {

			char c = rawText.charAt(i);
			switch (c) {
			case CommandManager.PARAMETER_START_CHAR:
			case CommandManager.PARAMETER_END_CHAR:
			case CommandManager.ID_VALUE_CHAR:
			case CommandManager.PARAMETER_SEPARATOR_CHAR:
			case CommandManager.ESCAPE_CHAR:
				if (buffer == null) {
					buffer = new StringBuilder(rawText.substring(0, i));
				}
				buffer.append(CommandManager.ESCAPE_CHAR);
				buffer.append(c);
				break;
			default:
				if (buffer != null) {
					buffer.append(c);
				}
				break;
			}

		}

		if (buffer == null) {
			return rawText;
		}
		return buffer.toString();
	}

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
	 */
	private static Collection<?> expandParameters(final int startIndex,
			final IParameter[] parameters) {
		final int nextIndex = startIndex + 1;
		final boolean noMoreParameters = (nextIndex >= parameters.length);

		final IParameter parameter = parameters[startIndex];
		final List<Object> parameterizations = new ArrayList<>();
		if (parameter.isOptional()) {
			parameterizations.add(null);
		}

		IParameterValues values = null;
		try {
			values = parameter.getValues();
		} catch (final ParameterValuesException e) {
			if (noMoreParameters) {
				return parameterizations;
			}

			// Make recursive call
			return expandParameters(nextIndex, parameters);
		}
		final Map<?, ?> parameterValues = values.getParameterValues();
		final Iterator<?> parameterValueItr = parameterValues.entrySet().iterator();
		while (parameterValueItr.hasNext()) {
			final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) parameterValueItr.next();
			final Parameterization parameterization = new Parameterization(parameter, (String) entry.getValue());
			parameterizations.add(parameterization);
		}

		// Check if another iteration will produce any more names.
		final int parameterizationCount = parameterizations.size();
		if (noMoreParameters) {
			// This is it, so just return the current parameterizations.
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = (Parameterization) parameterizations.get(i);
				final List<Parameterization> combination = new ArrayList<>(1);
				combination.add(parameterization);
				parameterizations.set(i, combination);
			}
			return parameterizations;
		}

		// Make recursive call
		final Collection<?> suffixes = expandParameters(nextIndex, parameters);
		while (suffixes.remove(null)) {
			// just keep deleting the darn things.
		}
		if (suffixes.isEmpty()) {
			// This is it, so just return the current parameterizations.
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = (Parameterization) parameterizations.get(i);
				final List<Parameterization> combination = new ArrayList<>(1);
				combination.add(parameterization);
				parameterizations.set(i, combination);
			}
			return parameterizations;
		}
		final Collection<List<?>> returnValue = new ArrayList<>();
		final Iterator<?> suffixItr = suffixes.iterator();
		while (suffixItr.hasNext()) {
			final List<?> combination = (List<?>) suffixItr.next();
			final int combinationSize = combination.size();
			for (int i = 0; i < parameterizationCount; i++) {
				final Parameterization parameterization = (Parameterization) parameterizations.get(i);
				final List<Object> newCombination = new ArrayList<>(combinationSize + 1);
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
	public static Collection generateCombinations(final Command command)
			throws NotDefinedException {
		final IParameter[] parameters = command.getParameters();
		if (parameters == null) {
			return Collections.singleton(new ParameterizedCommand(command, null));
		}

		final Collection<?> expansion = expandParameters(0, parameters);
		final Collection<ParameterizedCommand> combinations = new ArrayList<>(expansion.size());
		final Iterator<?> expansionItr = expansion.iterator();
		while (expansionItr.hasNext()) {
			final List<?> combination = (List<?>) expansionItr.next();
			if (combination == null) {
				combinations.add(new ParameterizedCommand(command, null));
			} else {
				while (combination.remove(null)) {
					// Just keep removing while there are null entries left.
				}
				if (combination.isEmpty()) {
					combinations.add(new ParameterizedCommand(command, null));
				} else {
					final Parameterization[] parameterizations = combination
							.toArray(new Parameterization[combination.size()]);
					combinations.add(new ParameterizedCommand(command, parameterizations));
				}
			}
		}

		return combinations;
	}

	/**
	 * Take a command and a map of parameter IDs to values, and generate the
	 * appropriate parameterized command.
	 *
	 * @param command
	 *            The command object. Must not be <code>null</code>.
	 * @param parameters
	 *            A map of String parameter ids to objects. May be
	 *            <code>null</code>.
	 * @return the parameterized command, or <code>null</code> if it could not
	 *         be generated
	 * @since 3.4
	 */
	public static ParameterizedCommand generateCommand(Command command, Map parameters) {
		// no parameters
		if (parameters == null || parameters.isEmpty()) {
			return new ParameterizedCommand(command, null);
		}

		try {
			ArrayList<Parameterization> parms = new ArrayList<>();
			for (Entry<?, ?> entry : ((Map<?, ?>) parameters).entrySet()) {
				String key = (String) entry.getKey();
				// get the parameter from the command
				IParameter parameter = command.getParameter(key);
				// if the parameter is defined add it to the parameter list
				if (parameter == null) {
					return null;
				}
				ParameterType parameterType = command.getParameterType(key);
				if (parameterType == null) {
					parms.add(new Parameterization(parameter, (String) entry.getValue()));
				} else {
					AbstractParameterValueConverter valueConverter = parameterType
							.getValueConverter();
					if (valueConverter != null) {
						String val = valueConverter.convertToString(entry.getValue());
						parms.add(new Parameterization(parameter, val));
					} else {
						parms.add(new Parameterization(parameter, (String) entry.getValue()));
					}
				}
			}

			// convert the parameters to an Parameterization array and create
			// the command
			return new ParameterizedCommand(command, parms.toArray(new Parameterization[parms.size()]));
		} catch (NotDefinedException | ParameterValueConversionException e) {
		}
		return null;
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

	private String name;

	/**
	 * Constructs a new instance of <code>ParameterizedCommand</code> with
	 * specific values for zero or more of its parameters.
	 *
	 * @param command
	 *            The command that is parameterized; must not be
	 *            <code>null</code>.
	 * @param parameterizations
	 *            An array of parameterizations binding parameters to values for
	 *            the command. This value may be <code>null</code>.
	 */
	public ParameterizedCommand(final Command command, final Parameterization[] parameterizations) {
		if (command == null) {
			throw new NullPointerException(
					"A parameterized command cannot have a null command"); //$NON-NLS-1$
		}

		this.command = command;
		IParameter[] parms = null;
		try {
			parms = command.getParameters();
		} catch (NotDefinedException e) {
			// This should not happen.
		}
		if (parameterizations != null && parameterizations.length>0 && parms != null) {
			int parmIndex = 0;
			Parameterization[] params = new Parameterization[parameterizations.length];
			for (IParameter parm : parms) {
				for (Parameterization pm : parameterizations) {
					if (parm.equals(pm.getParameter())) {
						params[parmIndex++] = pm;
					}
				}
			}
			this.parameterizations = params;
		} else {
			this.parameterizations = null;
		}
	}

	@Override
	public int compareTo(final Object object) {
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

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ParameterizedCommand)) {
			return false;
		}

		final ParameterizedCommand command = (ParameterizedCommand) object;
		return Objects.equals(this.command, command.command)
				&& Arrays.equals(this.parameterizations, command.parameterizations);
	}

	/**
	 * Executes this command with its parameters. This method will succeed
	 * regardless of whether the command is enabled or defined. It is
	 * preferrable to use {@link #executeWithChecks(Object, Object)}.
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
	 * @deprecated Please use {@link #executeWithChecks(Object, Object)}
	 *             instead.
	 */
	@Deprecated
	public Object execute(final Object trigger, final Object applicationContext)
			throws ExecutionException, NotHandledException {
		return command.execute(new ExecutionEvent(command, getParameterMap(), trigger, applicationContext));
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
	public Object executeWithChecks(final Object trigger, final Object applicationContext)
			throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		return command.executeWithChecks(new ExecutionEvent(command, getParameterMap(), trigger, applicationContext));
	}

	/**
	 * Returns the base command. It is possible for more than one parameterized
	 * command to have the same identifier.
	 *
	 * @return The command; never <code>null</code>, but may be undefined.
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * Returns the command's base identifier. It is possible for more than one
	 * parameterized command to have the same identifier.
	 *
	 * @return The command id; never <code>null</code>.
	 */
	public String getId() {
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
	public String getName() throws NotDefinedException {
		return getName(command.getName());
	}

	/**
	 * Returns a human-readable representation of this command with all of its
	 * parameterizations.
	 *
	 * @param baseName
	 *            The base name of the command that should be used to create the
	 *            parameterized command representation.
	 * @return The human-readable representation of this parameterized command;
	 *         never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the underlying command is not defined.
	 * @since 3.8
	 */
	public String getName(String baseName) throws NotDefinedException {
		if (name == null) {
			final StringBuilder nameBuffer = new StringBuilder();
			nameBuffer.append(baseName);
			if (parameterizations != null) {
				StringBuilder parametersBuffer = new StringBuilder();
				final int parameterizationCount = parameterizations.length;
				if (parameterizationCount == 1) {
					appendParameter(parametersBuffer, parameterizations[0], false);
				} else {
					for (int i = 0; i < parameterizationCount; i++) {

						appendParameter(parametersBuffer, parameterizations[i], true);

						// If there is another item, append a separator.
						if (i + 1 < parameterizationCount) {
							parametersBuffer.append(", "); //$NON-NLS-1$
						}
					}
				}
				if (parametersBuffer.length() > 0) {
					nameBuffer.append(" ("); //$NON-NLS-1$
					nameBuffer.append(parametersBuffer);
					nameBuffer.append(')');
				}
			}
			name = nameBuffer.toString();
		}
		return name;
	}

	private void appendParameter(final StringBuilder nameBuffer,
			final Parameterization parameterization, boolean shouldAppendName) {

		if(shouldAppendName) {
			nameBuffer.append(parameterization.getParameter().getName());
			nameBuffer.append(": "); //$NON-NLS-1$
		}
		try {
			nameBuffer.append(parameterization.getValueName());
		} catch (final ParameterValuesException e) {
			/*
			 * Just let it go for now. If someone complains we can
			 * add more info later.
			 */
		}
	}

	/**
	 * Returns the parameter map, as can be used to construct an
	 * <code>ExecutionEvent</code>.
	 *
	 * @return The map of parameter ids (<code>String</code>) to parameter
	 *         values (<code>String</code>). This map is never
	 *         <code>null</code>, but may be empty.
	 */
	public Map getParameterMap() {
		if ((parameterizations == null) || (parameterizations.length == 0)) {
			return Collections.EMPTY_MAP;
		}

		final Map<String, String> parameterMap = new HashMap<>();
		for (final Parameterization parameterization : parameterizations) {
			parameterMap.put(parameterization.getParameter().getId(), parameterization.getValue());
		}
		return parameterMap;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			hashCode = HASH_INITIAL * HASH_FACTOR + Objects.hashCode(command);
			hashCode = hashCode * HASH_FACTOR;
			if (parameterizations != null) {
				for (Parameterization parameterization : parameterizations) {
					hashCode += Objects.hashCode(parameterization);
				}
			}
			if (hashCode == HASH_CODE_NOT_COMPUTED) {
				hashCode++;
			}
		}
		return hashCode;
	}

	/**
	 * Returns a {@link String} containing the command id, parameter ids and
	 * parameter values for this {@link ParameterizedCommand}. The returned
	 * {@link String} can be stored by a client and later used to reconstruct an
	 * equivalent {@link ParameterizedCommand} using the
	 * {@link CommandManager#deserialize(String)} method.
	 * <p>
	 * The syntax of the returned {@link String} is as follows:
	 * </p>
	 *
	 * <blockquote>
	 * <code>serialization = <u>commandId</u> [ '(' parameters ')' ]</code><br>
	 * <code>parameters = parameter [ ',' parameters ]</code><br>
	 * <code>parameter = <u>parameterId</u> [ '=' <u>parameterValue</u> ]</code>
	 * </blockquote>
	 *
	 * <p>
	 * In the syntax above, sections inside square-brackets are optional. The
	 * characters in single quotes (<code>(</code>, <code>)</code>,
	 * <code>,</code> and <code>=</code>) indicate literal characters.
	 * </p>
	 * <p>
	 * <code><u>commandId</u></code> represents the command id encoded with
	 * separator characters escaped. <code><u>parameterId</u></code> and
	 * <code><u>parameterValue</u></code> represent the parameter ids and
	 * values encoded with separator characters escaped. The separator
	 * characters <code>(</code>, <code>)</code>, <code>,</code> and
	 * <code>=</code> are escaped by prepending a <code>%</code>. This
	 * requires <code>%</code> to be escaped, which is also done by prepending
	 * a <code>%</code>.
	 * </p>
	 * <p>
	 * The order of the parameters is not defined (and not important). A missing
	 * <code><u>parameterValue</u></code> indicates that the value of the
	 * parameter is <code>null</code>.
	 * </p>
	 * <p>
	 * For example, the string shown below represents a serialized parameterized
	 * command that can be used to show the Resource perspective:
	 * </p>
	 * <p>
	 * <code>org.eclipse.ui.perspectives.showPerspective(org.eclipse.ui.perspectives.showPerspective.perspectiveId=org.eclipse.ui.resourcePerspective)</code>
	 * </p>
	 * <p>
	 * This example shows the more general form with multiple parameters,
	 * <code>null</code> value parameters, and escaped <code>=</code> in the
	 * third parameter value.
	 * </p>
	 * <p>
	 * <code>command.id(param1.id=value1,param2.id,param3.id=esc%=val3)</code>
	 * </p>
	 *
	 * @return A string containing the escaped command id, parameter ids and
	 *         parameter values; never <code>null</code>.
	 * @see CommandManager#deserialize(String)
	 * @since 3.2
	 */
	public String serialize() {
		final String escapedId = escape(getId());

		if ((parameterizations == null) || (parameterizations.length == 0)) {
			return escapedId;
		}

		final StringBuilder buffer = new StringBuilder(escapedId);
		buffer.append(CommandManager.PARAMETER_START_CHAR);

		for (int i = 0; i < parameterizations.length; i++) {

			if (i > 0) {
				// insert separator between parameters
				buffer.append(CommandManager.PARAMETER_SEPARATOR_CHAR);
			}

			final Parameterization parameterization = parameterizations[i];
			final String parameterId = parameterization.getParameter().getId();
			final String escapedParameterId = escape(parameterId);

			buffer.append(escapedParameterId);

			final String parameterValue = parameterization.getValue();
			if (parameterValue != null) {
				final String escapedParameterValue = escape(parameterValue);
				buffer.append(CommandManager.ID_VALUE_CHAR);
				buffer.append(escapedParameterValue);
			}
		}

		buffer.append(CommandManager.PARAMETER_END_CHAR);

		return buffer.toString();
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder("ParameterizedCommand("); //$NON-NLS-1$
		buffer.append(command);
		buffer.append(',');
		buffer.append(Arrays.toString(parameterizations));
		buffer.append(')');
		return buffer.toString();
	}
}
