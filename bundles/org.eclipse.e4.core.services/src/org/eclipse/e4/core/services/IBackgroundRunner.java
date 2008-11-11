package org.eclipse.e4.core.services;

public interface IBackgroundRunner {

	public void schedule(long delay, String name, IRunnableWithProgress runnable);
}
