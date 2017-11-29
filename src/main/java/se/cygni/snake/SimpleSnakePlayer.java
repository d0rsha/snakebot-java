package se.cygni.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.snake.api.event.*;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.*;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.api.util.GameSettingsUtils;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

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
    private static SnakeDirection chosenDirection = SnakeDirection.RIGHT;
    private static BinarySearchTree path = new BinarySearchTree();
    private static BinarySearchTree path2 = new BinarySearchTree();
    private static BinarySearchTree NOK = new BinarySearchTree();
    private static BinarySearchTree NOK2 = new BinarySearchTree();
    private static int NOK_SIZE = 0;
    private static BinarySearchTree TARGETS = new BinarySearchTree();
    private static int T_IDX = 0;
    private static int counter = 0;
    private static int SIZE = 0;
    private MapCoordinate TARGET;
    private MapCoordinate TARGET2;
    private MapCoordinate MYPOS;
    private static final int WIDTH = 46;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
private SnakeDirection random_chooser(MapUtil map) {
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public boolean isTileAvailableForMovementTo(MapUtil mapUtil, MapCoordinate pos){
    if (NOK2.in_tree(translate(pos)) ){ // Dosen't check adjacent heads
        System.out.println("Not good tile");
        return false;
    }
    int p = translate(pos);
    for (int i = 0; i < NOK_SIZE; i++) {
        if (NOK.find(i) == p)
            return false;
    }
    return mapUtil.isTileAvailableForMovementTo(pos);
}

public boolean recursive(BinarySearchTree tree){

    return true;
}
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///Today
    private int translate(MapCoordinate coord ){
        return coord.x + (coord.y)*WIDTH;
    }

    private boolean fill_path(int max, MapUtil mapUtil){
        int cnt = 0;

        int pos = translate(MYPOS);
        MapCoordinate coord = MYPOS;

        while ( pos != translate(TARGET) || SIZE < max || TARGET.getManhattanDistanceTo(mapUtil.translatePosition(pos)) >1) {
            List<SnakeDirection> directions = new ArrayList<>();
            // Let's see in which directions I can move
            if (TARGET.x - coord.x < 0){
                directions.add(SnakeDirection.RIGHT);
                if (TARGET.y - coord.y < 0){
                    directions.add(SnakeDirection.DOWN);
                    directions.add(SnakeDirection.UP);
                } else {
                    directions.add(SnakeDirection.UP);
                    directions.add(SnakeDirection.DOWN);
                }
                directions.add(SnakeDirection.LEFT);
            }
            else{
                directions.add(SnakeDirection.LEFT);
                if (TARGET.y - coord.y > 0){
                    directions.add(SnakeDirection.UP);
                    directions.add(SnakeDirection.DOWN);
                } else{
                    directions.add(SnakeDirection.DOWN);
                    directions.add(SnakeDirection.UP);
                }
                directions.add(SnakeDirection.RIGHT);
            }




            int idx =0;
            boolean inserted =false;
            while (!inserted) {
                // Choose a random direction
               chosenDirection = directions.get(idx);

                if (chosenDirection == SnakeDirection.LEFT) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path2.in_tree(pos - 1)
                            && !NOK2.in_tree(pos - 1)) {
                        pos = pos - 1;
                        path2.insert(SIZE, pos);
                        path.insert(pos, SIZE++);inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.RIGHT) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path2.in_tree(pos + 1)
                            && !NOK2.in_tree(pos + 1)) {
                        pos = pos + 1;
                        path2.insert(SIZE, pos);
                        path.insert(pos, SIZE++);inserted =true;
                    }
                } else if (chosenDirection == SnakeDirection.UP) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path2.in_tree(pos - 46)
                            && !NOK2.in_tree(pos - 46)) {
                        pos = pos - 46;
                        path2.insert(SIZE, pos);
                        path.insert(pos, SIZE++);inserted =true;
                    }
                } else {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos + 1)) && !path2.in_tree(pos + 46)
                            && !NOK2.in_tree(pos + 46)) {
                        pos = pos + 46;
                        path2.insert(SIZE, pos);
                        path.insert(pos, SIZE++);inserted =true;
                    }
                }
                idx++;
                if( idx == 4 ){
                    NOK2.insert(NOK_SIZE, pos);
                    NOK.insert(pos, NOK_SIZE++);
                    if (SIZE <= 0) {System.out.println("Pathfinder lead back to start"); return false;}
                    path2.delete(path.find(SIZE));
                    path.delete(SIZE--);
                }

            }
            coord   = mapUtil.translatePosition(pos);
        }
        return true;
    }

    public SnakeDirection getChosenDirection(MapUtil map, MapCoordinate pos) {
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
        ansiPrinter.printMap(mapUpdateEvent);
        counter = (counter % 40) +1;
        // MapUtil find lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());

        int distance = 10000;
        System.out.println("Snakespread: ");
        for ( int xy : mapUtil.translateCoordinates(mapUtil.getSnakeSpread(getPlayerId()))){
            System.out.print(mapUtil.translatePosition(xy)); break;
        }
        System.out.println();

                MYPOS   = mapUtil.getMyPosition();
                int index = 0;
              for (SnakeInfo snakeInfo : mapUpdateEvent.getMap().getSnakeInfos()) {
                  if ( snakeInfo.isAlive() ) {
                    //  Get Tails
                      int idx = 0;
                      for (int i : snakeInfo.getPositions()) {
                          int d = mapUtil.translatePosition(i).getManhattanDistanceTo(MYPOS);
                          if (d < distance && idx == snakeInfo.getLength() ) {
                              TARGETS.insert(i, T_IDX++);
                              TARGET = mapUtil.translatePosition(i);
                              distance = d;
                              System.out.println("new TARGET == " + TARGET);
                          }
                          idx++;
                      }
                      // Get Head + body OF ALL SNAKES alive inc player
                      idx = 0;
                      for (int i :snakeInfo.getPositions() ) {
                          if ( idx != snakeInfo.getLength()-1) {
                              NOK2.insert(NOK_SIZE, i);
                              NOK.insert(i, NOK_SIZE++);
                          }
                          idx++;
                      }
                  }
              }
        for (MapCoordinate c : mapUtil.listCoordinatesContainingObstacle()) {
            NOK2.insert(NOK_SIZE, translate(c));
            NOK.insert(translate(c), NOK_SIZE++);
        }


        for (MapCoordinate c : mapUtil.listCoordinatesContainingFood()) {
            int d = c.getManhattanDistanceTo(MYPOS);
            if (d < distance && !NOK2.in_tree(translate(c))) {
                TARGETS.insert(translate(c), T_IDX++);
                TARGET = c;
                distance = d;
                System.out.println("new TARGET == " + TARGET);
            }
        }
        int idx =0;
        while ( idx < T_IDX ){
            if (fill_path(25, mapUtil)) {
                break;
            }
            idx++;
        }
        MapCoordinate fin_pos = mapUtil.translatePosition(path.find(0));
        if (fin_pos.x == fin_pos.y && fin_pos.x == 0)
            chosenDirection= random_chooser(mapUtil);
        else {
            System.out.println("Path == " + fin_pos);
            chosenDirection = getChosenDirection(mapUtil, fin_pos);
        }
        if (!mapUtil.canIMoveInDirection(chosenDirection))
            chosenDirection = random_chooser(mapUtil);

            // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenDirection);
        counter++;
        NOK.clear(NOK.root );
        NOK2.clear(NOK2.root);
        TARGETS.clear(TARGETS.root );
        T_IDX =0;
        NOK_SIZE =0;
        SIZE =0;
        path.clear(path.root);
        path2.clear(path2.root);
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
