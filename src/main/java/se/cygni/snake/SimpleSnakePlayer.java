package se.cygni.snake;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.snake.api.event.*;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.*;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.api.util.GameSettingsUtils;
import se.cygni.snake.client.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map;
import java.lang.Math;
public class SimpleSnakePlayer extends BaseSnakeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSnakePlayer.class);

    // Set to false if you want to start the game from a GUI
    private static final boolean AUTO_START_GAME = true;

    // Personalise your game ...
    private static final String SERVER_NAME = "snake.cygni.se";
    private static final int SERVER_PORT = 80;

    private static final GameMode GAME_MODE = GameMode.TRAINING;
    private static final String SNAKE_NAME = "d0rshbot";

    // Set to false if you don't want the game world printed every game tick.
    private static final boolean ANSI_PRINTER_ACTIVE = true;
    private AnsiPrinter ansiPrinter = new AnsiPrinter(ANSI_PRINTER_ACTIVE, false);

    public static void main(String[] args) {
        SimpleSnakePlayer simpleSnakePlayer = new SimpleSnakePlayer();

        try {
            ListenableFuture<WebSocketSession> connect = simpleSnakePlayer.connect();
            connect.get();
        } catch (Exception e) {
            LOGGER.error("Failed to connect to server", e);
            System.exit(1);
        }

        startTheSnake(simpleSnakePlayer);
    }

    /**
     * The Snake client will continue to run ...
     * : in TRAINING mode, until the single game ends.
     * : in TOURNAMENT mode, until the server tells us its all over.
     */
    private static void startTheSnake(final SimpleSnakePlayer simpleSnakePlayer) {
        Runnable task = () -> {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (simpleSnakePlayer.isPlaying());

            LOGGER.info("Shutting down");
        };

        Thread thread = new Thread(task);
        thread.start();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////   MY STUFF STARTS    /////////////////////////////////////////////////////////////////////////////
public static class ReverseIterating<T> implements Iterable<T> {
    private final LinkedList<T> list;

    public ReverseIterating(LinkedList<T> list) {
        this.list = list;
    }

    @Override
    public Iterator<T> iterator() {
        return list.descendingIterator();
    }
}
    //Variables
    private int counter =0;
    private MapCoordinate TARGET;
    private MapCoordinate HEAD;
    private MapCoordinate TAIL;
    private int WIDTH = 46;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private SnakeDirection random_chooser(MapUtil map) {
        System.out.println("MOVE TO RANDOM");
        // Can't move directly to TARGET so let Random decide
        List<SnakeDirection> directions = new ArrayList<>();

        // Let's see in which directions I can move
        for(SnakeDirection direction :SnakeDirection.values()) {
            if (map.canIMoveInDirection(direction)) {
                directions.add(direction);
            }
        }

        // Choose a random direction
        Random r = new Random();
        SnakeDirection chosenDirection = SnakeDirection.DOWN;

        if(!directions.isEmpty()){
            chosenDirection =directions.get(r.nextInt(directions.size()));
        }else {System.out.println("U dead"); }

        return chosenDirection;
    }



    public List<SnakeDirection> move_to_target(MapCoordinate tar, MapCoordinate pos, SnakeDirection come_from) {
       List<SnakeDirection> directions = new ArrayList<>();
                if (come_from == SnakeDirection.LEFT) {
                    directions.add(SnakeDirection.UP);
                    directions.add(SnakeDirection.RIGHT);
                    directions.add(SnakeDirection.DOWN);
                }else if (come_from == SnakeDirection.RIGHT) {
                    directions.add(SnakeDirection.DOWN);
                    directions.add(SnakeDirection.LEFT);
                    directions.add(SnakeDirection.UP);
                }
                else if ( come_from == SnakeDirection.UP) {
                    directions.add(SnakeDirection.RIGHT);
                    directions.add(SnakeDirection.DOWN);
                    directions.add(SnakeDirection.LEFT);
                }
                else{
                    directions.add(SnakeDirection.LEFT);
                    directions.add(SnakeDirection.UP);
                    directions.add(SnakeDirection.RIGHT);
                }
        return directions;
    }



   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///Today
    public int translate(MapCoordinate coord ){
        return coord.x + (coord.y)*WIDTH;
    }

    private int get_path_length(int max, MapUtil mapUtil, SnakeDirection go_to, List<Integer> TARGETS){
        BinarySearchTree path = new BinarySearchTree();
        path.insert(translate(TAIL), translate(TAIL));
        int pos = translate(HEAD);
        int length =0;
        ListIterator tarIterator = TARGETS.listIterator();

        int idx = 0;
        TARGET = mapUtil.translatePosition(TARGETS.get(idx++));
        while ( length < max && idx < TARGETS.size() ) {
            // Imag path suceded
            if (pos == translate(TARGET)){
                TARGET = mapUtil.translatePosition(TARGETS.get(idx++));
                System.out.println("Get Next Target:" + TARGET);
                length += 10;
            }

            switch (go_to){
                case RIGHT: path.insert(pos , pos );pos +=1;break;
                case  LEFT: path.insert(pos , pos );pos -=1;break;
                case  DOWN: path.insert(pos , pos); pos +=46;break;
                case    UP: path.insert(pos , pos); pos -=46;break;
                default:break;
            }
            MapCoordinate coord = mapUtil.translatePosition(pos);
           // System.out.println("Imag path:" + coord);
            // Kolla så att det är exakt tre val som kommer i List
            List<SnakeDirection> directions = move_to_target(TARGET, coord, go_to);
            ListIterator listIterator = directions.listIterator();
            boolean inserted =false;
            while (!inserted) {
                if( !listIterator.hasNext() ){ return length; }
                SnakeDirection chosenDirection = directions.remove(listIterator.nextIndex());
                if (chosenDirection == SnakeDirection.LEFT && coord.x >= 0) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos - 1)) && !path.in_tree(pos - 1)) {
                        go_to = SnakeDirection.LEFT;inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.RIGHT && coord.x <= 45) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path.in_tree(pos + 1)) {
                        go_to = SnakeDirection.RIGHT;inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.UP && coord.y >= 0) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos - 46)) && !path.in_tree(pos - 46)) {
                        go_to = SnakeDirection.UP;inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.DOWN && coord.y <= 33){
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 46)) && !path.in_tree(pos + 46)) {
                        go_to = SnakeDirection.DOWN;inserted =true;
                    }
                }
            }
            length++;
/*
            for (int i = 0; i < 33; i++) {
                for (int j = 0; j <45 ; j++) {

                    if (i*46 + j == translate(HEAD))
                        System.out.print(" H ");
                    else if (i*46 + j == translate(TAIL))
                         System.out.print(" T ");
                    else if (i*46 + j == translate(TARGET))
                        System.out.print(" A ");
                    else if (i*46 + j == path.find(i*46 + j))
                        System.out.print(" P ");
                    else if(mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(i*46 + j)))
                        System.out.print(" - ");
                    else
                        System.out.print(" X ");

                }
                System.out.println();
            }*/
        }
        return length;
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        long starTime = System.nanoTime();
        ansiPrinter.printMap(mapUpdateEvent);
        // MapUtil find lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());
        List<Integer> TARGETS = new ArrayList<>();
        WIDTH = mapUpdateEvent.getMap().getWidth();
        int distance = 10000;

        HEAD   = mapUtil.getMyPosition();
        int[] it = mapUtil.translateCoordinates(mapUtil.getSnakeSpread(getPlayerId()));
        TAIL = mapUtil.translatePosition(it[mapUtil.getPlayerLength(getPlayerId())-1]);
        System.out.println("SnakeHEAD == " + HEAD);
        System.out.println("SnakeTAIL == " + TAIL);



        for (SnakeInfo snakeInfo : mapUpdateEvent.getMap().getSnakeInfos()) {
            if ( snakeInfo.isAlive() && snakeInfo.getId() != getPlayerId() ) {
                //  Get Tails
                if (snakeInfo.getTailProtectedForGameTicks() == 0) {
                    it = snakeInfo.getPositions();
                    int insert = it[snakeInfo.getLength()-1];
                    if (insert != translate(TAIL)) {
                        int d = mapUtil.translatePosition(insert).getManhattanDistanceTo(HEAD);
                        if (d < distance ) {
                            TARGETS.add(0, insert);
                            distance = d;
                        } else
                            TARGETS.add(TARGETS.size() - 1, insert);
                        TARGET = mapUtil.translatePosition(it[snakeInfo.getLength() - 1]);
                        //System.out.println("TARGETS.addSnakeTail( " + TARGET);
                    }
                }
            }
        }


        for (MapCoordinate c : mapUtil.listCoordinatesContainingFood()) {
            if (mapUtil.isTileAvailableForMovementTo(c)) {
                //System.out.println("TARGETS.addFood     ( " + c);
                int d = c.getManhattanDistanceTo(HEAD);
                if ( d < distance ){
                    TARGETS.add(0, translate(c));
                    distance = d;
                }
                else
                    TARGETS.add(TARGETS.size()-1, translate(c));
            }
        }
        SnakeDirection chosenDirection = SnakeDirection.RIGHT;
        List<SnakeDirection> directions = new ArrayList<>();
        for (SnakeDirection direction : SnakeDirection.values()) {
            if (mapUtil.canIMoveInDirection(direction)) {
                directions.add(direction);
                chosenDirection = direction;
            }
        }
            int max = 0;
            for (SnakeDirection tmp_dir : directions )
            {
                int length = get_path_length(20, mapUtil, tmp_dir, TARGETS);
                if (chosenDirection == SnakeDirection.DOWN || chosenDirection ==SnakeDirection.UP)
                    length -= 5;
                if ( length > max){
                    chosenDirection = tmp_dir;
                  //  if (length >= 20 ) break;
                }
                // Check time consumed
                long elapseTime = (System.nanoTime() - starTime) /1000000;
                if (elapseTime > 240) {
                    System.out.println("--GETTING TARGETS--TIME LIMIT EXCEDED");
                    break;
                }
            }
        System.out.println("Moving to TARGET == " + TARGET );
       // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenDirection);
        counter++;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////   MY STUFF ENDS     ///////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onInvalidPlayerName(InvalidPlayerName invalidPlayerName) {
        LOGGER.debug("InvalidPlayerNameEvent: " + invalidPlayerName);
    }

    @Override
    public void onSnakeDead(SnakeDeadEvent snakeDeadEvent) {
        LOGGER.info("A snake {} died by {}",
                snakeDeadEvent.getPlayerId(),
                snakeDeadEvent.getDeathReason());
    }

    @Override
    public void onGameResult(GameResultEvent gameResultEvent) {
        LOGGER.info("Game result:");
        gameResultEvent.getPlayerRanks().forEach(playerRank -> LOGGER.info(playerRank.toString()));
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        LOGGER.debug("GameEndedEvent: " + gameEndedEvent);
    }

    @Override
    public void onGameStarting(GameStartingEvent gameStartingEvent) {
        LOGGER.debug("GameStartingEvent: " + gameStartingEvent);
    }

    @Override
    public void onPlayerRegistered(PlayerRegistered playerRegistered) {
        LOGGER.info("PlayerRegistered: " + playerRegistered);

        if (AUTO_START_GAME) {
            startGame();
        }
    }

    @Override
    public void onTournamentEnded(TournamentEndedEvent tournamentEndedEvent) {
        LOGGER.info("Tournament has ended, winner playerId: {}", tournamentEndedEvent.getPlayerWinnerId());
        int c = 1;
        for (PlayerPoints pp : tournamentEndedEvent.getGameResult()) {
            LOGGER.info("{}. {} - {} points", c++, pp.getName(), pp.getPoints());
        }
    }

    @Override
    public void onGameLink(GameLinkEvent gameLinkEvent) {
        LOGGER.info("The game can be viewed at: {}", gameLinkEvent.getUrl());
    }

    @Override
    public void onSessionClosed() {
        LOGGER.info("Session closed");
    }

    @Override
    public void onConnected() {
        LOGGER.info("Connected, registering for training...");
        GameSettings gameSettings = GameSettingsUtils.trainingWorld();
        registerForGame(gameSettings);
    }

    @Override
    public String getName() {
        return SNAKE_NAME;
    }

    @Override
    public String getServerHost() {
        return SERVER_NAME;
    }

    @Override
    public int getServerPort() {
        return SERVER_PORT;
    }

    @Override
    public GameMode getGameMode() {
        return GAME_MODE;
    }
}
