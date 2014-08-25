package me.dawson.progressball.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class ProgressBall extends View {
	public static final String TAG = "ProgressBall";

	public static final int DEFAUL_RADIUS = 16;

	private static enum Status {
		NONE, LEFT_IN, RIGHT_OUT, RIGH_IN, LEFT_OUT
	};

	private ValueAnimator animator;
	private int offset;
	private Status status;
	private Paint paint;
	private int ballRadius;

	public ProgressBall(Context context) {
		super(context);
		init();
	}

	public ProgressBall(Context context, AttributeSet set) {
		super(context, set);
		init();
	}

	private void init() {
		paint = new Paint();
		ballRadius = DEFAUL_RADIUS;
		status = Status.NONE;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setStatus(Status.LEFT_IN);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		setStatus(Status.NONE);
	}

	private void setStatus(Status st) {
		if (status == st) {
			return;
		}
		status = st;

		if (status == Status.NONE) {
			hideAnimation();
		} else {
			showAnimation();
		}
	}

	private void showAnimation() {
		animator = new ValueAnimator();
		animator.setDuration(800);
		animator.setRepeatCount(0);

		if (status == Status.LEFT_IN || status == Status.RIGHT_OUT) {
			animator.setIntValues(0, 100);
		} else if (status == Status.LEFT_OUT || status == Status.RIGH_IN) {
			animator.setIntValues(100, 0);
		}

		TimeInterpolator interpolator = null;
		if (status == Status.LEFT_IN || status == Status.RIGH_IN) {
			interpolator = new AccelerateInterpolator();
		} else if (status == Status.RIGHT_OUT || status == Status.LEFT_OUT) {
			interpolator = new DecelerateInterpolator();
		}
		animator.setInterpolator(interpolator);

		animator.addListener(al);
		animator.addUpdateListener(ul);
		animator.start();
	}

	private void hideAnimation() {
		if (animator != null && animator.isRunning()) {
			animator.cancel();
		}
	}

	private AnimatorUpdateListener ul = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			offset = (Integer) animation.getAnimatedValue();
			invalidate();
		}
	};

	private AnimatorListener al = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (status == Status.LEFT_IN) {
				setStatus(Status.RIGHT_OUT);
			} else if (status == Status.RIGHT_OUT) {
				setStatus(Status.RIGH_IN);
			} else if (status == Status.RIGH_IN) {
				setStatus(Status.LEFT_OUT);
			} else if (status == Status.LEFT_OUT) {
				setStatus(Status.LEFT_IN);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		paint.reset();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setColor(Color.TRANSPARENT);
		paint.setStyle(Paint.Style.FILL);

		int width = getWidth();
		int height = getHeight();

		if (height < ballRadius * 2) {
			ballRadius = height / 2;
		}

		paint.setColor(0xFFF05B72);

		// draw center ball
		int centerX = width / 2;
		int centerY = height / 2;
		paint.setAlpha(255);
		canvas.drawCircle(centerX, centerY, ballRadius, paint);

		int distance = centerX - ballRadius * 3;
		if (status == Status.LEFT_IN || status == Status.LEFT_OUT) {
			// draw right ball
			int cx = centerX + ballRadius * 2;
			int cy = centerY;
			canvas.drawCircle(cx, cy, ballRadius, paint);

			// draw left ball
			int alpha = (int) (106 + offset * 1.5);
			paint.setAlpha(alpha);
			cx = offset * distance / 100 + ballRadius;
			canvas.drawCircle(cx, cy, ballRadius, paint);
		} else if (status == Status.RIGH_IN || status == Status.RIGHT_OUT) {
			// draw left ball
			int cx = centerX - ballRadius * 2;
			int cy = centerY;
			canvas.drawCircle(cx, cy, ballRadius, paint);

			// draw right ball
			int alpha = (int) (256 - offset * 1.5);
			paint.setAlpha(alpha);
			cx = centerX + offset * distance / 100 + ballRadius * 2;
			canvas.drawCircle(cx, cy, ballRadius, paint);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = measureWidth(widthMeasureSpec);
		int measuredHeight = measureHeight(heightMeasureSpec);

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private int measureWidth(int measureSpec) {
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		int padding = getPaddingLeft() + getPaddingRight();
		int measuredSize;
		if (mode == MeasureSpec.EXACTLY) {
			measuredSize = size;
		} else {
			measuredSize = getSuggestedMinimumWidth();
			measuredSize += padding;
			if (mode == MeasureSpec.AT_MOST) {
				measuredSize = Math.max(measuredSize, size);
			}
		}
		return measuredSize;
	}

	private int measureHeight(int measureSpec) {
		int measuredSize;
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		int padding = getPaddingTop() + getPaddingBottom();
		if (mode == MeasureSpec.EXACTLY) {
			measuredSize = size;
		} else {
			measuredSize = getSuggestedMinimumHeight();
			measuredSize += padding;
			if (mode == MeasureSpec.AT_MOST) {
				measuredSize = Math.min(measuredSize, size);
			}
		}
		return measuredSize;
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return ballRadius * 10;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return ballRadius * 2;
	}
}
