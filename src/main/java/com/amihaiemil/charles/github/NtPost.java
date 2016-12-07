/*
 * Copyright (c) 2016, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of mention-notifications-ejb nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.charles.github;

import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifications post.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class NtPost extends AuthorizedRequest implements Post {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NtPost.class.getName());

    /**
     * Notifications held by this Post.
     */
    private Notifications notifications;

    /**
     * Ctor.
     * @param notifications Notifications for this Post to send.
     * @param atz Authorization that gives us the token to use.
     * @param endpoint Rest POST endpoint to send these notifications to.
     */
    public NtPost(
        Notifications notifications, Authorization atz, String endpoint
    ) {
        super(atz, endpoint);
        this.notifications = notifications;
    }

    @Override
    public void send() throws IOException {
        JsonArray parcel = this.pack(this.notifications.fetch());
        log.info("Sending notifications to " + this.request().uri().toString() + " ...");
        int status = this.request()
            .method(Request.POST).body().set(parcel).back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(
                Matchers.isOneOf(
                    HttpURLConnection.HTTP_OK,
                    HttpURLConnection.HTTP_UNAUTHORIZED
                )
            ).status();
        if(status == HttpURLConnection.HTTP_OK) {
            log.info("Notifications sent successfully! Marking notifications as read...");
            this.notifications.markAsRead();
            log.info("Notifications marked as read!");
        } else {
            log.error("Could not send, got response status: " + status);
        }
        
    }

    /**
     * Take a list of Github Notifications and "pack" them (simplify them,
     * only take the repo name and issue number, and put them in a JsonArray).
     * @param notifications Github json notifications.
     * @return JsonArray to be sent out.
     */
    private JsonArray pack(List<JsonObject> notifications) {
        log.info("Fetched " + notifications + " notifications from Github.");
        log.info("Simplifying notifications, we ony need the repo name and issue number...");
        JsonArrayBuilder parcel = Json.createArrayBuilder();
        for(JsonObject notification : notifications) {
            JsonObject subject = notification.getJsonObject("subject");
            String issueUrl = subject.getString("url");
            parcel.add(
                Json.createObjectBuilder()
                    .add("repoFullName", notification.getJsonObject("repository").getString("full_name"))
                    .add("issueNumber", Integer.parseInt(issueUrl.substring(issueUrl.lastIndexOf("/") + 1)))
                    .build()
            );
        }
        log.info("Done; notifications are packed and ready to be sent.");
        return parcel.build();
    }
}
