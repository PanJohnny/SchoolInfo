package me.panjohnny.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.panjohnny.Configurator;
import me.panjohnny.baka4j.BakaClient;
import me.panjohnny.baka4j.impl.BakaClientImpl;
import me.panjohnny.baka4j.util.ReqParameters;
import org.graalvm.collections.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BakalariService extends Service {

    private BakaClientImpl client;
    public BakalariService(Configurator config) {
        super(config);
    }

    @Override
    public void login() throws Exception {
        client = BakaClient.getInstance(config.getBakalariUrl());
        client.authorize(config.getBakalariUsername(), config.getBakalariPassword());
        client.enableRefreshJobs();
    }

    @Override
    public Pair<String[], String[]> getData() throws Exception {
        Calendar today = Calendar.getInstance();
        boolean isToday = true;
        // If it is after 16:10 go to the next day, or if it is a weekend, go to the next monday
        if (today.get(Calendar.HOUR_OF_DAY) > 16 || (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
            int daysToAdd = (Calendar.SATURDAY - today.get(Calendar.DAY_OF_WEEK) + 2) % 7;
            if (daysToAdd == 0) daysToAdd = 7;
            today.add(Calendar.DAY_OF_MONTH, daysToAdd);
            isToday = false;
        }

        String date = new SimpleDateFormat("yyyy-MM-dd").format(Date.from(today.toInstant()));
        JsonObject timetable = client.getJson("/api/3/timetable/actual", new ReqParameters().set("date", date)).getAsJsonObject();
        if (timetable.has("message")) {
            throw new Exception("Failed to get timetable: " + timetable.get("message").getAsString());
        }

        // get the Days array and find the current day (startsWith the date in the format yyyy-MM-dd), It is an array of objects with the field Date
        JsonArray days = timetable.getAsJsonArray("Days");
        JsonArray hours = timetable.getAsJsonArray("Hours"); // {Id, Caption, BeginTime, EndTime} - interest in begin-end display and id for matching with atoms
        JsonArray subjects = timetable.getAsJsonArray("Subjects"); // {Id, Name, Abbreviation} - interest in id for matching with atoms and name for display
        JsonArray rooms = timetable.getAsJsonArray("Rooms"); // {Id, Abbrev} - interest in id for matching with atoms and abbrev for display

        ArrayList<String> leftSide = new ArrayList<>();
        ArrayList<String> rightSide = new ArrayList<>();

        leftSide.add("###");
        rightSide.add(date);

        for (int i = 0; i < days.size(); i++) {
            JsonObject day = days.get(i).getAsJsonObject();
            if (day.get("Date").getAsString().startsWith(date)) {
                JsonArray atoms = day.getAsJsonArray("Atoms"); // {HourId, SubjectId, RoomId} - interest in hourid for matching with hours, subjectid for matching with subjects and roomid for matching with rooms
                for (JsonElement atom : atoms) {
                    JsonObject at = atom.getAsJsonObject();
                    String hourId = at.get("HourId").getAsString();
                    String subjectId = at.get("SubjectId").getAsString();
                    String roomId = at.get("RoomId").getAsString();

                    // find the hour with the id hourId
                    String hour = "";
                    String endTime = "";
                    for (JsonElement hourElement : hours) {
                        JsonObject hourObj = hourElement.getAsJsonObject();
                        if (hourObj.get("Id").getAsString().equals(hourId)) {
                            hour = hourObj.get("BeginTime").getAsString() + " - " + hourObj.get("EndTime").getAsString();
                            endTime = hourObj.get("EndTime").getAsString();
                            break;
                        }
                    }

                    if (isToday) {
                        // if the endTime is larger than the current time, skip this hour format: hh:ss
                        String[] currentTime = new SimpleDateFormat("HH:mm").format(new Date()).split(":");

                        String h = currentTime[0];
                        String m = currentTime[1];

                        String[] endTimeSplit = endTime.split(":");
                        String eh = endTimeSplit[0];
                        String em = endTimeSplit[1];

                        if (Integer.parseInt(h) > Integer.parseInt(eh)) {
                            continue;
                        } else if (Integer.parseInt(h) == Integer.parseInt(eh)) {
                            if (Integer.parseInt(m) > Integer.parseInt(em)) {
                                continue;
                            }
                        }
                    }

                    // find the subject with the id subjectId
                    String subject = "";
                    for (JsonElement subjectElement : subjects) {
                        JsonObject subjectObj = subjectElement.getAsJsonObject();
                        if (subjectObj.get("Id").getAsString().equals(subjectId)) {
                            subject = subjectObj.get("Name").getAsString();
                            break;
                        }
                    }

                    // find the room with the id roomId
                    String room = "";
                    for (JsonElement roomElement : rooms) {
                        JsonObject roomObj = roomElement.getAsJsonObject();
                        if (roomObj.get("Id").getAsString().equals(roomId)) {
                            room = roomObj.get("Abbrev").getAsString();
                            break;
                        }
                    }

                    leftSide.add(hour);
                    rightSide.add(subject + " (" + room + ")");
                }
                break;
            }
        }

        if (leftSide.isEmpty() || rightSide.isEmpty()) {
            leftSide.add("Dnes už nic nemáme");
            rightSide.add("Dnes už nic nemáme");
        }

        return Pair.create(leftSide.toArray(new String[0]), rightSide.toArray(new String[0]));
    }
}
