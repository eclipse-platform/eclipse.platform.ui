package org.eclipse.jface.databinding.swt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadable;
import org.eclipse.jface.databinding.UpdatableTracker;
import org.eclipse.jface.internal.databinding.swt.SWTUtil;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Control;

/**
 * A ControlUpdator updates an SWT control in response to changes in the model.
 * By wrapping a block of code in a ControlUpdator, clients can rely on the fact
 * that the block of code will be re-executed whenever anything changes in the
 * model that might affect its behavior.
 *  
 * <p>
 * ControlUpdators only execute when their controls are visible. If something changes
 * in the model while the control is invisible, the updator is flagged as dirty and
 * the updator stops listening to the model until the next time the control repaints.
 * This saves CPU cycles by deferring UI updates to widgets that are currently invisible.
 * </p>
 * 
 * <p>
 * Clients should subclass this when copying information from the model to
 * a control. Typical usage:
 * </p>
 * 
 * <ul>
 * <li>Override updateControl. It should do whatever is necessary to display
 *     the contents of the model in the control.</li>
 * <li>In the constructor, attach listeners to the model. The listeners should 
 *     call markDirty whenever anything changes in the model that affects 
 *     updateControl. Note: this step can be omitted when calling any method
 *     tagged with "@TrackedGetter" since ControlUpdator will automatically attach
 *     a listener to any object if a "@TrackedGetter" method is called in
 *     updateControl.</li>
 * <li>(optional)Extend dispose() to remove any listeners attached in the constructor</li>
 * </ul>
 * 
 * <p>
 * Example:
 * </p>
 * 
 * <code>
 * // Displays an updatable value in a label and keeps the label in synch with changes
 * // in the value.
 * IReadableValue someValue = ...
 * final Label myLabel = new Label(parent, SWT.NONE);
 * new ControlUpdator(myLabel) {
 * 		protected void updateControl() {
 * 		   myLabel.setText(someValue.getValue().toString);
 *      }
 * }
 * // myLabel will display the value of someValue the next time it repaints, and will automatically
 * // be updated whenever someValue changes and the label is visible
 * </code>
 * 
 * @since 3.2
 */
public abstract class ControlUpdator {
	
	private class PrivateInterface implements PaintListener, 
		DisposeListener, Runnable, IChangeListener {
		
		// PaintListener implementation
		public void paintControl(PaintEvent e) {
			updateIfNecessary();
		}

		// DisposeListener implementation
		public void widgetDisposed(DisposeEvent e) {
			ControlUpdator.this.dispose();
		}
		
		// Runnable implementation. This method runs at most once per repaint whenever the
		// value gets marked as dirty.
		public void run() {
			if (theControl != null && !theControl.isDisposed() && theControl.isVisible()) {
				updateIfNecessary();
			}
		}
		
		// IChangeListener implementation (listening to the ComputedValue)
		public void handleChange(ChangeEvent changeEvent) {
			// Whenever this updator becomes dirty, schedule the run() method 
			if (changeEvent.getChangeType() != ChangeEvent.VERIFY) {
				makeDirty();
			}
		}
		
	}
	
	private Runnable updateRunnable = new Runnable() {
		public void run() {
			updateControl();
		}
	};
	
	private PrivateInterface privateInterface = new PrivateInterface();
	private Control theControl;
	private Collection dependencies = new ArrayList();
	private boolean dirty = false;
	
	/**
	 * Creates an updator for the given control.  
	 * 
	 * @param toUpdate control to update
	 */
	public ControlUpdator(Control toUpdate) {
		theControl = toUpdate;
		
		theControl.addDisposeListener(privateInterface);
		theControl.addPaintListener(privateInterface);
		makeDirty();
	}
	
	private void updateIfNecessary() {
		if (dirty) {
			Set newDependencies = UpdatableTracker.runAndMonitor(updateRunnable);
			
			for (Iterator iter = newDependencies.iterator(); iter.hasNext();) {
				IReadable next = (IReadable) iter.next();
				
				// Add a change listener to the new dependency.
				next.addChangeListener(privateInterface);				
			}
			
			dependencies = newDependencies;
			dirty = false;
		}
	}

	/**
	 * This is called automatically when the control is disposed. It may also
	 * be called explicitly to remove this updator from the control. Subclasses
	 * will normally extend this method to detach any listeners they attached
	 * in their constructor.
	 */
	public void dispose() {
		theControl.removeDisposeListener(privateInterface);
		theControl.removePaintListener(privateInterface);

		stopListening();
	}

	private void stopListening() {
		// Stop listening for dependency changes
		IReadable[] updatables = (IReadable[]) dependencies.toArray(new IReadable[dependencies.size()]);
		dependencies.clear();
		
		for (int i = 0; i < updatables.length; i++) {
			IReadable readable = updatables[i];
				
			readable.removeChangeListener(privateInterface);
		}
	}

	/**
	 * Updates the control. This method will be invoked once after the
	 * updator is created, and once before any repaint during which the 
	 * control is visible and dirty.
	 *  
	 * <p>
	 * Subclasses should overload this method to provide any code that 
	 * changes the appearance of the widget.
	 * </p>
	 */
	protected abstract void updateControl();
	
	/**
	 * Marks this updator as dirty. Causes the updateControl method to
	 * be invoked before the next time the control is repainted.
	 */
	protected final void makeDirty() {
		if (!dirty) {
			dirty = true;
			stopListening();
			SWTUtil.runOnce(theControl.getDisplay(), privateInterface);
		}
	}
	
}
