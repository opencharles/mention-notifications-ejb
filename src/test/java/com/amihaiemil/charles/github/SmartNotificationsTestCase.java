/**
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
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link SmartNotifications}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $id$
 * @since 1.2.0
 */
public final class SmartNotificationsTestCase {
    
    /**
     * SmartNotifications can skip 3 checks if the the previous
     * 3 notifications came out empty.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void skipsThreeChecksAfterThreeEmptyChecks() throws IOException {
        List<JsonObject> found = new ArrayList<>();
        found.add(
            Json.createObjectBuilder().add("found", "testnotification").build()
        );
        Notifications three = Mockito.mock(Notifications.class);
        Mockito.when(three.fetch())
            .thenReturn(new ArrayList<JsonObject>())
            .thenReturn(new ArrayList<JsonObject>())
            .thenReturn(new ArrayList<JsonObject>())
            .thenReturn(found);
        Notifications smart = new SmartNotifications(three);
        for(int i=0;i<6;i++) {
            MatcherAssert.assertThat(
                smart.fetch(), Matchers.is(Matchers.empty())
            );
        }
        List<JsonObject> fetched = smart.fetch();
        MatcherAssert.assertThat(fetched.size(), Matchers.is(1));
        MatcherAssert.assertThat(
            fetched.get(0).getString("found"),
            Matchers.equalTo("testnotification")
        );
        Mockito.verify(three, Mockito.times(4)).fetch();
    }
    
    /**
     * SmartNotifications continues fetching notifications if less than 3
     * empty checks happened in a row.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void doesntSkipAfterTwoEmptyChecks() throws IOException {
        List<JsonObject> found = new ArrayList<>();
        found.add(
            Json.createObjectBuilder().add("found", "testnotification").build()
        );
        Notifications three = Mockito.mock(Notifications.class);
        Mockito.when(three.fetch())
            .thenReturn(new ArrayList<JsonObject>())
            .thenReturn(new ArrayList<JsonObject>())
            .thenReturn(found);
        Notifications smart = new SmartNotifications(three);
        MatcherAssert.assertThat(
            smart.fetch(), Matchers.is(Matchers.empty())
        );
        MatcherAssert.assertThat(
            smart.fetch(), Matchers.is(Matchers.empty())
        );
        MatcherAssert.assertThat(
            smart.fetch(), Matchers.not(Matchers.is(Matchers.empty()))
        );
        MatcherAssert.assertThat(
            smart.fetch(), Matchers.not(Matchers.is(Matchers.empty()))
        );
        MatcherAssert.assertThat(
            smart.fetch(), Matchers.not(Matchers.is(Matchers.empty()))
        );
        MatcherAssert.assertThat(
            smart.fetch(), Matchers.not(Matchers.is(Matchers.empty()))
        );
        Mockito.verify(three, Mockito.times(6)).fetch();
    }
}
