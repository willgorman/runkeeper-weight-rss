# runkeeper-weight-rss

Suppose that you got a cheap Bluetooth scale at Big Box Warehouse Chain.  Suppose that the scale only supported sending data to RunKeeper but you really wanted to use Fitbit.  Suppose you thought to yourself "Hey, I'll write a quick app to create an RSS feed of the weight measurements from my RunKeeper account and then use IFTTT to send them into my Fitbit account."  You might just end up with something pretty similar to this.  I suppose.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## Configuration

The following environment variables must be set:

* `CLIENT-ID` - RunKeeper OAuth client identifier
* `CLIENT-SECRET` - RunKeeper OAuth client secret
* `TOKEN` - User OAuth token
* `FEED-URL` - Relative path to where the RSS feed should be offered.  Since the feed is publicly accessible this is configurable rather than hard-coded just to allow for the maximum level of obsecurity.
