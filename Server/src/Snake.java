import java.util.*;

/**
 * Represents the snake
 */
public class Snake
{
    private LinkedList<Coord> body;
    private Coord head;
    private Direction dir;
    private Colour hcolour;
    private Colour bcolour;
    private boolean isAlive;
    
    // Constructors
    /**
     * Creates a snake based on the line and the col of its head and
     * its initial orientation (growth is done in the opposite direction)
     *
     * @param l The line of the head
     * @param c The col of the head
     * @param d The initial direction of the snake
     */
    public Snake(int l, int c, Direction d)
    {
        Direction growth = d.oppositeDirection();
        Coord cur = new Coord(l, c);

        head = new Coord(cur);
        body = new LinkedList<Coord>();

        for (int i = 0; i < Settings.INIT_SNAKE_LENGTH; i++)
        {
            body.add(cur);
            cur = cur.add(growth);
        }

        this.isAlive = true;
        this.dir = d;
        this.bcolour = Colour.SNAKE_BODY;
        this.hcolour = Colour.SNAKE_HEAD;
    }

    /**
     * Creates a snake based on Coord of its head and
     * its initial orientation (growth is done in the opposite direction)
     *
     * @param c The Coord of the head
     * @param d The initial direction of the snake
     */
    public Snake(Coord c, Direction d)
    {
        this(c.getLine(), c.getCol(), d);
    }

    // Methods
    /**
     * Performs a move
     *
     * @param growth Indicates whether the Snake should be growing at this turn
     */
    void moveForward(boolean growth)
    {
        head = head.add(dir);

        body.addFirst(head);
        if (!growth)
        {
            body.removeLast();
        }
    }

    // Getters

    public boolean getIsAlive()
    {
        return isAlive;
    }

    public Direction getDirection()
    {
        return dir;
    }

    public Colour getHeadColour()
    {
        return hcolour;
    }

    public Colour getBodyColour()
    {
        return bcolour;
    }

    public LinkedList<Coord> getPosition()
    {
        return body;
    }

    public Coord getHeadPosition()
    {
        return new Coord(body.peek());
    }

    public Coord getTailPosition()
    {
        return new Coord(body.peekLast());
    }

    public int getSnakeLength() 
    {
        return body.size();
    }
    
    // Setters
    public boolean setDirection(Direction d)
    {
        if (d != dir.oppositeDirection())
        {
            this.dir = d;
            return true;
        }

        return false;
    }

    public void setIsAlive(boolean bool){
        this.isAlive = bool;
    }

    public void setHeadColour(Colour c)
    {
        this.hcolour = c;
    }

    public void setBodyColour(Colour c)
    {
        this.bcolour = c;
    }

    // Others
    public String toString()
    {
        String result = "";
        for (Coord pos : body)
        {
            result += pos.toString() + "\n";
        }

        return result;
    }
}


