package east.orientation.receiver.media;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by ljq on 2018/1/24.
 */

public class OutputSurfaceView extends SurfaceView {
    public OutputSurfaceView(Context context) {
        super(context);
    }

    public OutputSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    }
}
