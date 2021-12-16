package cs451.Utils;

import cs451.Constants;
import cs451.Parser;
import cs451.Network.*;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;


public class Helper {

    private Helper() {
        throw new IllegalStateException("Utility Class");
    }

    private static void checkCounterLimit(int counter, int nbHost) throws Exception {
        if (counter != nbHost + 1) {
            throw new Exception(Constants.ERROR_CONFIG);
        }
    }

    public static void handleRun(Parser parser, AlteredBroadcast broadcast) throws InterruptedException {

        int nbMessage = 0;
        int id = parser.myId();
        int[][] causalities = null;
        int nbHost = parser.hosts().size();

        try {
            causalities = new int[nbHost][];
            Scanner scanner = new Scanner(new FileInputStream(parser.config()));
            nbMessage = Integer.parseInt(scanner.nextLine());

            int counter = 1;
            while (scanner.hasNextLine() && counter <= nbHost) {
                int[] ints = Arrays.stream(scanner.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                if (ints[0] != counter || ints.length < 1) {
                    if (counter == nbHost + 1 && ints.length < 1) {
                        break;
                    }
                    throw new Exception(Constants.ERROR_CONFIG);
                }
                causalities[counter - 1] = Arrays.copyOfRange(ints, 1, ints.length);
                counter++;
            }
            checkCounterLimit(counter, nbHost);
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Number of messages : " + nbMessage);

        CountDownLatch countDownLatch1 = new CountDownLatch(nbMessage);
        CountDownLatch countDownLatch2 = new CountDownLatch(nbMessage * (nbHost / 2));

        Writer writer = FileUtils.getInstance(parser.output());

        AlteredBroadcastDelivery alteredBroadcastDelivery = (basisId, msgId) -> {
            writer.log(Constants.DELIVER + basisId + Constants.SPACE + msgId);
            if (basisId == id) {
                countDownLatch1.countDown();
            } else {
                countDownLatch2.countDown();
            }
        };

        try {
            broadcast = buildCausalOrdering(alteredBroadcastDelivery, parser, nbMessage, causalities);
        } catch (Exception e) {
            return;
        }

        for (int messagePtr = 1; messagePtr <= nbMessage; ++messagePtr) {
            writer.log(Constants.BROADCAST + messagePtr);
            broadcast.broadcast(id, messagePtr);
        }
        broadcast.flush();
        countDownLatch1.await();
        countDownLatch2.await();

    }

    private static AlteredBroadcast buildCausalOrdering(AlteredBroadcastDelivery alteredBroadcastDelivery, Parser parser, int nbMessage, int[][] causalities) throws Exception {
        var alteredBroadcast = new AlteredBroadcast(alteredBroadcastDelivery);
        Broadcast causalManager = new CausalManager(parser.myId(), AlteredBroadcast.computeMessageSize(nbMessage), parser.hosts(), alteredBroadcast, causalities);
        alteredBroadcast.init(causalManager);
        return alteredBroadcast;
    }

}
