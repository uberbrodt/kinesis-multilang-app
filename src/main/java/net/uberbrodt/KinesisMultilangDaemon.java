package net.uberbrodt;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.multilang.MultiLangDaemon;
import com.amazonaws.services.kinesis.multilang.MultiLangDaemonConfig;


public class KinesisMultilangDaemon {

     /**
     * Utility for describing how to run this app.
     * 
     * @param stream Where to output the usage info.
     * @param messageToPrepend An optional error message to describe why the usage is being printed.
     */
    public static void printUsage(PrintStream stream, String messageToPrepend) {
        StringBuilder builder = new StringBuilder();
        if (messageToPrepend != null) {
            builder.append(messageToPrepend);
        }
        builder.append(String.format("java %s <properties file>", KinesisMultilangDaemon.class.getCanonicalName()));
        stream.println(builder.toString());
    }

    public static void main (String[] args) {
        if (args.length == 0) {
            printUsage(System.err, "You must provide a properties file");
            System.exit(1);
        }
        MultiLangDaemonConfig config = null;
        try {
            config = new MultiLangDaemonConfig(args[0]);
        } catch (IOException e) {
            printUsage(System.err, "You must provide a properties file");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            printUsage(System.err, e.getMessage());
            System.exit(1);
        }

        ExecutorService executorService = config.getExecutorService();

        // Daemon
        final MultiLangDaemon daemon = new MultiLangDaemon(
                config.getKinesisClientLibConfiguration(),
                config.getRecordProcessorFactory(),
                executorService);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //LOG.info("Process terminanted, will initiate shutdown.");
                try {
                    Field field = daemon.getClass().getDeclaredField("worker");
                    field.setAccessible(true);
                    Worker worker = (Worker)field.get(daemon);

                    Future<Void> fut = worker.requestShutdown();
                    fut.get(5000, TimeUnit.MILLISECONDS);
                    // LOG.info("Process shutdown is complete.");
                } catch (InterruptedException | ExecutionException | TimeoutException | NoSuchFieldException | IllegalAccessException e) {
                    // LOG.error("Encountered an error during shutdown.", e);
                }
            }
        });

        Future<Integer> future = executorService.submit(daemon);
        try {
            System.exit(future.get());
        } catch (InterruptedException | ExecutionException e) {
            // LOG.error("Encountered an error while running daemon", e);
        }
        System.exit(1);
    }
}
