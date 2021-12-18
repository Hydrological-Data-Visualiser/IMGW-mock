package pl.edu.agh.imgwmock.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.PolygonDataOld;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CSVUtils {

    public static List<Station> getStationListFromCSV(String pathToFile) {
        List<Station> stations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                Double[] arr = new Double[]{Double.parseDouble(record[3]), Double.parseDouble(record[4])};
                List<Double[]> list = new ArrayList<>() {{
                    add(arr);
                }};
                stations.add(new Station(
                        Long.parseLong(record[0]),
                        record[1],
                        list
                ));
            });
            return stations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<PolygonDataOld> getPolygons(String pathToFile, Optional<String> instant) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                Instant instant = Instant.parse(json.getAsJsonPrimitive().getAsString());
                return instant;
            }
        }).create();
        PolygonDataOld[] data = {};
        try (JsonReader reader = new JsonReader(new FileReader(pathToFile))) {
            data = gson.fromJson(reader, PolygonDataOld[].class);
            return Arrays.asList(data)
                    .stream()
                    .map(polygon -> new PolygonDataOld(polygon.getId(), polygon.getPoints(), polygon.getValue(), Instant.parse(instant.orElse("2021-10-10T10:10:10Z"))))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.asList(data);
    }


    public static List<PolygonDataOld> getNewPolygons(String pathToFile) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                Instant instant = Instant.parse(json.getAsJsonPrimitive().getAsString());
                return instant;
            }
        }).create();
        PolygonDataOld[] data = {};
        try (JsonReader reader = new JsonReader(new FileReader(pathToFile))) {
            data = gson.fromJson(reader, PolygonDataOld[].class);
            return Arrays.asList(data)
                    .stream()
                    .map(polygon -> new PolygonDataOld(polygon.getId(), polygon.getPoints(), polygon.getValue(), polygon.getDate()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.asList(data);
    }
}
