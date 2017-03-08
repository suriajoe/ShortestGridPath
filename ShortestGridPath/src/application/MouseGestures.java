import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

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
                    if(newValue) 
                    {
                    	((Cell)node).hover();
                    	xCoordinate.setValue(((Cell)node).getColumn());
                    	yCoordinate.setValue(((Cell)node).getRow());
                    	fValue.setValue(((Cell)node).getF());
                    	gValue.setValue(((Cell)node).getG());
                    	hValue.setValue(((Cell)node).getH());
                    } 
                    else 
                    {
                    	((Cell)node).unhover();
                    }
                }

            });
        }

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