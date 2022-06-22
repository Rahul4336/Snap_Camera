package s.nap;

import android.content.Context;
import android.graphics.BitmapFactory;

import java.io.InputStream;


public enum Filters {
    NORMAL,
    GREEN,
    YELOW,
    BULGE_DISTORTION,
    CGA_COLOR_SPACE,
    SKY,
    GLAY_SCALE,
    INVERT,
    LOOKUP_TABLE,
    MONOCHROME,
    OVERLAY,
    SEPIA,
    SHARPEN,
    WAKE,
    TONE_CURVE,
    TONE,
    VIGNETTE,
    WEAKPIXELINCLUSION,
    FILTER_GROUP,
    NOT_BLUE,
    TGREEN,
    DULL,SET,SPARK;

    public static GlFilter getFilterInstance(Filters filter, Context context) {
        switch (filter) {
            case GREEN:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_two));
            case YELOW:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_three));
            case BULGE_DISTORTION:
                return new GlBulgeDistortionFilter();
            case CGA_COLOR_SPACE:
                return new GlCGAColorspaceFilter();
            case SKY:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_four));
            case GLAY_SCALE:
                return new GlGrayScaleFilter();
            case INVERT:
                return new GlInvertFilter();
            case LOOKUP_TABLE:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.lookup_sample));
            case MONOCHROME:
                return new GlMonochromeFilter();
            case OVERLAY:
                return new GlBitmapOverlaySample(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
            case SEPIA:
                return new GlSepiaFilter();
            case SHARPEN:
                return new GlSharpenFilter();
            case WAKE:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_five));

            case TONE_CURVE:
                try {
                    InputStream inputStream = context.getAssets().open("acv/tone_cuver_sample.acv");
                    return new GlToneCurveFilter(inputStream);
                } catch (Exception e) {
                    return new GlFilter();
                }
            case TONE:
                return new GlToneFilter();
            case VIGNETTE:
                return new GlVignetteFilter();
            case WEAKPIXELINCLUSION:
                return new GlWeakPixelInclusionFilter();
            case FILTER_GROUP:
                return new GlFilterGroup(new GlMonochromeFilter(), new GlVignetteFilter());
            case NOT_BLUE:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_six));
            case TGREEN:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_seven));
            case DULL:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_eight));
            case SET:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_nine));
            case SPARK:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.sam_ten));


            default:
                return new GlFilter();
        }

    }

}
