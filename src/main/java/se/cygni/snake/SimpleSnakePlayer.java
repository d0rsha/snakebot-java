package se.cygni.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.snake.api.event.*;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.GameMode;
import se.cygni.snake.api.model.GameSettings;
import se.cygni.snake.api.model.PlayerPoints;
import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.api.util.GameSettingsUtils;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private AnsiPrinter ansiPrinter = new AnsiPrinter(ANSI_PRINTER_ACTIVE, true);

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
    private static List<MapCoordinate> STARS;
    private static BinarySearchTree bst = new BinarySearchTree();
    private boolean target_locked = false;
    private MapCoordinate TARGET;
    private int distance = 46;

    private boolean first_run = true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
private SnakeDirection path_chooser(MapUtil map) {
    // Move shortest path to TARGET
    // Try if to the RIGHT
    if (TARGET.x - map.getMyPosition().x > 0)

    {
        if (map.canIMoveInDirection(SnakeDirection.RIGHT))
            return SnakeDirection.RIGHT;
        if (TARGET.y - map.getMyPosition().y < 0) {
            if (map.canIMoveInDirection(SnakeDirection.UP))
                return SnakeDirection.UP;
        } else {
            if (map.canIMoveInDirection(SnakeDirection.DOWN))
                return SnakeDirection.DOWN;
        }
    }
    // Try if to the LEFT
    else if(TARGET.x -map.getMyPosition().x< 0)

    {
        if (map.canIMoveInDirection(SnakeDirection.LEFT))
            return SnakeDirection.LEFT;
        if (TARGET.y - map.getMyPosition().y > 0) {
            if (map.canIMoveInDirection(SnakeDirection.DOWN))
                return SnakeDirection.DOWN;
        } else {
            if (map.canIMoveInDirection(SnakeDirection.UP))
                return SnakeDirection.UP;
        }
    }
    // At the same x.Position
    else

    {
        if (TARGET.y - map.getMyPosition().y > 0) {
            if (map.canIMoveInDirection(SnakeDirection.DOWN))
                return SnakeDirection.DOWN;
        } else {
            if (map.canIMoveInDirection(SnakeDirection.UP))
                return SnakeDirection.UP;
        }
    }

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



    //Path finder
    private SnakeDirection path_finder(MapUtil map){

            MapCoordinate xy[];
            xy = map.listCoordinatesContainingObstacle();
            for (MapCoordinate coordinate : map.listCoordinatesContainingFood() ) {
                STARS.add(coordinate);
                if (coordinate.getManhattanDistanceTo(map.getMyPosition()) < distance){
                    TARGET = coordinate;
                    System.out.println(coordinate);
                }
            }


        return path_chooser(map);
   }


    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        ansiPrinter.printMap(mapUpdateEvent);

        // MapUtil contains lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());
        if (first_run){
            MapCoordinate  coord = mapUtil.getMyPosition();

            Integer pos = coord.x*mapUpdateEvent.getMap().getWidth() + coord.y*mapUpdateEvent.getMap().getHeight();

            while (bst.size < 5){
                Random r = new Random();
                int c = r.nextInt(3);
               // System.out.print(" " + c);
                if ( c == 0 ){
                    pos += mapUpdateEvent.getMap().getWidth();
                }
                else if( c == 1 ){
                    pos += 1;
                }
                else if (c == 2 ) {
                    pos -= 1;
                }
                else {
                    pos -= mapUpdateEvent.getMap().getWidth();
                }
                if (mapUtil.isTileAvailableForMovementTo(mapUtil.translatePosition(pos)) ) {
                    bst.insert(pos);
                }
            }
          //  bst.display(bst.root);
            first_run=false;
        }

        int head = bst.get_head();
        MapCoordinate coord = (mapUtil.translatePosition(head));

        SnakeDirection chosenDirection = SnakeDirection.UP;//path_finder(mapUtil);
        if (coord.x - mapUtil.getMyPosition().x < 0)
            chosenDirection = SnakeDirection.LEFT;
        else if (coord.x - mapUtil.getMyPosition().x > 0)
            chosenDirection = SnakeDirection.RIGHT;
        else if (coord.y - mapUtil.getMyPosition().y < 0)
            chosenDirection = SnakeDirection.DOWN;



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
