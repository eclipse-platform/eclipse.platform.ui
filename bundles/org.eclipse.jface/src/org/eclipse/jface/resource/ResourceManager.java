/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Hannes Wellmann - Parameterize DeviceResourceDescriptor with the described resource type
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * This class manages SWT resources. It manages reference-counted instances of resources
 * such as Fonts, Images, and Colors, and allows them to be accessed using descriptors.
 * Everything allocated through the registry should also be disposed through the registry.
 * Since the resources are shared and reference counted, they should never be disposed
 * directly.
 * <p>
 * ResourceManager handles correct allocation and disposal of resources. It differs from
 * the various JFace *Registry classes, which also map symbolic IDs onto resources. In
 * general, you should use a *Registry class to map IDs onto descriptors, and use a
 * ResourceManager to convert the descriptors into real Images/Fonts/etc.
 * </p>
 *
 * @since 3.1
 */
public abstract class ResourceManager {

	/**
	 * List of Runnables scheduled to run when the ResourceManager is disposed.
	 * null if empty.
	 */
	private List<Runnable> disposeExecs = null;

	/**
	 * Returns the Device for which this ResourceManager will create resources
	 *
	 * @since 3.1
	 *
	 * @return the Device associated with this ResourceManager
	 */
	public abstract Device getDevice();

	/**
	 * Returns the resource described by the given descriptor. If the resource
	 * already exists, the reference count is incremented and the exiting resource
	 * is returned. Otherwise, a new resource is allocated. Every call to this
	 * method should have a corresponding call to
	 * {@link #destroy(DeviceResourceDescriptor)}.
	 *
	 * <p>
	 * If the resource is intended to live for entire lifetime of this
	 * resource-manager, a subsequent call to
	 * {@link #destroy(DeviceResourceDescriptor)} may be omitted and the resource
	 * will be cleaned up when this resource-manager is disposed. This pattern is
	 * useful for short-lived {@link LocalResourceManager}s, but should never be
	 * used with the global resource-manager since doing so effectively leaks the
	 * resource.
	 * </p>
	 *
	 * <p>
	 * The resources returned from this method are reference counted and may be
	 * shared internally with other resource-managers. They should never be disposed
	 * outside of the ResourceManager framework, or it will cause exceptions in
	 * other code that shares them. For example, never call
	 * {@link org.eclipse.swt.graphics.Resource#dispose()} on anything returned from
	 * this method.
	 * </p>
	 *
	 * @since 3.1
	 *
	 * @param descriptor descriptor for the resource to allocate
	 * @return the newly allocated resource (not null)
	 * @throws DeviceResourceException if unable to allocate the resource
	 */
	public abstract <R> R create(DeviceResourceDescriptor<R> descriptor);

	/**
	 * Deallocates a resource previously allocated by {@link #create(DeviceResourceDescriptor)}.
	 * Descriptors are compared by equality, not identity. If the same resource was
	 * created multiple times, this may decrement a reference count rather than
	 * disposing the actual resource.
	 *
	 * @since 3.1
	 *
	 * @param descriptor identifier for the resource
	 */
	public abstract <R> void destroy(DeviceResourceDescriptor<R> descriptor);

	/**
	 * Returns a previously-allocated resource or allocates a new one if none exists
	 * yet. The resource will remain allocated for at least the lifetime of this
	 * resource-manager. If necessary, the resource will be deallocated
	 * automatically when this resource-manager is disposed.
	 *
	 * <p>
	 * The resources returned from this method are reference counted and may be
	 * shared internally with other resource-managers. They should never be disposed
	 * outside of the ResourceManager framework, or it will cause exceptions in
	 * other code that shares them. For example, never call
	 * {@link org.eclipse.swt.graphics.Resource#dispose()} on anything returned from
	 * this method.
	 * </p>
	 *
	 * <p>
	 * This method should only be used for resources that should remain allocated
	 * for the lifetime of this resource-manager. To allocate shorter-lived
	 * resources, manage them with <code>create</code>, and <code>destroy</code>
	 * rather than this method.
	 * </p>
	 *
	 * <p>
	 * This method should never be called on the global resource-manager, since all
	 * resources will remain allocated for the lifetime of the app and will be
	 * effectively leaked.
	 * </p>
	 *
	 * @param descriptor identifier for the requested resource
	 * @return the requested resource, never null
	 * @throws DeviceResourceException if the resource does not exist yet and cannot
	 *                                 be created for any reason.
	 *
	 * @since 3.3
	 */
	public final <R> R get(DeviceResourceDescriptor<R> descriptor) {
		R cached = find(descriptor);
		return cached != null ? cached : create(descriptor);
	}

	/**
	 * <p>Creates an image, given an image descriptor. Images allocated in this manner must
	 * be disposed by {@link #destroyImage(ImageDescriptor)}, and never by calling
	 * {@link Image#dispose()}.</p>
	 *
	 * <p>
	 * If the image is intended to remain allocated for the lifetime of the ResourceManager,
	 * the call to destroyImage may be omitted and the image will be cleaned up automatically
	 * when the ResourceManager is disposed. This should only be done with short-lived ResourceManagers,
	 * as doing so with the global manager effectively leaks the resource.
	 * </p>
	 *
	 * @since 3.1
	 *
	 * @param descriptor descriptor for the image to create
	 * @return the Image described by this descriptor (possibly shared by other
	 *         equivalent ImageDescriptors)
	 * @throws DeviceResourceException if unable to allocate the Image
	 * @deprecated use {@link #create(DeviceResourceDescriptor)} instead
	 */
	@Deprecated(since = "3.31")
	public final Image createImage(ImageDescriptor descriptor) {
		// Assertion added to help diagnose client bugs.  See bug #83711 and bug #90454.
		Assert.isNotNull(descriptor);
		return create(descriptor);
	}

	/**
	 * Creates an image, given an image descriptor. Images allocated in this manner must
	 * be disposed by {@link #destroyImage(ImageDescriptor)}, and never by calling
	 * {@link Image#dispose()}.
	 *
	 * @since 3.1
	 *
	 * @param descriptor descriptor for the image to create
	 * @return the Image described by this descriptor (possibly shared by other equivalent
	 * ImageDescriptors)
	 */
	public final Image createImageWithDefault(ImageDescriptor descriptor) {
		if (descriptor == null) {
			return getDefaultImage();
		}

		try {
			return create(descriptor);
		} catch (DeviceResourceException | SWTException e) {
			Policy.getLog().log(Status.warning("The image could not be loaded: " + descriptor, e)); //$NON-NLS-1$
			return getDefaultImage();
		}
	}

	/**
	 * Returns the default image that will be returned in the event that the intended
	 * image is missing.
	 *
	 * @since 3.1
	 *
	 * @return a default image that will be returned in the event that the intended
	 * image is missing.
	 */
	protected abstract Image getDefaultImage();

	/**
	 * Undoes everything that was done by {@link #createImage(ImageDescriptor)}.
	 *
	 * @since 3.1
	 *
	 * @param descriptor identifier for the image to dispose
	 * @deprecated use {@link #destroy(DeviceResourceDescriptor)} instead
	 */
	@Deprecated(since = "3.31")
	public final void destroyImage(ImageDescriptor descriptor) {
		destroy(descriptor);
	}

	/**
	 * Allocates a color, given a color descriptor. Any color allocated in this
	 * manner must be disposed by calling {@link #destroyColor(ColorDescriptor)},
	 * or by an eventual call to {@link #dispose()}. {@link Color#dispose()} must
	 * never been called directly on the returned color.
	 *
	 * @since 3.1
	 *
	 * @param descriptor descriptor for the color to create
	 * @return the Color described by the given ColorDescriptor (not null)
	 * @throws DeviceResourceException if unable to create the color
	 * @deprecated use {@link #create(DeviceResourceDescriptor)} instead
	 */
	@Deprecated(since = "3.31")
	public final Color createColor(ColorDescriptor descriptor) {
		return create(descriptor);
	}

	/**
	 * Allocates a color, given its RGB value. Any color allocated in this
	 * manner must be disposed by calling {@link #destroyColor(RGB)},
	 * or by an eventual call to {@link #dispose()}. {@link Color#dispose()} must
	 * never been called directly on the returned color.
	 *
	 * @since 3.1
	 *
	 * @param descriptor descriptor for the color to create
	 * @return the Color described by the given ColorDescriptor (not null)
	 * @throws DeviceResourceException if unable to create the color
	 */
	public final Color createColor(RGB descriptor) {
		return create(new RGBColorDescriptor(descriptor));
	}

	/**
	 * Undoes everything that was done by a call to {@link #createColor(RGB)}.
	 *
	 * @since 3.1
	 *
	 * @param descriptor RGB value of the color to dispose
	 */
	public final void destroyColor(RGB descriptor) {
		destroy(new RGBColorDescriptor(descriptor));
	}

	/**
	 * Undoes everything that was done by a call to {@link #createColor(ColorDescriptor)}.
	 *
	 *
	 * @since 3.1
	 *
	 * @param descriptor identifier for the color to dispose
	 * @deprecated use {@link #destroy(DeviceResourceDescriptor)} instead
	 */
	@Deprecated(since = "3.31")
	public final void destroyColor(ColorDescriptor descriptor) {
		destroy(descriptor);
	}

	/**
	 * Returns the Font described by the given FontDescriptor. Any Font
	 * allocated in this manner must be deallocated by calling disposeFont(...),
	 * or by an eventual call to {@link #dispose()}.  The method {@link Font#dispose()}
	 * must never be called directly on the returned font.
	 *
	 * @since 3.1
	 *
	 * @param descriptor description of the font to create
	 * @return the Font described by the given descriptor
	 * @throws DeviceResourceException if unable to create the font
	 * @deprecated use {@link #create(DeviceResourceDescriptor)} instead
	 */
	@Deprecated(since = "3.31")
	public final Font createFont(FontDescriptor descriptor) {
		return create(descriptor);
	}

	/**
	 * Undoes everything that was done by a previous call to {@link #createFont(FontDescriptor)}.
	 *
	 * @since 3.1
	 *
	 * @param descriptor description of the font to destroy
	 * @deprecated use {@link #destroy(DeviceResourceDescriptor)} instead
	 */
	@Deprecated(since = "3.31")
	public final void destroyFont(FontDescriptor descriptor) {
		destroy(descriptor);
	}

	/**
	 * Disposes any remaining resources allocated by this manager.
	 */
	public void dispose() {
		if (disposeExecs == null) {
			return;
		}

		// If one of the runnables throws an exception, we need to propagate it.
		// However, this should not prevent the remaining runnables from being
		// notified. If any runnables throw an exception, we remember one of them
		// here and throw it at the end of the method.
		RuntimeException foundException = null;

		Runnable[] execs = disposeExecs.toArray(new Runnable[disposeExecs.size()]);
		for (Runnable exec : execs) {
			try {
				exec.run();
			} catch (RuntimeException e) {
				// Ensure that we propagate an exception, but don't stop notifying
				// the remaining runnables.
				if (foundException == null) {
					foundException = e;
				} else {
					foundException.addSuppressed(e);
				}
			}
		}

		if (foundException != null) {
			// If any runnables threw an exception, propagate one of them.
			throw foundException;
		}
	}

	/**
	 * Returns a previously allocated resource associated with the given descriptor, or
	 * null if none exists yet.
	 *
	 * @since 3.1
	 *
	 * @param descriptor descriptor to find
	 * @return a previously allocated resource for the given descriptor or null if none.
	 */
	public abstract <R> R find(DeviceResourceDescriptor<R> descriptor);

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked just
	 * before the receiver is disposed. The runnable can be subsequently canceled by
	 * a call to {@link #cancelDisposeExec(Runnable)}.
	 *
	 * @param r runnable to execute.
	 */
	public void disposeExec(Runnable r) {
		Assert.isNotNull(r);

		if (disposeExecs == null) {
			disposeExecs = new ArrayList<>();
		}

		disposeExecs.add(r);
	}

	/**
	 * Cancels a runnable that was previously scheduled with
	 * {@link #disposeExec(Runnable)}. Has no effect if the given runnable was not
	 * previously registered with disposeExec.
	 *
	 * @param r runnable to cancel
	 */
	public void cancelDisposeExec(Runnable r) {
		Assert.isNotNull(r);

		if (disposeExecs == null) {
			return;
		}

		disposeExecs.remove(r);

		if (disposeExecs.isEmpty()) {
			disposeExecs = null;
		}
	}
}
