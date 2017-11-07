/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.notification.hook;

import org.apache.atlas.model.notification.AtlasNotificationMessage;
import org.apache.atlas.notification.AbstractMessageDeserializer;
import org.apache.atlas.notification.AbstractNotification;
import org.apache.atlas.v1.model.notification.HookNotification.HookNotificationMessage;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Hook notification message deserializer.
 */
public class HookMessageDeserializer extends AbstractMessageDeserializer<HookNotificationMessage> {

    /**
     * Logger for hook notification messages.
     */
    private static final Logger NOTIFICATION_LOGGER = LoggerFactory.getLogger(HookMessageDeserializer.class);


    // ----- Constructors ----------------------------------------------------

    /**
     * Create a hook notification message deserializer.
     */
    public HookMessageDeserializer() {
        super(new TypeReference<HookNotificationMessage>() {},
              new TypeReference<AtlasNotificationMessage<HookNotificationMessage>>() {},
              AbstractNotification.CURRENT_MESSAGE_VERSION, NOTIFICATION_LOGGER);
    }

    @Override
    public HookNotificationMessage deserialize(String messageJson) {
        final HookNotificationMessage ret = super.deserialize(messageJson);

        if (ret != null) {
            ret.normalize();
        }

        return ret;
    }
}
