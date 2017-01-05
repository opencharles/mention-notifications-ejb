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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;

import org.junit.Test;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;

/**
 * Unit tests for {@link NtPost}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
@SuppressWarnings("resource")
public final class NtPostTestCase {

    /**
     * NtPost  can send notifications successfully.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsNotificationsOk() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
            .start(port);
        try {
        	Notifications notifications = new Notifications.FakeOtherNotifications();
            Authorization fake = new Authorization.Fake();
        	Post ntp = new NtPost(
            	notifications, fake, "http://localhost:"+port+"/"
            );
            ntp.send();
            MkQuery request = server.take();
            String auth = request.headers().get(HttpHeaders.AUTHORIZATION).get(0);
            assertTrue(auth.contains(fake.token()));
            JsonArray received = Json.createReader(
                new ByteArrayInputStream(request.binary())
            ).readArray();
            assertTrue(received != null);
            assertTrue(received.size() > 0);
            JsonObject first = received.getJsonObject(0);
            assertTrue(first.keySet().size() == 2);
            assertTrue(first.containsKey("repoFullName"));
            assertTrue(first.containsKey("issueNumber"));
            assertTrue(first.getInt("issueNumber") != 0);
        } finally {
            server.stop();
        }
    }

    /**
     * NtPost does not make a http request if there are no notifications..
     * @throws Exception If something goes wrong.
     */
    @Test
    public void doesntSendEmptyParcel() throws Exception {
        Notifications notifications = new Notifications.FakeEmptyNotifications();
        Authorization fake = new Authorization.Fake();
        Post ntp = new NtPost(
            notifications, fake, "http://localhost:8080/"
        );
        ntp.send();
    }
    
    /**
     * NtPost handles unauthorized response.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsNotificationsWithUnauthorized() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_UNAUTHORIZED))
            .start(port);
        try {
        	Notifications notifications = new Notifications.FakeErrorOnMarkRead();
            Authorization fake = new Authorization.Fake();
        	Post ntp = new NtPost(
            	notifications, fake, "http://localhost:"+port+"/"
            );
            ntp.send();
            MkQuery request = server.take();
            String auth = request.headers().get(HttpHeaders.AUTHORIZATION).get(0);
            assertTrue(auth.contains(fake.token()));
        } finally {
            server.stop();
        }
    }
    
    /**
     * NtPost throws assertion error on unexpected status response.
     * @throws Exception If something goes wrong.
     */
    @Test (expected = AssertionError.class)
    public void sendsNotificationsWithServerError() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .start(port);
        try {
        	Notifications notifications = new Notifications.FakeErrorOnMarkRead();
            Authorization fake = new Authorization.Fake();
        	Post ntp = new NtPost(
            	notifications, fake, "http://localhost:"+port+"/"
            );
            ntp.send();
            MkQuery request = server.take();
            String auth = request.headers().get(HttpHeaders.AUTHORIZATION).get(0);
            assertTrue(auth.contains(fake.token()));
        } finally {
            server.stop();
        }
    }
    
    /**
     * Find a free port.
     * @return A free port.
     * @throws IOException If something goes wrong.
     */
    private int port() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
