package me.kgaz.kasyno.poker.table;

import me.kgaz.MantraLibs;
import me.kgaz.kasyno.poker.Card;
import me.kgaz.kasyno.poker.Hand;
import org.bukkit.entity.Player;

import java.util.Random;

public class PokerGame {

    private Player player1, player2;
    private int bank1, bank2;

    private boolean[] cards;

    private Hand hand1, hand2, middle;

    int stage = 0;

    public PokerGame(Player player1, int bank1, Player player2, int bank2) {

        this.player1 = player1;
        this.player2 = player2;

        this.bank1 = bank1;
        this.bank2 = bank2;

        cards = new boolean[Card.values().length];
        for(int i = 0; i < cards.length; i++) cards[i] = true;

        hand1 = new Hand(); hand2 = new Hand(); middle = new Hand();

        hand1.addCard(drawCard());
        hand1.addCard(drawCard());

        hand2.addCard(drawCard());
        hand2.addCard(drawCard());

        for(Card card : hand1.getCards()) player1.getInventory().addItem(MantraLibs.getInstance().getCardInitiator().getCardView(card));
        for(Card card : hand2.getCards()) player2.getInventory().addItem(MantraLibs.getInstance().getCardInitiator().getCardView(card));

    }

    public void surrenderPlayer1() {

        player1.sendMessage("§cGracz §4"+player1.getName()+" §cpoddal gre. Gracz §4"+player2.getName()+"§c wygrywa!");
        player2.sendMessage("§cGracz §4"+player1.getName()+" §cpoddal gre. Gracz §4"+player2.getName()+"§c wygrywa!");

        end();

    }

    public void surrenderPlayer2() {

        player1.sendMessage("§cGracz §4"+player2.getName()+" §cpoddal gre. Gracz §4"+player1.getName()+"§c wygrywa!");
        player2.sendMessage("§cGracz §4"+player2.getName()+" §cpoddal gre. Gracz §4"+player1.getName()+"§c wygrywa!");

        end();

    }

    public void end() {

        for(int i = 0; i < 9; i++) {
            player1.getInventory().setItem(i, null);
            player2.getInventory().setItem(i, null);
        }

    }

    private Card drawCard() {

        Random rand = new Random();
        int random = rand.nextInt(cards.length);

        while(!cards[random]) {

            random = rand.nextInt(cards.length);

        }

        cards[random] = false;
        return Card.values()[random];

    }

}
