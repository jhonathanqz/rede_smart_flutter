package br.com.jylabtech.rede_payment_tech.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodChannel;
import org.junit.Test;

public class ReplyOnceTest {

    private static final class Recording implements MethodChannel.Result {
        final List<String> calls = new ArrayList<>();

        @Override
        public void success(Object result) {
            calls.add("success:" + result);
        }

        @Override
        public void error(String code, String message, Object details) {
            calls.add("error:" + code);
        }

        @Override
        public void notImplemented() {
            calls.add("notImplemented");
        }
    }

    @Test
    public void forwardsOnlyTheFirstReply() {
        Recording delegate = new Recording();
        ReplyOnce reply = new ReplyOnce(delegate);

        assertFalse(reply.isReplied());
        reply.error("REDE_APP_NOT_FOUND", "x", null);
        reply.success("depois");
        reply.notImplemented();
        reply.error("OUTRO", "y", null);

        assertTrue(reply.isReplied());
        assertEquals(1, delegate.calls.size());
        assertEquals("error:REDE_APP_NOT_FOUND", delegate.calls.get(0));
    }

    @Test
    public void successThenErrorKeepsSuccess() {
        Recording delegate = new Recording();
        ReplyOnce reply = new ReplyOnce(delegate);

        reply.success("ok");
        reply.error("UNEXPECTED_RESULT", "z", null);

        assertEquals(1, delegate.calls.size());
        assertEquals("success:ok", delegate.calls.get(0));
    }

    @Test
    public void notImplementedCountsAsReply() {
        Recording delegate = new Recording();
        ReplyOnce reply = new ReplyOnce(delegate);

        reply.notImplemented();
        reply.success("x");

        assertEquals(1, delegate.calls.size());
        assertEquals("notImplemented", delegate.calls.get(0));
    }
}
