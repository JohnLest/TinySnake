import java.io.Serializable;

/**
 * Represents a coordinate in a table using line and col numbers
 */
public class Coord implements Serializable
{
    private int line;
    private int col;

    public Coord() {}
    /**
     * Creates a new Coords based on line and col
     *
     * @param l The line of the Coord
     * @param c The column of the Coord
     */
    public Coord(int l, int c)
    {
        this.line = l;
        this.col = c;
    }

    /**
     * Creates a new Coord based on another Coord
     *
     * @param c The Coord to copy
     */
    public Coord(Coord c)
    {
        line = c.line;
        col = c.col;
    }

    /**
     * Tests if two Coords are equals
     *
     * @param c The Coord to test
     *
     * @return true if this and c are equals, false otherwise
     */
    public boolean equals(Coord c) 
    {
        return (line == c.line && col == c.col);
    }

    /**
     * Performs addition of coordinates
     *
     * @param c The Coord to add
     *
     * @return The results of the computation this + c
     */
    public Coord add(Coord c)
    {
        return new Coord(line + c.line, col + c.col);
    }

    /**
     * Compute the opposite of a Coord
     *
     * @param c The Coord for which the opposite is computed
     *
     * @return -c
     */
    public Coord neg(Coord c)
    {
        return new Coord(-line, -col);
    }

    // Getters
    public int getLine()
    {
        return line;
    }

    public int getCol()
    {
        return col;
    }

    // Setters
    public void setLine(int l)
    {
        this.line = l;
    }

    public void setCol(int c)
    {
        this.col = c;
    }

    public String toString()
    {
        return "(" + line + ", " + col + ")";
    }
}

