import java.util.*;

/**
 * A simple play board using array of cases
 */
public class PlayArea extends Rectangle
{
    private Colour[][] board;
    private LinkedList<Coord> coordChanges;
    private LinkedList<Colour> colourChanges;

    /**
     * Reset Playboard
     */
    private void resetColour()
    {
        for (Coord c : this)
        {
            board[c.getLine()][c.getCol()] = Colour.BACKGROUND;
        }

        while (!coordChanges.isEmpty())
        {
            coordChanges.pop();
            colourChanges.pop();
        }
    }

    /**
     * New PlayBoard using height and width
     *
     * @param h The height of the playboard
     * @param w The width of the playboard
     */
    public PlayArea(int h, int w)
    {
        super(w, h, 10);
        board = new Colour[h][w];
        coordChanges = new LinkedList<Coord>();
        colourChanges = new LinkedList<Colour>();

        resetColour();
    }

    /**
     * Init a square playboard
     *
     * @param size The length of the playboard side in squares
     */
    public PlayArea(int size)
    {
        this(size, size);
    }

    /**
     * Copy the content of the Array into target
     *
	 * @param target the PlayerArea instance that will receive a copy of the board
     *
     **/
    public void copy(PlayArea target) {
        int lines = this.getHeight();
        int cols = this.getLength();
        for (int i = 0; i < lines; i++)
        {
	        System.arraycopy(target.board[i], 0, board[i], 0, cols);
        }
    }

    /**
     * Synchronize this and target board based on Changes Lists
     *
     * @param target The PlayArea to update
     */
    public void updateCopy(PlayArea target)
    {
        while (coordChanges.size() > 0)
        {
            Coord c = coordChanges.pop();
            Colour col = colourChanges.pop();

            target.setCaseColour(c, col);
        }
    }

    /**
     * Initialises a new PlayArea by setting borders
     */
    public void init()
    {
        resetColour();

        // Set up border
        Rectangle[] borders = new Rectangle[4];
        // Top Border
        borders[0] = new Rectangle(this.getLength(), 1);
        // Bottom Border
        borders[1] = new Rectangle(this.getHeight() - 1, 0, this.getLength(), 1);
        // Left
        borders[2] = new Rectangle(1, 0, 1, this.getHeight() - 2);
        // Right
        borders[3] = new Rectangle(1, this.getLength() - 1, 1, this.getHeight() - 2);

        for (Rectangle border : borders)
        {
            for (Coord c : border)
            {
                this.setCaseColour(c, Colour.WALL);
            }
        }
    }

    public boolean caseIsEmpty(Coord c)
    {
        return board[c.getLine()][c.getCol()] == Colour.BACKGROUND;
    }

    public boolean caseIsSafe(Coord c)
    {
        return board[c.getLine()][c.getCol()] == Colour.BACKGROUND
            || board[c.getLine()][c.getCol()] == Colour.FRUIT;
    }

    public Coord getMiddle()
    {
        return new Coord(this.getHeight() / 2, this.getLength()/2);
    }

    public Colour getCaseColour(int l, int c)
    {
        return board[l][c];
    }

    public Colour getCaseColour(Coord c)
    {
        return board[c.getLine()][c.getCol()];
    }

    public void setCaseColour(int l, int c, Colour col)
    {
        board[l][c] = col;
        coordChanges.add(new Coord(l, c));
        colourChanges.add(col);
    }

    public void setCaseColour(Coord c, Colour col)
    {
        board[c.getLine()][c.getCol()] = col;
        coordChanges.add(new Coord(c));
        colourChanges.add(col);
    }

    public String toString()
    {
        String result = "";
        for (Colour[] lines : board)
        {
            for (Colour cur : lines)
            {
                switch(cur)
                {
                    case WALL:
                        result += "X";
                        break;
                    case SNAKE_HEAD:
                        result += "O";
                        break;
                    case SNAKE_BODY:
                        result += "o";
                        break;
                    case FRUIT:
                        result += "*";
                        break;
                    default:
                        result += " ";
                        break;
                }
            }
            result += "\n";
        }

        return result;
    }
}
