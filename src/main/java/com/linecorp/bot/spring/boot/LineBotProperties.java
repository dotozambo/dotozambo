/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.bot.spring.boot;

import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.linecorp.bot.client.DefaultLineBotClient;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "line.bot")
public class LineBotProperties {
    private static final Pattern TRAILING_SLASH = Pattern.compile("/$");

    @Valid
    @NotNull
    private String channelMid;

    @Valid
    @NotNull
    private String channelId;

    @Valid
    @NotNull
    private String channelSecret;

    @Valid
    @NotNull
    private String apiEndPoint = DefaultLineBotClient.DEFAULT_API_END_POINT;

    @Valid
    @NotNull
    private int connectTimeout;

    @Valid
    @NotNull
    private int connectionRequestTimeout;

    @Valid
    @NotNull
    private int socketTimeout;

    @Valid
    @NotNull
    private Long sendingMessageChannelId = DefaultLineBotClient.DEFAULT_SENDING_MESSAGE_CHANNEL_ID;

    @Valid
    @NotNull
    private String sendingMessageEventId = DefaultLineBotClient.DEFAULT_SENDING_MESSAGE_EVENT_ID;

    @Valid
    @NotNull
    private String sendingMultipleMessagesEventId =
            DefaultLineBotClient.DEFAULT_SENDING_MULTIPLE_MESSAGES_EVENT_ID;
}
