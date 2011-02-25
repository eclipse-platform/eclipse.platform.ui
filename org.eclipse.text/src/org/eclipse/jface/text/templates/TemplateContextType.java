/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * A context type defines a context within which templates are resolved. It
 * stores a number of <code>TemplateVariableResolver</code>s. A
 * <code>TemplateBuffer</code> can be resolved in a
 * <code>TemplateContext</code> using the
 * {@link #resolve(TemplateBuffer, TemplateContext)} method.
 * <p>
 * Clients may extend this class.
 * </p>
 *
 * @since 3.0
 */
public class TemplateContextType {

	/** The id of the context type. */
	private /* final */ String fId= null;

	/** Variable resolvers used by this content type. */
	private final Map/*<String, TemplateVariableResolver>*/ fResolvers= new HashMap();

	/** The name of the context type. */
	private String fName= null;

	/**
	 * Creates a context type with an identifier. The identifier must be unique,
	 * a qualified name is suggested. The id is also used as name.
	 *
	 * @param id the unique identifier of the context type
	 */
	public TemplateContextType(String id) {
		this(id, id);
	}

	/**
	 * Creates a context type with an identifier. The identifier must be unique, a qualified name is suggested.
	 *
	 * @param id the unique identifier of the context type
	 * @param name the name of the context type
	 */
	public TemplateContextType(String id, String name) {
		Assert.isNotNull(id);
		Assert.isNotNull(name);
		fId= id;
		fName= name;
	}

	/**
	 * Returns the id of the context type.
	 *
	 * @return the id of the receiver
	 */
	public String getId() {
	    return fId;
	}


	/**
	 * Returns the name of the context type.
	 *
	 * @return the name of the context type
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Creates a context type with a <code>null</code> identifier.
	 * <p>
	 * This is a framework-only constructor that exists only so that context
	 * types can be contributed via an extension point and that should not be
	 * called in client code except for subclass constructors; use
	 * {@link #TemplateContextType(String)} instead.
	 * </p>
	 */
	public TemplateContextType() {
	}

	/**
	 * Sets the id of this context.
	 * <p>
	 * This is a framework-only method that exists solely so that context types
	 * can be contributed via an extension point and that should not be called
	 * in client code; use {@link #TemplateContextType(String)} instead.
	 * </p>
	 *
	 * @param id the identifier of this context
	 * @throws RuntimeException an unspecified exception if the id has already
	 *         been set on this context type
	 */
	public final void setId(String id) throws RuntimeException {
		Assert.isNotNull(id);
		Assert.isTrue(fId == null); // may only be called once when the context is instantiated
		fId= id;
	}

	/**
	 * Sets the name of the context type.
	 *
	 * <p>
	 * This is a framework-only method that exists solely so that context types
	 * can be contributed via an extension point and that should not be called
	 * in client code; use {@link #TemplateContextType(String, String)} instead.
	 * </p>
	 *
	 * @param name the name of the context type
	 */
	public final void setName(String name) {
		Assert.isTrue(fName == null); // only initialized by extension code
		fName= name;
	}

	/**
	 * Adds a variable resolver to the context type. If there already is a resolver
	 * for the same type, the previous one gets replaced by <code>resolver</code>.
	 *
	 * @param resolver the resolver to be added under its name
	 */
	public void addResolver(TemplateVariableResolver resolver) {
		Assert.isNotNull(resolver);
		fResolvers.put(resolver.getType(), resolver);
	}

	/**
	 * Removes a template variable from the context type.
	 *
	 * @param resolver the variable to be removed
	 */
	public void removeResolver(TemplateVariableResolver resolver) {
		Assert.isNotNull(resolver);
		fResolvers.remove(resolver.getType());
	}

	/**
	 * Removes all template variables from the context type.
	 */
	public void removeAllResolvers() {
		fResolvers.clear();
	}

	/**
	 * Returns an iterator for the variables known to the context type.
	 *
	 * @return an iterator over the variables in this context type (element type: {@link TemplateVariableResolver})
	 */
	public Iterator resolvers() {
	 	return Collections.unmodifiableMap(fResolvers).values().iterator();
	}

	/**
	 * Returns the resolver for the given type.
	 *
	 * @param type the type for which a resolver is needed
	 * @return a resolver for the given type, or <code>null</code> if none is registered
	 */
	protected TemplateVariableResolver getResolver(String type) {
		return (TemplateVariableResolver) fResolvers.get(type);
	}

	/**
	 * Validates a pattern, a <code>TemplateException</code> is thrown if
	 * validation fails.
	 *
	 * @param pattern the template pattern to validate
	 * @throws TemplateException if the pattern is invalid
	 */
	public void validate(String pattern) throws TemplateException {
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(pattern);
		validateVariables(buffer.getVariables());
	}

	/**
	 * Validates the variables in this context type. If a variable is not valid,
	 * e.g. if its type is not known in this context type, a
	 * <code>TemplateException</code> is thrown.
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 *
	 * @param variables the variables to validate
	 * @throws TemplateException if one of the variables is not valid in this
	 *         context type
	 */
	protected void validateVariables(TemplateVariable[] variables) throws TemplateException {
	}

	/**
	 * Resolves the variables in <code>buffer</code> within <code>context</code>
	 * and edits the template buffer to reflect the resolved variables.
	 *
	 * @param buffer the template buffer
	 * @param context the template context
	 * @throws MalformedTreeException if the positions in the buffer overlap
	 * @throws BadLocationException if the buffer cannot be successfully modified
	 */
	public void resolve(TemplateBuffer buffer, TemplateContext context) throws MalformedTreeException, BadLocationException {
		Assert.isNotNull(context);
		TemplateVariable[] variables= buffer.getVariables();

		List positions= variablesToPositions(variables);
		List edits= new ArrayList(5);

        // iterate over all variables and try to resolve them
        for (int i= 0; i != variables.length; i++) {
            TemplateVariable variable= variables[i];

			if (!variable.isResolved())
				resolve(variable, context);

			String value= variable.getDefaultValue();
			int[] offsets= variable.getOffsets();
			// update buffer to reflect new value
			for (int k= 0; k != offsets.length; k++)
				edits.add(new ReplaceEdit(offsets[k], variable.getInitialLength(), value));

        }

    	IDocument document= new Document(buffer.getString());
        MultiTextEdit edit= new MultiTextEdit(0, document.getLength());
        edit.addChildren((TextEdit[]) positions.toArray(new TextEdit[positions.size()]));
        edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
        edit.apply(document, TextEdit.UPDATE_REGIONS);

		positionsToVariables(positions, variables);

        buffer.setContent(document.get(), variables);
    }

	/**
	 * Resolves a single variable in a context. Resolving is delegated to the registered resolver.
	 *
	 * @param variable the variable to resolve
	 * @param context the context in which to resolve the variable
	 * @since 3.3
	 */
	public void resolve(TemplateVariable variable, TemplateContext context) {
		String type= variable.getType();
		TemplateVariableResolver resolver= (TemplateVariableResolver) fResolvers.get(type);
		if (resolver == null)
			resolver= new TemplateVariableResolver(type, ""); //$NON-NLS-1$
		resolver.resolve(variable, context);
	}

	private static List variablesToPositions(TemplateVariable[] variables) {
   		List positions= new ArrayList(5);
		for (int i= 0; i != variables.length; i++) {
		    int[] offsets= variables[i].getOffsets();
		    for (int j= 0; j != offsets.length; j++)
				positions.add(new RangeMarker(offsets[j], 0));
		}

		return positions;
	}

	private static void positionsToVariables(List positions, TemplateVariable[] variables) {
		Iterator iterator= positions.iterator();

		for (int i= 0; i != variables.length; i++) {
		    TemplateVariable variable= variables[i];

			int[] offsets= new int[variable.getOffsets().length];
			for (int j= 0; j != offsets.length; j++)
				offsets[j]= ((TextEdit) iterator.next()).getOffset();

		 	variable.setOffsets(offsets);
		}
	}
}
