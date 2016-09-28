import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joe on 27/09/2016.
 */
public class Main {

    public static void insertGame(Connection conn, String gameName, String gameGenre, String gamePlatform, int releaseYear)throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES(NULL,?,?,?,?)");
        stmt.setString(1,gameName);
        stmt.setString(2,gameGenre);
        stmt.setString(3,gamePlatform);
        stmt.setInt(4, releaseYear);
        stmt.execute();
    }

    public static void deleteGame(Connection conn, String gameName)throws SQLException{

        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE text=?");
        stmt.setString(1,gameName);
        stmt.execute();
    }


    public static ArrayList<Game> selectGames(Connection conn)throws SQLException{
        ArrayList<Game> gameList = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM games");

        while (results.next()){
            int id = results.getInt("id");
            String gameName = results.getString("text");
            String gameGenre = results.getString("genre");
            String gamePlatform = results.getString("platform");
            int releaseYear = results.getInt("release_year");

            Game game = new Game(id,gameName,gameGenre,gamePlatform,releaseYear);
            gameList.add(game);
        }
        return gameList;
    }

    public static void updateGame(Connection conn, String gameName,
                                  String gameGenre, String gamePlatform, int releaseYear)throws SQLException{

        PreparedStatement stmt = conn.prepareStatement("UPDATE games SET text=?, genre=?,platform=?,release_year=? WHERE text=?");
        stmt.setString(1,gameName);
        stmt.setString(2,gameGenre);
        stmt.setString(3,gamePlatform);
        stmt.setInt(4, releaseYear);
        stmt.setString(5,gameName);
        stmt.execute();
    }

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args)throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS games (" +
                "id IDENTITY, text VARCHAR, genre VARCHAR, platform VARCHAR, release_year INT)");

        Spark.init();

        Spark.get("/", ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");

                    User user = users.get(name);

                    HashMap m = new HashMap();
            
                    ArrayList<Game> games = selectGames(conn);
                    m.put("games",games);

                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    } else {
                        return new ModelAndView(m, "home.html");

                    }
                }),
                new MustacheTemplateEngine());

        Spark.post("/login",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }

                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                }));

        Spark.post("/logout", ((request, response) -> {

                    Session session=request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }));

        Spark.post("/create-game",
                ((request, response) -> {

                    Session session = request.session();
                    String userName = session.attribute("userName");
                    User user = users.get(userName);
                    if (user == null) {
                        throw new Exception("User is not logged in");
                    }


                    String gameGenre = request.queryParams("gameGenre");
                    String gameName = request.queryParams("gameName");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));

                    insertGame(conn,gameName,gameGenre,gamePlatform,gameYear);

                    response.redirect("/");
                    return "";
                }));
        Spark.post("/delete-game", ((request, response) -> {
            String gameName=request.queryParams("gameName");

            deleteGame(conn,gameName);

            response.redirect("/");
            return "";
        }));

        Spark.post("/edit-game",((request, response) -> {

            String gameGenre = request.queryParams("gameGenre");
            String gameName = request.queryParams("gameName");
            String gamePlatform = request.queryParams("gamePlatform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));

            updateGame(conn,gameName,gameGenre,gamePlatform,gameYear);

            response.redirect("/");
            return "";
        }));
    }
}
