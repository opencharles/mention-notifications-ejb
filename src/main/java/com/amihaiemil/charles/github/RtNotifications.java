/*
 * Copyright (c) 2016-2017, Mihai Emil Andronache
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;

/**
 * A Github Notification.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public final class RtNotifications extends AuthorizedRequest implements Notifications {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RtNotifications.class.getName());

    /**
     * Reason for the notifications. What type of notifications
     * are we interested in?
     */
    private Reason reason;

    /**
     * Notifications' read time. Every time notifications are read, this time has to be
     * initialized, to be used when marking notifications as read. We only want to mark the
     * notifications we have fetched already. <br><br>
     * If it wasn't for this, we could mark as read notifications that came in between the time
     * we fetch() is called and markAsRead() is called.
     */
    private String lastReadAt;

    /**
     * Ctor.
     * @param res Reason.
     * @param atz Authorization that gives us the token to use.
     * @param edp String endpoint.
     */
    public RtNotifications(Reason res, Authorization atz, String edp) {
        super(atz, edp);
        this.reason = res;
        this.lastReadAt = DateFormatUtils.formatUTC(
            new Date(System.currentTimeMillis()),
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        );
    }

    @Override
    public List<JsonObject> fetch() throws IOException {
        List<JsonObject> filtered = new ArrayList<JsonObject>();
        JsonArray notifications = this.request().fetch()
            .as(RestResponse.class).assertStatus(HttpURLConnection.HTTP_OK)
            .as(JsonResponse.class).json().readArray();
        this.lastReadAt = DateFormatUtils.formatUTC(
            new Date(System.currentTimeMillis()),
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        );
        log.info("Found " + notifications.size() + " new notifications!");
        if(notifications.size() > 0) {
            List<JsonObject> unfiltered = new ArrayList<JsonObject>();
            for(int i=0; i<notifications.size(); i++) {
                unfiltered.add(notifications.getJsonObject(i));
            }
            filtered = this.reason.filter(unfiltered);
        }
        return filtered;
    }

    @Override
	public void markAsRead() throws IOException {
        this.request().uri()
            .queryParam("last_read_at", this.lastReadAt).back()
            .method(Request.PUT).body().set("{}").back().fetch()
            .as(RestResponse.class)
            .assertStatus(
                Matchers.isOneOf(
                    HttpURLConnection.HTTP_OK,
                    HttpURLConnection.HTTP_RESET
                )
            );		
	}

}
