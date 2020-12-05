/**
 * Stores infos relative to player
 */
public class PlayerInfo
{
    private String name;
    private int score;

	/**
     * Creates empty player
     */
    public PlayerInfo()
    {
        name = null;
        score = 0;
    }

    /**
     * Creates player based on nickname
     *
     * @param name The player name
     */
    public PlayerInfo(String name)
    {
        this.name = name;
        score = 0;
    }

    public int getScore()
    {
        return score;
    }

    public String getName()
    {
        return name;
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
}
