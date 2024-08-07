Please ensure that every class, every method, and every field contains a Javadoc comment.  Don't assume that the reader is already familiar with jargon.

Please add some tests.  For example, read a file, convert to PCS, and then write it, verifying that the output is identical to the input.  Then, build up the test suite.
Hmm, it looks like the assets/ directory might contain that.  Please ensure that the Gradle "test" task runs them.  Also, the Gradle convention is to use a "resources/" directory rather than an "assets/" directory.

I have a personal preference against wildcard imports, especially when they are not for a well-known library such as the JDK.

I have a preference against the Optional type.  See https://homes.cs.washington.edu/~mernst/advice/nothing-is-better-than-optional.html .

In a Javadoc `@param`, `@return`, etc. clause, the initial text is a sentence fragment that starts with a lowercase (not capital) letter and does not end with a period unless followed by another sentence.

A .*rc file is executable code by convention.  The `.envrc` file seems to contain a comment instead.  Why do I want to run `flake nix`?  What does it do?
