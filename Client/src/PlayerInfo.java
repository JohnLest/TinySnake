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

    public PlayerInfo()
    {
        this.id = null;
        this.name = null;
        this.score = 0;
        this.ready = false;
    }

    public PlayerInfo(String name, UUID id, int score, boolean ready)
    {
        this.id = id;
        this.name = name;
        this.score = score;
        this.ready = ready;
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

}
