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
import java.io.PrintWriter;
import java.util.*;
import java.util.Map;

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

    private MapCoordinate TARGET;
    private MapCoordinate MYPOS;
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



    public List<SnakeDirection> move_to_target(MapCoordinate tar, MapCoordinate coord){
        List<SnakeDirection> directions = new ArrayList<>();
        // I vilken ordning skall det kontrolleras, nu x-axis
        if (tar.x - coord.x < 0){
            directions.add(SnakeDirection.RIGHT);
            if (tar.y - coord.y > 0){ //FLIP
                directions.add(SnakeDirection.DOWN);
                directions.add(SnakeDirection.UP);
            } else {
                directions.add(SnakeDirection.UP);
                directions.add(SnakeDirection.DOWN);
            }
            directions.add(SnakeDirection.LEFT);
        }
        else {
            directions.add(SnakeDirection.LEFT);
            if (tar.y - coord.y > 0) {  //FLIP
                directions.add(SnakeDirection.UP);
                directions.add(SnakeDirection.DOWN);
            } else {
                directions.add(SnakeDirection.DOWN);
                directions.add(SnakeDirection.UP);
            }
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
        int pos = translate(mapUtil.getMyPosition());
        int length =0;
        ListIterator tarIterator = TARGETS.listIterator();
        MapCoordinate coord = mapUtil.translatePosition(pos);
        switch (go_to){
            case RIGHT: path.insert(pos -1 ,pos -1);break;
            case  LEFT: path.insert(pos +1, pos +1);break;
            case  DOWN: path.insert(pos-46, pos-46);break;
            case    UP: path.insert(pos+46, pos+46);break;
            default:break;
        }
        while ( length < max ) {
            if (pos == translate(TARGET)){
                TARGET = mapUtil.translatePosition(TARGETS.get(TARGETS.get(tarIterator.nextIndex())));
            }

            List<SnakeDirection> directions = move_to_target(TARGET, coord);
            ListIterator listIterator = directions.listIterator();
            boolean inserted =false;
            while (!inserted) {
                if( !listIterator.hasNext() ){ return length; }
                SnakeDirection chosenDirection = directions.remove(listIterator.nextIndex());
                if (chosenDirection == SnakeDirection.LEFT) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path.in_tree(pos - 1)) {
                        pos = pos - 1;path.insert(pos, pos);inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.RIGHT) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path.in_tree(pos + 1)) {
                        pos = pos + 1;path.insert(pos, pos);inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.UP) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path.in_tree(pos - 46)) {
                        pos = pos - 46;path.insert(pos, pos);inserted =true;
                    }
                } else {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path.in_tree(pos + 46)) {
                        pos = pos + 46;path.insert(pos, pos);inserted =true;
                    }
                }
            }
            coord   = mapUtil.translatePosition(pos);
            System.out.println("path.add( " + coord);
            length++;
        }
        return length;
    }

    ////////////////////
    //////
    public SnakeDirection getChosenDirection(MapUtil map, MapCoordinate pos) {
        SnakeDirection chosenDirection = SnakeDirection.RIGHT;
        if ( pos.x - MYPOS.x >= 1 )
            chosenDirection = SnakeDirection.RIGHT;
        else if ( pos.x - MYPOS.x <= -1)
            chosenDirection = SnakeDirection.LEFT;
        else if ( pos.y - MYPOS.y <= -1)
            chosenDirection = SnakeDirection.UP;
        else if ( pos.x - MYPOS.x >= 1)
            chosenDirection = SnakeDirection.DOWN;
        else{
            System.out.println("Final chosen direction is wrong" );
        }
        if (!map.canIMoveInDirection(chosenDirection)) {
            System.out.println("Cannot move in wished dir");
            return random_chooser(map);
        }
        return chosenDirection;
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

        int[] it = mapUtil.translateCoordinates(mapUtil.getSnakeSpread(getPlayerId()));
        System.out.print("SnakeHEAD: " + mapUtil.translatePosition(it[0]) );


        MYPOS   = mapUtil.getMyPosition();
        for (SnakeInfo snakeInfo : mapUpdateEvent.getMap().getSnakeInfos()) {
            if ( snakeInfo.isAlive() ) {
                //  Get Tails
                int idx = 0;
                if (snakeInfo.getId() != getPlayerId() && snakeInfo.getTailProtectedForGameTicks() == 0) {
                    for (int i : snakeInfo.getPositions()) {
                        
                        if (idx == snakeInfo.getLength()-1) {
                            TARGETS.add(0, i);
                            TARGET = mapUtil.translatePosition(i);
                            System.out.println("TARGETS.addSnakeTail( " + TARGET);
                        }
                        idx++;
                    }
                }
            }
        }


        for (MapCoordinate c : mapUtil.listCoordinatesContainingFood()) {
            int d = c.getManhattanDistanceTo(MYPOS);
            if (mapUtil.isTileAvailableForMovementTo(c)) {
                System.out.println("TARGETS.addFood     ( " + c);
                TARGETS.add(0, translate(c));
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
                if ( length > max){ chosenDirection = tmp_dir; }
                // Check time consumed
                long elapseTime = (System.nanoTime() - starTime) /1000000;
                if (elapseTime > 240) {
                    System.out.println("--GETTING TARGETS--TIME LIMIT EXCEDED");
                    break;
                }
            }

       // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenDirection);
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
