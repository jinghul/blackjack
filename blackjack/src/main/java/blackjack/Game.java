package blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/*
    Game class which setups and
    starts the game for each player/computer.
*/
public abstract class Game implements Comparator<Hand> {
    public static final int SLEEP_TIME = 500;
    public static Scanner sc;

    // games
    public static final String OPTION_1 = "Blackjack";
    public static final String OPTION_2 = "Trianta Ena";
    public static String choice;

    // card constants
    public static final int FIRST_INDEX = 0;
    public static final int SECOND_INDEX = 1;
    public static final int FACE_UP = 0;
    public static final int FACE_DOWN = 1;

    // game specific
    public int MAX_VAL;
    public int MAX_PLAYERS;
    public int MAX_DEALER_VAL;

    // game constant structures
    public ArrayList<Player> players;
    public Dealer dealer;
    public int round;

    public Game() {
        players = new ArrayList<Player>();
        round = 0;
    }

    public Game(int maxVal, int dealVal, int maxPlayers) {
        this();
        MAX_VAL = maxVal;
        MAX_DEALER_VAL = dealVal;
        MAX_PLAYERS = maxPlayers;
    }

    public static void select() {
        sc = new Scanner(System.in);
        System.out.println("*** Welcome to the Casino! ***\n");
        System.out.printf("What would you like to play today?\n1. %s\n2. %s\n3. Quit\n\n", OPTION_1, OPTION_2);
        
        choice = "";

        while (true) {
            System.out.print("Please input a number or the game name.\n--- ");
            String input = sc.nextLine();
            if (input.equals("1") || input.equalsIgnoreCase(OPTION_1)) {
                choice = OPTION_1;
                Blackjack.start();
                break;
            } else if (input.equals("2") || input.equalsIgnoreCase(OPTION_2)) {
                choice = OPTION_2;
                TriantaEna.start();
                break;
            } else if (input.equals("3") || input.equalsIgnoreCase("Quit")) break;
        }

        if (choice.equals(OPTION_1)) {
            System.out.printf("Would you like to try %s? `yes` or `no`?\n--- ", OPTION_2);
            if ("yes".equals(sc.nextLine().toLowerCase())) {
                choice = OPTION_2;
                TriantaEna.start();
            }
        } else if (choice.equals(OPTION_2)) {
            System.out.printf("Would you like to try %s? `yes` or `no`?\n--- ", OPTION_1);
            if ("yes".equals(sc.nextLine().toLowerCase())) {
                choice = OPTION_1;
                Blackjack.start();
            }
        }

        // after all prompts finish
        System.out.println("\n*** Thank you for visiting the casino! ***\n");
        sc.close();
    }

    // repeat calls needs same scanner
    public int initialize() throws EndGameException {
        System.out.printf("\n*** Welcome to the %s table! ***\n\n", choice);

        printInitialInstructions();
        int numPlayers = 0;

        // asserts the string between 1 and 9, if the input is quit then it returns
        while (!(numPlayers >= 1 && numPlayers <= MAX_PLAYERS)) {
            System.out.printf("Please enter the number of players ('1' to '%d') or `quit` to quit.\n--- ", MAX_PLAYERS);
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("quit")) {
                // quits the game
                throw new EndGameException();
            }

            try {
                numPlayers = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                numPlayers = 0;
            }
        }

        return numPlayers;
    }

    public static void printInitialInstructions() {
        System.out.println("--- Instructions ---");
        System.out.println("Watch the commands on the screen to do a specific action.");
        System.out.println("Cards with numbers 2 - 9 are valued accordingly, Face cards are a value of 10, and ace can be 1 or 11.");
        System.out.println("Player tries to obtain a value over the dealer without going over 21.");
        System.out.println("[3D] means 3 of Diamonds - [AS] means Ace of Spades - etc.\n");
    }

    public void createPlayers(String playerName, String dealerName, int playerStart, int dealerStart) throws EndGameException {

        // select number of players
        int numPlayers = 1;
        try {
            numPlayers = initialize();
        } catch (EndGameException e) {
            throw e;
        }

        // build the players/computers
        if (numPlayers == 1) {
            System.out.println("\n--- Starting a one player game! ---\n");
            dealer = new Computer("Computer", dealerName, dealerStart);
            players.add(new Player(playerName + " 1", playerStart));
        } else {
            int rand = (int)Math.random() + numPlayers - 1;
            System.out.printf("\n--- Starting a %d player game! ---\n%s is player %d.\n\n", numPlayers, dealerName, rand);
            dealer = new Dealer(playerName + " " + rand, dealerName, dealerStart);
            
            for (int i = 1; i <= numPlayers; i++) {
                if (i != rand) {
                    players.add(new Player(playerName + " " + i, playerStart));
                }
            }
        }
    }

    // prints out the controls on each turn
    public abstract void printPlayerControls();
    public abstract void printDealerControls();

    // game logic of starting the game and each round
    public void play() throws EndGameException {
        try {
            while (startRound()) {
                printRanking();
            }
        } catch (EndGameException e) {
            throw e;
        }
    }
    public abstract boolean startRound() throws EndGameException;
    public abstract void deal();

    public void playerTurn() throws EndGameException {
        try {
            for (Player player : players) {
                while (!player.done()) {
                    for (int i = 0; i < player.getHands().size(); i++) {
                        Hand hand = player.getHand(i);
                        if (hand.done()) {
                            continue;
                        }

                        // player turn for this hand
                        checkActionPlayerHand(player, hand);
                    }
                }
            }
        } catch (EndGameException e) {
            throw e;
        }
    }

    public void dealerTurn() throws EndGameException {
        // dealer.start returns false if its a computer
        if (dealer.start(MAX_DEALER_VAL)) {
            while (true) {
                checkActionDealerHand(dealer.getHand());
                if (dealer.done()) break;
            }
        }

        checkHandResult(dealer.getHand());
    }

    public abstract void checkActionPlayerHand(Player player, Hand hand) throws EndGameException;
    public abstract void checkActionDealerHand(Hand hand) throws EndGameException;

    public boolean checkHandResult(Hand hand) {
        int handSum = hand.computeValue();
        if (handSum > MAX_VAL) {
            hand.bust = true;
            System.out.println("!!! Hand went BUST!\n");
        } else if (handSum == MAX_VAL) {
            System.out.printf("!!! Hand has %d!\n\n", MAX_VAL);
        }

        return !hand.done();
    }

    public void checkResult() {
        System.out.println("-----------------------------------");
        if (dealer.bust()) {
            System.out.printf("!!! %s went BUST!\n\n", dealer.name);
            for (Player player : players) {
                if (player.fold || player.broke()) continue;
                int total = 0;
                for (Hand hand : player.getHands()) {
                    hand.get(FIRST_INDEX).setVisible(true);
                    if (hand.bust()) continue;
                    total += player.bet;
                    win(player, hand);
                }
                System.out.printf("$$$ %s won total $%d from %s.\n\n", player.name, total, dealer.name);
            }
        } else if (!allPlayersFold()) {
            System.out.printf("%s hand: %s", dealer.name, dealer.getHandsString());
            for (Player player : players) {
                if (player.broke()) {
                    continue;
                } else if (player.fold) {
                    System.out.printf("%s: FOLDED\n", player.name);
                    continue;
                }
                for (Hand hand : player.getHands()) {
                    if (hand.bust()) {
                        lose(player, hand);
                        continue;
                    }
                    hand.get(FIRST_INDEX).setVisible(true);
                    switch (compare(hand, dealer.getHand())) {
                        case -1: 
                            lose(player, hand);
                            break;
                        case 0: 
                            tie(player, hand);       
                            break;
                        case 1: 
                            win(player, hand);
                            break;
                    }
                }
            }
        }
        System.out.println("-----------------------------------\n");
    }

    public void win(CardPlayer player, Hand hand) {
        System.out.printf("%s hand: %s --- won $%d\n", player.name, hand.toString(), player.bet);
        player.winBet(dealer);
    }

    public void tie(CardPlayer player, Hand hand) {
        System.out.printf("%s hand: %s --- tied!\n", player.name, hand.toString());
    }

    public void lose(CardPlayer player, Hand hand) {
        System.out.printf("%s hand: %s --- lost $%d\n", player.name, hand.toString(), player.bet);
        player.loseBet(dealer);
    }

    public void printRanking() {
        System.out.println("\n--- Ranking ---");
        Collections.sort(players, new Comparator<Player>() {
            public int compare(Player p1, Player p2) {
                if (p1.money > p2.money) {
                    return -1;
                } else if (p1.money == p2.money) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        int count = 1;
        for (Player player : players) {
            System.out.println(count + ". " + player.name + ": $" + player.money);
            count += 1;
        }
    }

    public boolean allPlayersBroke() {
        boolean broke = true;
        for (CardPlayer player : players) {
            broke = broke && player.broke();
        }
        return broke;
    }

    public boolean allPlayersDone() {
        boolean bust = true;
        for (CardPlayer player : players) {
            bust = bust && (player.bust() || player.fold || player.broke());
        }

        return bust;
    }

    public boolean allPlayersFold() {
        boolean fold = true;
        for (CardPlayer player : players) {
            fold = fold && player.fold;
        }

        return fold;
    }
}
