/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.mqtt.session;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.transport.TransportService;
import org.thingsboard.server.common.transport.auth.TransportDeviceInfo;
import org.thingsboard.server.gen.transport.TransportProtos;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by nickAS21 on 08.12.22
 */
@Slf4j
public class SparkplugSessionCtx extends AbstractGatewayDeviceSessionContext {

    public SparkplugSessionCtx(AbstractGatewaySessionHandler parent,
                               TransportDeviceInfo deviceInfo,
                               DeviceProfile deviceProfile,
                               ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap,
                               TransportService transportService) {
        super(parent, deviceInfo, deviceProfile, mqttQoSMap, transportService);
    }

    @Override
    public void onAttributeUpdate(UUID sessionId, TransportProtos.AttributeUpdateNotificationMsg notification) {
        log.trace("[{}] Received attributes update notification to sparkplug device", sessionId);
        createMqttPublishMsg(this, notification).ifPresent(parent::writeAndFlush);
    }

    private Optional<MqttPublishMessage> createMqttPublishMsg (MqttDeviceAwareSessionContext ctx, TransportProtos.AttributeUpdateNotificationMsg notification) {
        try {
            // TODO metrics from notification & MetricsDBIRTH
            byte[] payloadInBytes = new byte[3];
            String topic = ((SparkplugNodeSessionHandler) parent).sparkplugTopicNode.toString() + "/" + deviceInfo.getDeviceName();
            return Optional.of(parent.getPayloadAdaptor().createMqttPublishMsg(ctx, topic, payloadInBytes));
        } catch (Exception e) {
            log.trace("[{}] Failed to convert device attributes response to MQTT sparkplug  msg", sessionId, e);
            return Optional.empty();
        }
    }

}
