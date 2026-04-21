package org.eclipse.jface.text;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 3.29
 */
public class Activator {
	/**
	 * The identifier of the descriptor of this plugin in plugin.xml.
	 */
	public static final String ID= "org.eclipse.jface.text"; //$NON-NLS-1$

	private Activator() {
	}

	public static ExecutorService getExecutor() {
		return Holder.EXECUTOR;
	}

	private static final class Holder {
		static final ExecutorService EXECUTOR= new ThreadPoolExecutor(
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
