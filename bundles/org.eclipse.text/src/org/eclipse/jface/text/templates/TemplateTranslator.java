/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The template translator translates a string into a template buffer. Regions marked as variables
 * are translated into <code>TemplateVariable</code>s.
 * <p>
 * The EBNF grammar of a valid string is as follows:
 * </p>
 * <pre> template := (text | escape)*.
 * text := character - dollar.
 * escape := dollar ('{' variable '}' | dollar).
 * dollar := '$'.
 * variable := identifier | identifier ':' type.
 * type := qualifiedname | qualifiedname '(' arguments ')'.
 * arguments := (argument ',')* argument.
 * argument := qualifiedname | argumenttext.
 * qualifiedname := (identifier '.')* identifier.
 * argumenttext := "'" (character - "'" | "'" "'")* "'".
 * identifier := javaidentifierpart - "$".</pre>
 * <p>
 * Clients may only replace the <code>createVariable</code> method of this class.
 * </p>
 *
 * @since 3.0
 */
public class TemplateTranslator {
	/**
	 * Regex pattern for identifier.
	 * Note: For historic reasons, this pattern <em>allows</em> numbers at the beginning of an identifier.
	 * @since 3.7
	 */
	private static final String IDENTIFIER= "(?:[\\p{javaJavaIdentifierPart}&&[^\\$]]++)"; //$NON-NLS-1$

	/**
	 * Regex pattern for qualifiedname
	 * @since 3.4
	 */
	private static final String QUALIFIED_NAME= "(?:" + IDENTIFIER + "\\.)*+" + IDENTIFIER; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Regex pattern for argumenttext
	 * @since 3.4
	 */
	private static final String ARGUMENT_TEXT= "'(?:(?:'')|(?:[^']))*+'"; //$NON-NLS-1$

	/**
	 * Regex pattern for argument
	 * @since 3.4
	 */
	private static final String ARGUMENT= "(?:" + QUALIFIED_NAME + ")|(?:" + ARGUMENT_TEXT + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Regex pattern for whitespace
	 * @since 3.5
	 */
	private static final String SPACES= "\\s*+"; //$NON-NLS-1$

	/**
	 * Precompiled regex pattern for qualified names.
	 * @since 3.3
	 */
	private static final Pattern PARAM_PATTERN= Pattern.compile(ARGUMENT);
	/**
	 * Precompiled regex pattern for valid dollar escapes (dollar literals and variables) and
	 * (invalid) single dollars.
	 * @since 3.3
	 */
	private static final Pattern ESCAPE_PATTERN= Pattern.compile(
			"\\$\\$|\\$\\{" + // $$|${											//$NON-NLS-1$
			SPACES +
			"(" + IDENTIFIER + "?+)" + // variable id group (1)					//$NON-NLS-1$ //$NON-NLS-2$
			SPACES +
			"(?:" +																//$NON-NLS-1$
				":" +															//$NON-NLS-1$
				SPACES +
				"(" + QUALIFIED_NAME + ")" + // variable type group (2)			//$NON-NLS-1$ //$NON-NLS-2$
				SPACES +
				"(?:" +															//$NON-NLS-1$
					"\\(" + // (												//$NON-NLS-1$
					SPACES +
					"((?:(?:" + ARGUMENT + ")" + SPACES + "," + SPACES + ")*+(?:" + ARGUMENT + "))" + // arguments group (3)	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					SPACES +
					"\\)" + // )												//$NON-NLS-1$
				")?" +															//$NON-NLS-1$
				SPACES +
			")?" +																//$NON-NLS-1$
			"\\}|\\$"); // }|$													//$NON-NLS-1$

	/**
	 * @since 3.3
	 */
	private final class VariableDescription {
		final List<Integer> fOffsets= new ArrayList<>(5);
		final String fName;
		TemplateVariableType fType;

		VariableDescription(String name, TemplateVariableType type) {
			fName= name;
			fType= type;
		}

		void mergeType(TemplateVariableType type) throws TemplateException {
			if (type == null) {
				return;
			}
			if (fType == null) {
				fType= type;
			}
			if (!type.equals(fType)) {
				fail(TextTemplateMessages.getFormattedString("TemplateTranslator.error.incompatible.type", fName)); //$NON-NLS-1$
			}
		}
	}

	/** Last translation error. */
	private String fErrorMessage;
	/**
	 * Used to ensure compatibility with subclasses overriding
	 * {@link #createVariable(String, String, int[])}.
	 * @since 3.3
	 */
	private TemplateVariableType fCurrentType;

	/**
	 * Returns an error message if an error occurred for the last translation, <code>null</code>
	 * otherwise.
	 *
	 * @return the error message if an error occurred during the most recent translation,
	 *         <code>null</code> otherwise
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}

	/**
	 * Translates a template to a <code>TemplateBuffer</code>. <code>null</code> is returned if
	 * there was an error. <code>getErrorMessage()</code> retrieves the associated error message.
	 *
	 * @param template the template to translate.
	 * @return returns the template buffer corresponding to the string
	 * @see #getErrorMessage()
	 * @throws TemplateException if translation failed
	 */
	public TemplateBuffer translate(Template template) throws TemplateException {
		return parse(template.getPattern());
	}

	/**
	 * Translates a template string to <code>TemplateBuffer</code>. <code>null</code> is
	 * returned if there was an error. <code>getErrorMessage()</code> retrieves the associated
	 * error message.
	 *
	 * @param string the string to translate.
	 * @return returns the template buffer corresponding to the string
	 * @see #getErrorMessage()
	 * @throws TemplateException if translation failed
	 */
	public TemplateBuffer translate(String string) throws TemplateException {
		return parse(string);
	}

	/**
	 * Internal parser.
	 *
	 * @param string the string to parse
	 * @return the parsed <code>TemplateBuffer</code>
	 * @throws TemplateException if the string does not conform to the template format
	 */
	private TemplateBuffer parse(String string) throws TemplateException {

		fErrorMessage= null;
		final StringBuilder buffer= new StringBuilder(string.length());
		final Matcher matcher= ESCAPE_PATTERN.matcher(string);
		final Map<String, VariableDescription> variables= new LinkedHashMap<>();

		int complete= 0;
		while (matcher.find()) {
			// append any verbatim text
			buffer.append(string.substring(complete, matcher.start()));

			// check the escaped sequence
			switch (matcher.group()) {
			case "$": //$NON-NLS-1$
				fail(TextTemplateMessages.getString("TemplateTranslator.error.incomplete.variable")); //$NON-NLS-1$
				break;
			case "$$": //$NON-NLS-1$
				// escaped $
				buffer.append('$');
				break;
			default:
				// parse variable
				String name= matcher.group(1);
				String typeName= matcher.group(2);
				String params= matcher.group(3);
				TemplateVariableType type= createType(typeName, params);
				updateOrCreateVariable(variables, name, type, buffer.length());
				buffer.append(name);
				break;
			}
			complete= matcher.end();
		}
		// append remaining verbatim text
		buffer.append(string.substring(complete));

		TemplateVariable[] vars= createVariables(variables);
		fixOffsetsAndBuffer(buffer, vars);
		return new TemplateBuffer(buffer.toString(), vars);
	}

	/**
	 * In cases default value is provided dynamically when instantiating the variable (so not part
	 * of the {@link VariableDescription}), the buffer would actually contain incorrect text (name instead
	 * of default value) and offsets can also be inconsistent if variable name and default value have different
	 * length. This methods fix both buffer and variable offsets to ensure default value is used.
	 * @param buffer the default template string
	 * @param vars variables
	 */
	private void fixOffsetsAndBuffer(StringBuilder buffer, TemplateVariable[] vars) {
		SortedMap<Integer, TemplateVariable> varsByOffset = new TreeMap<>();
		for (TemplateVariable var : vars) {
			for (int offset : var.getOffsets()) {
				varsByOffset.put(Integer.valueOf(offset), var);
			}
		}
		int totalOffsetDelta = 0;
		Map<TemplateVariable, Collection<Integer>> fixedOffsets = new HashMap<>(vars.length, 1.f);
		for (Entry<Integer, TemplateVariable> entry : varsByOffset.entrySet()) {
			final int initialOffset = entry.getKey().intValue();
			TemplateVariable variable = entry.getValue();
			final int fixedOffset = initialOffset + totalOffsetDelta;
			fixedOffsets.computeIfAbsent(variable, v -> new ArrayList<>(v.getOffsets().length)).add(Integer.valueOf(fixedOffset));
			int currentOffsetDelta = variable.getDefaultValue().length() - variable.getName().length();
			buffer.replace(fixedOffset, fixedOffset + variable.getName().length(), variable.getDefaultValue());
			totalOffsetDelta += currentOffsetDelta;
		}
		fixedOffsets.forEach((variable, fixs) -> variable.setOffsets(fixs.stream().mapToInt(Integer::valueOf).toArray()));
	}

	private TemplateVariableType createType(String typeName, String paramString) {
		if (typeName == null) {
			return null;
		}

		if (paramString == null) {
			return new TemplateVariableType(typeName);
		}

		final Matcher matcher= PARAM_PATTERN.matcher(paramString);
		List<String> params= new ArrayList<>(5);
		while (matcher.find()) {
			String argument= matcher.group();
			if (argument.charAt(0) == '\'') {
				// argumentText
				argument= argument.substring(1, argument.length() - 1).replace("''", "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			params.add(argument);
		}

		return new TemplateVariableType(typeName, params.toArray(new String[params.size()]));
	}

	private void fail(String message) throws TemplateException {
		fErrorMessage= message;
		throw new TemplateException(message);
	}

	/**
	 * If there is no variable named <code>name</code>, a new variable with the given type, name
	 * and offset is created. If one exists, the offset is added to the variable and the type is
	 * merged with the existing type.
	 *
	 * @param variables the variables by variable name
	 * @param name the name of the variable
	 * @param type the variable type, <code>null</code> for not defined
	 * @param offset the buffer offset of the variable
	 * @return the variable description found or created for that name
	 * @throws TemplateException if merging the type fails
	 * @since 3.3
	 */
	private VariableDescription updateOrCreateVariable(Map<String, VariableDescription> variables, String name, TemplateVariableType type, int offset) throws TemplateException {
		VariableDescription varDesc= variables.get(name);
		if (varDesc == null) {
			varDesc= new VariableDescription(name, type);
			variables.put(name, varDesc);
		} else {
			varDesc.mergeType(type);
		}
		varDesc.fOffsets.add(Integer.valueOf(offset));
		return varDesc;
	}

	/**
	 * Creates proper {@link TemplateVariable}s from the variable descriptions.
	 *
	 * @param variables the variable descriptions by variable name
	 * @return the corresponding variables
	 * @since 3.3
	 */
	private TemplateVariable[] createVariables(Map<String, VariableDescription> variables) {
		TemplateVariable[] result= new TemplateVariable[variables.size()];
		int idx= 0;
		for (Iterator<VariableDescription> it= variables.values().iterator(); it.hasNext(); idx++) {
			VariableDescription desc= it.next();
			TemplateVariableType type= desc.fType == null ? new TemplateVariableType(desc.fName) : desc.fType;
			int[] offsets= new int[desc.fOffsets.size()];
			int i= 0;
			for (Iterator<Integer> intIt= desc.fOffsets.iterator(); intIt.hasNext(); i++) {
				Integer offset= intIt.next();
				offsets[i]= offset.intValue();
			}
			fCurrentType= type;
			/*
			 * Call the deprecated version of createVariable. When not overridden, it will delegate
			 * to the new version using fCurrentType.
			 */
			TemplateVariable var= createVariable(fCurrentType, desc.fName, offsets);
			result[idx]= var;
		}
		fCurrentType= null; // avoid dangling reference
		return result;
	}

	/**
	 * Hook method to create new variables. Subclasses may override to supply their custom variable
	 * type.
	 * <p>
	 * Clients may replace this method.
	 * </p>
	 *
	 * @param type the type of the new variable.
	 * @param name the name of the new variable.
	 * @param offsets the offsets where the variable occurs in the template
	 * @return a new instance of <code>TemplateVariable</code>
	 * @deprecated as of 3.3 use {@link #createVariable(TemplateVariableType, String, int[])} instead
	 */
	@Deprecated
	protected TemplateVariable createVariable(String type, String name, int[] offsets) {
		return createVariable(fCurrentType, name, offsets);
	}

	/**
	 * Hook method to create new variables. Subclasses may override to supply their custom variable
	 * type.
	 * <p>
	 * Clients may replace this method.
	 * </p>
	 *
	 * @param type the type of the new variable.
	 * @param name the name of the new variable.
	 * @param offsets the offsets where the variable occurs in the template
	 * @return a new instance of <code>TemplateVariable</code>
	 * @since 3.3
	 */
	protected TemplateVariable createVariable(TemplateVariableType type, String name, int[] offsets) {
		return new TemplateVariable(type, name, name, offsets);
	}
}
