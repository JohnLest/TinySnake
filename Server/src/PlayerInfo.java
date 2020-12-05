import java.io.Serializable;
import java.util.UUID;

/**
 * Stores infos relative to player
 */
public class PlayerInfo implements Serializable
{
    private static final long serialVersionUID = 6529685098267757690L;
    private String name;
    private int score;
    private Boolean ready;
    private UUID id; 

	/**
     * Creates empty player
     */
    public PlayerInfo()
    {
        ready = false;
        id = null;
        name = null;
        score = 0;
    }

    /**
     * Creates player based on nickname
     *
     * @param name The player name
     */
    public PlayerInfo(String name, UUID id)
    {
        this.id = id;
        this.name = name;
        score = 0;
        ready = false;
    }

    public int getScore()
    {
        return score;
    }

    public String getName()
    {
        return name;
    }

    public UUID getID()
    {
        return id;
    }

    public Boolean getReady()
    {
        return ready;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setScore(int s)
    {
        score = s;
    }

    /**
     * Increases the player score
     *
     * @param inc The amount to add to the score
     *
     * @return the new score
     */
    public int addToScore(int inc)
    {
        score += inc;
        return score;
    }

    public void setReady(boolean ready)
    {
        this.ready = ready;
    }
}
