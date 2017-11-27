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
    private static final boolean ANSI_PRINTER_ACTIVE = false;
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

    //Variables
    private static SnakeDirection chosenDirection = SnakeDirection.RIGHT;
    private static List<MapCoordinate> path;
    private static int counter = 0;
    private MapCoordinate TARGET;
    private MapCoordinate TARGET2;
    private MapCoordinate MYPOS;
    private static final int WIDTH = 46;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
private SnakeDirection path_chooser(MapUtil map) {
    // Move shortest path to TARGET
    // Try if to the RIGHT
    if (TARGET2.x - MYPOS.x > 0) {
       // System.out.println("GO RIGHT?");
        if (map.canIMoveInDirection(SnakeDirection.RIGHT))
            return SnakeDirection.RIGHT;
        if (TARGET2.y - MYPOS.y < 0) {
            if (map.canIMoveInDirection(SnakeDirection.UP))
                return SnakeDirection.UP;
        } else {
            if (map.canIMoveInDirection(SnakeDirection.DOWN))
                return SnakeDirection.DOWN;
        }
      //  System.out.println("NOPE");
    }
    // Try if to the LEFT
    else if(TARGET2.x - MYPOS.x < 0) {
     //   System.out.println("GO LEFT?");
        if (map.canIMoveInDirection(SnakeDirection.LEFT))
            return SnakeDirection.LEFT;
        if (TARGET2.y - MYPOS.y > 0) {
            if (map.canIMoveInDirection(SnakeDirection.DOWN))
                return SnakeDirection.DOWN;
        } else {
            if (map.canIMoveInDirection(SnakeDirection.UP))
                return SnakeDirection.UP;
        }
      //  System.out.println("NOPE");
    }
    // At the same x.Position
    else

    {
        if (TARGET2.y - MYPOS.y > 0) {
            if (map.canIMoveInDirection(SnakeDirection.DOWN))
                return SnakeDirection.DOWN;
        } else {
            if (map.canIMoveInDirection(SnakeDirection.UP))
                return SnakeDirection.UP;
        }
    }
    //System.out.println("Choose by RANDOM");
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

    if(!directions.isEmpty())
        chosenDirection =directions.get(r.nextInt(directions.size()));
    return chosenDirection;
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///Today
    private int translate(MapCoordinate coord ){
        return coord.x + (coord.y)*WIDTH;
    }

    private MapCoordinate fill_path(int max, MapUtil mapUtil){
        int c = 0;
        path.clear();

        int pos = translate(MYPOS);
        int cnt = 0;
        MapCoordinate coord = TARGET;


        while (path.size() < max){
            boolean inserted = false;
            while (!inserted) {
                if (TARGET.x - MYPOS.x > 0) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos+1)) && !path.contains(pos+1)){
                        path.add(mapUtil.translatePosition(pos+1)); inserted = true;
                    }
                    if (TARGET.y - MYPOS.y < 0) {
                        if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos-WIDTH)) && !path.contains(pos-WIDTH)){
                            path.add(mapUtil.translatePosition(pos-WIDTH));inserted = true;
                        }
                    } else {
                        if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos+WIDTH)) && !path.contains(pos+WIDTH)){
                            path.add(mapUtil.translatePosition(pos+WIDTH)); inserted = true;
                        }
                    }
                }
                // Try if to the LEFT
                else if(TARGET.x - MYPOS.x < 0) {
                    if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos-1)) && !path.contains(pos-1)){
                        path.add(mapUtil.translatePosition(pos-1)); inserted = true;
                    }
                    if (TARGET.y - MYPOS.y > 0) {
                        if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos+WIDTH)) && !path.contains(pos+WIDTH)){
                            path.add(mapUtil.translatePosition(pos+WIDTH)); inserted = true;
                        }
                    } else {
                        if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos-WIDTH)) && !path.contains(pos-WIDTH)){
                            path.add(mapUtil.translatePosition(pos-WIDTH)); inserted =true;
                        }
                    }
                }
                // At the same x.Position
                else {
                    if (TARGET.y - MYPOS.y > 0) {
                        if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos+WIDTH)) && !path.contains(pos+WIDTH)){
                            path.add(mapUtil.translatePosition(pos+WIDTH)); inserted = true;
                        }

                    } else {
                        if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos-WIDTH)) && !path.contains(pos-WIDTH)){
                            path.add(mapUtil.translatePosition(pos-WIDTH)); inserted =true;
                        }
                        else
                            inserted = true; // Did not insert Hehe...
                    }

                }
                pos = translate(path.get(path.size()-1));
                if(first){
                    first = false;
                    coord = mapUtil.translatePosition(pos);
                }
            }
        }
        return coord;
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        ansiPrinter.printMap(mapUpdateEvent);
        counter = (counter % 20) +1;
        // MapUtil contains lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());

        int distance = 200;

        MYPOS   = mapUtil.getMyPosition();
        if (counter < 10){

            for (SnakeInfo snakeInfo : mapUpdateEvent.getMap().getSnakeInfos()) {
                if ( !snakeInfo.getId().equals(getPlayerId()) && snakeInfo.getTailProtectedForGameTicks() == 0){
                    for (int i : snakeInfo.getPositions()) {
                        int d = mapUtil.translatePosition(i).getManhattanDistanceTo(MYPOS);
                        if ( d < distance){
                            TARGET = mapUtil.translatePosition(i);
                            distance = d;
                        }
                    }
                }else{
                    System.out.print(snakeInfo.getTailProtectedForGameTicks());
                }

            }
        }else{

            for ( MapCoordinate c : mapUtil.listCoordinatesContainingFood() ){
                int d =c.getManhattanDistanceTo(MYPOS);
                if (d < distance){
                    TARGET = c;
                    distance = d;
                }
            }
        }

        TARGET2 = fill_path(5, mapUtil);
        chosenDirection = path_chooser(mapUtil);
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
