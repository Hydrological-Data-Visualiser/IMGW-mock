package pl.edu.agh.imgwmock.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pl.edu.agh.imgwmock.model.Polygon;
import pl.edu.agh.imgwmock.model.Station;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVUtils {

    public static List<Station> getStationListFromCSV(String pathToFile) {
        List<Station> stations = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(pathToFile))) {
            List<String[]> csvRecords = reader.readAll();

            csvRecords.forEach(record -> {
                stations.add(new Station(
                        Long.parseLong(record[0]),
                        record[1],
                        Integer.parseInt(record[2]),
                        Double.parseDouble(record[3]),
                        Double.parseDouble(record[4])
                ));
            });
            return stations;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Polygon> getPolygons(String pathToFile) {
        Gson gson = new Gson();
        Polygon[] data = {};
        try (JsonReader reader = new JsonReader(new FileReader(pathToFile))) {
            data = gson.fromJson(reader, Polygon[].class);
            return Arrays.asList(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.asList(data);
    }
}
