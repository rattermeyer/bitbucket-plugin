

package com.cloudbees.jenkins.plugins;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.URIish;

/**
 * Override GitStatus https://github.com/jenkinsci/git-plugin/blob/master/src/main/java/hudson/plugins/git/GitStatus.java
 * in two regards:
 * - ignore case
 * - ignore '/scm/' (as used in Bitbucket Server https URLs)
 */
public class GitStatus {
    /**
     * Used to test if what we have in the job configuration matches what was submitted to the notification endpoint.
     * It is better to match loosely and wastes a few polling calls than to be pedantic and miss the push notification,
     * especially given that Git tends to support multiple access protocols.
     */
    public static boolean looselyMatches(URIish lhs, URIish rhs) {
        return StringUtils.equalsIgnoreCase(lhs.getHost(),rhs.getHost())
                && StringUtils.equalsIgnoreCase(normalizePath(lhs.getPath()), normalizePath(rhs.getPath()));
    }

    private static String normalizePath(String path) {
        if (path.startsWith("/"))   path=path.substring(1);
        if (path.endsWith("/"))     path=path.substring(0,path.length()-1);
        if (path.endsWith(".git"))  path=path.substring(0,path.length()-4);
        path = path.replaceAll("/scm/","/");
        return path;
    }
}
