package org.eclipse.jface.text;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @since 3.29
 */
public class Activator implements BundleActivator {
	/**
	 * The identifier of the descriptor of this plugin in plugin.xml.
	 */
	public static final String ID= "org.eclipse.jface.text"; //$NON-NLS-1$

	private static Activator activator;

	private ExecutorService executor;

	@Override
	public void start(BundleContext context) {
		activator= this;
	}

	@Override
	public void stop(BundleContext context) {
		activator= null;
		if (executor != null) {
			executor.shutdownNow();
			executor= null;
		}
	}

	public static ExecutorService getExecutor() {
		activator.createExecutor();
		return activator.executor;
	}

	private void createExecutor() {
		if (activator.executor != null) {
			return;
		}

		executor= new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors(),
				3L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(),
				new ThreadFactory() {
					AtomicInteger count= new AtomicInteger(1);

					@Override
					public Thread newThread(Runnable r) {
						// Name the threads numerically for better debugging.
						Thread t= new Thread(r, ID + "-worker-" + count.getAndIncrement()); //$NON-NLS-1$

						// No need to keep the JVM running just because of the completion proposals
						t.setDaemon(true);
						return t;
					}
				});
	}
}
