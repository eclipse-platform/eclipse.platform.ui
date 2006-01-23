/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The template translator translates a string into a template buffer. Regions
 * marked as variables are translated into <code>TemplateVariable</code>s.
 * <p>
 * The EBNF grammar of a valid string is as follows:</p>
 * <p>
 * template := (text | escape)*. <br />
 * text := character - dollar. <br />
 * escape := dollar ('{' identifier '}' | dollar). <br />
 * dollar := '$'. <br />
 * </p>
 * <p>
 * Clients may extend the <code>createVariable</code> method of this class.
 * </p>
 *
 * @since 3.0
 */
public class TemplateTranslator {

	// states
	private static final int TEXT= 0;
	private static final int ESCAPE= 1;
	private static final int IDENTIFIER= 2;

	// tokens
	private static final char ESCAPE_CHARACTER= '$';
	private static final char IDENTIFIER_BEGIN= '{';
	private static final char IDENTIFIER_END= '}';

	/** a buffer for the translation result string */
    private final StringBuffer fBuffer= new StringBuffer();
    /** position offsets of variables */
    private final Vector fOffsets= new Vector();
    /** position lengths of variables */
    private final Vector fLengths= new Vector();

	/** the current parsing state */
    private int fState;
    /** the last translation error */
    private String fErrorMessage;

	/**
	 * Returns an error message if an error occurred for the last translation,
	 * <code>null</code> otherwise.
	 *
	 * @return the error message if an error occurred during the most recent
	 *         translation, <code>null</code> otherwise
	 */
	public String getErrorMessage() {
	    return fErrorMessage;
	}

	/**
	 * Translates a template to a <code>TemplateBuffer</code>. <code>null</code>
	 * is returned if there was an error. <code>getErrorMessage()</code> retrieves the
	 * associated error message.
	 *
	 * @param template the template to translate.
	 * @return returns the template buffer corresponding to the string, <code>null</code>
	 *         if there was an error.
	 * @see #getErrorMessage()
	 * @throws TemplateException if translation failed
	 */
	public TemplateBuffer translate(Template template) throws TemplateException {
		return translate(template.getPattern());
	}

	/**
	 * Translates a template string to <code>TemplateBuffer</code>. <code>null</code>
	 * is returned if there was an error. <code>getErrorMessage()</code> retrieves the
	 * associated error message.
	 *
	 * @param string the string to translate.
	 * @return returns the template buffer corresponding to the string, <code>null</code>
	 *         if there was an error.
	 * @see #getErrorMessage()
	 * @throws TemplateException if translation failed
	 */
	public TemplateBuffer translate(String string) throws TemplateException {

	    fBuffer.setLength(0);
	    fOffsets.clear();
	    fLengths.clear();
	    fState= TEXT;
	    fErrorMessage= null;

		if (!parse(string))
			throw new TemplateException(fErrorMessage);

		switch (fState) {
		case TEXT:
			break;

		// illegal
		case ESCAPE:
			throw new TemplateException(TextTemplateMessages.getString("TemplateTranslator.error.incomplete.variable")); //$NON-NLS-1$

		// illegal
		case IDENTIFIER:
			throw new TemplateException(TextTemplateMessages.getString("TemplateTranslator.error.incomplete.variable")); //$NON-NLS-1$
		}

		int[] offsets= new int[fOffsets.size()];
		int[] lengths= new int[fLengths.size()];

		for (int i= 0; i < fOffsets.size(); i++) {
			offsets[i]= ((Integer) fOffsets.get(i)).intValue();
			lengths[i]= ((Integer) fLengths.get(i)).intValue();
		}

		String translatedString= fBuffer.toString();
		TemplateVariable[] variables= findVariables(translatedString, offsets, lengths);

		return new TemplateBuffer(translatedString, variables);
	}

	private TemplateVariable[] findVariables(String string, int[] offsets, int[] lengths) {

		Map map= new HashMap();

		for (int i= 0; i != offsets.length; i++) {
		    int offset= offsets[i];
		    int length= lengths[i];

		    String content= string.substring(offset, offset + length);
		    Vector vector= (Vector) map.get(content);
		    if (vector == null) {
		    	vector= new Vector();
		    	map.put(content, vector);
		    }
		    vector.add(new Integer(offset));
		}

		TemplateVariable[] variables= new TemplateVariable[map.size()];
		int k= 0;

		Set keys= map.keySet();
		for (Iterator i= keys.iterator(); i.hasNext(); ) {
			String name= (String) i.next();
			Vector vector= (Vector) map.get(name);

			int[] offsets_= new int[vector.size()];
			for (int j= 0; j != offsets_.length; j++)
				offsets_[j]= ((Integer) vector.get(j)).intValue();

			variables[k]= createVariable(name, name, offsets_);
			k++;
		}

		return variables;
	}

	/**
	 * Hook method to create new variables. Subclasses may override to supply their
	 * custom variable type.
	 * <p>
	 * Clients may replace this method.
	 * </p>
	 *
	 * @param type the type of the new variable.
	 * @param name the name of the new variable.
	 * @param offsets the offsets where the variable occurs in the template
	 * @return a new instance of <code>TemplateVariable</code>
	 */
	protected TemplateVariable createVariable(String type, String name, int[] offsets) {
		return new TemplateVariable(type, name, offsets);
	}

	/**
	 * Internal parser.
	 *
	 * @param string the string to parse
	 * @return <code>true</code> if parsing was successful
	 */
	private boolean parse(String string) {

		for (int i= 0; i != string.length(); i++) {
		    char ch= string.charAt(i);

			switch (fState) {
			case TEXT:
				switch (ch) {
				case ESCAPE_CHARACTER:
					fState= ESCAPE;
					break;

				default:
					fBuffer.append(ch);
					break;
				}
				break;

			case ESCAPE:
				switch (ch) {
				case ESCAPE_CHARACTER:
					fBuffer.append(ch);
					fState= TEXT;
					break;

				case IDENTIFIER_BEGIN:
					fOffsets.add(new Integer(fBuffer.length()));
					fState= IDENTIFIER;
					break;

				default:
					// illegal single escape character, but be tolerant
					fErrorMessage= TextTemplateMessages.getString("TemplateTranslator.error.incomplete.variable"); //$NON-NLS-1$
					fBuffer.append(ESCAPE_CHARACTER);
					fBuffer.append(ch);
					fState= TEXT;
					return false;
				}
				break;

			case IDENTIFIER:
				switch (ch) {
				case IDENTIFIER_END:
					int offset= ((Integer) fOffsets.get(fOffsets.size() - 1)).intValue();
					fLengths.add(new Integer(fBuffer.length() - offset));
					fState= TEXT;
					break;

				default:
					if (!Character.isUnicodeIdentifierStart(ch) &&
						!Character.isUnicodeIdentifierPart(ch))
					{
						// illegal identifier character
						fErrorMessage= TextTemplateMessages.getString("TemplateTranslator.error.invalid.identifier"); //$NON-NLS-1$
						return false;
					}

					fBuffer.append(ch);
					break;
				}
				break;
			}
		}

		return true;
	}

}
