/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * A context type is a context factory.
 * 
 * @since 3.0
 */
public class ContextType {

	/** Name of the context type. */
	private final String fName;

	/** Variable resolvers used by this content type. */
	private final Map fResolvers= new HashMap();

	/**
	 * Creates a context type with a name.
	 * 
	 * @param name the name of the context. It has to be unique wrt to other context names.
	 */
	public ContextType(String name) {
		fName= name;   
	}

	/**
	 * Returns the name of the context type.
	 * 
	 * @return the name of the receiver
	 */
	public String getName() {
	    return fName;
	}
	
	/**
	 * Adds a variable resolver to the context type. If there already is a resolver
	 * for the same type, the previous one gets replaced by <code>resolver</code>.
	 * 
	 * @param resolver the resolver to be added under its name
	 */
	public void addResolver(TemplateVariableResolver resolver) {
		fResolvers.put(resolver.getType(), resolver);   
	}
	
	/**
	 * Removes a template variable from the context type.
	 * 
	 * @param resolver the varibable to be removed
	 */
	public void removeVariable(TemplateVariableResolver resolver) {
		fResolvers.remove(resolver.getType());
	}

	/**
	 * Removes all template variables from the context type.
	 */
	public void removeAllVariables() {
		fResolvers.clear();
	}

	/**
	 * Returns an iterator for the variables known to the context type.
	 * 
	 * @return an iterator over the variables in this context type
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
	 * Validates a pattern and returnes <code>null</code> if the validation was
	 * a success or an error message if not.
	 * 
	 * @param pattern the template pattern to validate
	 * @return the translated pattern if successful, or an error message if not TODO what do we return there? throw an exception
	 */
	public String validate(String pattern) {
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(pattern);
		if (buffer != null) {
			return validateVariables(buffer.getVariables());
		}
		return translator.getErrorMessage();
	}
	
	protected String validateVariables(TemplateVariable[] variables) {
		return null;
	}

	/**
	 * Resolves the variables in <code>buffer</code> withing <code>context</code>
	 * and edits the template buffer to reflect the resolved variables.
	 * 
	 * @param buffer the template buffer
	 * @param context the template context
	 * @throws MalformedTreeException if the positions in the buffer overlap
	 * @throws BadLocationException if the buffer cannot be successfully modified
	 */
	public void resolve(TemplateBuffer buffer, TemplateContext context) throws MalformedTreeException, BadLocationException {
		TemplateVariable[] variables= buffer.getVariables();

		List positions= variablesToPositions(variables);
		List edits= new ArrayList(5);

        // iterate over all variables and try to resolve them
        for (int i= 0; i != variables.length; i++) {
            TemplateVariable variable= variables[i];

			if (variable.isResolved())
				continue;			

			// remember old values
			int[] oldOffsets= variable.getOffsets();
			int oldLength= variable.getLength();
			
			String type= variable.getType();
			TemplateVariableResolver resolver= (TemplateVariableResolver) fResolvers.get(type);
			if (resolver != null)
				resolver.resolve(variable, context);
			
			if (variable.isResolved()) {
				String value= variable.getDefaultValue();
				
				// update buffer to reflect new value
				for (int k= 0; k != oldOffsets.length; k++)
					edits.add(new ReplaceEdit(oldOffsets[k], oldLength, value));
			}
			
        }

    	IDocument document= new Document(buffer.getString());
        MultiTextEdit edit= new MultiTextEdit(0, document.getLength());
        edit.addChildren((TextEdit[]) positions.toArray(new TextEdit[positions.size()]));
        edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
        edit.apply(document, TextEdit.UPDATE_REGIONS);

		positionsToVariables(positions, variables);
        
        buffer.setContent(document.get(), variables);
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
