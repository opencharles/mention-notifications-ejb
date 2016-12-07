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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Github notifications.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public interface Notifications {

    /**
     * Fetch them.
     * @return List of json notifications.
     * @throws IOException if something goes wrong.
     */
    List<JsonObject> fetch() throws IOException;

    /**
     * Mark them as read.
     * @throws IOException if something goes wrong.
     */
    void markAsRead() throws IOException;

    /**
     * No notifications; used for unit tests.
     */
    final static class FakeEmptyNotifications implements Notifications {

        @Override
        public List<JsonObject> fetch() throws IOException {
            return new ArrayList<JsonObject>();
        }

        @Override
        public void markAsRead() throws IOException {
            //nothing to do.            
        }
    }
    
    /**
     * Notifications which throw ISE on markAsRead(), for unit tests.
     */
    final static class FakeErrorOnMarkRead implements Notifications {

        @Override
        public List<JsonObject> fetch() throws IOException {
            return new Notifications.FakeOtherNotifications().fetch();
        }

        @Override
        public void markAsRead() throws IOException {
            throw new IllegalStateException ("Exception while marking as read...");
        }
    }

    /**
     * Notifications that contain valid "mention" ones; used for unit tests.
     */
    final static class FakeNotificationsWithMentions implements Notifications {

        @Override
        public List<JsonObject> fetch() throws IOException {
            final List<JsonObject> notifications = new ArrayList<JsonObject>();
            notifications.add(
                this.mockNotification(
                    "fork", "/url/here/qwe/1",
                    "last/comment/urlq", "amihaiemil/charles"
                )
            );
            notifications.add(
                this.mockNotification(
                    "mention", "/url/here/2",
                    "/some/url/here/", "amihaiemil/charles"
                )
            );
            notifications.add(
                this.mockNotification(
                    "star", "/url/here/bad/3",
                    "/comment/url/8", "amihaiemil/charles"
                )
            );
            notifications.add(
                this.mockNotification(
                    "mention", "/here/2",
                    "localhost:80/here/3", "amihaiemil/charles"
                )
            );
            return notifications;
        }

        @Override
        public void markAsRead() throws IOException {
            //nothing to do.            
        }

        /**
         * Mock a notification json for unit tests.
         * @param reason Reason of it.
         * @param url Url
         * @param lastCommentUrl Last comment url.
         * @param repoFullName Repo name e.g. amihaiemil/eva
         * @return Json object.
         */
        private JsonObject mockNotification(
            final String reason, final String url,
            final String lastCommentUrl, final String repoFullName
        ) {
            return Json.createObjectBuilder()
                .add("reason", reason)
                .add(
                    "subject",
                    Json.createObjectBuilder()
                        .add("url", url)
                        .add("latest_comment_url", lastCommentUrl)
                        .build()
                ).add(
                    "repository",
                    Json.createObjectBuilder()
                        .add("full_name", repoFullName)
                        .build()
                ).build();
        }
    }

    /**
     * Notifications that do not contain valid "mention" ones; used for unit tests.
     */
    final static class FakeOtherNotifications implements Notifications {

        @Override
        public List<JsonObject> fetch() throws IOException {
            final List<JsonObject> notifications = new ArrayList<JsonObject>();
            notifications.add(
                this.mockNotification(
                    "fork", "/url/here/4",
                    "last/comment/url/5", "amihaiemil/charles"
                )
            );
            notifications.add(
                this.mockNotification(
                    "mention", "/same/here/7",
                    "/same/here/7", "amihaiemil/charles"
                )
            );
            notifications.add(
                this.mockNotification(
                    "star", "/url/here/asd/15",
                    "last/comment/url/20", "amihaiemil/charles"
                )
            );
            return notifications;
        }

        @Override
        public void markAsRead() throws IOException {
            //nothing to do.            
        }
        
        /**
         * Mock a notification json for unit tests.
         * @param reason Reason of it.
         * @param url Url
         * @param lastCommentUrl Last comment url.
         * @param repoFullName Repo name e.g. amihaiemil/eva
         * @return Json object.
         */
        private JsonObject mockNotification(
            final String reason, final String url,
            final String lastCommentUrl, final String repoFullName
        ) {
            return Json.createObjectBuilder()
                .add("reason", reason)
                .add(
                    "subject",
                    Json.createObjectBuilder()
                        .add("url", url)
                        .add("latest_comment_url", lastCommentUrl)
                        .build()
                ).add(
                    "repository",
                    Json.createObjectBuilder()
                        .add("full_name", repoFullName)
                        .build()
                ).build();
        }
    }
}
