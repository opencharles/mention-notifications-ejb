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
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import javax.json.JsonObject;

import org.apache.http.HttpStatus;
import org.junit.Test;

/**
 * Unit tests for {@link RtNotifications}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $id$
 * @since 1.0.0
 */
@SuppressWarnings("resource")
public class RtNotificationsTestCase {

    /**
     * RtNotifications can fetch the notifications from the server.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void fetchesNotifications() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("[{\"notification\":\"first\"},{\"notification\":\"second\"}]")).start(port);
        try {
            Notifications notifications = new RtNotifications(
                new Reason.Fake(), "token", "http://localhost:"+port+"/"
            );
            List<JsonObject> found = notifications.fetch();
            assertTrue(found.size() == 2);
            assertTrue(found.get(0).getString("notification").equals("first"));
            assertTrue(found.get(1).getString("notification").equals("second"));
        } finally {
            server.stop();
        }
    }
    
    /**
     * RtNotifications.fetch throws assertion error when the response status is not OK
     * @throws Exception If something goes wrong.
     */
    @Test (expected = AssertionError.class)
    public void fetchesNotificationsWithError() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpStatus.SC_BAD_REQUEST)).start(port);
        try {
            Notifications notifications = new RtNotifications(
                new Reason.Fake(), "token", "http://localhost:"+port+"/"
            );
            notifications.fetch();
        } finally {
            server.stop();
        }
    }

    /**
     * RtNotifications can fetch 0 notifications from the server.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void nothingFetched() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("[]")).start(port);
        try {
            Notifications notifications = new RtNotifications(
                new Reason.Fake(), "token", "http://localhost:"+port+"/"
            );
            List<JsonObject> found = notifications.fetch();
            assertTrue(found.size() == 0);
        } finally {
            server.stop();
        }
    }
    
    /**
     * RtNotifications can mark notifications as read with OK status response
     * @throws Exception If something goes wrong.
     */
    @Test
    public void markAsReadOk() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpStatus.SC_OK)).start(port);
        try {
            Notifications notifications = new RtNotifications(
                new Reason.Fake(), "token", "http://localhost:"+port+"/"
            );
            notifications.markAsRead();
            MkQuery req = server.take();
            assertTrue(req.method().equals(Request.PUT));
            assertTrue(req.body().equals("{}"));
            assertTrue(req.uri().getQuery().contains("last_read_at="));
        } finally {
            server.stop();
        }
    }

    /**
     * RtNotifications can mark notifications as read with RESET status response.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void markAsReadReset() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpStatus.SC_RESET_CONTENT)).start(port);
        try {
            Notifications notifications = new RtNotifications(
                new Reason.Fake(), "token", "http://localhost:"+port+"/"
            );
            notifications.markAsRead();
            MkQuery req = server.take();
            assertTrue(req.method().equals(Request.PUT));
            assertTrue(req.body().equals("{}"));
            assertTrue(req.uri().getQuery().contains("last_read_at="));
        } finally {
            server.stop();
        }
    }

    /**
     * RtNotifications.markAsRead throws assertion error when there is an
     * unexpected status response.
     * @throws Exception If something goes wrong.
     */
    @Test (expected = AssertionError.class)
    public void markAsReadServerNotOk() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpStatus.SC_INTERNAL_SERVER_ERROR)).start(port);
        try {
            Notifications notifications = new RtNotifications(
                new Reason.Fake(), "token", "http://localhost:"+port+"/"
            );
            notifications.markAsRead();
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
