package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.example.xyzreader.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdaterService extends IntentService {
    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";
    private static final String TAG = "UpdaterService";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                //  throw new JSONException("Invalid parsed item array" ); todo uncomment
                JSONObject object = new JSONObject(getDefaultJson());
                array = object.getJSONArray("");
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                values.put(ItemsContract.Items.SERVER_ID, object.getString("id"));
                values.put(ItemsContract.Items.AUTHOR, object.getString("author"));
                values.put(ItemsContract.Items.TITLE, object.getString("title"));
                values.put(ItemsContract.Items.BODY, object.getString("body"));
                values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb"));
                values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo"));
                values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio"));
                time.parse3339(object.getString("published_date"));
                values.put(ItemsContract.Items.PUBLISHED_DATE, time.toMillis(false));
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }

    //todo remove
    private String getDefaultJson() {
        return "[         {"
                + "                       \"id\": \"4\","
                + "                \"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p004.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p004.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Carl Sagan\","
                + "\"title\": \"Mysteries of the Universe Solved\","
                + "\"published_date\": \"2013-06-20T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "        {"
                + "\"id\": \"2\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p002.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p002.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Edgar Allen Poe\","
                + "\"title\": \"A Flatiron State of Mind\","
                + "\"published_date\": \"2013-01-19T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"11\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p011.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p011.jpg\","
                + "\"aspect_ratio\": 0.66667,"
                + "\"author\": \"Ernest Hemingway\","
                + "\"title\": \"An Empire State of Mind\","
                + "\"published_date\": \"2013-06-19T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"3\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p003.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p003.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Vincent Van Gogh\","
                + "\"title\": \"10 Tips for Hipster Tea Parties\","
                + "\"published_date\": \"2013-05-10T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"1\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p001.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p001.jpg\","
                + "\"aspect_ratio\": 1.50376,"
                + "\"author\": \"Tom Brokaw\","
                + "\"title\": \"My Story of Climbing a Mountain\","
                + "\"published_date\": \"2013-05-02T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "        },"
                + "{"
                + "\"id\": \"5\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p005.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p005.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Thomas Edison\","
                + "\"title\": \"How Fido Got His Bone Back\","
                + "\"published_date\": \"2013-03-19T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"7\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p007.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p007.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Plato\","
                + "\"title\": \"Why I Love Yellow\","
                + "\"published_date\": \"2013-01-16T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"8\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p008.jpg\","
                + "                \"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p008.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Robin Williams\","
                + "\"title\": \"Agriculturist's Weekly Update, Delivered Once Daily, 24/7\","
                + "\"published_date\": \"2013-01-15T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"9\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p009.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p009.jpg\","
                + "\"aspect_ratio\": 1,"
                + "\"author\": \"Jacqueline Kennedy Onasis\","
                + "\"title\": \"Brooklyn Sidewalks Anonymous\","
                + "\"published_date\": \"2013-01-14T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"10\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p010.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p010.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Pablo Picasso\","
                + "\"title\": \"3 Great Dessert Recipes for This Weekend\","
                + "\"published_date\": \"2013-01-13T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"6\","
                + "                \"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p006.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p006.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Margaret Thatcher\","
                + "\"title\": \"TV in Modern Beach Environments\","
                + "\"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"12\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p012.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p012.jpg\","
                + "                \"aspect_ratio\": 1,"
                + "\"author\": \"Ernest Hemingway\","
                + "\"title\": \"What I Found While Swimming\","
                + "\"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"13\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p013.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p013.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "                \"author\": \"Ernest Hemingway\","
                + "\"title\": \"Bourgeois Office Furniture\","
                + "\"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"14\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p014.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p014.jpg\","
                + "\"aspect_ratio\": 1.49925,"
                + "\"author\": \"Ernest Hemingway\","
                + "\"title\": \"String Beans and What They Mean for You\","
                + "\"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"15\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p015.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p015.jpg\","
                + "\"aspect_ratio\": 1.50602,"
                + "\"author\": \"Ernest Hemingway\","
                + "\"title\": \"I Can't Get Enough Fantastic Sunsets\","
                + "\"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"16\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p016.jpg\","
                + "                \"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p016.jpg\","
                + "\"aspect_ratio\": 1.50602,"
                + "\"author\": \"Ernest Hemingway\","
                + "\"title\": \"The Beauty That is Mount Pumpkinfoot\","
                + "                \"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of " +
                "forever, laws of physics muse about.<br><br>Photos courtesy of " +
                "<a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "},"
                + "{"
                + "\"id\": \"17\","
                + "\"photo\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/images/p017.jpg\","
                + "\"thumb\": \"https://dl.dropboxusercontent" +
                ".com/u/231329/xyzreader_data/thumbs/p017.jpg\","
                + "\"aspect_ratio\": 1.11235,"
                + "\"author\": \"Ernest Hemingway\","
                + "\"title\": \"Busy Streets Are Still Busy, Even If You Don't Want Them To Be\","
                + "\"published_date\": \"2013-01-12T00:00:00.000Z\","
                + "\"body\": \"Paroxysm of global economics " +
                "<a href='http://www.google.com'>Google Search</a> event take root and flourish, " +
                "realm of the galaxies take root and flourish light years, circumnavigated " +
                "Tunguska event Vangelis. Realm of the galaxies as a patch of light " +
                "extraplanetary?<br><br>The carbon in our apple pies hundreds of thousands of " +
                "brilliant syntheses cosmic ocean Hypatia explorations across the centuries take " +
                "root and flourish muse about with pretty stories for which there's little good " +
                "evidence. Tunguska event birth billions upon billions venture tesseract billions" +
                " upon billions! Muse about dream of the mind's eye! Radio telescope. The only " +
                "home we've ever known with pretty stories for which there's little good " +
                "evidence! Hydrogen atoms cosmic fugue brain is the seed of intelligence the only" +
                " home we've ever known? Inconspicuous motes of rock and gas of brilliant " +
                "syntheses.<br><br>Network of wormholes across the centuries Jean-François " +
                "Champollion hearts of the stars? Vastness is bearable only through love, a still" +
                " more glorious dawn awaits worldlets the carbon in our apple pies worldlets " +
                "citizens of distant epochs corpus callosum quasar ship of the imagination. " +
                "Colonies something incredible is waiting to be known from which we spring " +
                "billions upon billions, paroxysm of global death with pretty stories for which " +
                "there's little good evidence, intelligent beings astonishment.<br><br>Brain is " +
                "the seed of intelligence, billions upon billions, corpus callosum trillion " +
                "stirred by starlight consciousness cosmic fugue dispassionate extraterrestrial " +
                "observer.<br><br>Bits of moving fluff. Muse about Apollonius of Perga worldlets " +
                "the only home we've ever known dispassionate extraterrestrial observer with " +
                "pretty stories for which there's little good evidence venture at the edge of forever, laws of physics muse about.<br><br>Photos courtesy of <a href='https://unsplash.com/'>Unsplash.com</a>.\""
                + "}"
                + "]";
    }
}
