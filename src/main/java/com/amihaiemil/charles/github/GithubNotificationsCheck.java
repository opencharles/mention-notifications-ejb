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
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB that checks periodically for github notifications (mentions of the agent using @username).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: 3d9b4dcc6885f853200060cecd9ce7d0d33ed129 $
 * @since 1.0.0
 * 
 */
@Singleton
@Startup
public class GithubNotificationsCheck {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GithubNotificationsCheck.class.getName());

    /**
     * Java EE timer service.
     */
    @Resource
    private TimerService timerService;

    /**
     * Posts of notifications.
     */
    private final Post[] posts;

    /**
     * Default Ctor.
     */
    public GithubNotificationsCheck() {
        this.posts = new FromSystem().posts();
    }

    /**
     * After this bean is constructed the checks are scheduled at a given
     * interval (minutes).
     */
    @PostConstruct
    public void schedule() {
        String checksInterval = System.getProperty("checks.interval.minutes");
        int intervalMinutes = 2;
        if(checksInterval != null && !checksInterval.isEmpty()) {
            try {
                intervalMinutes = Integer.parseInt(checksInterval);
                log.info("The check for Github notifications will be performed every " + intervalMinutes + " minutes!");
            } catch (NumberFormatException ex) {
                log.error("NumberFormatException when parsing interval " + checksInterval, ex);
            }
        }
        this.timerService.createTimer(1000*60*intervalMinutes, 1000*60*intervalMinutes, null);
    }

    /**
     * When the timeout occurs, post the notifications from Github somewhere.
     */
    @Timeout
    public void check() {
        try {
            for(final Post post : this.posts) {
                post.send();
            }
        } catch (IOException e) {
            log.error("IOException when checking or sending notifications: ", e);
        } catch (AssertionError err) {
            log.error("Unexpected status response when checking or sending notifications: ", err);
        }
    }
    
}
