package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.PlatformUI;

/**
 * The <code>SystemSummaryEditorInputFactory</code> creates
 * <code>SystemSummaryEditorInput</code> objects.
 */
public class SystemSummaryEditorInputFactory implements IElementFactory {
	/*
	 * The ID of the factory that creates this input.
	 */
	static final String FACTORY_ID = PlatformUI.PLUGIN_ID + ".SystemSummaryEditorInputFactory"; //$NON-NLS-1$

	/**
	 * Creates the factory, should not be called
	 */
	public SystemSummaryEditorInputFactory() {
		super();
	}
	
	/**
	 * @see org.eclipse.ui.IElementFactory#createElement(IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		return new SystemSummaryEditorInput();
	}
}
