//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.util.LinkedHashMap;

import jd.PluginWrapper;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.jdownloader.scripting.JavaScriptEngineFactory;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "beeg.com" }, urls = { "https?://(?:www\\.)?beeg\\.com/((?!section|tag)[a-z0-9\\-]+/[a-z0-9\\-]+|\\d+)" }, flags = { 0 })
public class BeegCom extends PluginForHost {

    /* DEV NOTES */
    /* Porn_plugin */

    private String DLLINK = null;

    public BeegCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://beeg.com/contacts/";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    private static final String INVALIDLINKS = "http://(www\\.)?beeg\\.com/generator.+";
    private boolean             server_issue = false;

    @SuppressWarnings({ "deprecation", "unchecked" })
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws Exception {
        server_issue = false;
        final String fid = new Regex(downloadLink.getDownloadURL(), "(\\d+)$").getMatch(0);
        if (downloadLink.getDownloadURL().matches(INVALIDLINKS)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        this.br.getPage("https://api.beeg.com/api/v5/video/" + fid);
        if (this.br.getHttpConnection().getResponseCode() == 404) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        final LinkedHashMap<String, Object> entries = (LinkedHashMap<String, Object>) JavaScriptEngineFactory.jsonToJavaObject(br.toString());
        String filename = (String) entries.get("title");
        final String[] qualities = { "1080", "720", "480", "360", "240" };
        for (final String quality : qualities) {
            DLLINK = (String) entries.get(quality + "p");
            if (DLLINK != null) {
                break;
            }
        }

        if (filename == null || DLLINK == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (DLLINK.startsWith("//")) {
            DLLINK = "https:" + DLLINK;
        }
        DLLINK = DLLINK.replace("{DATA_MARKERS}", "data=pc.DE");
        final String key = new Regex(this.DLLINK, "/key=([^<>\"=]+)%2Cend=").getMatch(0);
        if (key != null) {

        }
        String ext = DLLINK.substring(DLLINK.lastIndexOf("."));
        if (ext == null || ext.length() > 5) {
            ext = ".flv";
        }
        filename = filename.trim();
        if (filename.endsWith(".")) {
            filename = filename.substring(0, filename.length() - 1);
        }
        downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + ext);
        br.setFollowRedirects(true);
        br.getHeaders().put("Referer", downloadLink.getDownloadURL());
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(DLLINK);
            if (con.isOK() && !con.getContentType().contains("html") && !con.getContentType().contains("text")) {
                downloadLink.setDownloadSize(con.getLongContentLength());
            } else {
                server_issue = true;
            }
            return AvailableStatus.TRUE;
        } finally {
            try {
                con.disconnect();
            } catch (Throwable e) {
            }
        }
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (server_issue) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Unknown server issue", 30 * 60 * 1000l);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html") || dl.getConnection().getContentType().contains("text")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    /** TODO: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/beeg.py */
    private String decryptKey(final String key) {
        return null;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }
}