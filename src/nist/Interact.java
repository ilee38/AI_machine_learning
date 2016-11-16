package nist;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class Interact {
    // This sets up an interaction between an "environment" and
    // and an "agent". Each gets an input stream (Scanner) and
    // and output stream (PrintStream) to talk to each other.
    // This main method also monitors the messages being sent
    // between the environment and agent. The environment and
    // agent should each print "quit" when finished.
    public static void main(String[] args) {
        // set this to true to see a lot of output
        boolean debug = false;
        
        PipedOutputStream pipeout = new PipedOutputStream();
        PipedInputStream pipein;
        try {
            pipein = new PipedInputStream(pipeout);
        } catch (Exception e) {
            throw new RuntimeException("pipe failed " + e);
        }
        Scanner envirIn = new Scanner(pipein);
        PrintStream printToEnvir = new PrintStream(pipeout, true);
        pipeout = new PipedOutputStream();
        try {
            pipein = new PipedInputStream(pipeout);
        } catch (Exception e) {
            throw new RuntimeException("pipe failed " + e);
        }
        InputStreamReader readFromEnvir = new InputStreamReader(pipein);
        PrintStream envirOut = new PrintStream(pipeout, true);

        pipeout = new PipedOutputStream();
        try {
            pipein = new PipedInputStream(pipeout);
        } catch (Exception e) {
            throw new RuntimeException("pipe failed " + e);
        }
        Scanner agentIn = new Scanner(pipein);
        PrintStream printToAgent = new PrintStream(pipeout, true);
        pipeout = new PipedOutputStream();
        try {
            pipein = new PipedInputStream(pipeout);
        } catch (Exception e) {
            throw new RuntimeException("pipe failed " + e);
        }
        InputStreamReader readFromAgent = new InputStreamReader(pipein);
        PrintStream agentOut = new PrintStream(pipeout, true);

        // These are the only two lines that need to be
        // changed for a different environment and agent.
        Runnable envir = new Nist(envirIn, envirOut);
        Runnable agent = new Learner(agentIn, agentOut);

        /*
         * ExecutorService threads = Executors.newCachedThreadPool();
         * threads.execute(envir); threads.execute(agent); threads.shutdown();
         */

        Thread ethread = new Thread(envir);
        Thread athread = new Thread(agent);
        ethread.start();
        athread.start();

        boolean alive1 = true;
        boolean alive2 = true;
        StringBuffer envirBuffer = new StringBuffer();
        StringBuffer agentBuffer = new StringBuffer();
        while (alive1 || alive2) {
            String line = null;
            boolean wait = true;
            if (alive1) {
                try {
                    line = readLineNoBlock(readFromEnvir, envirBuffer);
                } catch (Exception e) {
                    System.out.println(e);
                    line = null;
                }
                if (line == null) {
                    alive1 = ethread.isAlive();
                } else {
                    wait = false;
                    if (debug)
                    System.out.println(envir.getClass() + " " + line);
                    printToAgent.print(line);
                    printToAgent.print('\n');
                }
            }
            if (alive2) {
                wait = false;
                try {
                    line = readLineNoBlock(readFromAgent, agentBuffer);
                } catch (Exception e) {
                    line = null;
                }
                if (line == null) {
                    alive2 = athread.isAlive();
                } else {
                    if (debug)
                    System.out.println(agent.getClass() + " " + line);
                    printToEnvir.print(line);
                    printToEnvir.print('\n');
                }
            }
            if (wait) {
                try {
                    Thread.sleep(0);
                } catch (Exception e) {
                }
            }
        }
        envirIn.close();
        envirOut.close();
        try {
            readFromEnvir.close();
        } catch (IOException e) {
            // Who cares? We're quitting anyway
        }
        printToEnvir.close();
        agentIn.close();
        agentOut.close();
        try {
            readFromAgent.close();
        } catch (IOException e) {
            // Who cares? We're quitting anyway
        }
        printToAgent.close();
    }

    public static String readLineNoBlock(InputStreamReader in, StringBuffer buf)
            throws IOException {
        while (in.ready()) {
            char c = (char) in.read();
            if (c == '\n') {
                String s = buf.toString();
                buf.delete(0, buf.length());
                return s;
            }
            buf.append(c);
        }
        return null;
    }
}
