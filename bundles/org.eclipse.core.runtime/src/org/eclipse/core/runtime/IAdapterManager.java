package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An adapter manager maintains a registry of adapter factories.
 * Clients directly invoke methods on an adapter manager to register
 * and unregister adapters.
 * All adaptable objects (that is, objects that implement the 
 * <code>IAdaptable</code> interface) funnel 
 * <code>IAdaptable.getAdapter</code> invocations to their
 * adapter manager's <code>IAdapterManger.getAdapter</code>
 * method. The adapter manager then forwards this request
 * unmodified to the <code>IAdapterFactory.getAdapter</code> 
 * method on one of the registered adapter factories.
 * <p>
 * The following code snippet shows how one might register
 * an adapter of type <code>com.example.acme.Sticky</code>
 * on resources in the workspace.
 * <p>
 * <pre>
 * IAdapterFactory pr = new IAdapterFactory() {
 *     public Class[] getAdapterList() {
 *         return new Class[] { com.example.acme.Sticky.class };
 *     }
 *     public Object getAdapter(Object adaptableObject, adapterType) {
 *         IResource res = (IResource) adaptableObject;
 *         QualifiedName key = new QualifiedName("com.example.acme", "sticky-note");
 *         try {
 *             com.example.acme.Sticky v = (com.example.acme.Sticky) res.getSessionProperty(key);
 *             if (v == null) {
 *                 v = new com.example.acme.Sticky();
 *                 res.setSessionProperty(key, v);
 *             }
 *         } catch (CoreException e) {
 *             // unable to access session property - ignore
 *         }
 *         return v;
 *     }
 * }
 * Platform.getAdapterManager().registerAdapters(pr, IResource.class);
</pre>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IAdaptable
 * @see IAdapterFactory
 */
public interface IAdapterManager {
/**
 * Returns an object which is an instance of the given class
 * associated with the given object. Returns <code>null</code> if
 * no such object can be found.
 *
 * @param adaptable the adaptable object being queried
 *   (usually an instance of <code>IAdaptable</code>)
 * @param adapterType the type of adapter to look up
 * @return a object castable to the given adapter type, 
 *    or <code>null</code> if the given adaptable object does not
 *    have an adapter of the given type
 */
public Object getAdapter(Object adaptable, Class adapterType);
/**
 * Registers the given adapter factory as extending objects of
 * the given type.
 * <p>
 * If the type being extended is a class,
 * the given factory's adapters are available on instances
 * of that class and any of its subclasses.  If it is an interface, 
 * the adapters are available to all classes that directly 
 * or indirectly implement that interface.
 * </p>
 *
 * @param factory the adapter factory
 * @param adaptable the type being extended
 * @see #unregisterAdapters
 */
public void registerAdapters(IAdapterFactory factory, Class adaptable);
/**
 * Removes the given adapter factory completely from the list of 
 * registered factories. Equivalent to calling
 * <code>unregisterAdapters(IAdapterFactory,Class)</code>
 * on all classes against which it had been explicitly registered.
 * Does nothing if the given factory is not currently registered.
 *
 * @param factory the adapter factory to remove
 * @see #registerAdapters
 */
public void unregisterAdapters(IAdapterFactory factory);
/**
 * Removes the given adapter factory from the list of factories
 * registered as extending the given class.
 * Does nothing if the given factory and type combination is not
 * registered.
 *
 * @param factory the adapter factory to remove
 * @param adaptable one of the types against which the given
 *    factory is registered
 * @see #registerAdapters
 */
public void unregisterAdapters(IAdapterFactory factory, Class adaptable);
}
