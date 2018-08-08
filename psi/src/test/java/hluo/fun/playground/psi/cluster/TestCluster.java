package hluo.fun.playground.psi.cluster;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import hluo.fun.playground.psi.execution.JobId;
import hluo.fun.playground.psi.execution.TaskState;
import hluo.fun.playground.psi.server.JobInfo;
import hluo.fun.playground.psi.server.NodeState;
import hluo.fun.playground.psi.server.TaskUpdateRequest;
import hluo.fun.playground.psi.testing.TestingPsiCluster;
import hluo.fun.playground.psi.testing.TestingPsiServer;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.JsonResponseHandler;
import io.airlift.http.client.Request;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.testing.Closeables;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static io.airlift.http.client.JsonBodyGenerator.jsonBodyGenerator;
import static io.airlift.http.client.JsonResponseHandler.createJsonResponseHandler;
import static io.airlift.http.client.Request.Builder.prepareDelete;
import static io.airlift.http.client.Request.Builder.preparePost;
import static io.airlift.http.client.StaticBodyGenerator.createStaticBodyGenerator;
import static io.airlift.json.JsonCodec.jsonCodec;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;

public class TestCluster
{
    private TestingPsiCluster cluster;
    private String testingClassName;
    private String testingSourceCode;
    private HttpClient client;

    @BeforeMethod
    public void setup()
            throws Exception
    {
        TestingPsiCluster.Builder builder = new TestingPsiCluster.Builder()
                .setExtraProperties(ImmutableMap.of("task.max-worker-threads", "3"))
                .setNodeCount(5)
                .setSingleMasterProperty("http-server.http.port", "9090");
        cluster = builder.build();
        client = new JettyHttpClient();

        // set up the testing stream function
        testingClassName = "hluo.fun.playground.psi.example.WordCountStreamFunction";
        StringBuilder stringBuilder = new StringBuilder()
                .append("package hluo.fun.playground.psi.example;\n")
                .append("\n")
                .append("import com.google.common.collect.ImmutableList;\n")
                .append("import hluo.fun.playground.psi.execution.StreamFunction;\n")
                .append("import hluo.fun.playground.psi.execution.Tuple;\n")
                .append("import io.airlift.log.Logger;\n")
                .append("import io.airlift.units.Duration;\n")
                .append("\n")
                .append("import java.util.HashMap;\n")
                .append("import java.util.List;\n")
                .append("import java.util.Map;\n")
                .append("import java.util.Random;\n")
                .append("import java.util.concurrent.TimeUnit;\n")
                .append("\n")
                .append("import static com.google.common.base.Preconditions.checkArgument;\n")
                .append("\n")
                .append("/**\n")
                .append(" * This is a stream function that does simple word counts.\n")
                .append(" * <p>\n")
                .append(" * 1. constructor generates a set of random words (~1k words, 10 chars per word)\n")
                .append(" * 2. sourc() will pick a word at random\n")
                .append(" * 3. proc() will count the occurrences of the word\n")
                .append(" * 4. sink() will printout the info every 5 seconds\n")
                .append(" */\n")
                .append("public class WordCountStreamFunction\n")
                .append("        implements StreamFunction\n")
                .append("{\n")
                .append("    private static final Logger log = Logger.get(WordCountStreamFunction.class);\n")
                .append("\n")
                .append("    private static final int ARRAY_LENGTH = 1024;\n")
                .append("    private static final int WORD_LENGTH = 10;\n")
                .append("    private final String[] words = new String[ARRAY_LENGTH];\n")
                .append("    private final Random rnd = new Random(31);\n")
                .append("\n")
                .append("    private final Map<String, Integer> countMap;\n")
                .append("\n")
                .append("    private long lastUpdateTimestamp;\n")
                .append("    private static final Duration OUTPUT_INTERVAL = new Duration(5, TimeUnit.SECONDS);\n")
                .append("\n")
                .append("    public WordCountStreamFunction()\n")
                .append("    {\n")
                .append("        // generate the word map\n")
                .append("        RandomString randomString = new RandomString(WORD_LENGTH);\n")
                .append("        for (int i = 0; i < ARRAY_LENGTH; i++) {\n")
                .append("            words[i] = randomString.nextString();\n")
                .append("        }\n")
                .append("        countMap = new HashMap<>();\n")
                .append("        lastUpdateTimestamp = System.nanoTime();\n")
                .append("    }\n")
                .append("\n")
                .append("    @Override\n")
                .append("    public List<Tuple> source()\n")
                .append("    {\n")
                .append("        return ImmutableList.of(new WordTuple(words[rnd.nextInt(ARRAY_LENGTH)]));\n")
                .append("    }\n")
                .append("\n")
                .append("    @Override\n")
                .append("    public List<Tuple> proc(Tuple tuple)\n")
                .append("    {\n")
                .append("        checkArgument(tuple instanceof WordTuple, \"Tuple must be type of WordTuple\");\n")
                .append("        String key = ((WordTuple) tuple).getWord();\n")
                .append("        if (countMap.get(key) == null) {\n")
                .append("            countMap.put(key, 1);\n")
                .append("        }\n")
                .append("        else {\n")
                .append("            Integer val = countMap.get(key);\n")
                .append("            countMap.put(key, ++val);\n")
                .append("        }\n")
                .append("\n")
                .append("        return ImmutableList.of(tuple);\n")
                .append("    }\n")
                .append("\n")
                .append("    @Override\n")
                .append("    public void sink(List<Tuple> list)\n")
                .append("    {\n")
                .append("        // printout the result every five seconds\n")
                .append("        if (Duration.nanosSince(lastUpdateTimestamp).compareTo(OUTPUT_INTERVAL) > 0) {\n")
                .append("            log.info(\"========= WORD COUNT =========\");\n")
                .append("            log.info(countMap.toString());\n")
                .append("            log.info(\"==============================\");\n")
                .append("            lastUpdateTimestamp = System.nanoTime();\n")
                .append("        }\n")
                .append("    }\n")
                .append("\n")
                .append("    public class WordTuple\n")
                .append("            implements Tuple\n")
                .append("    {\n")
                .append("        private final String word;\n")
                .append("\n")
                .append("        WordTuple(String word)\n")
                .append("        {\n")
                .append("            this.word = word;\n")
                .append("        }\n")
                .append("\n")
                .append("        String getWord()\n")
                .append("        {\n")
                .append("            return word;\n")
                .append("        }\n")
                .append("    }\n")
                .append("\n")
                .append("    // Utils class to generate random String at given length\n")
                .append("    public static class RandomString\n")
                .append("    {\n")
                .append("        private final char[] symbols;\n")
                .append("\n")
                .append("        private final Random random = new Random();\n")
                .append("\n")
                .append("        private final char[] buf;\n")
                .append("\n")
                .append("        public RandomString(int length)\n")
                .append("        {\n")
                .append("            // Construct the symbol set\n")
                .append("            StringBuilder tmp = new StringBuilder();\n")
                .append("            for (char ch = '0'; ch <= '9'; ++ch) {\n")
                .append("                tmp.append(ch);\n")
                .append("            }\n")
                .append("\n")
                .append("            for (char ch = 'a'; ch <= 'z'; ++ch) {\n")
                .append("                tmp.append(ch);\n")
                .append("            }\n")
                .append("\n")
                .append("            symbols = tmp.toString().toCharArray();\n")
                .append("            if (length < 1) {\n")
                .append("                throw new IllegalArgumentException(\"length < 1: \" + length);\n")
                .append("            }\n")
                .append("\n")
                .append("            buf = new char[length];\n")
                .append("        }\n")
                .append("\n")
                .append("        public String nextString()\n")
                .append("        {\n")
                .append("            for (int idx = 0; idx < buf.length; ++idx) {\n")
                .append("                buf[idx] = symbols[random.nextInt(symbols.length)];\n")
                .append("            }\n")
                .append("\n")
                .append("            return new String(buf);\n")
                .append("        }\n")
                .append("    }\n")
                .append("}\n")
                .append("\n");
        testingSourceCode = stringBuilder.toString();
    }

    @SuppressWarnings("deprecation")
    @AfterMethod
    public void teardown()
    {
        Closeables.closeQuietly(cluster);
    }

    @Test
    void testMaster()
            throws Exception
    {
        TestingPsiServer server = new TestingPsiServer();
    }

    @Test
    void testCluster()
            throws Exception
    {
        JobId jobId;
        JsonResponseHandler<JobInfo> responseHandler = createJsonResponseHandler(jsonCodec(JobInfo.class));

        {
            // submit job
            Request request = preparePost()
                    .setUri(new URI("http://127.0.0.1:9090/v1/job"))
                    .setHeader("X-Pica-Class-Name", testingClassName)
                    .setBodyGenerator(createStaticBodyGenerator(testingSourceCode, UTF_8))
                    .build();

            JobInfo jobInfo = client.execute(request, responseHandler);
            jobId = jobInfo.getJobId();
            jobInfo.getTaskInfos().stream()
                    .forEach(x -> assertEquals(x.getTaskStatus().getState(), TaskState.RUNNING));
        }

        sleep(10000);

        {
            // delete job
            Request request = prepareDelete()
                    .setUri(new URI("http://127.0.0.1:9090/v1/job/" + jobId))
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                    .setBodyGenerator(jsonBodyGenerator(jsonCodec(JobId.class), jobId))
                    .build();

            JobInfo jobInfo = client.execute(request, responseHandler);
            jobInfo.getTaskInfos().stream()
                    .forEach(x -> assertEquals(x.getTaskStatus().getState(), TaskState.CANCELED));
        }

        // wait for some time before we destroy the cluster
        // FIXME: fix the wait time by implementing graceful shutdown...
        sleep(5000);
    }
}
