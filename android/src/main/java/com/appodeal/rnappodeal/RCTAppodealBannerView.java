package com.appodeal.rnappodeal;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;


public class RCTAppodealBannerView extends ReactViewGroup implements BannerCallbacks {
    enum BannerSize {
        PHONE,
        TABLET
    }

    private BannerSize size = BannerSize.PHONE;
    private String placement = "default";

    private FrameLayout container;

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(
                        MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY)
                );
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    };

    public RCTAppodealBannerView(ThemedReactContext context) {
        super(context);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    public BannerSize getSize() {
        return this.size;
    }

    public void setAdSize(String adSize) {
        if (adSize.equals("tablet")) {
            this.size = BannerSize.TABLET;
        } else {
            this.size = BannerSize.PHONE;
        }

        cacheAdIfNeeded();
        addContainerIfNeeded();
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public void hideBannerView() {
        Activity activity = getReactContext().getCurrentActivity();
        if (container != null && activity != null) {
            container.removeAllViews();
            Appodeal.hide(activity, Appodeal.BANNER_VIEW);
        }
    }

    public void showBannerView() {
        postDelayed(() -> {
            Activity activity = getReactContext().getCurrentActivity();
            View adView;

            if (activity == null || container == null) {
                return;
            }

            Appodeal.set728x90Banners(size == RCTAppodealBannerView.BannerSize.TABLET);
            adView = Appodeal.getBannerView(activity);

            int height = getEstimatedHeight();

            Resources r = getReactContext().getResources();
            DisplayMetrics dm = r.getDisplayMetrics();

            int pxW = r.getDisplayMetrics().widthPixels;
            int pxH = dp2px(height, dm);

            LayoutParams bannerLayoutParams = new BannerView.LayoutParams(pxW, pxH);

            adView.setLayoutParams(bannerLayoutParams);
            adView.setVisibility(VISIBLE);

            container.addView(adView);

            if (placement != null) {
                Appodeal.show(activity, Appodeal.BANNER_VIEW, placement);
            } else {
                Appodeal.show(activity, Appodeal.BANNER_VIEW);
            }
        }, 250L);
    }

    private void cacheAdIfNeeded() {
        if (!Appodeal.isAutoCacheEnabled(Appodeal.BANNER_VIEW)) {
            Activity activity = getReactContext().getCurrentActivity();
            if (activity != null) {
                Appodeal.cache(activity, Appodeal.BANNER_VIEW);
            }
        }
    }

    private void addContainerIfNeeded() {
        if (container == null) {
            container = new FrameLayout(getReactContext());
            addView(container);
        }
    }

    private int getEstimatedHeight() {
        if (size == BannerSize.TABLET) {
            return 90;
        }
        return 50;
    }

    private ReactContext getReactContext() {
        return (ReactContext) getContext();
    }

    private RCTEventEmitter getEmitter() {
        return getReactContext().getJSModule(RCTEventEmitter.class);
    }

    private int dp2px(int dp, DisplayMetrics dm) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm));
    }

    @Override
    public void onBannerLoaded(int height, boolean isPrecache) {
        WritableMap params = Arguments.createMap();
        params.putInt("height", height);
        params.putBoolean("isPrecache", isPrecache);
        getEmitter().receiveEvent(getId(), "onBannerLoaded", params);
    }

    @Override
    public void onBannerFailedToLoad() {
        getEmitter().receiveEvent(getId(), "onBannerFailedToLoad", null);
    }

    @Override
    public void onBannerClicked() {
        getEmitter().receiveEvent(getId(), "onBannerClicked", null);
    }

    @Override
    public void onBannerExpired() {
        getEmitter().receiveEvent(getId(), "onBannerExpired", null);
    }

    @Override
    public void onBannerShowFailed() { }

    @Override
    public void onBannerShown() { }
}