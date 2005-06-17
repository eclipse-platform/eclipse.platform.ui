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
package org.eclipse.jface.text;

/**
 * Extension interface for {@link org.eclipse.jface.text.IInformationControlCreator}<p>
 * Introduces tests whether information controls can be reused and whether information
 * control creators can replace each other.
 *
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.IInformationControl
 * @since 3.0
 */
public interface IInformationControlCreatorExtension {

	/**
	 * Tests if an existing information control can be reused.
	 *
	 * @param control the information control to test
	 * @return <code>true</code> if the control can be reused
	 */
	boolean canReuse(IInformationControl control);

	/**
	 * Tests whether this information control creator can replace the given
	 * information control creator. This is the case if the two creators create
	 * the same kind of information controls.
	 *
	 * @param creator the creator to be checked
	 * @return <code>true</code> if the given creator can be replaced,
	 *         <code>false</code> otherwise
	 */
	boolean canReplace(IInformationControlCreator creator);
}
