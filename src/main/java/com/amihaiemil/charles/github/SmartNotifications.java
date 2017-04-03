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

import javax.json.JsonObject;

/**
 * Smart notifications. If there are no new notifications for a few
 * checks in a row, it will skip a few next checks, thus saving bandwidth
 * and making less HTTP calls. This is all based on the assumption that
 * notifications aren't so frequent, so it makes no sense to always, blindly
 * repeat the cycle.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.2.0
 * @todo #13:30m/DEV Let's make this configurable. Currently it is
 *  hard-coded to 3 empty Checks. It should configurable via the system
 *  property skip.empty.notifications.
 */
public final class SmartNotifications implements Notifications {

    /**
     * Original notifications to use.
     */
    private Notifications original;
    
    /**
     * Count of empty notifications that can pass.
     */
    private int allowedEmpty = 3;
    
    /**
     * Nr of consecutive empty checks.
     */
    private int empty = 0;

    /**
     * Skip this fetch, or not?
     */
    private boolean skip = false;

    /**
     * Ctor.
     * @param orig Given Notifications
     */
    public SmartNotifications(final Notifications orig) {
        this.original = orig;
    }
    
    @Override
    public List<JsonObject> fetch() throws IOException {
        List<JsonObject> notifications = new ArrayList<>();
        if(!this.skip) {
            if(this.allowedEmpty == 0) {
                this.allowedEmpty = 3;
                this.empty = 0;
            }
            notifications = this.original.fetch();
            if(notifications.isEmpty()) {
                this.empty++;
            } else {
                this.empty = 0;
            }
        } else {
            this.allowedEmpty--;
        }
        this.skip = this.empty >= this.allowedEmpty && this.allowedEmpty > 0;
        return notifications;
    }

    @Override
    public void markAsRead() throws IOException {
        this.original.markAsRead();        
    }

}
