package blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TriantaEna extends Game {
    public static TriantaEna instance;

    // player constants
    public static final int STARTING_FUNDS_PLAYER = 100;
    public static final int STARTING_FUNDS_BANKER = 300;
    public static final String PLAYER_NAME = "Player";
    public static final String DEALER_NAME = "Banker";

    // game constants
    public static final int MAX_PLAYERS = 9;
    public static final int MAX_VAL = 31;
    public static final int MAX_DEALER_VAL = 27;
    public static final int NATURAL_HAND = 3;
    public static final int WILD_CARD = 14;

    private TriantaEna() {
        super(MAX_VAL, MAX_DEALER_VAL, MAX_PLAYERS);
    }

    public static void start() {
        try {
            Hand.setMax(MAX_VAL);
            instance = new TriantaEna();
            instance.createPlayers(PLAYER_NAME, DEALER_NAME, STARTING_FUNDS_PLAYER, STARTING_FUNDS_BANKER);
            instance.play();
        } catch (EndGameException e) {
            exit();
        }
    }

    public static void exit(){
        System.out.println("\n*** Thank you for playing TriantaEna! ***\n");
        instance = null;
    }

    @Override
    public void printPlayerControls() {
        System.out.println("Controls:\n1 or `hit` to hit\n2 or `stand` to stand\n3 or `peek` to peek\n4 or `help` for help\n5 or `quit` to quit\n");
    }

    @Override
    public void printDealerControls() {
        System.out.println("Controls:\n1 or `hit` to hit\n2 or `stand` to stand\n3 or `help` for help\n4 or `quit` to quit\n");
    }

    @Override
    public void play() throws EndGameException {
        System.out.println("!!! Remember to press `enter` after your inputs.");
        System.out.printf("!!! Player starts with $%d.\n", STARTING_FUNDS_PLAYER);
        System.out.printf("!!! Dealer starts with $%d.\n", STARTING_FUNDS_BANKER);

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
                dealer.dealCard(player, player.getHand(FIRST_INDEX), FACE_DOWN);
                Thread.sleep(SLEEP_TIME);
            }
            
            dealer.dealCard(dealer, dealer.getHand(), FACE_UP);
            Thread.sleep(SLEEP_TIME);

            for (Player player : players) {
                if (player.broke()) continue;
                player.start(sc);
            }

            /* Second card given */
            for (Player player : players) {
                if (player.broke() || player.fold) continue;
                dealer.dealCard(player, player.getHand(FIRST_INDEX));
                Thread.sleep(SLEEP_TIME);
            }

            /* Third card given */
            for (Player player : players) {
                if (player.broke() || player.fold) continue;
                dealer.dealCard(player, player.getHand(FIRST_INDEX));
                Thread.sleep(SLEEP_TIME);
            }

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
            dealer.checkAndShuffle(players);
            dealer.newRound();

            for (Player player : players) {
                if (player.broke()) continue;
                player.newRound();
            }

            deal();
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
        if (!oneWinner()) {
            System.out.print("Would players like to continue to next round? `Yes` or `No`\n--- ");
            restart = "yes".equals(sc.nextLine().toLowerCase());
        }

        return restart;
    }

    public boolean oneWinner() {
        if (allPlayersBroke()) {
            return true;
        } else {
            if (!dealer.broke()) {
                return false;
            } else {
                int count = 0;
                for (Player player : players) {
                    if (!player.broke()) {
                        count += 1;
                    }
                }
                return count == 1;
            }
        }
    }

    public void convertBanker(ArrayList<CardPlayer> ranking) {
        for (int i = 0; i < ranking.size(); i++) {
            CardPlayer candidate = ranking.get(i);
            if (candidate.broke()) continue;
            if (candidate == dealer || candidate.money <= dealer.money) {
                break;
            } else {
                System.out.printf("%s has money exceeding %s.\n", candidate.name, dealer.name);
                String input;
                while (true) {
                    System.out.printf("Would %s like to be banker? `yes` or `no`?\n--- ", candidate.name);
                    input = sc.nextLine();
                    if (input.equalsIgnoreCase("yes")) {
                        players.remove(candidate);
                        String newName = dealer.name();
                        players.add(new Player(newName, dealer.money));
                        dealer = new Dealer(candidate.name, DEALER_NAME, candidate.money);
                        return;
                    } else if (input.equalsIgnoreCase("no")) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int compare(Hand h1, Hand h2) {
        int result = h1.compareTo(h2);
        if (result == 0) {
            if (h1.computeValue() == MAX_VAL) {
                if (h1.size() == NATURAL_HAND && h2.size() != NATURAL_HAND) return 1;
                else if (h1.size() != NATURAL_HAND && h2.size() == NATURAL_HAND) return -1;
            }
        } else if (result == -1) {
            if (h1.computeValue() == WILD_CARD) {
                String suit = h1.get(FIRST_INDEX).getSuit();
                for (Card c : h1.getAll()) {
                    if (!c.getSuit().equals(suit)) {
                        return result;
                    }
                }

                return 1;
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
                case "peek":
                System.out.printf(player.name + ": " + player.peekCards() + "\n");
                    break;
                case "4":
                case "help":
                    break;
                case "5":
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

    @Override
    public void tie(CardPlayer player, Hand hand) {
        super.tie(player, hand);
        player.loseBet(dealer);
    }

    @Override
    public void printRanking() {
        System.out.println("\n*** Results ***");
        ArrayList<CardPlayer> allPlayers = new ArrayList<CardPlayer>();
        allPlayers.addAll(players);
        allPlayers.add(dealer);

        Collections.sort(allPlayers, new Comparator<CardPlayer>() {
            public int compare(CardPlayer p1, CardPlayer p2) {
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
        for (CardPlayer player : allPlayers) {
            System.out.print(count + ". " + player.name + ": ");
            if (player.money > 0) {
                System.out.println("$" + player.money);
            } else {
                System.out.println("BUST");
            }
            count += 1;
        }

        System.out.println("");
        convertBanker(allPlayers);
    }
}