/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.util.ClassLoaderUtil;

/**
 * JXPathIntrospector  maintains a registry of {@link JXPathBeanInfo
 * JXPathBeanInfo} objects for Java classes.
 */
public class JXPathIntrospector {

    private static Map byClass = Collections.synchronizedMap(new HashMap());
    private static Map byInterface = Collections.synchronizedMap(new HashMap());

    static {
        registerAtomicClass(Class.class);
        registerAtomicClass(Boolean.TYPE);
        registerAtomicClass(Boolean.class);
        registerAtomicClass(Byte.TYPE);
        registerAtomicClass(Byte.class);
        registerAtomicClass(Character.TYPE);
        registerAtomicClass(Character.class);
        registerAtomicClass(Short.TYPE);
        registerAtomicClass(Short.class);
        registerAtomicClass(Integer.TYPE);
        registerAtomicClass(Integer.class);
        registerAtomicClass(Long.TYPE);
        registerAtomicClass(Long.class);
        registerAtomicClass(Float.TYPE);
        registerAtomicClass(Float.class);
        registerAtomicClass(Double.TYPE);
        registerAtomicClass(Double.class);
        registerAtomicClass(String.class);
        registerAtomicClass(Date.class);
        registerAtomicClass(java.sql.Date.class);
        registerAtomicClass(java.sql.Time.class);
        registerAtomicClass(java.sql.Timestamp.class);

        registerDynamicClass(Map.class, MapDynamicPropertyHandler.class);
    }

    /**
     * Automatically creates and registers a JXPathBeanInfo object
     * for the specified class. That object returns true to isAtomic().
     * @param beanClass to register
     */
    public static void registerAtomicClass(final Class beanClass) {
        synchronized (byClass) {
            byClass.put(beanClass, new JXPathBasicBeanInfo(beanClass, true));
        }
    }

    /**
     * Automatically creates and registers a {@link JXPathBeanInfo} object
     * for the specified class. That object returns true to
     * {@link JXPathBeanInfo#isDynamic()}.
     *
     * @param beanClass to register
     * @param dynamicPropertyHandlerClass to handle beanClass
     */
    public static void registerDynamicClass(final Class beanClass,
            final Class dynamicPropertyHandlerClass) {
        final JXPathBasicBeanInfo bi =
            new JXPathBasicBeanInfo(beanClass, dynamicPropertyHandlerClass);
        if (beanClass.isInterface()) {
            synchronized (byInterface) {
                byInterface.put(beanClass, bi);
            }
        }
        else {
            synchronized (byClass) {
                byClass.put(beanClass, bi);
            }
        }
    }

    /**
     * Creates and registers a JXPathBeanInfo object for the supplied class. If
     * the class has already been registered, returns the registered
     * JXPathBeanInfo object.
     * <p>
     * The process of creation of JXPathBeanInfo is as follows:
     * <ul>
     * <li>If class named {@code <beanClass>XBeanInfo} exists,
     *     an instance of that class is allocated.
     * <li>Otherwise, an instance of {@link JXPathBasicBeanInfo
     *     JXPathBasicBeanInfo}  is allocated.
     * </ul>
     * @param beanClass whose info to get
     * @return JXPathBeanInfo
     */
    public static JXPathBeanInfo getBeanInfo(final Class beanClass) {
        JXPathBeanInfo beanInfo = (JXPathBeanInfo) byClass.get(beanClass);
        if (beanInfo == null) {
            beanInfo = findDynamicBeanInfo(beanClass);
            if (beanInfo == null) {
                beanInfo = findInformant(beanClass);
                if (beanInfo == null) {
                    beanInfo = new JXPathBasicBeanInfo(beanClass);
                }
            }
            synchronized (byClass) {
                byClass.put(beanClass, beanInfo);
            }
        }
        return beanInfo;
    }

    /**
     * Find a dynamic bean info if available for any superclasses or
     * interfaces.
     * @param beanClass to search for
     * @return JXPathBeanInfo
     */
    private static JXPathBeanInfo findDynamicBeanInfo(final Class beanClass) {
        JXPathBeanInfo beanInfo;
        if (beanClass.isInterface()) {
            beanInfo = (JXPathBeanInfo) byInterface.get(beanClass);
            if (beanInfo != null && beanInfo.isDynamic()) {
                return beanInfo;
            }
        }

        final Class[] interfaces = beanClass.getInterfaces();
        if (interfaces != null) {
            for (final Class element : interfaces) {
                beanInfo = findDynamicBeanInfo(element);
                if (beanInfo != null && beanInfo.isDynamic()) {
                    return beanInfo;
                }
            }
        }

        final Class sup = beanClass.getSuperclass();
        if (sup != null) {
            beanInfo = (JXPathBeanInfo) byClass.get(sup);
            if (beanInfo != null && beanInfo.isDynamic()) {
                return beanInfo;
            }
            return findDynamicBeanInfo(sup);
        }
        return null;
    }

    /**
     * find a JXPathBeanInfo instance for the specified class.
     * Similar to javax.beans property handler discovery; search for a
     * class with "XBeanInfo" appended to beanClass.name, then check
     * whether beanClass implements JXPathBeanInfo for itself.
     * Invokes the default constructor for any class it finds.
     * @param beanClass for which to look for an info provider
     * @return JXPathBeanInfo instance or null if none found
     */
    private static synchronized JXPathBeanInfo findInformant(final Class beanClass) {
        final String name = beanClass.getName() + "XBeanInfo";
        try {
            return (JXPathBeanInfo) instantiate(beanClass, name);
        }
        catch (final Exception ignore) { // NOPMD
            // Just drop through
        }

        // Now try checking if the bean is its own JXPathBeanInfo.
        try {
            if (JXPathBeanInfo.class.isAssignableFrom(beanClass)) {
                return (JXPathBeanInfo) beanClass.getConstructor().newInstance();
            }
        }
        catch (final Exception ignore) { // NOPMD
            // Just drop through
        }

        return null;
    }

    /**
     * Try to create an instance of a named class.
     * First try the classloader of "sibling", then try the system
     * classloader.
     * @param sibling Class
     * @param className to instantiate
     * @return new Object
     * @throws Exception if instantiation fails
     */
    private static Object instantiate(final Class sibling, final String className)
            throws Exception {

        // First check with sibling's classloader (if any).
        final ClassLoader cl = sibling.getClassLoader();
        if (cl != null) {
            try {
                final Class cls = cl.loadClass(className);
                return cls.getConstructor().newInstance();
            }
            catch (final Exception ex) { //NOPMD
                // Just drop through and use the ClassLoaderUtil.
            }
        }

        // Now try the ClassLoaderUtil.
        final Class cls = ClassLoaderUtil.getClass(className);
        return cls.newInstance();
    }
}
