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
package org.apache.commons.jxpath.ri.model.dynamic;

import java.util.Locale;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyIterator;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;

/**
 * A Pointer that points to an object with Dynamic Properties. It is used for
 * the first element of a path; following elements will by of type
 * {@link PropertyPointer}.
 */
public class DynamicPointer extends PropertyOwnerPointer {
    private final QName name;
    private final Object bean;
    private final DynamicPropertyHandler handler;

    private static final long serialVersionUID = -1842347025295904256L;

    /**
     * Create a new DynamicPointer.
     * @param name property name
     * @param bean owning bean
     * @param handler DynamicPropertyHandler
     * @param locale Locale
     */
    public DynamicPointer(final QName name, final Object bean,
            final DynamicPropertyHandler handler, final Locale locale) {
        super(null, locale);
        this.name = name;
        this.bean = bean;
        this.handler = handler;
    }

    /**
     * Create a new DynamicPointer.
     * @param parent parent pointer
     * @param name property name
     * @param bean owning bean
     * @param handler DynamicPropertyHandler
     */
    public DynamicPointer(final NodePointer parent, final QName name,
            final Object bean, final DynamicPropertyHandler handler) {
        super(parent);
        this.name = name;
        this.bean = bean;
        this.handler = handler;
    }

    @Override
    public PropertyPointer getPropertyPointer() {
        return new DynamicPropertyPointer(this, handler);
    }

    @Override
    public NodeIterator createNodeIterator(
                final String property, final boolean reverse, final NodePointer startWith) {
        return new PropertyIterator(this, property, reverse, startWith);
    }

    @Override
    public NodeIterator attributeIterator(final QName name) {
        return new DynamicAttributeIterator(this, name);
    }

    @Override
    public QName getName() {
        return name;
    }

    @Override
    public boolean isDynamicPropertyDeclarationSupported() {
        return true;
    }

    /**
     * Returns the DP object iself.
     * @return Object
     */
    @Override
    public Object getBaseValue() {
        return bean;
    }

    @Override
    public boolean isLeaf() {
        final Object value = getNode();
        return value == null || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    /**
     * Returns 1.
     * @return int
     */
    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public String asPath() {
        return parent == null ? "/" : super.asPath();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(bean) + (name == null ? 0 : name.hashCode());
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof DynamicPointer)) {
            return false;
        }

        final DynamicPointer other = (DynamicPointer) object;
        if (bean != other.bean) {
            return false;
        }
        return name == other.name || name != null && name.equals(other.name);
    }
}
