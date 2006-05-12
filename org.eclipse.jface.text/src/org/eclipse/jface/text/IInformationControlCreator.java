/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import org.eclipse.swt.widgets.Shell;


/**
 * Interface of a factory for information controls (
 * {@link org.eclipse.jface.text.IInformationControl}).
 *
 * In order to provide backward compatibility for clients of
 * <code>IInformationControlCreator</code>, extension interfaces are used as
 * a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.IInformationControlCreatorExtension} since
 *     version 3.0 introducing checks of whether existing information control can
 *     be reused and whether information control creators can replace each other.
 * </li>
 * </ul>
 *
 * @see org.eclipse.jface.text.IInformationControlCreatorExtension
 * @since 2.0
 */
public interface IInformationControlCreator {

	/**
	 * Creates a new information control with the given shell as the control's parent.
	 *
	 * @param parent the parent shell
	 * @return the created information control
	 */
	IInformationControl createInformationControl(Shell parent);
}

