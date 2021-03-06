package org.jdownloader.plugins.components.config;

import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.jdownloader.plugins.config.Order;
import org.jdownloader.plugins.config.PluginConfigInterface;
import org.jdownloader.plugins.config.PluginHost;
import org.jdownloader.plugins.config.Type;
import org.jdownloader.translate._JDT;

@PluginHost(host = "deluxemusic.tv", type = Type.HOSTER)
public interface DeluxemusicTvConfigInterface extends PluginConfigInterface {
    public static class TRANSLATION {
        public String getFastLinkcheckEnabled_label() {
            return _JDT.T.lit_enable_fast_linkcheck();
        }

        public String getEnableCategoryCrawler_label() {
            return "Enable category crawler? This may add huge amounts of URLs!";
        }

        public String getOnlyGrabBestQuality_label() {
            return "Only grab best quality?";
        }
    }

    public static final DeluxemusicTvConfigInterface.TRANSLATION TRANSLATION = new TRANSLATION();

    @DefaultBooleanValue(false)
    @Order(9)
    boolean isFastLinkcheckEnabled();

    void setFastLinkcheckEnabled(boolean b);

    @DefaultBooleanValue(false)
    @Order(10)
    boolean isEnableCategoryCrawler();

    void setEnableCategoryCrawler(boolean b);

    @DefaultBooleanValue(false)
    @Order(20)
    boolean isOnlyGrabBestQuality();

    void setOnlyGrabBestQuality(boolean b);
}