package pl.edu.agh.imgwmock.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import pl.edu.agh.imgwmock.model.Polygon;
import pl.edu.agh.imgwmock.model.PolygonDataNew;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolygonsUtils {

    public static List<PolygonDataNew> getPolygonData() {
        String pathToFile = "src/main/resources/polygonsDataNew.json";
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                Instant instant = Instant.parse(json.getAsJsonPrimitive().getAsString());
                return instant;
            }
        }).create();
        PolygonDataNew[] data = {};
        try (JsonReader reader = new JsonReader(new FileReader(pathToFile))) {
            data = gson.fromJson(reader, PolygonDataNew[].class);
            return Arrays.asList(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.asList(data);
    }

    public static List<Polygon> getPolygonsStations() {
        String pathToFile = "src/main/resources/polygonsStation.json";
        Gson gson = new Gson();
        List<Polygon> list = new ArrayList<>();
        try {
            list = Arrays.asList(gson.fromJson(new FileReader(pathToFile), Polygon[].class));

            list = list.stream().map(polygon -> {
                List<Double[]> points = polygon.getPoints().stream().map(points1 ->
                        new Double[]{points1[1], points1[0]}).collect(Collectors.toList());
                return new Polygon(polygon.getId(), polygon.getName(), points);
            }).collect(Collectors.toList());

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
