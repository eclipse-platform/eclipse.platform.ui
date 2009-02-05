package org.eclipse.e4.core.services;

/**
 * TODO replace with IExecutor from org.eclipse.equinox.concurrent
 */
public interface IBackgroundRunner {

	public void schedule(long delay, String name, IRunnableWithProgress runnable);
}
