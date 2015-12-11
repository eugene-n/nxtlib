![http://nxtlib.googlecode.com/svn/wiki/LOGO_NXT.jpg](http://nxtlib.googlecode.com/svn/wiki/LOGO_NXT.jpg)

This client library will support both bluetooth and USB connections (via external code). It will provide access to all of the documented commands supported by the NXT communications protocol. Another goal for this project is to keep the library compatible with CLDC 1.1 (for mobile phone support).

For bluetooth support, I've written test code using bluecove.jar (which implements JSR-082 on Win XP). I haven't tested with USB. If someone can point me to a java USB client for windows, I'd be happy to test that. I just have so many things going, I haven't had time to get that tracked down.


NOTE: Subversion has been populated. I've built a distribution (including jar and javadoc). Functionality is limited to commands that have been coded so far. The test code is for bluetooth and is a bit rough.