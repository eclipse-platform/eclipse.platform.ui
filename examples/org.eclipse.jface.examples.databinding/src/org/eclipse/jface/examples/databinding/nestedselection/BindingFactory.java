/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.nestedselection;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.NestedObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersObservableFactory;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * An example application-level data binding factory implementation. This should
 * be copied into your application and be modified to include the specific
 * updatable factories your application needs in the order it needs them.
 * <p>
 * Note that the search order for IObservableFactory implementations is last to
 * first.
 * </p>
 * 
 * @since 1.0
 */
public class BindingFactory {

	/**
	 * Creates a data binding context whose lifecycle is bound to an SWT
	 * control, and which supports binding to SWT controls, JFace viewers, and
	 * POJO model objects with JavaBeans-style notification.
	 * <p>
	 * This method is a convenience method; its implementation is equivalent to
	 * calling
	 * {@link DataBindingContext#createContext(Control, IObservableFactory[]) }
	 * where the array of factories consists of a
	 * {@link NestedObservableFactory}, a {@link BeanObservableFactory}
	 * instance, a {@link SWTObservableFactory}, and a
	 * {@link ViewersObservableFactory}.
	 * </p>
	 * 
	 * @param control
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(Control control) {
		final DataBindingContext context = createContext();
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				context.dispose();
			}
		});
		return context;
	}

	/**
	 * Creates a data binding context which supports binding to SWT controls,
	 * JFace viewers, and POJO model objects with JavaBeans-style notification.
	 * This data binding context's life cycle is not bound to the dispose event
	 * of any SWT control. Consequently, the programmer is responsible to
	 * manually dispose any IObservables created using this data binding context
	 * as necessary.
	 * <p>
	 * This method is a convenience method; its implementation is equivalent to
	 * calling
	 * {@link DataBindingContext#createContext(Control, IObservableFactory[]) }
	 * where the array of factories consists of a
	 * {@link NestedObservableFactory}, a {@link BeanObservableFactory}
	 * instance, a {@link SWTObservableFactory}, and a
	 * {@link ViewersObservableFactory}.
	 * </p>
	 * 
	 * @return a data binding context
	 */
	public static DataBindingContext createContext() {
		DataBindingContext context = new DataBindingContext();
		context.addObservableFactory(new NestedObservableFactory(context));
		context.addObservableFactory(new BeanObservableFactory(context, null,
				new Class[] { Widget.class }));
		context.addObservableFactory(new SWTObservableFactory());
		context.addObservableFactory(new ViewersObservableFactory());
		context.addBindSupportFactory(new DefaultBindSupportFactory());
		context.addBindingFactory(new DefaultBindingFactory());
		context.addBindingFactory(new ViewersBindingFactory());
		return context;
	}
}
