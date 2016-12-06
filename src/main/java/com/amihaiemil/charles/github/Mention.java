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
import javax.json.JsonObject;

/**
 * A notification can have a "mention" reason, meaning
 * the logged in user was mentioned in a comment.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public final class Mention implements Reason {

    /**
     * Filter for notifications that have the reason "mention" and also
     * have <b>different</b> url and last_comment_url. This added condition
     * comes because once a "mention" notification is received (due to a
     * mention in a comment), notifications will come from that issue for other
     * reasons as well, such as issue close, repoen etc. If url and
     * last_comment_url are the same it means we have a "subsequent"
     * notification which we have to ignore. If they differ,
     * it means a new comment was added, so we check it.
     * @param notification Unfiltered notifications.
     */
    @Override
    public List<JsonObject> filter(List<JsonObject> notifications) {
        List<JsonObject> filtered = new ArrayList<JsonObject>();
        for (JsonObject notification : notifications) {
            if ("mention".equals(notification.getString("reason"))) {
                JsonObject subject = notification.getJsonObject("subject"); 
                if (
                    !subject.getString("url").equals(
                        subject.getString("latest_comment_url")
                    )
                ) {
                    filtered.add(notification);
                }
            }
        }
        return filtered;
    }

}
