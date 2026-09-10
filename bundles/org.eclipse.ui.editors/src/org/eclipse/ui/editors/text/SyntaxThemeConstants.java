/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel (vogella GmbH) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

/**
 * Theme color ids for syntax coloring. Editors should resolve them against the
 * workbench {@code ColorRegistry} instead of defining colors of their own, so
 * that editors follow the active theme consistently.
 * <p>
 * For the color of unstyled text use the editor foreground color instead.
 * </p>
 *
 * @since 3.23
 */
public final class SyntaxThemeConstants {

	/** Color of element and tag names. */
	public static final String TAG_COLOR= "org.eclipse.editors.syntax.tagColor"; //$NON-NLS-1$

	/** Color of attribute names. */
	public static final String ATTRIBUTE_NAME_COLOR= "org.eclipse.editors.syntax.attributeNameColor"; //$NON-NLS-1$

	/** Color of quoted values, for example attribute values and string literals. */
	public static final String STRING_COLOR= "org.eclipse.editors.syntax.stringColor"; //$NON-NLS-1$

	/** Color of comments. */
	public static final String COMMENT_COLOR= "org.eclipse.editors.syntax.commentColor"; //$NON-NLS-1$

	/**
	 * Color of directives, for example an XML declaration, a document type
	 * declaration, a preprocessor statement or a shebang line.
	 */
	public static final String DIRECTIVE_COLOR= "org.eclipse.editors.syntax.directiveColor"; //$NON-NLS-1$

	private SyntaxThemeConstants() {
	}
}
