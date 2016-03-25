package com.cloudbees.jenkins.plugins;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketPayloadProcessorTest {

    @Mock private HttpServletRequest request;
    @Mock private BitbucketJobProbe probe;

    private BitbucketPayloadProcessor payloadProcessor;

    @Before
    public void setUp() {
        payloadProcessor = new BitbucketPayloadProcessor(probe);
    }

    @Test
    public void testProcessWebhookPayload() {
        // Set headers so that payload processor will parse as new Webhook payload
        when(request.getHeader("user-agent")).thenReturn("Bitbucket-Webhooks/2.0");
        when(request.getHeader("x-event-key")).thenReturn("repo:push");

        String user = "test_user";
        String url = "https://bitbucket.org/test_user/test_repo";

        JSONObject payload = new JSONObject()
            .element("actor", new JSONObject()
                .element("username", user))
            .element("repository", new JSONObject()
                .element("links", new JSONObject()
                    .element("html", new JSONObject()
                        .element("href", url))));

        JSONObject hgLoad = new JSONObject()
            .element("scm", "hg")
            .element("owner", new JSONObject()
                .element("username", user))
            .element("links", new JSONObject()
                .element("html", new JSONObject()
                    .element("href", url)));

        payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs(user, url, "git", payload.toString());

        payloadProcessor.processPayload(hgLoad, request);

        verify(probe).triggerMatchingJobs(user, url, "hg", hgLoad.toString());
    }

    @Test
    public void testProcessPostServicePayload() {
        // Ensure header isn't set so that payload processor will parse as old POST service payload
        when(request.getHeader("user-agent")).thenReturn(null);

        JSONObject payload = new JSONObject()
            .element("canon_url", "https://staging.bitbucket.org")
            .element("user", "old_user")
            .element("repository", new JSONObject()
                .element("scm", "git")
                .element("absolute_url", "/old_user/old_repo"));

        payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs("old_user", "https://staging.bitbucket.org/old_user/old_repo", "git", payload.toString());
    }

    @Test
    public void testBitBucketServerProcessPostServicePayload() {
        // Ensure header isn't set so that payload processor will parse as old POST service payload
        when(request.getHeader("user-agent")).thenReturn(null);
        when(request.getRemoteHost()).thenReturn("https://mybitbucketserver.org");
        // https://confluence.atlassian.com/bitbucketserver/post-service-webhook-for-bitbucket-server-776640367.html
        JSONObject payload = JSONObject.fromObject("{\n" +
                "   \"repository\":{\n" +
                "      \"slug\":\"iridium-parent\",\n" +
                "      \"id\":11,\n" +
                "      \"name\":\"iridium-parent\",\n" +
                "      \"scmId\":\"git\",\n" +
                "      \"state\":\"AVAILABLE\",\n" +
                "      \"statusMessage\":\"Available\",\n" +
                "      \"forkable\":true,\n" +
                "      \"project\":{\n" +
                "         \"key\":\"IR\",\n" +
                "         \"id\":21,\n" +
                "         \"name\":\"Iridium\",\n" +
                "         \"public\":false,\n" +
                "         \"type\":\"NORMAL\",\n" +
                "         \"isPersonal\":false\n" +
                "      },\n" +
                "      \"public\":false\n" +
                "   },\n" +
                "   \"refChanges\":[\n" +
                "      {\n" +
                "         \"refId\":\"refs/heads/master\",\n" +
                "         \"fromHash\":\"2c847c4e9c2421d038fff26ba82bc859ae6ebe20\",\n" +
                "         \"toHash\":\"f259e9032cdeb1e28d073e8a79a1fd6f9587f233\",\n" +
                "         \"type\":\"UPDATE\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"changesets\":{\n" +
                "      \"size\":1,\n" +
                "      \"limit\":100,\n" +
                "      \"isLastPage\":true,\n" +
                "      \"values\":[\n" +
                "         {\n" +
                "            \"fromCommit\":{\n" +
                "               \"id\":\"2c847c4e9c2421d038fff26ba82bc859ae6ebe20\",\n" +
                "               \"displayId\":\"2c847c4\"\n" +
                "            },\n" +
                "            \"toCommit\":{\n" +
                "               \"id\":\"f259e9032cdeb1e28d073e8a79a1fd6f9587f233\",\n" +
                "               \"displayId\":\"f259e90\",\n" +
                "               \"author\":{\n" +
                "                  \"name\":\"jhocman\",\n" +
                "                  \"emailAddress\":\"jhocman@atlassian.com\"\n" +
                "               },\n" +
                "               \"authorTimestamp\":1374663446000,\n" +
                "               \"message\":\"Updating poms ...\",\n" +
                "               \"parents\":[\n" +
                "                  {\n" +
                "                     \"id\":\"2c847c4e9c2421d038fff26ba82bc859ae6ebe20\",\n" +
                "                     \"displayId\":\"2c847c4\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            \"changes\":{\n" +
                "               \"size\":2,\n" +
                "               \"limit\":500,\n" +
                "               \"isLastPage\":true,\n" +
                "               \"values\":[\n" +
                "                  {\n" +
                "                     \"contentId\":\"2f259b79aa7e263f5829bb6e98096e7ec976d998\",\n" +
                "                     \"path\":{\n" +
                "                        \"components\":[\n" +
                "                           \"iridium-common\",\n" +
                "                           \"pom.xml\"\n" +
                "                        ],\n" +
                "                        \"parent\":\"iridium-common\",\n" +
                "                        \"name\":\"pom.xml\",\n" +
                "                        \"extension\":\"xml\",\n" +
                "                        \"toString\":\"iridium-common/pom.xml\"\n" +
                "                     },\n" +
                "                     \"executable\":false,\n" +
                "                     \"percentUnchanged\":-1,\n" +
                "                     \"type\":\"MODIFY\",\n" +
                "                     \"nodeType\":\"FILE\",\n" +
                "                     \"srcExecutable\":false,\n" +
                "                     \"link\":{\n" +
                "                        \"url\":\"/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-common/pom.xml\",\n" +
                "                        \"rel\":\"self\"\n" +
                "                     }\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"contentId\":\"2f259b79aa7e263f5829bb6e98096e7ec976d998\",\n" +
                "                     \"path\":{\n" +
                "                        \"components\":[\n" +
                "                           \"iridium-magma\",\n" +
                "                           \"pom.xml\"\n" +
                "                        ],\n" +
                "                        \"parent\":\"iridium-magma\",\n" +
                "                        \"name\":\"pom.xml\",\n" +
                "                        \"extension\":\"xml\",\n" +
                "                        \"toString\":\"iridium-magma/pom.xml\"\n" +
                "                     },\n" +
                "                     \"executable\":false,\n" +
                "                     \"percentUnchanged\":-1,\n" +
                "                     \"type\":\"MODIFY\",\n" +
                "                     \"nodeType\":\"FILE\",\n" +
                "                     \"srcExecutable\":false,\n" +
                "                     \"link\":{\n" +
                "                        \"url\":\"/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-magma/pom.xml\",\n" +
                "                        \"rel\":\"self\"\n" +
                "                     }\n" +
                "                  }\n" +
                "               ],\n" +
                "               \"start\":0,\n" +
                "               \"filter\":null\n" +
                "            },\n" +
                // / With Atlassian Bitbucket Server v4.4.0 and Bitbucket Web Post Hooks Plugin 3.0.3, I get a different payload documented
                "            \"links\": { \"self\":[{ \"href\": \"https://mybitbucketserver.org/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-magma/pom.xml\" }]}\n" +
                //"            \"link\":{\n" +
                //"               \"url\":\"/projects/IR/repos/iridium-parent/commits/f259e9032cdeb1e28d073e8a79a1fd6f9587f233#iridium-magma/pom.xml\",\n" +
                //"               \"rel\":\"self\"\n" +
                //"            }\n" +
                // \ With Atlassian Bitbucket Server v4.4.0 and Bitbucket Web Post Hooks Plugin 3.0.3, I get a different payload than documented
                "         }\n" +
                "      ],\n" +
                "      \"start\":0,\n" +
                "      \"filter\":null\n" +
                "   }\n" +
                "}");

            payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs("jhocman", "https://mybitbucketserver.org/scm/IR/iridium-parent", "git", payload.toString());
    }

}
