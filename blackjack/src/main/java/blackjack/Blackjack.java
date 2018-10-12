package blackjack;

public class Blackjack extends Game {
    public static Blackjack instance;

    // player constants
    public static final int STARTING_FUNDS = 500;
    public static final String PLAYER_NAME = "Player";
    public static final String DEALER_NAME = "Dealer";

    // blackjack/game constants
    public static final int MAX_PLAYERS = 5;
    public static final int MAX_VAL = 21;
    public static final int MAX_DEALER_VAL = 17;
    public static final int NATURAL_HAND = 2;

    // empty singleton constructor
    private Blackjack() {
        super(MAX_VAL, MAX_DEALER_VAL, MAX_PLAYERS);
    }
    
    // All EndGameExceptions should propagate to this method
    public static void start() {
        try {
            Hand.setMax(MAX_VAL);
            instance = new Blackjack();
            instance.createPlayers(PLAYER_NAME, DEALER_NAME, STARTING_FUNDS, STARTING_FUNDS);
            instance.play();
        } catch (EndGameException e) {
            exit();
        }
    }

    public static void exit(){
        if (instance.round >= 1) {
            instance.printRanking();
        }
        System.out.println("\n*** Thank you for playing Blackjack! ***\n");
        instance = null;
    }

    @Override
    public void printRanking() {
        super.printRanking();
        System.out.printf("\nDealer: %s\n", dealer.name);
    }

    @Override
    public void printPlayerControls() {
        System.out.println("Controls:\n1 or `hit` to hit\n2 or `stand` to stand\n3 or `split` to split\n4 or `double` to double up\n5 or `help` for help\n6 or `quit` to quit\n");
    }

    @Override
    public void printDealerControls() {
        System.out.println("Controls:\n1 or `hit` to hit\n2 or `stand` to stand\n3 or `help` for help\n4 or `quit` to quit\n");
    }

    @Override
    public void play() throws EndGameException {
        System.out.println("!!! Remember to press `enter` after your inputs.");
        System.out.printf("!!! Player starts with $%d.\n", STARTING_FUNDS);

        super.play();

        boolean restart = false;
        if (allPlayersBroke()) {
            System.out.println("$$$ All players have gone bankrupt!\n");
            System.out.print("Would you like to play again? `Yes` or `No`\n--- ");

            restart = "yes".equals(sc.nextLine().toLowerCase());
        }

        if (restart) {
            start();
        } else {
            exit();
        }
    }

    @Override
    public void deal() {
        System.out.printf("%s is dealing...\n", dealer.name);

        try {
            Thread.sleep(SLEEP_TIME);

            /* First card given */
            for (Player player : players) {
                if (player.broke()) continue;
                dealer.dealCard(player, player.getHand(FIRST_INDEX));
                Thread.sleep(SLEEP_TIME);
            }
            
            dealer.dealCard(dealer, dealer.getHand(), FACE_UP);
            Thread.sleep(SLEEP_TIME);

            /* Second card given */
            for (Player player : players) {
                if (player.broke()) continue;
                dealer.dealCard(player, player.getHand(FIRST_INDEX));
                Thread.sleep(SLEEP_TIME);
            }

            // second card = face down
            dealer.dealCard(dealer, dealer.getHand(), FACE_DOWN);
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {}
    }

    @Override
    public boolean startRound() throws EndGameException {
        round += 1;
        System.out.printf("\n--- Starting round %d ---\n\n", round);

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {} 
        
        try {
            dealer.newRound();
            dealer.shuffle();
            for (Player player : players) {
                if (player.broke()) continue;
                player.newRound();
            }

            deal();

            for (Player player : players) {
                player.start(sc);
            }

            playerTurn();

            // no need for dealer turn if player already lost
            if (!allPlayersDone()) {
                dealerTurn();
                checkResult();
            } else {
                checkResult();
                System.out.printf("!!! All Players went BUST or FOLDED.\n\n");
            }
        } catch (EndGameException e){
            throw e;
        }

        boolean restart = false;
        if (!allPlayersBroke()) {
            System.out.print("Would players like to continue? `Yes` or `No`\n--- ");
            restart = "yes".equals(sc.nextLine().toLowerCase());
        }

        return restart;
    }

    @Override
    public int compare(Hand h1, Hand h2) {
        int result = h1.compareTo(h2);
        if (result == 0) {
            if (h1.computeValue() == MAX_VAL) {
                if (h1.size() == NATURAL_HAND && h2.size() != NATURAL_HAND) return 1;
                else if (h1.size() != NATURAL_HAND && h2.size() == NATURAL_HAND) return -1;
            }
        }
        return result;
    }

    @Override
    public void checkActionPlayerHand(Player player, Hand hand) throws EndGameException {
        boolean result;

        do {
            printPlayerControls();
            System.out.printf("%s: what would you like to do for your hand:\n%s\n--- ", player.name, hand.toString());
            String action = sc.nextLine().toLowerCase();
            System.out.println("");

            result = false;

            switch (action) {
                case "1" :
                case "hit" : 
                    player.hit(hand, dealer);
                    break;
                case "2":
                case "stand":
                    player.stand(hand);
                    break;
                case "3":
                case "split":
                    // insert new hands in same position so outer loop goes in order
                    if (!player.split(hand, dealer)) {
                        System.out.println(" -- invalid split.\nPlease enter a new command.\n");
                    }
                    break;
                case "4":
                case "double":
                    if (!player.doubleUp(hand, dealer)) {
                        System.out.println(" -- invalid Double Up.\nPlease enter a new command.\n");
                    }
                    break;
                case "5":
                case "help":
                    break;
                case "6":
                case "quit": 
                    // quit application
                    throw new EndGameException();
                default:
                    System.out.println("Invalid command.\n");
                    break;
            }

            result = checkHandResult(hand);

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {}
        } while (result);
    }

    @Override
    public void checkActionDealerHand(Hand hand) throws EndGameException {
        boolean result;
        do {
            printDealerControls();
            System.out.printf("%s: what would you like to do for your hand:\n%s -- Must hit until hand value reaches %d!\n--- ", dealer.name, hand.toString(), MAX_DEALER_VAL);
            String action = sc.nextLine().toLowerCase();
            System.out.println("");

            result = false;

            switch (action) {
                case "1":
                case "hit" : 
                    dealer.hit(hand, dealer);
                    break;
                case "2":
                case "stand" :
                    if (dealer.getHand().computeValue() < MAX_DEALER_VAL) {
                        System.out.printf("Must hit until hand value reaches %d.\n\n", MAX_DEALER_VAL);
                    } else {
                        dealer.stand(hand);
                    }
                    break;
                case "3":
                case "help":
                    break;
                case "4":
                case "quit": 
                    throw new EndGameException();
                default:
                    System.out.println("Invalid command.\n");
                    printDealerControls();
                    break;
            }

            result = checkHandResult(hand);

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {}
        } while (result);
    }
}