# mention-notifications-ejb
[![Build Status](https://travis-ci.org/opencharles/mention-notifications-ejb.svg?branch=master)](https://travis-ci.org/opencharles/mention-notifications-ejb)
[![Coverage Status](https://coveralls.io/repos/github/opencharles/mention-notifications-ejb/badge.svg?branch=master)](https://coveralls.io/github/opencharles/mention-notifications-ejb?branch=master)

An EJB that reads and posts Github mention notifications.

More specifically, it checks for the logged-in user's notifications of type "mention" and posts them to a specified REST endpoint in **simplified format**. Only the ``repoFullName`` and ``issueNumber`` are sent - the receiver then has to implement the look-up logic in order to find and handle the proper mentioning comment. 

Other info returned by the Github API in a Notification object would be rather useless payload since it consists mostly of links that the receiver can build on its own knowing the repo name and issue number.

## The main use
The use of such a checker would be together with a Github bot account; naturally, the bot has to act upon received notifications. The bot implementation would have a rest POST endpoint to receive the notifications sent by this checker.

## Authorization
As the table at the end says, you need to specify the system property **github.auth.token**. This is used in two places:

When calling the Github API to fetch notifications.

and

When making the HTTP Post, the checker also adds the **Authorization** header containing **github.auth.token**. This is for 2 reasons:

1. Security. You want to make sure nobody starts POSTing randomly to your receiver's endpoint.
2. To make sure that both the checker and the bot use the same Github account. It wouldn't make a lot of sense for account A to act on the notifications of account B.

However, this is up to you to implement - you can leave the receiver's POST endpoint open and forget about this part, but I guess you don't go in vacation leaving your house unlocked, do you?

## How I use it
I use this in one of my projects so far and will probably use it again with others. It was initially a part of said repository but I decided to pull it out and make it reusable.

BTW, I implement all the Github interaction using [this](https://github.com/jcabi/jcabi-github/) awesome library. Check it out, it also offers a mock version of the API so you can unit test your code instantly.

Why not [Github WebHooks](https://developer.github.com/webhooks/)?
A few reasons:
- When I first wrote it I didn't even know about the webhooks and when I had it written I decided that I'd rather have the checker configurable (you can configure this EJB to check at any interval of minutes) and extendable - a class can easely be added to also handle other type of notifications.
- I don't want the users of my bot to have to configure their repos and setup the hooks.
- Smaller load, since this checker only sends the required info.

## Deployment
This is designed as a single ejb jar, to be deployed on a single server so for this, take the <a href="https://oss.sonatype.org/service/local/repositories/releases/content/com/amihaiemil/web/mention-notifications-ejb/1.1.0/mention-notifications-ejb-1.1.0-jar-with-dependencies.jar">fat</a>
jar.

**It should work on any webserver** (e.g. Glassfish, Jboss, Payara, WebSphere even), it doesn't rely on proprietary property files or dubious assembly xml files. I spin it inside a Glassfish.

If, for any reason you want to include it in your ``.war`` and you are using Maven, you can use the dependency

But **keep in mind** the following: if you deploy your package on multiple nodes, make sure to **specify different check intervals**. It doesn't make sense to have, let's say, 5 checkers spinning, each fetching notifications from Github at the same time.

```
<dependency>
    <groupId>com.amihaiemil.web</groupId>
    <artifactId>mention-notifications-ejb</artifactId>
    <version>1.1.0</version>
</dependency>
```

You will need to set the following system properties. **Pay a lot of attention while configuring these, since everything relies on them**.

## EJB notifications checker sys props
<table>
  <tr>
    <th>Name</th><th>Value</th><th>Description</th>
  </tr>
  <tr>
    <td>checks.interval.minutes</td>
    <td>integer</td>
    <td><b>Optional</b>. Minutes that should <br> pass between checks. Defaults to 2.</td>
  </tr>
  <tr>
    <td>post.rest.endpoint</td>
    <td>**path/to/post/resource**/</td>
    <td><b>Mantadory</b>. Rest endpoint <br>where the found notifications should be sent for handling.</td>
  </tr>
  <tr>
    <td>github.auth.token</td>
    <td>string</td>
    <td><b>Mantadory</b>. Github agent's access token. It should have limited permissions. For instance, I only let it fetch notifications, star repos and post comments. <b>Do not give full permissions!</b></td>
  </tr>
  <tr>
    <td>LOG_ROOT</td>
    <td>string</td>
    <td><b>Optional</b>. Place where the log files will be stored. Defaults to . (dot)</td>
  </tr>
</table>

## Contributors wanted
Read [this](http://www.amihaiemil.com/2016/12/30/becoming-a-contributor.html) post for more details.
