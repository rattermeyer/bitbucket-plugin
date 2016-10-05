package com.cloudbees.jenkins.plugins;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BitbucketPayloadProcessor {

    private final BitbucketJobProbe probe;

    public BitbucketPayloadProcessor(BitbucketJobProbe probe) {
        this.probe = probe;
    }

    public BitbucketPayloadProcessor() {
        this(new BitbucketJobProbe());
    }


    /**
     * Three different types of payload are supported:
     * - Bitbucket - Event Payloads
     * - Bitbucket - Post Service Management
     * - Bitbucket Server - Post Service Webhook for Bitbucket Server
     * @param payload
     * @param request
     */
    public void processPayload(JSONObject payload, HttpServletRequest request) {
        // TODO make distinction based on header??
        if (payload.has("repository") && payload.getJSONObject("repository").has("slug")) {
            LOGGER.log(Level.INFO, "Processing Bitbucker Server Webhook payload");
            processBitbucketServerPostServicePayload(payload);
        }
        else if ("Bitbucket-Webhooks/2.0".equals(request.getHeader("user-agent"))) {
            if ("repo:push".equals(request.getHeader("x-event-key"))) {
                LOGGER.log(Level.INFO, "Processing new Webhooks payload");
                processWebhookPayload(payload);
            }
        } else {
            LOGGER.log(Level.INFO, "Processing old POST service payload");
            processPostServicePayload(payload);
        }
    }


/*
Bitbucket - Event Payloads
https://confluence.atlassian.com/bitbucket/event-payloads-740262817.html
{
  "actor": User,
  "repository": Repository,
  "push": {
    "changes": [
      {
        "new": {
          "type": "branch",
          "name": "name-of-branch",
          "target": {
            "type": "commit",
            "hash": "709d658dc5b6d6afcd46049c2f332ee3f515a67d",
            "author": User,
            "message": "new commit message\n",
            "date": "2015-06-09T03:34:49+00:00",
            "parents": [
              {
                "type": "commit",
                "hash": "1e65c05c1d5171631d92438a13901ca7dae9618c",
                "links": {
                  "self": {
                    "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commit/8cbbd65829c7ad834a97841e0defc965718036a0"
                  },
                  "html": {
                    "href": "https://bitbucket.org/user_name/repo_name/commits/8cbbd65829c7ad834a97841e0defc965718036a0"
                  }
                }
              }
            ],
            "links": {
              "self": {
                "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commit/c4b2b7914156a878aa7c9da452a09fb50c2091f2"
              },
              "html": {
                "href": "https://bitbucket.org/user_name/repo_name/commits/c4b2b7914156a878aa7c9da452a09fb50c2091f2"
              }
            },
          },
          "links": {
            "self": {
              "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/refs/branches/master"
            },
            "commits": {
              "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commits/master"
            },
            "html": {
              "href": "https://bitbucket.org/user_name/repo_name/branch/master"
            }
          }
        },
        "old": {
          "type": "branch",
          "name": "name-of-branch",
          "target": {
            "type": "commit",
            "hash": "1e65c05c1d5171631d92438a13901ca7dae9618c",
            "author": User,
            "message": "old commit message\n",
            "date": "2015-06-08T21:34:56+00:00",
            "parents": [
              {
                "type": "commit",
                "hash": "e0d0c2041e09746be5ce4b55067d5a8e3098c843",
                "links": {
                  "self": {
                    "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commit/9c4a3452da3bc4f37af5a6bb9c784246f44406f7"
                  },
                  "html": {
                    "href": "https://bitbucket.org/user_name/repo_name/commits/9c4a3452da3bc4f37af5a6bb9c784246f44406f7"
                  }
                }
              }
            ],
            "links": {
              "self": {
                "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commit/b99ea6dad8f416e57c5ca78c1ccef590600d841b"
              },
              "html": {
                "href": "https://bitbucket.org/user_name/repo_name/commits/b99ea6dad8f416e57c5ca78c1ccef590600d841b"
              }
            }
          },
          "links": {
            "self": {
              "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/refs/branches/master"
            },
            "commits": {
              "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commits/master"
            },
            "html": {
              "href": "https://bitbucket.org/user_name/repo_name/branch/master"
            }
          }
        },
        "links": {
          "html": {
            "href": "https://bitbucket.org/user_name/repo_name/branches/compare/c4b2b7914156a878aa7c9da452a09fb50c2091f2..b99ea6dad8f416e57c5ca78c1ccef590600d841b"
          },
          "diff": {
            "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/diff/c4b2b7914156a878aa7c9da452a09fb50c2091f2..b99ea6dad8f416e57c5ca78c1ccef590600d841b"
          },
          "commits": {
            "href": "https://api.bitbucket.org/2.0/repositories/user_name/repo_name/commits?include=c4b2b7914156a878aa7c9da452a09fb50c2091f2&exclude=b99ea6dad8f416e57c5ca78c1ccef590600d841b"
          }
        },
        "created": false,
        "forced": false,
        "closed": false,
        "commits": [
          {
            "hash": "03f4a7270240708834de475bcf21532d6134777e",
            "type": "commit",
            "message": "commit message\n",
            "author": User,
            "links": {
              "self": {
                "href": "https://api.bitbucket.org/2.0/repositories/user/repo/commit/03f4a7270240708834de475bcf21532d6134777e"
              },
              "html": {
                "href": "https://bitbucket.org/user/repo/commits/03f4a7270240708834de475bcf21532d6134777e"
              }
            }
          }
        ],
        "truncated": false
      }
    ]
  }
}
 */
    private void processWebhookPayload(JSONObject payload) {
        if (payload.has("repository")) {
            JSONObject repo = payload.getJSONObject("repository");
            LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);

            String user = payload.getJSONObject("actor").getString("username");
            String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = repo.has("scm") ? repo.getString("scm") : "git";

            probe.triggerMatchingJobs(user, url, scm, payload.toString());
        } else if (payload.has("scm")) {
            LOGGER.log(Level.INFO, "Received commit hook notification for hg: {0}", payload);
            String user = payload.getJSONObject("owner").getString("username");
            String url = payload.getJSONObject("links").getJSONObject("html").getString("href");
            String scm = payload.has("scm") ? payload.getString("scm") : "hg";

            probe.triggerMatchingJobs(user, url, scm, payload.toString());
        }

    }

/*
Bitbucket - Post Service Management
https://confluence.atlassian.com/bitbucket/post-service-management-223216518.html

{
    "canon_url": "https://bitbucket.org",
    "commits": [
        {
            "author": "marcus",
            "branch": "master",
            "files": [
                {
                    "file": "somefile.py",
                    "type": "modified"
                }
            ],
            "message": "Added some more things to somefile.py\n",
            "node": "620ade18607a",
            "parents": [
                "702c70160afc"
            ],
            "raw_author": "Marcus Bertrand <marcus@somedomain.com>",
            "raw_node": "620ade18607ac42d872b568bb92acaa9a28620e9",
            "revision": null,
            "size": -1,
            "timestamp": "2012-05-30 05:58:56",
            "utctimestamp": "2012-05-30 03:58:56+00:00"
        }
    ],
    "repository": {
        "absolute_url": "/marcus/project-x/",
        "fork": false,
        "is_private": true,
        "name": "Project X",
        "owner": "marcus",
        "scm": "git",
        "slug": "project-x",
        "website": "https://atlassian.com/"
    },
    "user": "marcus"
}
*/
    private void processPostServicePayload(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);

        String user = payload.getString("user");
        String url = payload.getString("canon_url") + repo.getString("absolute_url");
        String scm = repo.getString("scm");

        probe.triggerMatchingJobs(user, url, scm, payload.toString());
    }

/*
Bitbucket Server - Post Service Webhook for Bitbucket Server
https://confluence.atlassian.com/bitbucketserver/post-service-webhook-for-bitbucket-server-776640367.html
{
   "repository":{
      "slug":"iridium-parent",
      "id":11,
      "name":"iridium-parent",
      "scmId":"git",
      "state":"AVAILABLE",
      "statusMessage":"Available",
      "forkable":true,
      "project":{
         "key":"IR",
         "id":21,
         "name":"Iridium",
         "public":false,
         "type":"NORMAL",
         "isPersonal":false
      },
      "public":false
   },
   "refChanges":[
      {
         "refId":"refs/heads/master",
         "fromHash":"2c847c4e9c2421d038fff26ba82bc859ae6ebe20",
         "toHash":"f259e9032cdeb1e28d073e8a79a1fd6f9587f233",
         "type":"UPDATE"
      }
   ],
   "changesets":{
      "size":1,
      "limit":100,
      "isLastPage":true,
      "values":[
         {
            "fromCommit":{
               "id":"2c847c4e9c2421d038fff26ba82bc859ae6ebe20",
               "displayId":"2c847c4"
            },
            "toCommit":{
               "id":"f259e9032cdeb1e28d073e8a79a1fd6f9587f233",
               "displayId":"f259e90",
               "author":{
                  "name":"jhocman",
                  "emailAddress":"jhocman@atlassian.com"
               },
               "authorTimestamp":1374663446000,
               "message":"Updating poms ...",
               "parents":[
                  {
                     "id":"2c847c4e9c2421d038fff26ba82bc859ae6ebe20",
                     "displayId":"2c847c4"
                  }
               ]
            },
            "changes":{
               "size":2,
               "limit":500,
               "isLastPage":true,
               "values":[
                  {
                     "contentId":"2f259b79aa7e263f5829bb6e98096e7ec976d998",
                     "path":{
                        "components":[
                           "iridium-common",
                           "pom.xml"
                        ],
                        "parent":"iridium-common",
                        "name":"pom.xml",
                        "extension":"xml",
                        "toString":"iridium-common/pom.xml"
                     },
                     "executable":false,
                     "percentUnchanged":-1,
                     "type":"MODIFY",
                     "nodeType":"FILE",
                     "srcExecutable":false,
                     "link":{
                        "url":"/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-common/pom.xml",
                        "rel":"self"
                     }
                  },
                  {
                     "contentId":"2f259b79aa7e263f5829bb6e98096e7ec976d998",
                     "path":{
                        "components":[
                           "iridium-magma",
                           "pom.xml"
                        ],
                        "parent":"iridium-magma",
                        "name":"pom.xml",
                        "extension":"xml",
                        "toString":"iridium-magma/pom.xml"
                     },
                     "executable":false,
                     "percentUnchanged":-1,
                     "type":"MODIFY",
                     "nodeType":"FILE",
                     "srcExecutable":false,
                     "link":{
                        "url":"/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-magma/pom.xml",
                        "rel":"self"
                     }
                  }
               ],
               "start":0,
               "filter":null
            },
            "link":{
               "url":"/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-magma/pom.xml",
               "rel":"self"
            }
         }
      ],
      "start":0,
      "filter":null
   }
}
 */

    private void processBitbucketServerPostServicePayload(JSONObject payload) {
        JSONObject repo = payload.getJSONObject("repository");
        LOGGER.log(Level.INFO, "Received commit hook notification for {0}", repo);
        LOGGER.log(Level.FINEST, "Received commit hook notification for {0}", repo.toString() + " with payload " + payload.toString());

        JSONArray changesetsValues = payload.getJSONObject("changesets").getJSONArray("values");
        JSONObject changesetLastValue = (JSONObject) changesetsValues.get(changesetsValues.size() - 1);
        String user = changesetLastValue.getJSONObject("toCommit").getJSONObject("author").getString("name");
        JSONObject lastSelf = (JSONObject) changesetLastValue.getJSONObject("links").getJSONArray("self").get(0);
        String href = lastSelf.getString("href");
        URL hrefURL = null;
        try {
            hrefURL = new URL(href);

            JSONObject project = repo.getJSONObject("project");
            String url = (hrefURL.getProtocol() + "://" + hrefURL.getHost()  + "/scm/" +  project.getString("key") + "/" + repo.getString("slug"));
            String scm = repo.getString("scmId");

            probe.triggerMatchingJobs(user, url, scm, payload.toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Could not parse repository uri: {0}, {1}", new Object[]{href, e});
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketPayloadProcessor.class.getName());

}
