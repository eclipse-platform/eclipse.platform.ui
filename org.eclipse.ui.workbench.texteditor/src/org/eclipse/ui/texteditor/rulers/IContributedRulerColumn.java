/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.rulers;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;

import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Interface that has to be implemented by contributions to the
 * <code>org.eclipse.ui.texteditor.rulerColumns</code> extension point.
 * <p>
 * Subclasses must have a zero-argument constructor so that they can be created
 * by {@link IConfigurationElement#createExecutableExtension(String)}.</p>
 * <p>
 * <em>This API is provisional and may change any time before the 3.3 API freeze.</em>
 * </p>
 * 
 * @since 3.3
 */
public interface IContributedRulerColumn extends IVerticalRulerColumn {

	/**
	 * Returns the extension point descriptor of this ruler.
	 * 
	 * @return descriptor the extension point descriptor of this ruler or <code>null</code> if called before {@link #columnCreated()}
	 */
	RulerColumnDescriptor getDescriptor();

	/**
	 * Sets the extension point descriptor of this ruler.
	 * 
	 * @param descriptor the extension point descriptor
	 */
	void setDescriptor(RulerColumnDescriptor descriptor);

	/**
	 * Sets the editor (called right after the extension was instantiated).
	 * 
	 * @param editor the editor targeted by this ruler instance
	 */
	void setEditor(ITextEditor editor);

	/**
	 * Returns the editor targeted by this ruler instance.
	 * 
	 * @return the editor targeted by this ruler instance or <code>null</code> if called before {@link #columnCreated()}
	 */
	ITextEditor getEditor();

	/**
	 * Hook method called after a column has been instantiated, but before it is
	 * added to a {@link CompositeRuler} and before
	 * {@linkplain IVerticalRulerColumn#createControl(CompositeRuler, Composite) createControl}
	 * is called.
	 * <p>
	 * This happens when
	 * <ul>
	 * <li>the column is set visible by the user or programmatically</li>
	 * <li>the editor is created, if this ruler targets the editor and is enabled by default</li>
	 * <li>the editor input changes and the column now targets the new editor contents.</li>
	 * </ul></p>
	 */
	void columnCreated();

	/**
	 * Hook method called after a column has been removed from the {@link CompositeRuler}.
	 * <p>
	 * This happens when  
	 * <ul>
	 * <li>the column is hidden by the user or programmatically</li>
	 * <li>the editor is closed</li>
	 * <li>the editor input changes and the column no longer targets the editor
	 * contents.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The column will not be used after this method has been called. A new
	 * column will be instantiated if the same column type should be shown for
	 * the same editor.
	 * </p>
	 */
	void columnRemoved();

}
