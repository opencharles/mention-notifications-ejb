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

/**
 * The checker does each post based on System Properties.
 * System properties github.auth.tokens and post.endpoints each contain endpoints/
 * tokens separated by ';'. A is a Post for each pair.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public final class FromSystem implements Posts{

    private String[] githubTokens;
    
    private String[] postEndpoints;
    
    public FromSystem() {
        String github = System.getProperty("github.auth.tokens", "");
        String endpoints = System.getProperty("post.endpoints", "");
        if(github.isEmpty() || endpoints.isEmpty()) {
            throw new IllegalStateException(
                "Both github.auth.tokens and post.endpoints System Properties"
                + " are mandatory!"
            );
        } else {
            this.githubTokens = github.split(";");
            this.postEndpoints = endpoints.split(";");
            if(this.githubTokens.length != this.postEndpoints.length) {
                throw new IllegalStateException(
                    "github.auth.tokens and post.endpoints should have "
                    + "the same number of elements, separated by ';'!"
                );
            }
        }
    }
    
    @Override
    public Post[] posts() {
        final Post[] posts = new NtPost[this.githubTokens.length];
        for(int i=0; i< githubTokens.length;i++) {
            posts[i] = new NtPost(
                new SmartNotifications(
                    new RtNotifications(
                        new Mention(),
                        "token " + this.githubTokens[i].trim(),
                        "https://api.github.com/notifications"
                    )
                ),
                this.githubTokens[i].trim(),
                this.postEndpoints[i].trim()
             );
        }
        return posts;
    }
    
}
