# Debuggable Acceptance Tests with JBehave, Selenide, and Guice

This package contains a base for debuggable Selenide acceptance tests. But what means debuggable?

When Java/Scala Selenium tests run into errors they just stop somewhere. Browser closes and that's it. You don't have a chance for
intervention at that moment. You can take a screenshot and save the HTML for later analysis, which might help. But this is
still too low level for effective debugging.

I created this sample project from prior project experiences and provide an example base for acceptance tests based
on [JBehave](http://jbehave.org), Selenide and Google Guice.

The tests just click a bit around on my website, the test coverage is quite small. There is one case running through smoothly and another
case that fails. In this case, a Swing windows comes up and prompts you what to do.

## How is this project different?

You are able to interact when `AsertionError`'s occur. You can retry the action (nice, when doing Ajax),
continue (skip the error) or cancel the whole test run. You do not need to guess where to set a breakpoint in your test suite.
As soon as exceptions occur, you'll be prompted how to proceed.

<img src="images/debug-screen.png" width="400" >

You can retrieve the HTML page source as it is seen by the browser. 

<img src="images/debug-evaluate.png" width="400" >

And you can inspect particular elements using Selenide's JQuery syntax (`$("selector")` or `$("selector")`).

<img src="images/debug-evaluate-jquery-style.png" width="400" >

The code is evaluated using Java's JavaScript engine. Available functions are:

 * `$(...)` and methods/functions of Selenide's `$` result (e. g. `$(a").val()`)
 * `$$(...)` and methods/functions of Selenide's `$$` (e. g. `$("a").size()`)
 * `html()`
 * WebDriver methods/functions exposed by `wd` variable (e. g. `wd.getPageSource()`)

 The result is displayed `toString`'ed.

## Components

This project uses multiple components for its operations.

* Configuration
* JBehave
* Selenide
* Selenium
* Google Guice

### Configuration

All configuration values are stored in `src/main/resources/services.properties`. The Config file uses property substitution and
properties can be overridden by System Properties (they have to be defined in `services.properties` first).

### JBehave

[JBehave](http://jbehave.org) is a framework for Behaviour-Driven Development. JBehave needs story files and Java implementations
to know, what to invoke. See `src/test/stories/paluch.biz.blog.story` and `src/test/java/biz/paluch/testing/acceptance/steps/BlogSteps.java`
for examples.

The included/excluded stories can be controlled by directory/file patterns like `*/**.stories`. See `biz.paluch.testing.acceptance.AcceptanceProperties` for
property names. Included classes for dependency injection (scanner) are configured in `biz.paluch.testing.acceptance.SeleniumJBehaveStories`.

### Selenide
[Selenide](http://selenide.org) is a wrapper around [Selenium](http://docs.seleniumhq.org/projects/webdriver/) with many advantages.
It simplifies the way how to deal with Selenium providing a JQuery-like API and improved AJAX support.

### Google Guice

[Guice](https://github.com/google/guice) provides dependency injection and the interceptor mechanism for `@DebugableInvocation`.


## Running

The easiest way to run the project is using Maven.

`$ mvn clean install`

Since this way is also used for batch execution (on your Continuous Integration server) it does not utilize any debug interaction.
Else this would harm your build process.

If you want to run the project with debug interaction, import it into your favorite IDE and start the class

`biz.paluch.testing.acceptance.StandaloneLauncher` as JUnit test.

It is very likely, that you want to specify a different browser. You can find all constants for supported browsers in the
enum `biz.paluch.testing.acceptance.selenium.Browser`


License
-------
* [The MIT License (MIT)] (http://opensource.org/licenses/MIT)
