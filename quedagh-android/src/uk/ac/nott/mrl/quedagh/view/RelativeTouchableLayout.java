package uk.ac.nott.mrl.quedagh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class RelativeTouchableLayout extends RelativeLayout
{
	private View view;

	public RelativeTouchableLayout(final Context context)
	{
		super(context);
	}

	public RelativeTouchableLayout(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	public RelativeTouchableLayout(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev)
	{
		if(view != null)
		{
			view.invalidate();
		}
		return super.onInterceptTouchEvent(ev);
	}

	public void setView(View view)
	{
		this.view = view;		
	}
}
