/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Weirig <alex.weirig@telewalfer.lu> - Collapse All (https://bugs.eclipse.org/bugs/show_bug.cgi?id=65268)
 *******************************************************************************/
package org.eclipse.ui.editors.text;


/**
 * Command IDs for folding commands.
 * <p>
 * This interface contains constants only; it is not intended to be
 * implemented.
 * </p>
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFoldingCommandIds {

	/**
	 * Identifier of the "collapse folded region" command.
	 * <p>
	 * Value: {@value}.</p>
	 */
	String FOLDING_COLLAPSE= "org.eclipse.ui.edit.text.folding.collapse"; //$NON-NLS-1$

	/**
	 * Identifier of the "expand folded region" command.
	 * <p>
	 * Value: {@value}.</p>
	 */
	String FOLDING_EXPAND= "org.eclipse.ui.edit.text.folding.expand"; //$NON-NLS-1$

	/**
	 * Identifier of the "expand all folded regions" command.
	 * <p>
	 * Value: {@value}.</p>
	 */
	String FOLDING_EXPAND_ALL= "org.eclipse.ui.edit.text.folding.expand_all"; //$NON-NLS-1$

	/**
	 * Identifier of the "toggle folding" command.
	 * <p>
	 * Value: {@value}.</p>
	 */
	String FOLDING_TOGGLE= "org.eclipse.ui.edit.text.folding.toggle"; //$NON-NLS-1$

	/**
	 * Identifier of the "collapse all folded regions" command.
	 * <p>
	 * Value: {@value}.</p>
	 *
	 * @since 3.2
	 */
	String FOLDING_COLLAPSE_ALL = "org.eclipse.ui.edit.text.folding.collapse_all"; //$NON-NLS-1$

	/**
	 * Identifier of the "restore folding structure" command.
	 * <p>
	 * Value: {@value}.</p>
	 *
	 * @since 3.2
	 */
	String FOLDING_RESTORE= "org.eclipse.ui.edit.text.folding.restore"; //$NON-NLS-1$

}
