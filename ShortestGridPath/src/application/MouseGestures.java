import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

public class MouseGestures 
{
    boolean showHoverCursor = true;
    
    public void color(Node node, IntegerProperty xCoordinate, IntegerProperty yCoordinate,
    		DoubleProperty fValue, DoubleProperty gValue, DoubleProperty hValue) 
    {
        if(showHoverCursor) 
        {
            node.hoverProperty().addListener(new ChangeListener<Boolean>(){

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
                {

                    //System.out.println( visible + ": " + newValue);

                    if(newValue) 
                    {
                    	//((Cell)node).hoverHighlight();
                    	xCoordinate.setValue(((Cell)node).getColumn());
                    	yCoordinate.setValue(((Cell)node).getRow());
                    	fValue.setValue(((Cell)node).getF());
                    	gValue.setValue(((Cell)node).getG());
                    	hValue.setValue(((Cell)node).getH());
                    } 
                    else 
                    {
                    	//((Cell)node).hoverUnhighlight();
                    }

                    //for( String s: node.getStyleClass())
                        //System.out.println( node + ": " + s);
                }

            });
        }

        //node.setOnMousePressed(onMousePressedEventHandler);
        //node.setOnDragDetected(onDragDetectedEventHandler);
        //node.setOnMouseDragEntered(onMouseDragEnteredEventHandler);

    }
    
    /*
    EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

        Cell cell = (Cell) event.getSource();

        if(event.isPrimaryButtonDown()) 
        {
            cell.highlight();
        } 
        else if(event.isSecondaryButtonDown()) 
        {
            cell.unhighlight();
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

        PickResult pickResult = event.getPickResult();
        Node node = pickResult.getIntersectedNode();

        if( node instanceof Cell) {

            Cell cell = (Cell) node;

            if(event.isPrimaryButtonDown()) 
            {
                cell.highlight();
            } 
            else if(event.isSecondaryButtonDown()) 
            {
                cell.unhighlight();
            }

        }

    };       
    EventHandler<MouseEvent> onDragDetectedEventHandler = event -> {

        Cell cell = (Cell) event.getSource();
        cell.startFullDrag();

    };

    EventHandler<MouseEvent> onMouseDragEnteredEventHandler = event -> {

        Cell cell = (Cell) event.getSource();

        if( event.isPrimaryButtonDown()) {
            cell.highlight();
        } else if( event.isSecondaryButtonDown()) {
            cell.unhighlight();
        }

    };
    */
    
    public void startPoint(Node node)
    {
    	((Cell)node).clean();
    	((Cell)node).start();
    }
    public void goalPoint(Node node)
    {
    	((Cell)node).clean();
    	((Cell)node).goal();
    }
    public void roughColor(Node node)
    {
    	if((Cell)node == null)//cell reached out of bounds
    	{
    		
    	}
    	else
    	  ((Cell)node).brown();
    }

}