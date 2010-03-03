package org.eclipse.e4.internal.core.services.bundle;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;

public class CompositeContextStrategy implements ISchedulerStrategy, ILookupStrategy, IDisposable {

	private ILookupStrategy lookupStrategy;
	private ISchedulerStrategy schedulerStrategy;

	public CompositeContextStrategy(ISchedulerStrategy schedulerStrategy,
			ILookupStrategy lookupStrategy) {
		this.schedulerStrategy = schedulerStrategy;
		this.lookupStrategy = lookupStrategy;
	}

	public Object lookup(String name, IEclipseContext context) {
		return lookupStrategy.lookup(name, context);
	}

	public boolean containsKey(String name, IEclipseContext context) {
		return lookupStrategy.containsKey(name, context);
	}

	public void schedule(Runnable runnable) {
		schedulerStrategy.schedule(runnable);
	}

	public boolean schedule(IRunAndTrack runnable, ContextChangeEvent event) {
		return schedulerStrategy.schedule(runnable, event);
	}

	public void dispose() {
		if (schedulerStrategy instanceof IDisposable) {
			((IDisposable) schedulerStrategy).dispose();
		}
		schedulerStrategy = null;
		if (lookupStrategy instanceof IDisposable) {
			((IDisposable) lookupStrategy).dispose();
		}
		lookupStrategy = null;
	}

}
