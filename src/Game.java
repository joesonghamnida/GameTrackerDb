/**
 * Created by joe on 27/09/2016.
 */
public class Game {
    int id;
    String name;
    String genre;
    String platform;
    int releaseYear;

    public Game(int id,String name, String genre, String platform, int releaseYear) {
        this.id=id;
        this.name = name;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}
