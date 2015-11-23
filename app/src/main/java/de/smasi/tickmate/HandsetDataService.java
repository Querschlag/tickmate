package de.smasi.tickmate;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import de.smasi.tickmatedata.database.DataSource;
import de.smasi.tickmatedata.models.Tick;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.HandsetDataClient;
import de.smasi.tickmatedata.wear.WearDataClient;

/**
 * Created by Adrian Geuss on 15.11.15.
 */
public class HandsetDataService extends WearableListenerService {

    private DataSource dataSource = DataSource.getInstance();
    private HandsetDataClient dataClient = null;

    private HandsetDataClient getDataClient() {
        if (dataClient == null) {
            dataClient = new HandsetDataClient(this, null, null);
        }
        return dataClient;
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        if ( messageEvent.getPath().equalsIgnoreCase( WearDataClient.WEAR_MESSAGE_GET_TRACKS ) ) {
            final List<Track> tracks = dataSource.getTracks();
            final byte[] data = DataUtils.dataFromTrackList(tracks);

            final HandsetDataClient dataClient = getDataClient();
            // get nodes
            Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    for (Node node: getConnectedNodesResult.getNodes()) {
                        dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_GET_TRACKS, data, node);
                    }
                }
            });

        } else if (messageEvent.getPath().equalsIgnoreCase( WearDataClient.WEAR_MESSAGE_GET_TICKS )) {
            LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.get("track");
                Calendar calendar = (Calendar) args.get("calendar");
                List<Tick> ticks = dataSource.getTicksForDay(track, calendar);
                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("ticks", ticks);
                response.put("track", track);
                response.put("calendar", calendar);
                final byte[] data = DataUtils.dataFromHashMap(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node: getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_GET_TICKS, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_IS_TICKED)) {
            LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.get("track");
                Calendar calendar = (Calendar) args.get("calendar");
                Boolean hasTimeInfo = (Boolean) args.get("hasTimeInfo");

                Boolean isTicked = dataSource.isTicked(track, calendar, hasTimeInfo);

                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("track", track);
                response.put("calendar", calendar);
                response.put("isTicked", isTicked);
                final byte[] data = DataUtils.dataFromHashMap(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_IS_TICKED, data, node);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_SET_TICK)) {
            LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.get("track");
                Calendar calendar = (Calendar) args.get("calendar");
                Boolean hasTimeInfo = (Boolean) args.get("hasTimeInfo");

                dataSource.setTick(track, calendar, hasTimeInfo);

                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("track", track);
                response.put("calendar", calendar);
                if (!track.multipleEntriesEnabled()) {
                    response.put("isTicked", dataSource.isTicked(track, calendar, hasTimeInfo));
                } else {
                    List<Tick> ticks = dataSource.getTicksForDay(track, calendar);
                    response.put("ticks", ticks);
                }
                final byte[] data = DataUtils.dataFromHashMap(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_SET_TICK, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY)) {
            LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.get("track");
                Calendar calendar = (Calendar) args.get("calendar");

                dataSource.removeLastTickOfDay(track, calendar);
                List<Tick> ticks = dataSource.getTicksForDay(track, calendar);

                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("track", track);
                response.put("calendar", calendar);
                response.put("ticks", ticks);

                final byte[] data = DataUtils.dataFromHashMap(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_REMOVE_TICK)) {
            LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
            try {
                Track track = (Track) args.get("track");
                Calendar calendar = (Calendar) args.get("calendar");
                Boolean hasTimeInfo = (Boolean) args.get("hasTimeInfo");

                dataSource.removeTick(track, calendar);

                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("track", track);
                response.put("calendar", calendar);
                if (!track.multipleEntriesEnabled()) {
                    response.put("isTicked", dataSource.isTicked(track, calendar, hasTimeInfo));
                }
                final byte[] data = DataUtils.dataFromHashMap(response);

                final HandsetDataClient dataClient = getDataClient();
                // get nodes
                Wearable.NodeApi.getConnectedNodes(dataClient.googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            dataClient.sendMessage(WearDataClient.WEAR_MESSAGE_REMOVE_TICK, data, node);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
