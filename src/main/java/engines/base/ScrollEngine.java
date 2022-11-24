package engines.base;

import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public class ScrollEngine implements AdjustmentListener
{
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    public final static int START = 0;
    public final static int END = 1;

    private int viewportPosition;

    private JScrollBar scrollBar;
    private boolean adjustScrollBar = true;

    private int previousValue = -1;
    private int previousMaximum = -1;

    /**
     *  Convenience constructor.
     *  Scroll direction is VERTICAL and viewport position is at the END.
     *
     *  @param scrollPane the scroll pane to monitor
     */
    public ScrollEngine(JScrollPane scrollPane) {
        this(scrollPane, VERTICAL, END);
    }

    /**
     *  Convenience constructor.
     *  Scroll direction is VERTICAL.
     *
     *  @param scrollPane the scroll pane to monitor
     *  @param viewportPosition valid values are START and END
     */
    public ScrollEngine(JScrollPane scrollPane, int viewportPosition) {
        this(scrollPane, VERTICAL, viewportPosition);
    }

    /**
     *  Specify how the SmartScroller will function.
     *
     *  @param scrollPane the scroll pane to monitor
     *  @param scrollDirection indicates which JScrollBar to monitor.
     *                         Valid values are HORIZONTAL and VERTICAL.
     *  @param viewportPosition indicates where the viewport will normally be
     *                          positioned as data is added.
     *                          Valid values are START and END
     */
    public ScrollEngine(JScrollPane scrollPane, int scrollDirection, int viewportPosition) {
        if (scrollDirection != HORIZONTAL &&  scrollDirection != VERTICAL) {
            throw new IllegalArgumentException("invalid scroll direction specified");
        }
        if (viewportPosition != START &&  viewportPosition != END) {
            throw new IllegalArgumentException("invalid viewport position specified");
        }
        this.viewportPosition = viewportPosition;
        if (scrollDirection == HORIZONTAL) {
            scrollBar = scrollPane.getHorizontalScrollBar();
        } else {
            scrollBar = scrollPane.getVerticalScrollBar();
        }
        scrollBar.addAdjustmentListener(this);
        Component view = scrollPane.getViewport().getView();
        if (view instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) view;
            DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    @Override
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                checkScrollBar(e);
            }
        });
    }
    
    private void checkScrollBar(AdjustmentEvent e) {
        JScrollBar scrollBar = (JScrollBar)e.getSource();
        BoundedRangeModel listModel = scrollBar.getModel();
        
        int value = listModel.getValue();
        int extent = listModel.getExtent();
        int maximum = listModel.getMaximum();

        boolean valueChanged = previousValue != value;
        boolean maximumChanged = previousMaximum != maximum;

        if (valueChanged && !maximumChanged) {
            if (viewportPosition == START)
                adjustScrollBar = value != 0;
            else
                adjustScrollBar = value + extent >= maximum;
        }

        if (adjustScrollBar && viewportPosition == END) {
            scrollBar.removeAdjustmentListener( this );
            value = maximum - extent;
            scrollBar.setValue( value );
            scrollBar.addAdjustmentListener( this );
        }

        if (adjustScrollBar && viewportPosition == START) {
            scrollBar.removeAdjustmentListener( this );
            value = value + maximum - previousMaximum;
            scrollBar.setValue( value );
            scrollBar.addAdjustmentListener( this );
        }

        previousValue = value;
        previousMaximum = maximum;
    }
}