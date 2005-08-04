package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class monitors an SWT display and attempts to determine if the display
 * is currently reacting to user input. This can be used to disable certain
 * APIs (for example, APIs that steal focus or change the workbench layout)
 * when they are called from an *syncExec.
 * 
 * @since 3.2
 */
public class InputMonitor {
	private Display display;
	
	private int[] eventIds = {
			SWT.KeyDown,
			SWT.KeyUp,
			SWT.MouseDown,
			SWT.MouseUp,
			SWT.MouseDoubleClick,
			SWT.Selection,
			SWT.MenuDetect,
			SWT.DragDetect
	};

	private boolean enabled = false;
	
	private Listener listener = new Listener() {
		public void handleEvent(Event event) {
			enableInput();
		}
	};

	private Runnable disabler = new Runnable() {
		public void run() {
			enabled = false;
		}
	};
	
	/**
	 * Returns true iff the Display for the current thread is currently
	 * processing a user input event. Must be called in the UI thread.
	 * 
	 * @return true iff the Display for the current thread is currently
	 * processing a user input event. 
	 */
	public static boolean isProcessingUserInput() {
		return getMonitor(Display.getCurrent()).checkIsProcessingUserInput();
	}
	
	/**
	 * Ensures that the input monitor for the current thread is initialized.
	 * Multiple calls to this method in the same thread are ignored.
	 */
	public static void init() {
		getMonitor(Display.getCurrent());
	}
	
	/**
	 * Returns a InputMonitor for the given display. Sucessive calls
	 * will return the same object instance.
	 * 
	 * @param d
	 * @return
	 */
	public static InputMonitor getMonitor(Display d) {
		String key = InputMonitor.class.getName();
		Object mon = d.getData(key);
		
		if (mon != null && mon instanceof InputMonitor) {
			return (InputMonitor) mon;
		}
		
		InputMonitor newMonitor = new InputMonitor(d);
		
		d.setData(key, newMonitor);
		
		return newMonitor;
	}
	
	public InputMonitor(Display d) {
		this.display = d;
		
		for (int i = 0; i < eventIds.length; i++) {
			int id = eventIds[i];
			
			d.addFilter(id, listener );
		}
	}

	public boolean checkIsProcessingUserInput() {
		return enabled;
	}
	
	public void dispose() {
		for (int i = 0; i < eventIds.length; i++) {
			int id = eventIds[i];
			
			display.removeFilter(id, listener);
		}		
	}
	
	protected void enableInput() {
		if (enabled) return;
		
		enabled = true;
		display.asyncExec(disabler);
	}
}
