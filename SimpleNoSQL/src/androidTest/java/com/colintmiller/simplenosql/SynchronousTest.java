package com.colintmiller.simplenosql;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityUnitTestCase;
import com.colintmiller.simplenosql.toolbox.SynchronousRetrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by cmiller on 10/21/14.
 */
public class SynchronousTest extends ActivityUnitTestCase<Activity> {

    private Context context;

    public SynchronousTest() {
        super(Activity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.context = getInstrumentation().getTargetContext();
    }

    public void testSynchronousGet() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<SampleBean> result = new ArrayList<SampleBean>();
        assertEquals(0, result.size());
        NoSQL.with(context).using(SampleBean.class).bucketId("dne").retrieve(new RetrievalCallback<SampleBean>() {
            @Override
            public void retrievedResults(List<NoSQLEntity<SampleBean>> noSQLEntities) {
                result.add(new SampleBean());
                latch.countDown();
            }
        });
        latch.await();
        assertEquals(1, result.size());
    }

    public void testSynchronousRetrieval() throws Throwable {
        SampleBean item = new SampleBean();
        item.setName("item");
        final CountDownLatch lock = new CountDownLatch(1);
        NoSQL.with(context).using(SampleBean.class).addObserver(new OperationObserver() {
            @Override
            public void hasFinished() {
                lock.countDown();
            }
        }).save(new NoSQLEntity<SampleBean>("dne", "1", item));
        lock.await();
        SynchronousRetrieval<SampleBean> retrievalCallback = new SynchronousRetrieval<SampleBean>();
        NoSQL.with(context).using(SampleBean.class).bucketId("dne").retrieve(retrievalCallback);
        List<NoSQLEntity<SampleBean>> results = retrievalCallback.getSynchronousResults();
        assertEquals(1, results.size());
    }
}
