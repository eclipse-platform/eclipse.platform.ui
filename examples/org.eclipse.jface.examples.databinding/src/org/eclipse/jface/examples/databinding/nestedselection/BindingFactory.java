package org.eclipse.jface.examples.databinding.nestedselection;

import org.eclipse.jface.internal.databinding.api.DataBinding;
import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.IObservableFactory;
import org.eclipse.jface.internal.databinding.api.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.api.beans.NestedObservableFactory;
import org.eclipse.jface.internal.databinding.api.swt.SWTObservableFactory;
import org.eclipse.jface.internal.databinding.api.viewers.ViewersObservableFactory;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * An example application-level data binding factory implementation. This should
 * be copied into your application and be modified to include the specific
 * updatable factories your application needs in the order it needs them.
 * <p>
 * Note that the search order for IObservableFactory implementations is last to
 * first.
 * </p>
 * 
 * @since 3.2
 */
public class BindingFactory {

	/**
	 * Creates a data binding context whose lifecycle is bound to an SWT
	 * control, and which supports binding to SWT controls, JFace viewers, and
	 * POJO model objects with JavaBeans-style notification.
	 * <p>
	 * This method is a convenience method; its implementation is equivalent to
	 * calling {@link DataBinding#createContext(Control, IObservableFactory[]) }
	 * where the array of factories consists of a
	 * {@link NestedObservableFactory}, a {@link BeanObservableFactory}
	 * instance, a {@link SWTObservableFactory}, and a
	 * {@link ViewersObservableFactory}.
	 * </p>
	 * 
	 * @param control
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(Control control) {
		final IDataBindingContext context = DataBinding
				.createContext(new IObservableFactory[] {
						new NestedObservableFactory(),
						new BeanObservableFactory(),
						new SWTObservableFactory(),
						new ViewersObservableFactory() });
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
	 * calling {@link DataBinding#createContext(Control, IObservableFactory[]) }
	 * where the array of factories consists of a
	 * {@link NestedObservableFactory}, a {@link BeanObservableFactory}
	 * instance, a {@link SWTObservableFactory}, and a
	 * {@link ViewersObservableFactory}.
	 * </p>
	 * 
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext() {
		return DataBinding.createContext(new IObservableFactory[] {
				new NestedObservableFactory(), new BeanObservableFactory(),
				new SWTObservableFactory(), new ViewersObservableFactory() });
	}
}
