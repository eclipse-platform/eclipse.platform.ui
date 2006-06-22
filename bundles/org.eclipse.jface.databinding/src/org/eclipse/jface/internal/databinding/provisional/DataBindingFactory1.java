/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional;

import org.eclipse.jface.internal.databinding.provisional.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.NestedObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersObservableFactory;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;


/**
 * A data binding context factory that configures the data binding context with
 * all the default factories used by version 1.0 of the JFace data binding
 * framework.  Once version 1.0 ship, the order and behavior of these factories
 * will be frozen so that clients can depend on this class's behavior.
 * <p>
 * If you need to add your own factories in addition to or instead of the default
 * ones, this class may be subclassed by overriding the
 * {@link #addObservableFactories(IObservableFactory[])},
 * {@link #addBindSupportFactories(BindSupportFactory[])}, and
 * {@link #addBindingFactories(IBindingFactory[])} methods, changing the
 * contents and/or order of the factories, and then delegating to super().
 *  
 * @since 3.3
 */
public class DataBindingFactory1 extends	AbstractDataBindingContextFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.AbstractDataBindingContextFactory#createContext()
	 */
	public void configureContext(DataBindingContext context) {
		addObservableFactories(new IObservableFactory[] {
				new NestedObservableFactory(context),
				new BeanObservableFactory(context, null, new Class[] { Widget.class }),
				new SWTObservableFactory(),
				new ViewersObservableFactory(),
				new DefaultObservableFactory(context)
		});
		addBindSupportFactories(new BindSupportFactory[] {
				new DefaultBindSupportFactory()
		});
		addBindingFactories(new IBindingFactory[] {
				new DefaultBindingFactory(),
				new ViewersBindingFactory()
		});
	}

	/**
	 * Creates, configures, and returns a new data binding context.
	 * 
	 * @param parentComposite
	 *            when parentComposite is disposed, it will automatically
	 *            dispose the DataBindingContext.
	 * @return DataBindingContext a configured data binding context.
	 */
	public DataBindingContext createContext(Composite parentComposite) {
		final DataBindingContext result = createContext();
		
		parentComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				result.dispose();
			}
		});
		
		return result;
	}
}
