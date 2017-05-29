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
                "  \"actor\": {\n" +
                "    \"username\": \"richard.attermeyer\",\n" +
                "    \"displayName\": \"Richard Attermeyer\"\n" +
                "  },\n" +
                "  \"repository\": {\n" +
                "    \"scmId\": \"git\",\n" +
                "    \"project\": {\n" +
                "      \"key\": \"BHW\",\n" +
                "      \"name\": \"hello-world\"\n" +
                "    },\n" +
                "    \"slug\": \"hello-world\",\n" +
                "    \"links\": {\n" +
                "      \"self\": [\n" +
                "        {\n" +
                "          \"href\": \"https://mybitbucketserver.org/projects/BHW/repos/hello-world/browse\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"ownerName\": \"BHW\",\n" +
                "    \"public\": false,\n" +
                "    \"owner\": {\n" +
                "      \"username\": \"BHW\",\n" +
                "      \"displayName\": \"BHW\"\n" +
                "    },\n" +
                "    \"fullName\": \"BHW/hello-world\"\n" +
                "  },\n" +
                "  \"push\": {\n" +
                "    \"changes\": [\n" +
                "      {\n" +
                "        \"created\": false,\n" +
                "        \"closed\": false,\n" +
                "        \"new\": {\n" +
                "          \"type\": \"branch\",\n" +
                "          \"name\": \"master\",\n" +
                "          \"target\": {\n" +
                "            \"type\": \"commit\",\n" +
                "            \"hash\": \"d93ef85579f7aa4d88e4bbf2a64b60842687cb5c\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"old\": {\n" +
                "          \"type\": \"branch\",\n" +
                "          \"name\": \"master\",\n" +
                "          \"target\": {\n" +
                "            \"type\": \"commit\",\n" +
                "            \"hash\": \"ce7f168c051581c217dd17be63384e5a3789a14f\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");

            payloadProcessor.processPayload(payload, request);

        verify(probe).triggerMatchingJobs("richard.attermeyer", "https://mybitbucketserver.org/scm/BHW/hello-world", "git", payload.toString());
    }

}
