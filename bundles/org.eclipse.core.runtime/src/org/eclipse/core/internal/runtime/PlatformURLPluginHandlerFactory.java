/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

//TODO Part of this code must go in the backward compatibility, other plugins must use the URL Handler service

package org.eclipse.core.internal.runtime;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Hashtable;
import org.eclipse.core.runtime.*;
import org.osgi.service.url.*;

public class PlatformURLPluginHandlerFactory {

	IConfigurationElement ce = null;

	static final String URL_HANDLERS_POINT = "org.eclipse.core.runtime.urlHandlers"; //$NON-NLS-1$
	static final String PROTOCOL = "protocol"; //$NON-NLS-1$
	static final String HANDLER = "class"; //$NON-NLS-1$

	public PlatformURLPluginHandlerFactory(IConfigurationElement ce) {
		super();
		this.ce = ce;
	}

	public URLStreamHandler createURLStreamHandler(String protocol) {
		URLStreamHandler handler = null;
		try {
			handler = (URLStreamHandler) ce.createExecutableExtension(HANDLER);
		} catch (CoreException e) {
		}
		return handler;
	}

	public static void deregister(IExtension extension) {
		// TODO
	}

	public static void register(IExtension extension) {
		// register URL handler extensions
		IConfigurationElement[] ce = extension.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			installURL(ce[i]);
		}
	}

	public static void startup() {
		// register URL handler extensions
		IExtensionRegistry r = InternalPlatform.getDefault().getRegistry();
		if (r != null) {
			IConfigurationElement[] ce = r.getConfigurationElementsFor(URL_HANDLERS_POINT);
			for (int i = 0; i < ce.length; i++) {
				installURL(ce[i]);
			}
		}
		// initialize plugin and fragment connection support
		urlExtensionListener();
	}

	private static void urlExtensionListener() {
		InternalPlatform.getDefault().getRegistry().addRegistryChangeListener(new IRegistryChangeListener() {

			public void registryChanged(IRegistryChangeEvent event) {
				IExtensionDelta[] deltas = event.getExtensionDeltas("org.eclipse.core.runtime", IPlatform.PT_URLHANDLERS);
				for (int i = 0; i < deltas.length; i++) {
					if (deltas[i].getKind() == IExtensionDelta.ADDED) {
						register(deltas[i].getExtension());
						continue;
					}
					if (deltas[i].getKind() == IExtensionDelta.REMOVED) {
						deregister(deltas[i].getExtension());
						continue;
					}

				}

			}

		}, "org.eclipse.core.runtime");
	}

	private static void installURL(org.eclipse.core.runtime.IConfigurationElement ce) {
		String protocol;
		if ((protocol = ce.getAttribute(PROTOCOL)) == null)
			return;

		Object handlerInstance;
		try {
			handlerInstance = new URLHandlerWrapper((URLStreamHandler) ce.createExecutableExtension(HANDLER));

		} catch (CoreException e) {
			InternalPlatform.getDefault().log(new Status(1, "pluginId", 0, "error registering the URL" + protocol, e));
			return;
		}
		Hashtable properties = new Hashtable();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { protocol });
		InternalPlatform.getDefault().getBundleContext().registerService(URLStreamHandlerService.class.getName(), handlerInstance, properties);

		//		if(protocol.equals("help")) {
		//			try {
		//				URL u = new URL("help://foo.xml");
		//				InputStream is = u.openStream();
		//				is.close();
		//			} catch (MalformedURLException e1) {
		//				// TODO Auto-generated catch block
		//				e1.printStackTrace();
		//			} catch (IOException e1) {
		//				// TODO Auto-generated catch block
		//				e1.printStackTrace();
		//			}
		//		}
	}

	static class URLHandlerWrapper extends AbstractURLStreamHandlerService {
		private URLStreamHandler handler;

		public URLHandlerWrapper(URLStreamHandler handler) {
			this.handler = handler;
		}

		public void parseURL(URLStreamHandlerSetter realHandler, URL u, String spec, int start, int limit) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("parseURL", new Class[] { URL.class, String.class, int.class, int.class });
				m.setAccessible(true);
				m.invoke(handler, new Object[] { u, spec, new Integer(start), new Integer(limit)});
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public URLConnection openConnection(URL u) throws IOException {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("openConnection", new Class[] { URL.class });
				m.setAccessible(true);
				return (URLConnection) m.invoke(handler, new Object[] { u });
			} catch (Exception e) {
				throw new IOException();
			}
		}

		public boolean equals(URL u1, URL u2) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("equals", new Class[] { URL.class, URL.class });
				m.setAccessible(true);
				return ((Boolean) m.invoke(handler, new Object[] { u1, u2 })).booleanValue();
			} catch (Exception e) {
				return false;
			}
		}

		public int hashCode(URL u) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("hashCode", new Class[] { URL.class });
				m.setAccessible(true);
				return ((Integer) m.invoke(handler, new Object[] { u })).intValue();
			} catch (Exception e) {
				return super.hashCode(u);
			}
		}

		public int getDefaultPort() {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("getDefaultPort", null);
				m.setAccessible(true);
				return ((Integer) m.invoke(handler, new Object[] {
				})).intValue();
			} catch (Exception e) {
				return -1;
			}
		}

		public synchronized InetAddress getHostAddress(URL u) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("getHostAddress", new Class[] { URL.class });
				m.setAccessible(true);
				return (InetAddress) m.invoke(handler, new Object[] { u });
			} catch (Exception e) {
				return null;
			}
		}

		public boolean hostsEqual(URL u1, URL u2) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("hostsEqual", new Class[] { URL.class, URL.class });
				m.setAccessible(true);
				return ((Boolean) m.invoke(handler, new Object[] { u1, u2 })).booleanValue();
			} catch (Exception e) {
				return false;
			}
		}

		public boolean sameFile(URL u1, URL u2) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("sameFile", new Class[] { URL.class, URL.class });
				m.setAccessible(true);
				return ((Boolean) m.invoke(handler, new Object[] { u1, u2 })).booleanValue();
			} catch (Exception e) {
				return false;
			}
		}

		public void setURL(URL u, String protocol, String host, int port, String file, String ref) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("setURL", new Class[] { URL.class, String.class, String.class, int.class, String.class, String.class });
				m.setAccessible(true);
				m.invoke(handler, new Object[] { u, protocol, host, new Integer(port), file, ref });
			} catch (Exception e) {
			}
		}

		public void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref) {
			Method m;
			try {
				m = handler.getClass().getDeclaredMethod("setURL", new Class[] { URL.class, String.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class });
				m.setAccessible(true);
				m.invoke(handler, new Object[] { u, protocol, host, new Integer(port), authority, userInfo, path, query, ref });
			} catch (Exception e) {
			}
		}
	}
}