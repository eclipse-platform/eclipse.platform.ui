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
package org.apache.commons.jxpath.util;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ExtendedKeyManager;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.KeyManager;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.InfoSetUtil;

/**
 * Utility class.
 * @since JXPath 1.3
 */
public class KeyManagerUtils {
    /**
     * Adapt KeyManager to implement ExtendedKeyManager.
     */
    private static final class SingleNodeExtendedKeyManager implements
            ExtendedKeyManager {
        private final KeyManager delegate;

        /**
         * Create a new SingleNodeExtendedKeyManager.
         * @param delegate KeyManager to wrap
         */
        public SingleNodeExtendedKeyManager(final KeyManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public NodeSet getNodeSetByKey(final JXPathContext context, final String key,
                final Object value) {
            final Pointer pointer = delegate.getPointerByKey(context, key, InfoSetUtil.stringValue(value));
            final BasicNodeSet result = new BasicNodeSet();
            result.add(pointer);
            return result;
        }

        @Override
        public Pointer getPointerByKey(final JXPathContext context, final String keyName,
                final String keyValue) {
            return delegate.getPointerByKey(context, keyName, keyValue);
        }
    }

    /**
     * Gets an ExtendedKeyManager from the specified KeyManager.
     * @param keyManager to adapt, if necessary
     * @return {@code keyManager} if it implements ExtendedKeyManager
     *         or a basic single-result ExtendedKeyManager that delegates to
     *         {@code keyManager}.
     */
    public static ExtendedKeyManager getExtendedKeyManager(final KeyManager keyManager) {
        return keyManager instanceof ExtendedKeyManager ? (ExtendedKeyManager) keyManager
                : new SingleNodeExtendedKeyManager(keyManager);
    }
}
