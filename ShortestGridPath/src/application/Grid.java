import javafx.scene.layout.Pane;

public class Grid extends Pane 
{

    int rows;
    int columns;

    double width;
    double height;

    Cell[][] cells;

    public Grid(int columns, int rows, double width, double height) 
    {

        this.columns = columns;
        this.rows = rows;
        this.width = width;
        this.height = height;

        cells = new Cell[rows][columns];

    }

    /**
     * Add cell to array and to the UI.
     */
    public void add(Cell cell, int column, int row) 
    {

        cells[row][column] = cell;

        double w = width / columns;
        double h = height / rows;
        double x = w * column;
        double y = h * row;

        cell.setLayoutX(x*2);
        cell.setLayoutY(y*2);
        cell.setPrefWidth(w*2);
        cell.setPrefHeight(h*2);

        getChildren().add(cell);

    }

    public Cell getCell(int column, int row) 
    {
        return cells[row][column];
    }

    /**
     * Unhighlight all cells
     */
    public void unhighlight() 
    {
        for( int row=0; row < rows; row++) 
        {
            for( int col=0; col < columns; col++) 
            {
                cells[row][col].unhighlight();
            }
        }
    }
}
