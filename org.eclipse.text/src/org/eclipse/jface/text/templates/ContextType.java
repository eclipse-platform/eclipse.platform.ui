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
public class ContextType implements ITemplateEditor {

	/** name of the context type */
	private final String fName;

	/** variables used by this content type */
	private final Map fVariables= new HashMap();

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
	 * Adds a template variable to the context type. If there already is a variable
	 * with the same name, the previous one gets replaced by <code>variable</code>.
	 * 
	 * @param variable the variable to be added under its name
	 */
	public void addVariable(TemplateVariable variable) {
		fVariables.put(variable.getName(), variable);   
	}
	
	/**
	 * Removes a template variable from the context type.
	 * 
	 * @param variable the varibable to be removed
	 */
	public void removeVariable(TemplateVariable variable) {
		fVariables.remove(variable.getName());
	}

	/**
	 * Removes all template variables from the context type.
	 */
	public void removeAllVariables() {
		fVariables.clear();
	}

	/**
	 * Returns an iterator for the variables known to the context type.
	 * 
	 * @return an iterator over the variables in this context type
	 */
	public Iterator variableIterator() {
	 	return Collections.unmodifiableMap(fVariables).values().iterator();   
	}
	
	/**
	 * Returns the variable with the given name
	 */
	protected TemplateVariable getVariable(String name) {
		return (TemplateVariable) fVariables.get(name);
	}	

	/**
	 * Validates a pattern and returnes <code>null</code> if the validation was
	 * a success or an error message if not.
	 * 
	 * @param pattern the template pattern to validate
	 * @return the translated pattern if successful, or an error message if not
	 */
	public String validate(String pattern) {
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(pattern);
		if (buffer != null) {
			return validateVariables(buffer.getVariables());
		}
		return translator.getErrorMessage();
	}
	
	protected String validateVariables(TemplatePosition[] variables) {
		return null;
	}

    /*
     * @see ITemplateEditor#edit(TemplateBuffer)
     */
    public void edit(TemplateBuffer templateBuffer, TemplateContext context) throws BadLocationException {
    	IDocument document= new Document(templateBuffer.getString());
		TemplatePosition[] variables= templateBuffer.getVariables();

		List positions= variablesToPositions(variables);
		List edits= new ArrayList(5);

        // iterate over all variables and try to resolve them
        for (int i= 0; i != variables.length; i++) {
            TemplatePosition variable= variables[i];

			if (variable.isResolved())
				continue;			

			String name= variable.getName();
			int[] offsets= variable.getOffsets();
			int length= variable.getLength();
			
			TemplateVariable evaluator= (TemplateVariable) fVariables.get(name);
			String value= (evaluator == null)
				? null
				: evaluator.resolve(context);
			
			if (value == null)
				continue;

			variable.setLength(value.length());
			variable.setResolved(evaluator.isResolved(context));

        	for (int k= 0; k != offsets.length; k++)
				edits.add(new ReplaceEdit(offsets[k], length, value));
        }

        MultiTextEdit edit= new MultiTextEdit(0, document.getLength());
        edit.addChildren((TextEdit[]) positions.toArray(new TextEdit[positions.size()]));
        edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
        edit.apply(document, TextEdit.UPDATE_REGIONS);

		positionsToVariables(positions, variables);
        
        templateBuffer.setContent(document.get(), variables);
    }

	private static List variablesToPositions(TemplatePosition[] variables) {
   		List positions= new ArrayList(5);
		for (int i= 0; i != variables.length; i++) {
		    int[] offsets= variables[i].getOffsets();
		    for (int j= 0; j != offsets.length; j++)
				positions.add(new RangeMarker(offsets[j], 0));
		}
		
		return positions;
	}
	
	private static void positionsToVariables(List positions, TemplatePosition[] variables) {
		Iterator iterator= positions.iterator();
		
		for (int i= 0; i != variables.length; i++) {
		    TemplatePosition variable= variables[i];
		    
			int[] offsets= new int[variable.getOffsets().length];
			for (int j= 0; j != offsets.length; j++)
				offsets[j]= ((TextEdit) iterator.next()).getOffset();
			
		 	variable.setOffsets(offsets);   
		}
	}

}
