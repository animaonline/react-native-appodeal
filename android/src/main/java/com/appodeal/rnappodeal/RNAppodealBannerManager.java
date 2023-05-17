package com.appodeal.rnappodeal;

import com.appodeal.ads.Appodeal;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class RNAppodealBannerManager extends SimpleViewManager<RCTAppodealBannerView> {
    private final List<WeakReference<RCTAppodealBannerView>> instances = new ArrayList<>();

    @ReactProp(name = "adSize")
    public void setSize(RCTAppodealBannerView view, String size) { view.setAdSize(size); }

    @ReactProp(name = "placement")
    public void setPlacement(RCTAppodealBannerView view, String placement) { view.setPlacement(placement); }

    @NonNull
    @Override
    public RCTAppodealBannerView createViewInstance(@NonNull ThemedReactContext context) {
        RCTAppodealBannerView banner = new RCTAppodealBannerView(context);
        // Setup callbacks
        Appodeal.setBannerCallbacks(banner);
        // Hide previously created banners
        // Iterate through instances in forward direction
        Iterator<WeakReference<RCTAppodealBannerView>> iterator = this.instances.iterator();
        while (iterator.hasNext()) {
            WeakReference<RCTAppodealBannerView> reference = iterator.next();
            RCTAppodealBannerView mBanner = reference.get();
            if (mBanner != null) {
                mBanner.hideBannerView();
            }
        }
        // Save instance
        banner.showBannerView();
        this.instances.add(new WeakReference<>(banner));

        return banner;
    }

    @Override
    public void onDropViewInstance(@NonNull RCTAppodealBannerView view) {
        super.onDropViewInstance(view);
        view.hideBannerView();

        // Trying to show a previous banner
        // Iterate through instances in reverse direction
        ListIterator<WeakReference<RCTAppodealBannerView>> iterator = instances.listIterator(instances.size());
        while (iterator.hasPrevious()) {
            WeakReference<RCTAppodealBannerView> reference = iterator.previous();
            RCTAppodealBannerView banner = reference.get();
            if (banner == null) {
                continue;
            }

            if (banner == view) {
                iterator.remove();
            } else if (banner.getSize() == view.getSize()) {
                banner.showBannerView();
                break;
            }
        }
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "onBannerLoaded",
                MapBuilder.of("registrationName", "onAdLoaded"),
                "onBannerFailedToLoad",
                MapBuilder.of("registrationName", "onAdFailedToLoad"),
                "onBannerClicked",
                MapBuilder.of("registrationName", "onAdClicked"),
                "onBannerExpired",
                MapBuilder.of("registrationName", "onAdExpired")
        );
    }

    @Override
    public String getName() {
        return "RNAppodealBannerView";
    }
}
