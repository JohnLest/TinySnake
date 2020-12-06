public class Direction extends Coord
{
    public static final Direction UP = new Direction(-1, 0);
    public static final Direction DOWN = new Direction (1, 0);
    public static final Direction LEFT = new Direction (0, -1);
    public static final Direction RIGHT = new Direction (0, 1);

    public Direction(int l, int c)
    {
        super(l, c);
    }

    public Direction oppositeDirection()
    {
        if (this == UP)
            return DOWN;
        if (this == DOWN)
            return UP;
        if (this == LEFT)
            return RIGHT;
        else
            return LEFT;
    }
}
