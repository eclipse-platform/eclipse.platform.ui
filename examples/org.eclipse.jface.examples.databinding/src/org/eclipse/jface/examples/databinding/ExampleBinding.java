package org.eclipse.jface.examples.databinding;

import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.NestedUpdatableFactory;
import org.eclipse.jface.databinding.beans.BeanUpdatableFactory;
import org.eclipse.jface.databinding.swt.SWTUpdatableFactory;
import org.eclipse.jface.databinding.viewers.ViewersUpdatableFactory;
import org.eclipse.swt.widgets.Control;

/**
 * An example application-level data binding factory implementation. This should
 * be copied into your application and be modified to include the specific
 * updatable factories your application needs in the order it needs them.
 * <p>
 * Note that the search order for IUpdatableFactory implementations is last to
 * first.
 * </p>
 * 
 * @since 3.2
 */
public class ExampleBinding {

	/**
	 * Creates a data binding context whose lifecycle is bound to an SWT
	 * control, and which supports binding to SWT controls, JFace viewers, and
	 * POJO model objects with JavaBeans-style notification.
	 * <p>
	 * This method is a convenience method; its implementation is equivalent to
	 * calling {@link DataBinding#createContext(Control, IUpdatableFactory[]) }
	 * where the array of factories consists of a {@link BeanUpdatableFactory}
	 * instance, a {@link SWTUpdatableFactory}, and a
	 * {@link ViewersUpdatableFactory}.
	 * </p>
	 * 
	 * @param control
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(Control control) {
		return DataBinding.createContext(control, new IUpdatableFactory[] {
				new NestedUpdatableFactory(), new BeanUpdatableFactory(),
				new SWTUpdatableFactory(), new ViewersUpdatableFactory() });
	}

	/**
	 * Creates a data binding context which supports binding to SWT controls,
	 * JFace viewers, and POJO model objects with JavaBeans-style notification.
	 * This data binding context's life cycle is not bound to the dispose event
	 * of any SWT control. Consequently, the programmer is responsible to
	 * manually dispose any IUpdatables created using this data binding context
	 * as necessary.
	 * <p>
	 * This method is a convenience method; its implementation is equivalent to
	 * calling {@link DataBinding#createContext(Control, IUpdatableFactory[]) }
	 * where the array of factories consists of a {@link BeanUpdatableFactory}
	 * instance, a {@link SWTUpdatableFactory}, and a
	 * {@link ViewersUpdatableFactory}.
	 * </p>
	 * 
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext() {
		return DataBinding.createContext(new IUpdatableFactory[] {
				new BeanUpdatableFactory(), new SWTUpdatableFactory(),
				new ViewersUpdatableFactory() });
	}
}
