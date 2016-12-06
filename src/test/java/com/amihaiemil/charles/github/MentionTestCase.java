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

import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for {@link Mention}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public final class MentionTestCase {

    /**
     * Mention can filter an empty list.
     */
    @Test
    public void worksWithEmptyList() {
        final List<JsonObject> filtered = new Mention().filter(
            new ArrayList<JsonObject>()
        );
        assertTrue(filtered.isEmpty());
    }

    /**
     * Mention can filter out all the notifications, if all of them are bad.
     */
    @Test
    public void returnsEmptyList() {
        final List<JsonObject> notifications = new ArrayList<JsonObject>();
        notifications.add(
            this.mockNotification("fork", "/url/here/", "last/comment/url")
        );
        notifications.add(
            this.mockNotification("mention", "/same/here/", "/same/here/")
        );
        notifications.add(
            this.mockNotification("star", "/url/here/asd", "last/comment/url")
        );
        final List<JsonObject> filtered = new Mention().filter(notifications);
        assertTrue(filtered.isEmpty());
    }

    /**
     * Mention can filter notifications properly.
     */
    @Test
    public void returnsGoodNotifications() {
        final List<JsonObject> notifications = new ArrayList<JsonObject>();
        notifications.add(
            this.mockNotification("fork", "/url/here/qwe", "last/comment/urlq")
        );
        notifications.add(
            this.mockNotification("mention", "/url/here/", "/some/url/here/")
        );
        notifications.add(
            this.mockNotification("star", "/url/here/bad", "/comment/url/8")
        );
        notifications.add(
            this.mockNotification("mention", "/here/2", "localhost:80/here/3")
        );
        final List<JsonObject> filtered = new Mention().filter(notifications);
        assertTrue(filtered.size() == 2);
    }

    /**
     * Mention can filter the "mention" reason notifications that are
     * subsequent ones.
     */
    @Test
    public void allMentionButSubsequent() {
        final List<JsonObject> notifications = new ArrayList<JsonObject>();
        notifications.add(
            this.mockNotification("mention", "/url/here/1", "/url/here/1")
        );
        notifications.add(
            this.mockNotification("mention", "/url/here/2", "/url/here/2")
        );
        notifications.add(
            this.mockNotification("mention", "/url/here/3", "/url/here/3")
        );
        notifications.add(
            this.mockNotification("mention", "/url/here/4", "/url/here/4")
        );
        final List<JsonObject> filtered = new Mention().filter(notifications);
        assertTrue(filtered.isEmpty());
    }

    /**
     * Mock a notification json for unit tests.
     * @param reason Reason of it.
     * @param url Url
     * @param lastCommentUrl Last comment url.
     * @return Json object.
     */
    private JsonObject mockNotification(
        final String reason, final String url, final String lastCommentUrl
    ) { 
        return Json.createObjectBuilder()
            .add("reason", reason)
            .add(
                "subject",
                Json.createObjectBuilder()
                    .add("url", url)
                    .add("latest_comment_url", lastCommentUrl)
                    .build()
            ).build();
    }

}
