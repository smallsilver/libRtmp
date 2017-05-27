package org.yeshen.video.librtmp.carriers;

import android.os.Process;

import org.yeshen.video.librtmp.packets.Chunkd;
import org.yeshen.video.librtmp.tools.Error;
import org.yeshen.video.librtmp.tools.Lg;
import org.yeshen.video.librtmp.tools.Options;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*********************************************************************
 * Created by yeshen on 2017/05/17.
 * Copyright (c) 2017 yeshen.org. - All Rights Reserved
 *********************************************************************/


class Writer implements Runnable {

    private static final String TAG = "org.yeshen.video.librtmp.carriers.Writer";

    private volatile boolean running = false;
    private final Object locker = new Object();
    private Queue<Chunkd> queue = new ConcurrentLinkedQueue<>();
    private Thread thread = null;
    private SocketChannel channel = null;

    void start(SocketChannel channel) {
        this.channel = channel;
        synchronized (locker) {
            if (!running) {
                running = true;
                thread = new Thread(this, TAG);
                thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
            } else {
                locker.notify();
            }
        }
    }

    void post(Chunkd data) {
        queue.offer(data);
        synchronized (locker) {
            locker.notify();
        }
    }

    void stop() {
        synchronized (locker) {
            if (running) {
                running = false;
                if (!Thread.currentThread().equals(thread)) {
                    locker.notify();
                }
            }
            thread = null;
        }
    }

    private void looper() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Options.getInstance().bufferSize);
        while (!queue.isEmpty()) {
            Chunkd top = queue.poll();
            byteBuffer.put(top.btye());
        }
        byteBuffer.flip();

        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            Lg.e(Error.WRITER, e);
        }
    }

    @Override
    public void run() {
        while (running) {

            looper();

            if (!running) break;
            synchronized (locker) {
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
