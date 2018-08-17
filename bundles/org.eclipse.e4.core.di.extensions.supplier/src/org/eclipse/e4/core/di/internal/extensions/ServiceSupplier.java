/*******************************************************************************
 * Copyright (c) 2014, 2018 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 513563
 *******************************************************************************/
package org.eclipse.e4.core.di.internal.extensions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

/**
 * Supplier for {@link Service}
 */
@Component(service = { ExtendedObjectSupplier.class, EventHandler.class }, property = {
		"dependency.injection.annotation=org.eclipse.e4.core.di.extensions.Service",
		"event.topics=" + IEclipseContext.TOPIC_DISPOSE })
public class ServiceSupplier extends ExtendedObjectSupplier implements EventHandler {

	LoggerFactory factory;
	Logger logger;

	static class ServiceHandler implements ServiceListener {
		private final ServiceSupplier supplier;
		final Set<IRequestor> requestors = new HashSet<>();
		private final BundleContext bundleContext;
		private final Class<?> serviceType;

		public ServiceHandler(ServiceSupplier supplier, BundleContext bundleContext, Class<?> serviceType) {
			this.supplier = supplier;
			this.bundleContext = bundleContext;
			this.serviceType = serviceType;
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			cleanup();

			synchronized (this.supplier) {
				String[] data = (String[]) event.getServiceReference().getProperty(Constants.OBJECTCLASS);
				for (String d : data) {
					if (this.serviceType.getName().equals(d)) {
						this.requestors.forEach( r -> {
							try {
								r.resolveArguments(false);
								r.execute();
							} catch (InjectionException e) {
								this.supplier.logError("Injection failed", e); //$NON-NLS-1$
							}
						});
						break;
					}
				}
			}
		}

		public void cleanup() {
			synchronized (this.supplier) {
				Predicate<IRequestor> pr = IRequestor::isValid;
				this.requestors.removeIf(pr.negate());

				if (this.requestors.isEmpty()) {
					Map<Class<?>, ServiceHandler> map = this.supplier.handlerList.get(this.bundleContext);
					if (map != null) {
						map.remove(this.serviceType);

						if (map.isEmpty()) {
							this.supplier.handlerList.remove(this.bundleContext);
						}
					}

					this.bundleContext.removeServiceListener(this);
					return;
				}
			}
		}
	}

	Map<BundleContext, Map<Class<?>, ServiceHandler>> handlerList = new ConcurrentHashMap<>();

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		Type desiredType = descriptor.getDesiredType();
		Bundle b = FrameworkUtil.getBundle(requestor.getRequestingObjectClass());
		Service qualifier = descriptor.getQualifier(Service.class);

		if (desiredType instanceof ParameterizedType) {
			ParameterizedType t = (ParameterizedType) desiredType;
			if (t.getRawType() == Collections.class || t.getRawType() == List.class) {

				return handleCollection(b, t.getActualTypeArguments()[0], requestor, track && qualifier.dynamic(), qualifier);
			}
		}

		return handleSingle(b, desiredType, requestor, descriptor, track && qualifier.dynamic(), qualifier);
	}

	private Object handleSingle(Bundle bundle, Type t, IRequestor requestor, IObjectDescriptor descriptor, boolean track, Service qualifier) {
		BundleContext context = bundle.getBundleContext();
		if (context == null) {
			context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		}

		@SuppressWarnings("unchecked")
		Class<Object> cl = t instanceof ParameterizedType ? (Class<Object>) ((ParameterizedType) t).getRawType() : (Class<Object>) t;
		Object result = IInjector.NOT_A_VALUE;
		try {
			String filter = qualifier.filterExpression() != null && !qualifier.filterExpression().isEmpty()
					? qualifier.filterExpression()
					: null;

			ServiceReference<?>[] serviceReferences = context.getServiceReferences(cl.getName(), filter);
			if (serviceReferences != null) {
				Arrays.sort(serviceReferences);

				if (serviceReferences.length > 0) {
					result = context.getService(serviceReferences[serviceReferences.length - 1]);
				}
			}
		} catch (InvalidSyntaxException e) {
			logError("Invalid filter expression", e); //$NON-NLS-1$
		}

		if (track) {
			trackService(context, cl, requestor);
		}

		return result;
	}

	private List<Object> handleCollection(Bundle bundle, Type t, IRequestor requestor, boolean track, Service qualifier) {
		List<Object> rv = new ArrayList<>();

		BundleContext context = bundle.getBundleContext();
		if (context == null) {
			context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		}

		@SuppressWarnings("unchecked")
		Class<Object> cl = t instanceof ParameterizedType ? (Class<Object>) ((ParameterizedType) t).getRawType() : (Class<Object>) t;
		try {
			String filter = qualifier.filterExpression() != null && ! qualifier.filterExpression().isEmpty() ? qualifier.filterExpression() : null;

			ServiceReference<?>[] serviceReferences = context.getServiceReferences(cl.getName(), filter);
			if( serviceReferences != null ) {
				Arrays.sort(serviceReferences);

				for (ServiceReference<?> serviceReference : serviceReferences) {
					rv.add(context.getService(serviceReference));
				}
			}

			// We are in the wrong order
			Collections.reverse(rv);

			if (track) {
				trackService(context, cl, requestor);
			}
		} catch (InvalidSyntaxException e) {
			logError("Invalid filter expression", e); //$NON-NLS-1$
		}

		return rv;
	}

	private synchronized void trackService(BundleContext context, Class<?> serviceClass, IRequestor requestor) {
		Map<Class<?>, ServiceHandler> map = this.handlerList.computeIfAbsent(context, (k) -> new ConcurrentHashMap<>());
		ServiceHandler handler = map.computeIfAbsent(serviceClass, (cl) -> {
			ServiceHandler h = new ServiceHandler(this,context, serviceClass);
			context.addServiceListener(h);
			return h;
		});
		handler.requestors.add(requestor);
	}

	/**
	 * Method to log an exception.
	 *
	 * @param message
	 *            The log message.
	 * @param e
	 *            The exception that should be logged.
	 */
	void logError(String message, Throwable e) {
		Logger log = this.logger;
		if (log != null) {
			log.error(message, e);
		} else {
			// fallback if no LogService is available
			e.printStackTrace();
		}
	}

	@Override
	public void handleEvent(Event event) {
		this.handlerList.forEach((bc, map) -> {
			map.forEach((cl, sh) -> {
				sh.cleanup();
			});
		});
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	void setLogger(LoggerFactory factory) {
		this.factory = factory;
		this.logger = factory.getLogger(getClass());
	}

	void unsetLogger(LoggerFactory loggerFactory) {
		if (this.factory == loggerFactory) {
			this.factory = null;
			this.logger = null;
		}
	}
}
