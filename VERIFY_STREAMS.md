# Verifying station stream URLs

The URLs in `app/src/main/assets/stations.json` marked `"needsVerification": true`
are placeholders based on how these stations are commonly hosted (many Malawian
stations use Zeno.FM). **They are not guaranteed to work as-is.** Radio stream
URLs also change over time as stations switch hosting providers, so treat this
as a living file.

## How to find the real stream URL for a station

1. **Zeno.FM stations** — go to the station's Zeno.FM page (e.g.
   `zeno.fm/radio/<station-slug>/`), open it in a desktop browser, open
   DevTools → Network tab, hit play, and look for a request to
   `stream.zeno.fm/xxxxxxxxxxxxx` — that URL (usually ending in a random ID,
   not a nice slug) is the real stream endpoint.

2. **Station's own website** — many stations embed a "Listen Live" player.
   View page source or DevTools Network tab and look for a `.mp3`, `.aac`, or
   `.m3u8` URL, often from a Shoutcast/Icecast host (port `8000` or similar).

3. **TuneIn / Radio Garden / Streema** — these aggregators list many African
   stations and sometimes expose a direct stream link if you inspect their
   embedded player network requests.

4. **radio-browser.info** — a free, open, community-maintained API of radio
   station streams worldwide. Search `https://www.radio-browser.info` for
   Malawi stations; if listed, it usually has a verified working stream URL
   and you can pull it via their API (`/json/stations/bycountry/Malawi`).

## Updating the app

Once you have a verified URL, just edit the `streamUrl` field for that station
in `stations.json` and set `"needsVerification": false`. No code changes
needed — the app reads this file at runtime from the bundled assets.

## Testing a stream URL quickly

Paste the URL into VLC (mobile or desktop) → Open Network Stream. If it plays
there, it'll play in the app.
