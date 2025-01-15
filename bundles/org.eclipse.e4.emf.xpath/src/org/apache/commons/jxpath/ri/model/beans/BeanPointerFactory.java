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
package org.apache.commons.jxpath.ri.model.beans;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;

/**
 * Implements NodePointerFactory for JavaBeans.
 */
public class BeanPointerFactory implements NodePointerFactory {

    /** Factory order constant */
    public static final int BEAN_POINTER_FACTORY_ORDER = 900;

    @Override
    public int getOrder() {
        return BEAN_POINTER_FACTORY_ORDER;
    }

    @Override
    public NodePointer createNodePointer(final QName name, final Object bean, final Locale locale) {
        final JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(bean.getClass());
        return new BeanPointer(name, bean, bi, locale);
    }

    @Override
    public NodePointer createNodePointer(final NodePointer parent, final QName name,
            final Object bean) {
        if (bean == null) {
            return new NullPointer(parent, name);
        }

        final JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(bean.getClass());
        return new BeanPointer(parent, name, bean, bi);
    }
}
