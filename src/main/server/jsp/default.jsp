<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>TeamCity: VCSUpdate</title>
<style type="text/css">
body {background-color: #fff; color: #444; font-family: tahoma, verdana, arial, sans-serif; font-size: 12px}
</style>
</head>
<body>
<p>VCSUpdate is a simple plugin for the TeamCity continuous integration server that
allows for an efficient and flexible method of triggering new builds. Rather
than poll the version control system (VCS) constantly, you can simply call a URL
on the build server to kick off a check for new sources.</p>

<p>You can configure your VCS (such as Subversion) to request the URL on your build
server whenever a developer commits a change. Typically, this would be done with
a post-commit hook and a tool such as <b>wget</b> or <b>curl</b>.</p>

<p>Aside from the obvious benefit of reducing the load on your VCS (by eliminating
constant checks for updates), VCSUpdate actually allows your build server to be
more responsive, since it can be notified immediately whenever the code changes.</p>

<p><b>How to use VCSUpdate</b><br/>
Once you have the plugin installed on your TeamCity server, simply configure
your VCS server to request the <b>vcsupdate.html</b> page, and add parameters for each
of the VCS roots (by ID) that should be queried for changes.You can specify as many
"id" parameters as you would like, separated by the '&' character.</p>

<p>In addition to root IDs you can send build IDs with the "build" parameter. If you do
not pass the "build" parameter then it will mark all builds for a VCS update. You can
specify as many "build" parameters as you would like, separated by the '&' character.</p>

<p>f you're building the plugin yourself and specified a password, you can send it with
the "pass" parameter.</p>
</body>
</html>