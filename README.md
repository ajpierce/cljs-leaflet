# cljs-leaflet

A playground/laboratory for using clojurescript to manipulate leaflet maps

## Setup

### Add Secrets File
Add the following code to `src/cljs_leaflet/secrets.cljs`, replacing `your_api_key` with a valid Mapbox API key:

```clojure
(ns cljs-leaflet.secrets)
(defonce mapbox-api-key "your_api_key")
```

## Development

Open a terminal and type `lein repl` to start a Clojure REPL
(interactive prompt).

In the REPL, type

```clojure
(run)
(browser-repl)
```

The call to `(run)` starts the Figwheel server at port 3449, which takes care of
live reloading ClojureScript code and CSS. Figwheel's server will also act as
your app server, so requests are correctly forwarded to the http-handler you
define.

Running `(browser-repl)` starts the Weasel REPL server, and drops you into a
ClojureScript REPL. Evaluating expressions here will only work once you've
loaded the page, so the browser can connect to Weasel.

When you see the line `Successfully compiled "resources/public/app.js" in 21.36
seconds.`, you're ready to go. Browse to `http://localhost:3449` and enjoy.

**Attention: It is not needed to run `lein figwheel` separately. Instead we
launch Figwheel directly from the REPL**

## Trying it out

If all is well you now have a browser window saying 'Hello Chestnut',
and a REPL prompt that looks like `cljs.user=>`.

Open `resources/public/css/style.css` and change some styling of the
H1 element. Notice how it's updated instantly in the browser.

Open `src/cljs/cljs-leaflet/core.cljs`, and change `dom/h1` to
`dom/h2`. As soon as you save the file, your browser is updated.

In the REPL, type

```
(ns cljs-leaflet.core)
(swap! app-state assoc :text "Interactivity FTW")
```

Notice again how the browser updates.

## IDEs
I'm a VIM guy myself, but Chestnut has this to say about IDEs:

### Lighttable

Lighttable provides a tighter integration for live coding with an inline
browser-tab. Rather than evaluating cljs on the command line with weasel repl,
evaluate code and preview pages inside Lighttable.

Steps: After running `(run)`, open a browser tab in Lighttable. Open a cljs file
from within a project, go to the end of an s-expression and hit Cmd-ENT.
Lighttable will ask you which client to connect. Click 'Connect a client' and
select 'Browser'. Browse to [http://localhost:3449](http://localhost:3449)

View LT's console to see a Chrome js console.

Hereafter, you can save a file and see changes or evaluate cljs code (without saving a file). Note that running a weasel server is not required to evaluate code in Lighttable.

### Emacs/Cider

Start a repl in the context of your project with `M-x cider-jack-in`.

Switch to repl-buffer with `C-c C-z` and start web and figwheel servers with
`(run)`, and weasel server with `(browser-repl`). Load
[http://localhost:3449](http://localhost:3449) on an external browser, which
connects to weasel, and start evaluating cljs inside Cider.

To run the Clojurescript tests, do

```
lein doo phantom
```

## MIT License

Copyright © 2016 Andrew Pierce

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Chestnut

Created with [Chestnut](http://plexus.github.io/chestnut/) 0.11.0 (3b671cf8).
