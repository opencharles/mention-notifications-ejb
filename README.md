# mention-notifications-ejb
An EJB that reads and posts Github mention notifications.

More specifically, it checks for the logged-in user's notifications of type "mention" and posts them to a specified REST endpoint in simplified format. Only the ``repoFullName`` and ``issueNumber`` are specified - the receiver then has to implement the look-up logic in order to find and handle the proper mentioning comment. 

Other info returned by the Github API in a Notification object would be rather useless payload since it consists mostly of links that the receiver can build on its own knowing the repo name and issue number.

The main use of such a checker would be together with a Github bot account; naturally, the bot has to act upon received notifications. The bot implementation would have a rest POST endpoint to receive the notifications send by this checker.

I use this in one of my projects so far and will probably use it again with others. It was initially a part of said repository but I decided to pull it out and make it reusable.

Why not [Github WebHooks](https://developer.github.com/webhooks/)?
A few reasons:
- When I first wrote it I didn't even know about the webhooks and when I had it written I decided that I'd rather have the checker configurable (you can configure this EJB to check at any interval of minutes) and extendable - a class can easely be added to also handle other type of notifications.
- I don't want the users of my bot to have to configure their repos and setup the hooks.
- Smaller load, since this checker only sends the required info.
