/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.json.internal;

/**
 * Parser constants for the Json TipProvider.Tip[] Structure
 *
 * <pre>
 * {
 *   "provider": {
 *     "image": "data:image/png;base64,iVBORw0K ... y543==",
 *     "description": "TipProvider Description",
 *     "expression": "&lt;eclipse core expression&gt;",
 *     "tips": [
 *       {
 *         "subject": "Tip Subject",
 *         "date": "YYYY-MM-DD",
 *         "image": "data:image/png;base64,iVBORw0KGgo ... CYII=",
 *         "ratio": 1.5,
 *         "maxHeight": 300,
 *         "maxWidth": 200,
 *         "url": "url or html, not both",
 *         "html": "html or url, not both"
 *     }]
 *   }
 * }
 * </pre>
 */
public class JsonConstants {

	/**
	 * Provider description field
	 */
	public static final String P_DESCRIPTION = "description";

	/**
	 * Provider core expression field
	 */
	public static final String P_EXPRESSION = "expression";

	/**
	 * Provider image field
	 */
	public static final String P_IMAGE = "image";

	/**
	 * Provider provider field
	 */
	public static final String P_PROVIDER = "provider";

	/**
	 * The provider tips array
	 */
	public static final String P_TIPS = "tips";

	/**
	 * Tip date field
	 */
	public static final String T_DATE = "date";

	/**
	 * Tip HTML
	 */
	public static final String T_HTML = "html";

	/**
	 * Tip image field
	 */
	public static final String T_IMAGE = "image";

	/**
	 * Tip image maximum height (int)
	 */
	public static final String T_MAXHEIGHT = "maxHeight";

	/**
	 * Tip image maximum width (int)
	 */
	public static final String T_MAXWIDTH = "maxWidth";

	/**
	 * Tip image ratio field (double)
	 */
	public static final String T_RATIO = "ratio";

	/**
	 * Tip subject field
	 */
	public static final String T_SUBJECT = "subject";

	/**
	 * Tip URL
	 */
	public static final String T_URL = "url";

	/**
	 * Tip variables
	 */
	public static final String T_VARIABLES = "variables";
}