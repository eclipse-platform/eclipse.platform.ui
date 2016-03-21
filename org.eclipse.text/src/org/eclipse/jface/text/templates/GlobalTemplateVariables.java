/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids: sdavids@gmx.de - see bug 25376
 *     Jeremie Bresson <jbr@bsiag.com> - Allow to specify format for date variable - https://bugs.eclipse.org/75981
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486903, 487327, 487901
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import java.util.List;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

/**
 * Global variables which are available in any context.
 * <p>
 * Clients may instantiate the classes contained within this class.
 * </p>
 *
 * @since 3.0
 */
public class GlobalTemplateVariables {

	/** The type of the selection variables. */
	public static final String SELECTION= "selection"; //$NON-NLS-1$

	/**
	 * The cursor variable determines the cursor placement after template edition.
	 */
	public static class Cursor extends SimpleTemplateVariableResolver {

		/** Name of the cursor variable, value= {@value} */
		public static final String NAME= "cursor"; //$NON-NLS-1$

		/**
		 * Creates a new cursor variable
		 */
		public Cursor() {
			super(NAME, TextTemplateMessages.getString("GlobalVariables.variable.description.cursor")); //$NON-NLS-1$
			setEvaluationString(""); //$NON-NLS-1$
		}
	}

	/**
	 * The selection variable determines templates that work on a selection.
	 * 
	 * @since 3.6
	 */
	public static class Selection extends SimpleTemplateVariableResolver {

		/**
		 * Creates a word selection variable.
		 * 
		 * @param name the name of the variable
		 * @param description the description of the variable
		 */
		public Selection(String name, String description) {
			super(name, description);
		}

		@Override
		protected String resolve(TemplateContext context) {
			String selection= context.getVariable(SELECTION);
			if (selection == null)
				return ""; //$NON-NLS-1$
			return selection;
		}

		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			List<String> params= variable.getVariableType().getParams();
			if (params.size() >= 1 && params.get(0) != null) {
				resolveWithParams(variable, context, params);
			} else {
				// No parameter, use default:
				super.resolve(variable, context);
			}
		}

		private void resolveWithParams(TemplateVariable variable, TemplateContext context, List<String> params) {
			String selection= context.getVariable(SELECTION);
			if (selection != null && !selection.isEmpty()) {
				variable.setValue(selection);
			} else {
				String defaultValue= params.get(0);
				variable.setValue(defaultValue);
			}
			variable.setUnambiguous(true);
			variable.setResolved(true);
		}
	}

	/**
	 * The word selection variable determines templates that work on selected words, but not on
	 * selected lines.
	 */
	public static class WordSelection extends Selection {

		/** Name of the word selection variable, value= {@value} */
		public static final String NAME= "word_selection"; //$NON-NLS-1$

		/**
		 * Creates a new word selection variable
		 */
		public WordSelection() {
			super(NAME, TextTemplateMessages.getString("GlobalVariables.variable.description.selectedWord")); //$NON-NLS-1$
		}
	}

	/**
	 * The line selection variable determines templates that work on selected
	 * lines.
	 */
	public static class LineSelection extends Selection {

		/** Name of the line selection variable, value= {@value} */
		public static final String NAME= "line_selection"; //$NON-NLS-1$

		/**
		 * Creates a new line selection variable
		 */
		public LineSelection() {
			super(NAME, TextTemplateMessages.getString("GlobalVariables.variable.description.selectedLines")); //$NON-NLS-1$
		}
	}

	/**
	 * The dollar variable inserts an escaped dollar symbol.
	 */
	public static class Dollar extends SimpleTemplateVariableResolver {
		/**
		 * Creates a new dollar variable
		 */
		public Dollar() {
			super("dollar", TextTemplateMessages.getString("GlobalVariables.variable.description.dollar")); //$NON-NLS-1$ //$NON-NLS-2$
			setEvaluationString("$"); //$NON-NLS-1$
		}
	}

	/**
	 * The date variable evaluates to the current date. This supports a <code>pattern</code> and a
	 * <code>locale</code> as optional parameters. <code>pattern</code> is a pattern compatible with
	 * {@link SimpleDateFormat}. <code>locale</code> is a string representation of the locale
	 * compatible with the constructor parameter {@link ULocale#ULocale(String)}.
	 */
	public static class Date extends SimpleTemplateVariableResolver {
		/**
		 * Creates a new date variable
		 */
		public Date() {
			super("date", TextTemplateMessages.getString("GlobalVariables.variable.description.date")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			List<String> params= variable.getVariableType().getParams();
			if (params.size() >= 1 && params.get(0) != null) {
				resolveWithParams(variable, context, params);
			} else {
				// No parameter, use default format:
				super.resolve(variable, context);
			}
		}

		private void resolveWithParams(TemplateVariable variable, TemplateContext context, List<String> params) {
			try {
				// There is a least one parameter (params.get(0) is not null), set the format depending on second parameter:
				DateFormat format;
				if (params.size() >= 2 && params.get(1) != null) {
					format= new SimpleDateFormat(params.get(0), new ULocale(params.get(1)));
				} else {
					format= new SimpleDateFormat(params.get(0));
				}

				variable.setValue(format.format(new java.util.Date()));
				variable.setUnambiguous(true);
				variable.setResolved(true);
			} catch (IllegalArgumentException e) {
				// Date formating did not work, use default format instead:
				super.resolve(variable, context);
			}
		}

		@Override
		protected String resolve(TemplateContext context) {
			return DateFormat.getDateInstance().format(new java.util.Date());
		}
	}

	/**
	 * The year variable evaluates to the current year.
	 */
	public static class Year extends SimpleTemplateVariableResolver {
		/**
		 * Creates a new year variable
		 */
		public Year() {
			super("year", TextTemplateMessages.getString("GlobalVariables.variable.description.year")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		@Override
		protected String resolve(TemplateContext context) {
			return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		}
	}

	/**
	 * The time variable evaluates to the current time.
	 */
	public static class Time extends SimpleTemplateVariableResolver {
		/**
		 * Creates a new time variable
		 */
		public Time() {
			super("time", TextTemplateMessages.getString("GlobalVariables.variable.description.time")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		protected String resolve(TemplateContext context) {
			return DateFormat.getTimeInstance().format(new java.util.Date());
		}
	}

	/**
	 * The user variable evaluates to the current user.
	 */
	public static class User extends SimpleTemplateVariableResolver {
		/**
		 * Creates a new user name variable
		 */
		public User() {
			super("user", TextTemplateMessages.getString("GlobalVariables.variable.description.user")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		protected String resolve(TemplateContext context) {
			return System.getProperty("user.name"); //$NON-NLS-1$
		}
	}
}
